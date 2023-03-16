package org.javacord.bot.commands.slash;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.callback.InteractionMessageBuilderBase;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import org.javacord.bot.commands.workers.WikiCommandWorker;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The /wiki command which is used to link to Javacord's wiki.
 */
@ApplicationScoped
@Description("Shows a link to the wiki or searches through it")
@Asynchronous
public class WikiSlashCommand extends BaseSlashCommand {
    private static final String SEARCH_TERM = "search-term";
    private static final String SEARCH_IN_KEYWORDS = "search-in-keywords";
    private static final String SEARCH_IN_TITLES = "search-in-titles";
    private static final String SEARCH_IN_CONTENTS = "search-in-contents";

    @Inject
    WikiCommandWorker worker;

    @Override
    public List<SlashCommandOption> getOptions() {
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
        CompletableFuture<InteractionOriginalResponseUpdater> responseUpdater =
                sendResponseLater(commandContext);
        SlashCommandInteraction slashCommandInteraction = commandContext.getMessage();

        String searchTerm = slashCommandInteraction
                .getArgumentStringValueByName(SEARCH_TERM)
                .orElse(null);
        boolean searchInKeywords = slashCommandInteraction
                .getArgumentBooleanValueByName(SEARCH_IN_KEYWORDS)
                .orElse(Boolean.TRUE);
        boolean searchInTitles = slashCommandInteraction
                .getArgumentBooleanValueByName(SEARCH_IN_TITLES)
                .orElse(Boolean.TRUE);
        boolean searchInContents = slashCommandInteraction
                .getArgumentBooleanValueByName(SEARCH_IN_CONTENTS)
                .orElse(Boolean.FALSE);

        responseUpdater
                .thenCombine(
                        worker.execute(searchTerm, searchInKeywords, searchInTitles, searchInContents),
                        InteractionMessageBuilderBase::addEmbed)
                .thenCompose(InteractionOriginalResponseUpdater::update)
                .join();
    }
}
