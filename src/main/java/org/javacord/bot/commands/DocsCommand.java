package org.javacord.bot.commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import net.kautler.command.api.annotation.Usage;
import net.kautler.command.api.parameter.Parameters;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import org.javacord.bot.listeners.CommandCleanupListener;
import org.javacord.bot.util.LatestVersionFinder;
import org.javacord.bot.util.javadoc.parser.JavadocClass;
import org.javacord.bot.util.javadoc.parser.JavadocMethod;
import org.javacord.bot.util.javadoc.parser.JavadocParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.javacord.bot.Constants.ERROR_COLOR;
import static org.javacord.bot.Constants.JAVACORD_ORANGE;

/**
 * The !docs command which is used to show links to Javacord's JavaDocs.
 */
@ApplicationScoped
@Description("Shows a link to the JavaDoc or searches through it")
@Usage("[[('classes' | 'class' | 'c' | 'methods' | 'method' | 'm')] [('all' | 'a')] <search...>]")
@Asynchronous
public class DocsCommand extends BaseTextCommand {
    /**
     * The parameters that indicate searching for class names only.
     */
    private static final Set<String> CLASS_PARAMS = new HashSet<>(Arrays.asList("classes", "class", "c"));

    /**
     * The parameters that indicate searching for method names only.
     */
    private static final Set<String> METHOD_PARAMS = new HashSet<>(Arrays.asList("methods", "method", "m"));

    /**
     * The parameters that indicate also searching internal packages and the core docs.
     */
    private static final Set<String> INCLUDE_ALL_PARAMS = new HashSet<>(Arrays.asList("all", "a"));

    @Inject
    Logger logger;

    @Inject
    LatestVersionFinder versionFinder;

    /**
     * Executes the {@code !docs} command.
     */
    @Override
    protected void doExecute(CommandContext<? extends Message> commandContext, Message message,
                             TextChannel channel, Parameters<String> parameters) {
        DiscordApi api = channel.getApi();

        try (InputStream javacord3Icon = getClass().getResourceAsStream("/javacord3_icon.png")) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setThumbnail(javacord3Icon)
                    .setColor(JAVACORD_ORANGE);

            switch (parameters.size()) {
                case 0: // Just give an overview
                    embed.setTitle("Javacord Docs")
                            .addField("Overview", "https://docs.javacord.org/")
                            .addField("Latest release version JavaDoc", "https://docs.javacord.org/api/v/latest")
                            .addField("Hint", format(
                                    "You can search the docs using `%s%s %s`",
                                    commandContext.getPrefix().orElseThrow(AssertionError::new),
                                    commandContext.getAlias().orElseThrow(AssertionError::new),
                                    getUsage().orElseThrow(AssertionError::new)));
                    break;

                case 1:
                    populateMethods(api, embed, parameters.get("search").orElseThrow(AssertionError::new), false);
                    break;

                case 2: {
                    String searchString = parameters.get("search").orElseThrow(AssertionError::new);
                    if (includeAll(parameters)) {
                        populateMethods(api, embed, searchString, true);
                    } else if (classSearch(parameters)) {
                        populateClasses(api, embed, searchString, false);
                    } else if (methodSearch(parameters)) {
                        populateMethods(api, embed, searchString, false);
                    } else {
                        throw new AssertionError("Unexpected state while parameter count 2");
                    }
                    break;
                }

                case 3: {
                    if (!includeAll(parameters)) {
                        throw new AssertionError("Unexpected includeAll false while parameter count 3");
                    }
                    String searchString = parameters.get("search").orElseThrow(AssertionError::new);
                    if (classSearch(parameters)) {
                        populateClasses(api, embed, searchString, true);
                    } else if (methodSearch(parameters)) {
                        populateMethods(api, embed, searchString, true);
                    } else {
                        throw new AssertionError("Unexpected state while parameter count 3");
                    }
                    break;
                }

                default:
                    throw new AssertionError(format("Missing case for parameter count '%s'", parameters.size()));
            }

            CommandCleanupListener.insertResponseTracker(embed, message.getId());
            channel.sendMessage(embed).join();
        } catch (Throwable t) {
            logger
                    .atError()
                    .withThrowable(t)
                    .log("Exception while handling docs command");

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription(format(
                            "Something went wrong: ```%s```",
                            ExceptionLogger.unwrapThrowable(t).getMessage()))
                    .setColor(ERROR_COLOR);

            CommandCleanupListener.insertResponseTracker(embed, message.getId());
            channel.sendMessage(embed).join();
        }
    }

    private boolean classSearch(Parameters<String> parameters) {
        return parameters.getParameterNames().stream().anyMatch(CLASS_PARAMS::contains);
    }

    private boolean methodSearch(Parameters<String> parameters) {
        return parameters.getParameterNames().stream().anyMatch(METHOD_PARAMS::contains);
    }

    private boolean includeAll(Parameters<String> parameters) {
        return parameters.getParameterNames().stream().anyMatch(INCLUDE_ALL_PARAMS::contains);
    }

    /**
     * Populates the methods field inside the given embed.
     *
     * @param api          A discord api instance.
     * @param embed        The embed to populate.
     * @param searchString A search string.
     */
    private void populateMethods(DiscordApi api, EmbedBuilder embed, String searchString, boolean includeAll) {
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

        int totalTextCount = 25; // the maximum tracker string length
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
