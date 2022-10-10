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
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import org.javacord.api.util.logging.ExceptionLogger;
import org.javacord.bot.Constants;
import org.javacord.bot.util.wiki.parser.WikiPage;
import org.javacord.bot.util.wiki.parser.WikiParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The /wiki command which is used to link to Javacord's wiki.
 */
@ApplicationScoped
@Description("Shows a link to the wiki or searches through it")
@Asynchronous
public class WikiCommand extends BaseSlashCommand {
    private static final String SEARCH_TERM = "search-term";
    private static final String SEARCH_IN_KEYWORDS = "search-in-keywords";
    private static final String SEARCH_IN_TITLES = "search-in-titles";
    private static final String SEARCH_IN_CONTENTS = "search-in-contents";
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]++>");
    private static final Pattern END_OF_SENTENCE = Pattern.compile("\\.(?: |\\r?\\n)");

    @Inject
    Logger logger;

    @Override
    protected List<SlashCommandOption> doGetOptions() {
        return List.of(
                SlashCommandOption.createStringOption(
                        SEARCH_TERM,
                        "The term to search for",
                        false),
                SlashCommandOption.createBooleanOption(
                        SEARCH_IN_KEYWORDS,
                        "Whether to search in keywords (default: true)",
                        false),
                SlashCommandOption.createBooleanOption(
                        SEARCH_IN_TITLES,
                        "Whether to search in titles (default: true)",
                        false),
                SlashCommandOption.createBooleanOption(
                        SEARCH_IN_CONTENTS,
                        "Whether to search in content (default: false)",
                        false));
    }

    /**
     * Executes the {@code /wiki} command.
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
                // Just an overview
                embed.setTitle("Javacord Wiki")
                        .setDescription(String.format("The [Javacord Wiki](%s/wiki) is an excellent "
                                + "resource to get you started with Javacord.\n", WikiParser.BASE_URL));
            } else {
                String searchTerm = optionalSearchTerm.get();

                Predicate<WikiPage> searchCriteria = wikiPage -> false;

                if (slashCommandInteraction.getOptionBooleanValueByName(SEARCH_IN_KEYWORDS).orElse(Boolean.TRUE)) {
                    searchCriteria = searchCriteria.or(keywordsOnly(searchTerm));
                }
                if (slashCommandInteraction.getOptionBooleanValueByName(SEARCH_IN_TITLES).orElse(Boolean.TRUE)) {
                    searchCriteria = searchCriteria.or(titleOnly(searchTerm));
                }
                if (slashCommandInteraction.getOptionBooleanValueByName(SEARCH_IN_CONTENTS).orElse(Boolean.FALSE)) {
                    searchCriteria = searchCriteria.or(contentOnly(searchTerm));
                }

                populatePages(api, embed, searchCriteria);
            }

            responseUpdater.thenCompose(updater -> updater.addEmbed(embed).update()).join();
        } catch (Throwable t) {
            logger
                    .atError()
                    .withThrowable(t)
                    .log("Exception while handling wiki command");

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription(String.format(
                            "Something went wrong: ```%s```",
                            ExceptionLogger.unwrapThrowable(t).getMessage()))
                    .setColor(Constants.ERROR_COLOR);
            responseUpdater.thenCompose(updater -> updater.addEmbed(embed).update()).join();
        }
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
