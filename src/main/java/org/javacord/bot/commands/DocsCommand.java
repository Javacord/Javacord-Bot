package org.javacord.bot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import org.javacord.bot.Constants;
import org.javacord.bot.util.javadoc.parser.JavadocClass;
import org.javacord.bot.util.javadoc.parser.JavadocMethod;
import org.javacord.bot.util.javadoc.parser.JavadocParser;

import java.io.IOException;
import java.io.InputStream;
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
    
    /**
     * Executes the {@code !docs} command.
     *
     * @param channel The channel where the command was issued.
     * @param args    The arguments given to the command.
     * @throws IOException If the Javacord icon stream cannot be closed properly.
     */
    @Command(aliases = {"!docs"}, async = true)
    public void onCommand(TextChannel channel, String[] args) throws IOException {
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
        CompletableFuture<Set<JavadocMethod>> apiMethods = JavadocParser.getLatestJavaDocs(api)
                .thenApply(urlString -> new JavadocParser(api, urlString))
                .thenCompose(JavadocParser::getMethods);
        CompletableFuture<Set<JavadocMethod>> coreMethods = (includeAll)
                ? JavadocParser.getLatestCoreJavaDocs(api)
                .thenApply(urlString -> new JavadocParser(api, urlString))
                .thenCompose(JavadocParser::getMethods)
                : CompletableFuture.completedFuture(Collections.emptySet());

        Map<String, List<JavadocMethod>> methods = apiMethods.thenCombine(coreMethods, this::unionOf).join().stream()
                .filter(method -> method.getFullName().toLowerCase().contains(searchString.toLowerCase()))
                .filter(method -> {
                    String packageName = method.getPackageName();
                    return includeAll || !(packageName.endsWith(".internal") || packageName.contains(".internal."));
                })
                .sorted(Comparator.comparingInt(method -> method.getName().length()))
                .collect(Collectors.groupingBy(JavadocMethod::getClassName));


        if (methods.isEmpty()) {
            embed.setTitle("Methods");
            embed.setDescription("No matching methods found!");
            return;
        }

        int totalTextCount = 0;
        int classCounter = 0;
        for (Map.Entry<String, List<JavadocMethod>> entry : methods.entrySet()) {
            StringBuilder strBuilder = new StringBuilder();
            int methodCounter = 0;
            for (JavadocMethod method : entry.getValue()) {
                if (strBuilder.length() > 800) { // To prevent hitting the maximum field size
                    strBuilder.append("• ").append(entry.getValue().size() - methodCounter).append(" more ...");
                    break;
                }
                strBuilder.append("• [")
                        .append(method.getShortenedName())
                        .append("](")
                        .append(method.getFullUrl())
                        .append(")\n");
                methodCounter++;
            }
            embed.addField(entry.getKey(), strBuilder.toString());
            totalTextCount += entry.getKey().length() + strBuilder.length();
            classCounter++;
            if (totalTextCount > 5000) { // To prevent hitting the maximum embed size
                break;
            }
        }

        if (methods.size() - classCounter > 0) {
            embed.addField("And **" + (methods.size() - classCounter) + "** more classes ...",
                    "Maybe try a less generic search?");
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
        CompletableFuture<Set<JavadocClass>> apiClasses = JavadocParser.getLatestJavaDocs(api)
                .thenApply(urlString -> new JavadocParser(api, urlString))
                .thenCompose(JavadocParser::getClasses);
        CompletableFuture<Set<JavadocClass>> coreClasses = (includeAll)
                ? JavadocParser.getLatestCoreJavaDocs(api)
                .thenApply(urlString -> new JavadocParser(api, urlString))
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
