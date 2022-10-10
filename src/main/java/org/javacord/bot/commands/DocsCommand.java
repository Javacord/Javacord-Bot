package org.javacord.bot.commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import org.javacord.api.util.logging.ExceptionLogger;
import org.javacord.bot.Constants;
import org.javacord.bot.util.LatestVersionFinder;
import org.javacord.bot.util.javadoc.parser.JavadocClass;
import org.javacord.bot.util.javadoc.parser.JavadocMethod;
import org.javacord.bot.util.javadoc.parser.JavadocParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * The /docs command which is used to show links to Javacord's JavaDocs.
 */
@ApplicationScoped
@Description("Shows a link to the JavaDoc or searches through it")
@Asynchronous
public class DocsCommand extends BaseSlashCommand {
    private static final String SEARCH_TERM = "search-term";
    private static final String SEARCH_TYPE = "search-type";
    private static final String SEARCH_TYPE_CLASSES = "classes";
    private static final String SEARCH_TYPE_MEMBERS = "members";
    private static final String INCLUDE_ALL = "include-all";

    @Inject
    Logger logger;

    @Inject
    LatestVersionFinder versionFinder;

    @Override
    protected List<SlashCommandOption> doGetOptions() {
        return List.of(
                SlashCommandOption.createStringOption(
                        SEARCH_TERM,
                        "The term to search for",
                        false),
                SlashCommandOption.createWithChoices(
                        SlashCommandOptionType.STRING,
                        SEARCH_TYPE,
                        "The type of the search (default: Members)",
                        false,
                        List.of(
                                SlashCommandOptionChoice.create("Classes", SEARCH_TYPE_CLASSES),
                                SlashCommandOptionChoice.create("Members", SEARCH_TYPE_MEMBERS))),
                SlashCommandOption.createBooleanOption(
                        INCLUDE_ALL,
                        "Whether to search in internal and core classes (default: false)",
                        false));
    }

