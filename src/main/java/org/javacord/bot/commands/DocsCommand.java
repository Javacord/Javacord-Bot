package org.javacord.bot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.util.logging.ExceptionLogger;
import org.javacord.bot.Constants;
import org.javacord.bot.listeners.CommandCleanupListener;
import org.javacord.bot.util.LatestVersionFinder;
import org.javacord.bot.util.javadoc.parser.JavadocClass;
import org.javacord.bot.util.javadoc.parser.JavadocMethod;
import org.javacord.bot.util.javadoc.parser.JavadocParser;

import java.io.IOException;
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

/**
 * The !docs command which is used to show links to Javacord's JavaDocs.
 */
public class DocsCommand implements CommandExecutor {

    /**
     * The parameters that indicate searching for class names only.
     */
    private static final Set<String> classParams = new HashSet<>(Arrays.asList("classes", "class", "c"));

    /**
     * The parameters that indicate searching for method names only.
     */
    private static final Set<String> methodParams = new HashSet<>(Arrays.asList("methods", "method", "m"));

    /**
     * The parameters that indicate also searching internal packages and the core docs.
     */
    private static final Set<String> includeAllParams = new HashSet<>(Arrays.asList("all", "a"));

    private final LatestVersionFinder versionFinder;

    /**
     * Initializes the command.
     *
     * @param versionFinder The version finder to use to determine the latest Javacord version.
     */
    public DocsCommand(LatestVersionFinder versionFinder) {
        this.versionFinder = versionFinder;
    }

    /**
     * Executes the {@code !docs} command.
     *
     * @param server  The server where the command was issued.
     * @param channel The channel where the command was issued.
     * @param message The message the command was issued in.
     * @param args    The arguments given to the command.
     * @throws IOException If the Javacord icon stream cannot be closed properly.
     */
    @Command(aliases = {"!docs"}, async = true, usage = "!docs [method|class] <search>",
            description = "Shows a link to the JavaDoc or searches through it")
    public void onCommand(Server server, TextChannel channel, Message message, String[] args) throws IOException {
        // Only react in #java_javacord channel on Discord API server
        if ((server.getId() == Constants.DAPI_SERVER_ID) && (channel.getId() != Constants.DAPI_JAVACORD_CHANNEL_ID)) {
            return;
        }

        try (InputStream javacord3Icon = getClass().getClassLoader().getResourceAsStream("javacord3_icon.png")) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setThumbnail(javacord3Icon, "png")
                    .setColor(Constants.JAVACORD_ORANGE);
            if (args.length == 0) { // Just give an overview
                embed.addField("Overview", "https://docs.javacord.org/")
                        .addField("Latest release version", "https://docs.javacord.org/api/v/latest")
                        .addField("Latest snapshot", "https://docs.javacord.org/api/build/latest")
                        .addField("Hint", "You can search the docs using `!docs [method|class] <search>`");
            } else if (args.length == 1) { // Basic search - methods without internals
                populateMethods(channel.getApi(), embed, args[0], false);
            } else { // Extended search
                if (classParams.contains(args[0])) { // Search for a class
                    boolean searchAll = args.length > 2 && includeAllParams.contains(args[1]);
                    String searchString = String.join(" ", Arrays.copyOfRange(args, searchAll ? 2 : 1, args.length));
                    populateClasses(channel.getApi(), embed, searchString, searchAll);
                } else if (methodParams.contains(args[0])) { // Search for a method
                    boolean searchAll = args.length > 2 && includeAllParams.contains(args[1]);
                    String searchString = String.join(" ", Arrays.copyOfRange(args, searchAll ? 2 : 1, args.length));
                    populateMethods(channel.getApi(), embed, searchString, searchAll);
                } else { // Search for a method
                    boolean searchAll = includeAllParams.contains(args[0]);
                    String searchString = String.join(" ", Arrays.copyOfRange(args, searchAll ? 1 : 0, args.length));
                    populateMethods(channel.getApi(), embed, searchString, searchAll);
                }
            }
            CommandCleanupListener.insertResponseTracker(embed, message.getId());
            channel.sendMessage(embed).join();
        } catch (Throwable t) {
            channel.sendMessage(
                    "Something went wrong: ```" + ExceptionLogger.unwrapThrowable(t).getMessage() + "```").join();
            // Throw the caught exception again. The sdcf4j will log it.
            throw t;
        }
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
