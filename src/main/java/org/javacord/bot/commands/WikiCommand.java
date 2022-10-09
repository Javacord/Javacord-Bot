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
import org.javacord.bot.util.wiki.parser.WikiPage;
import org.javacord.bot.util.wiki.parser.WikiParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Locale.ROOT;
import static java.util.function.Predicate.not;
import static org.javacord.bot.Constants.ERROR_COLOR;
import static org.javacord.bot.Constants.JAVACORD_ORANGE;

/**
 * The !wiki command which is used to link to Javacord's wiki.
 */
@ApplicationScoped
@Description("Shows a link to the wiki or searches through it")
@Usage("[[('title' | 't' | 'page' | 'p' | 'full' | 'f' | 'content' | 'c')] <search...>]")
@Asynchronous
public class WikiCommand extends BaseTextCommand {
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]++>");
    private static final Pattern END_OF_SENTENCE = Pattern.compile("\\.(?: |\\r?\\n)");

    @Inject
    Logger logger;

    /**
     * Executes the {@code !wiki} command.
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
                case 0: // Just an overview
                    embed.setTitle("Javacord Wiki")
                            .setDescription(format("The [Javacord Wiki](%s/wiki) is an excellent "
                                    + "resource to get you started with Javacord.\n", WikiParser.BASE_URL))
                            .addInlineField("Hint", format(
                                    "You can search the wiki using `%s%s %s`",
                                    commandContext.getPrefix().orElseThrow(AssertionError::new),
                                    commandContext.getAlias().orElseThrow(AssertionError::new),
                                    getUsage().orElseThrow(AssertionError::new)));
                    break;

                case 1:
                    populatePages(api, embed,
                            commandContext.getPrefix().orElseThrow(AssertionError::new),
                            commandContext.getAlias().orElseThrow(AssertionError::new),
                            defaultSearch(parameters.get("search").orElseThrow(AssertionError::new).toLowerCase(ROOT)));
                    break;

                case 2:
                    String searchType = parameters
                            .getParameterNames()
                            .stream()
                            .filter(not("search"::equals))
                            .findAny()
                            .orElseThrow(AssertionError::new);
                    switch (searchType) {
                        case "page":
                        case "p":
                        case "title":
                        case "t":
                            populatePages(api, embed,
                                    commandContext.getPrefix().orElseThrow(AssertionError::new),
                                    commandContext.getAlias().orElseThrow(AssertionError::new),
                                    titleOnly(parameters.get("search").orElseThrow(AssertionError::new).toLowerCase(ROOT)));
                            break;

                        case "full":
                        case "f":
                        case "content":
                        case "c":
                            populatePages(api, embed,
                                    commandContext.getPrefix().orElseThrow(AssertionError::new),
                                    commandContext.getAlias().orElseThrow(AssertionError::new),
                                    fullSearch(parameters.get("search").orElseThrow(AssertionError::new).toLowerCase(ROOT)));
                            break;

                        default:
                            throw new AssertionError(format("Missing case for search type '%s'", searchType));
                    }
                    break;

                default:
                    throw new AssertionError(format("Missing case for parameter count '%s'", parameters.size()));
            }

            CommandCleanupListener.insertResponseTracker(embed, message.getId());
            channel.sendMessage(embed).join();
        } catch (Throwable t) {
            logger
                    .atError()
                    .withThrowable(t)
                    .log("Exception while handling wiki command");

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

    private Predicate<WikiPage> defaultSearch(String searchString) {
        return titleOnly(searchString).or(keywordsOnly(searchString));
    }

    private Predicate<WikiPage> fullSearch(String searchString) {
        return defaultSearch(searchString).or(contentOnly(searchString));
    }

    private Predicate<WikiPage> titleOnly(String searchString) {
        return page -> page.getTitle().toLowerCase().contains(searchString);
    }

    private Predicate<WikiPage> keywordsOnly(String searchString) {
        return page -> Arrays.stream(page.getKeywords())
                .map(String::toLowerCase)
                .anyMatch(keyword -> keyword.contains(searchString));
    }

    private Predicate<WikiPage> contentOnly(String searchString) {
        return page -> page.getContent().toLowerCase().contains(searchString);
    }


    private void populatePages(DiscordApi api, EmbedBuilder embed, String prefix, String alias, Predicate<WikiPage> criteria) throws IOException {
        List<WikiPage> pages;

        pages = new WikiParser(api)
                .getPagesBlocking().stream()
                .filter(criteria)
                .sorted()
                .collect(Collectors.toList());

        if (pages.isEmpty()) {
            embed.setTitle("Javacord Wiki");
            embed.setUrl(WikiParser.BASE_URL + "/wiki/");
            embed.setDescription("No pages found. Maybe try another search.");
            embed.addField("Standard Search", format("Use `%s%s <search>` to search page titles and keywords.", prefix, alias));
            embed.addField("Title Search", format("Use `%s%s [page|p|title|t] <search>` to exclusively search page titles.", prefix, alias));
            embed.addField("Full Search", format("Use `%s%s [full|f|content|c] <search>` to perform a full search.", prefix, alias));
        } else if (pages.size() == 1) {
            WikiPage page = pages.get(0);
            displayPagePreview(embed, page);
        } else {
            displayPageList(embed, pages);
        }
    }

    private void displayPagePreview(EmbedBuilder embed, WikiPage page) {
        embed.setTitle("Javacord Wiki");
        String cleanedDescription = HTML_TAG.matcher(page.getContent()).replaceAll("").trim();
        int length = 0;
        int sentences = 0;
        Matcher endOfSentenceMatcher = END_OF_SENTENCE.matcher(cleanedDescription);
        while ((length < 600) && (sentences < 3) && endOfSentenceMatcher.find(length)) {
            length = endOfSentenceMatcher.end();
            sentences++;
        }
        length = Math.min(length, 1500);
        StringBuilder description = new StringBuilder()
                .append(format("**[%s](%s)**\n\n", page.getTitle(), WikiParser.BASE_URL + page.getPath()))
                .append(cleanedDescription, 0, length);
        if (length < cleanedDescription.length()) {
            description
                    .append("\n\n[*view full page*](")
                    .append(WikiParser.BASE_URL)
                    .append(page.getPath())
                    .append(")");
        }
        embed.setDescription(description.toString());
    }

    private void displayPageList(EmbedBuilder embed, List<WikiPage> pages) {
        embed.setTitle("Javacord Wiki");
        embed.setUrl(WikiParser.BASE_URL + "/wiki/");

        StringBuilder builder = new StringBuilder();
        int counter = 0;
        for (WikiPage page : pages) {
            String pageLink = "• " + page.asMarkdownLink();
            if (builder.length() + pageLink.length() > 1900) { // Prevent hitting the description size limit
                break;
            }
            builder.append(pageLink).append("\n");
            counter++;
        }
        if (pages.size() > counter) {
            builder.append("and ").append(pages.size() - counter).append(" more ...");
        }
        embed.setDescription(builder.toString());
    }
}
