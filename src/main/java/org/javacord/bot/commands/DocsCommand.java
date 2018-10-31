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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The !docs command which is used to show links to Javacord's JavaDocs.
 */
public class DocsCommand implements CommandExecutor {

    /**
     * Executes the {@code !docs} command.
     *
     * @param channel The channel where the command was issued.
     * @param args    The arguments given to the command.
     */
    @Command(aliases = {"!docs"}, async = true)
    public void onCommand(TextChannel channel, String[] args) {
        try {
            if (args.length == 0) { // Just give an overview
                EmbedBuilder embed = new EmbedBuilder()
                        .addField("Overview", "https://docs.javacord.org/")
                        .addField("Latest release version", "https://docs.javacord.org/api/v/latest")
                        .addField("Latest snapshot", "https://docs.javacord.org/api/build/latest")
                        .addField("Hint", "You can search the docs using `!docs [method|class] <search>`")
                        .setThumbnail(getClass().getClassLoader().getResourceAsStream("javacord3_icon.png"), "png")
                        .setColor(Constants.JAVACORD_ORANGE);
                channel.sendMessage(embed).join();
            } else { // Search
                EmbedBuilder embed = new EmbedBuilder()
                        .setThumbnail(getClass().getClassLoader().getResourceAsStream("javacord3_icon.png"), "png")
                        .setColor(Constants.JAVACORD_ORANGE);
                if (args[0].matches("(classes|class|c)")) { // Search for a class
                    String searchString = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    populateClasses(channel.getApi(), embed, searchString);
                } else if (args[0].matches("(methods|method|m)")) { // Search for a method
                    String searchString = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    populateMethods(channel.getApi(), embed, searchString);
                } else { // Search for a method
                    String searchString = String.join(" ", args);
                    populateMethods(channel.getApi(), embed, searchString);
                }
                channel.sendMessage(embed).join();
            }
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
    private void populateMethods(DiscordApi api, EmbedBuilder embed, String searchString) {
        Map<String, List<JavadocMethod>> methods = new JavadocParser(api, JavadocParser.getLatestJavaDocs(api).join())
                .getMethods().join().stream()
                .filter(method -> method.getFullName().toLowerCase().contains(searchString.toLowerCase()))
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
     * Populates the classes field inside the given embed.
     *
     * @param api          A discord api instance.
     * @param embed        The embed to populate.
     * @param searchString A search string.
     */
    private void populateClasses(DiscordApi api, EmbedBuilder embed, String searchString) {
        List<JavadocClass> classes = new JavadocParser(api, JavadocParser.getLatestJavaDocs(api).join())
                .getClasses().join().stream()
                .filter(clazz -> clazz.getName().toLowerCase().contains(searchString.toLowerCase()))
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
            strBuilder.append("[")
                    .append(clazz.getName())
                    .append("](")
                    .append(clazz.getFullUrl())
                    .append("), ");
            counter++;
            if (strBuilder.length() > 1950) { // Prevent hitting the description size limit
                break;
            }
        }

        if (classes.size() - counter > 0) {
            strBuilder.append("and ").append(classes.size() - counter).append(" more ...");
        }

        embed.setDescription(strBuilder.toString());
    }

}