    /**
     * Executes the {@code /docs} command.
     */
    @Override
    public void execute(CommandContext<? extends SlashCommandInteraction> commandContext) {
        SlashCommandInteraction slashCommandInteraction = commandContext.getMessage();
        DiscordApi api = slashCommandInteraction.getApi();

        CompletableFuture<InteractionOriginalResponseUpdater> responseUpdater =
                sendResponseLater(slashCommandInteraction);

        try (InputStream javacord3Icon = getClass().getResourceAsStream("/javacord3_icon.png")) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setThumbnail(javacord3Icon)
                    .setColor(Constants.JAVACORD_ORANGE);

            Optional<String> optionalSearchTerm = slashCommandInteraction
                    .getOptionStringValueByName(SEARCH_TERM)
                    .map(term -> term.toLowerCase(Locale.ROOT));
            if (optionalSearchTerm.isEmpty()) {
                // Just give an overview
                embed.setTitle("Javacord Docs")
                        .addField("Overview", "https://docs.javacord.org/")
                        .addField("Latest release version JavaDoc", "https://docs.javacord.org/api/v/latest");
            } else {
                String searchTerm = optionalSearchTerm.get();
                String searchType = slashCommandInteraction
                        .getOptionStringValueByName(SEARCH_TYPE)
                        .orElse(SEARCH_TYPE_MEMBERS);
                boolean includeAll = slashCommandInteraction
                        .getOptionBooleanValueByName(INCLUDE_ALL)
                        .orElse(Boolean.FALSE);

                switch (searchType) {
                    case SEARCH_TYPE_MEMBERS:
                        populateMembers(api, embed, searchTerm, includeAll);
                        break;

                    case SEARCH_TYPE_CLASSES:
                        populateClasses(api, embed, searchTerm, includeAll);
                        break;

                    default:
                        throw new AssertionError(String.format("Missing case for search type '%s'", searchType));
                }
            }

            responseUpdater.thenCompose(updater -> updater.addEmbed(embed).update()).join();
        } catch (Throwable t) {
            logger
                    .atError()
                    .withThrowable(t)
                    .log("Exception while handling docs command");

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription(String.format(
                            "Something went wrong: ```%s```",
                            ExceptionLogger.unwrapThrowable(t).getMessage()))
                    .setColor(Constants.ERROR_COLOR);

            responseUpdater.thenCompose(updater -> updater.addEmbed(embed).update()).join();
        }
    }

    /**
     * Populates the members fields inside the given embed.
     *
     * @param api          A discord api instance.
     * @param embed        The embed to populate.
     * @param searchString A search string.
     */
    private void populateMembers(DiscordApi api, EmbedBuilder embed, String searchString, boolean includeAll) {
        CompletableFuture<Set<JavadocMethod>> apiMethods = versionFinder.findLatestVersion()
                .thenApply(latestVersion -> new JavadocParser(api, "api", latestVersion))
                .thenCompose(JavadocParser::getMethods);
        CompletableFuture<Set<JavadocMethod>> coreMethods = (includeAll)
                ? versionFinder.findLatestVersion()
                .thenApply(latestVersion -> new JavadocParser(api, "core", latestVersion))
                .thenCompose(JavadocParser::getMethods)
                : CompletableFuture.completedFuture(Collections.emptySet());

        Map<String, List<JavadocMethod>> methodsByClass = apiMethods
                .thenCombine(coreMethods, this::unionOf).join().stream()
                .filter(method -> method.getFullName().toLowerCase().contains(searchString.toLowerCase()))
                .filter(method -> {
                    String packageName = method.getPackageName();
                    return includeAll || !(packageName.endsWith(".internal") || packageName.contains(".internal."));
                })
                .sorted(Comparator.comparingInt(method -> method.getName().length()))
                .collect(Collectors.groupingBy(JavadocMethod::getClassName));


        if (methodsByClass.isEmpty()) {
            embed.setTitle("Methods");
            embed.setDescription("No matching methods found!");
            return;
        }

        int totalTextCount = 0;
        List<Map.Entry<String, List<JavadocMethod>>> entries = new ArrayList<>(methodsByClass.entrySet());
        entries.sort(Map.Entry.comparingByKey(String::compareToIgnoreCase));
        int classesAmount = entries.size();
        for (int classIndex = 0; classIndex < classesAmount; classIndex++) {
            Map.Entry<String, List<JavadocMethod>> entry = entries.get(classIndex);
            List<JavadocMethod> methods = entry.getValue();
            methods.sort(Comparator.comparing(JavadocMethod::getShortenedName, String::compareToIgnoreCase));
            StringBuilder methodsBuilder = new StringBuilder();
            int methodsAmount = methods.size();
            for (int methodIndex = 0; methodIndex < methodsAmount; methodIndex++) {
                JavadocMethod method = methods.get(methodIndex);
                StringBuilder methodBuilder = new StringBuilder()
                        .append("• [")
                        .append(method.getShortenedName())
                        .append("](")
                        .append(method.getFullUrl())
                        .append(")\n");
                int nextMoreSize = methodIndex == (methodsAmount - 1)
                        ? 0
                        : 11 + (int) (Math.log10(methodsAmount - methodIndex - 1) + 1);
                if ((methodsBuilder.length() + methodBuilder.length() + nextMoreSize) < 1000) {
                    methodsBuilder.append(methodBuilder);
                } else {
                    methodsBuilder.append("• ").append(methodsAmount - methodIndex).append(" more ...");
                    break;
                }
            }
            int nextMoreSize = classIndex == (classesAmount - 1)
                    ? 0
                    : 57 + (int) (Math.log10(classesAmount - classIndex - 1) + 1);
            String className = entry.getKey();
            if ((totalTextCount + className.length() + methodsBuilder.length() + nextMoreSize) <= 5900) {
                embed.addField(className, methodsBuilder.toString());
                totalTextCount += className.length() + methodsBuilder.length();
            } else {
                embed.addField(String.format("And **%d** more classes ...", classesAmount - classIndex),
                        "Maybe try a less generic search?");
                break;
            }
        }
    }

    /**
     * Union operation for sets.
     *
     * @param set1 First set.
     * @param set2 Second set.
     * @param <T>  Type of set content.
     * @return The union of the two sets.
     */
    private <T> Set<T> unionOf(Set<T> set1, Set<T> set2) {
        if (set2.isEmpty()) {
            return set1;
        } else {
            Set<T> union = new HashSet<>(set1);
            union.addAll(set2);
            return union;
        }
    }

    /**
     * Populates the classes field inside the given embed.
     *
     * @param api          A discord api instance.
     * @param embed        The embed to populate.
     * @param searchString A search string.
     */
    private void populateClasses(DiscordApi api, EmbedBuilder embed, String searchString, boolean includeAll) {
        CompletableFuture<Set<JavadocClass>> apiClasses = versionFinder.findLatestVersion()
                .thenApply(latestVersion -> new JavadocParser(api, "api", latestVersion))
                .thenCompose(JavadocParser::getClasses);
        CompletableFuture<Set<JavadocClass>> coreClasses = (includeAll)
                ? versionFinder.findLatestVersion()
                .thenApply(latestVersion -> new JavadocParser(api, "core", latestVersion))
                .thenCompose(JavadocParser::getClasses)
                : CompletableFuture.completedFuture(Collections.emptySet());

        List<JavadocClass> classes = apiClasses.thenCombine(coreClasses, this::unionOf).join().stream()
                .filter(clazz -> clazz.getName().toLowerCase().contains(searchString.toLowerCase()))
                .filter(clazz -> {
                    String packageName = clazz.getPackageName();
                    return includeAll || !(packageName.endsWith(".internal") || packageName.contains(".internal."));
                })
                .sorted(Comparator.comparingInt(clazz -> clazz.getName().length()))
                .collect(Collectors.toList());

        embed.setTitle("Classes");
        if (classes.isEmpty()) {
            embed.setDescription("No matching classes found!");
            return;
        }

        StringBuilder strBuilder = new StringBuilder();
        int counter = 0;
        for (JavadocClass clazz : classes) {
            if (strBuilder.length() > 0) {
                strBuilder.append(", ");
            }
            strBuilder.append("[")
                    .append(clazz.getName())
                    .append("](")
                    .append(clazz.getFullUrl())
                    .append(")");
            counter++;
            if (strBuilder.length() > 1950) { // Prevent hitting the description size limit
                break;
            }
        }

        if (classes.size() - counter > 0) {
            strBuilder.append("\nand ").append(classes.size() - counter).append(" more ...");
        }

        embed.setDescription(strBuilder.toString());
    }
}
