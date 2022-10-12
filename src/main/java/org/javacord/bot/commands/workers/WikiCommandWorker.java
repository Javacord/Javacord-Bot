package org.javacord.bot.commands.workers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import org.javacord.bot.Constants;
import org.javacord.bot.util.JavacordIconProvider;
import org.javacord.bot.util.wiki.parser.WikiPage;
import org.javacord.bot.util.wiki.parser.WikiParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The wiki commands worker which is used to link to Javacord's wiki.
 */
@ApplicationScoped
public class WikiCommandWorker {
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]++>");
    private static final Pattern END_OF_SENTENCE = Pattern.compile("\\.(?: |\\r?\\n)");

    @Inject
    DiscordApi api;

    @Inject
    Logger logger;

    @Inject
    JavacordIconProvider iconProvider;

    /**
     * Executes the {@code wiki} commands.
     */
    public CompletableFuture<EmbedBuilder> execute(String searchTerm, boolean searchInKeywords,
                                                   boolean searchInTitles, boolean searchInContents) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                EmbedBuilder embed = new EmbedBuilder()
                        .setThumbnail(iconProvider.getIcon())
                        .setColor(Constants.JAVACORD_ORANGE);

                if (searchTerm == null) {
                    // Just an overview
                    embed.setTitle("Javacord Wiki")
                            .setDescription(String.format("The [Javacord Wiki](%s/wiki) is an excellent "
                                    + "resource to get you started with Javacord.\n", WikiParser.BASE_URL));
                } else {
                    String lowerCaseSearchTerm = searchTerm.toLowerCase(Locale.ROOT);

                    Predicate<WikiPage> searchCriteria = wikiPage -> false;

                    if (searchInKeywords) {
                        searchCriteria = searchCriteria.or(keywordsOnly(lowerCaseSearchTerm));
                    }
                    if (searchInTitles) {
                        searchCriteria = searchCriteria.or(titleOnly(lowerCaseSearchTerm));
                    }
                    if (searchInContents) {
                        searchCriteria = searchCriteria.or(contentOnly(lowerCaseSearchTerm));
                    }

                    populatePages(api, embed, searchCriteria);
                }
                return embed;
            } catch (Throwable t) {
                logger
                        .atError()
                        .withThrowable(t)
                        .log("Exception while handling wiki command");

                return new EmbedBuilder()
                        .setTitle("Error")
                        .setDescription(String.format(
                                "Something went wrong: ```%s```",
                                ExceptionLogger.unwrapThrowable(t).getMessage()))
                        .setColor(Constants.ERROR_COLOR);
            }
        }, api.getThreadPool().getExecutorService());
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


    private void populatePages(DiscordApi api, EmbedBuilder embed, Predicate<WikiPage> criteria) throws IOException {
        List<WikiPage> pages;

        pages = new WikiParser(api)
                .getPagesBlocking().stream()
                .filter(criteria)
                .sorted()
                .collect(Collectors.toList());

        if (pages.isEmpty()) {
            embed.setTitle("Javacord Wiki");
            embed.setUrl(WikiParser.BASE_URL + "/wiki/");
            embed.setDescription("No pages found. Maybe try another search or tweak the search parameters.");
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
                .append(String.format("**[%s](%s)**\n\n", page.getTitle(), WikiParser.BASE_URL + page.getPath()))
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
