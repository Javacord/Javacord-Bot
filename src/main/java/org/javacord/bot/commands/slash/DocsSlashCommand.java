package org.javacord.bot.commands.slash;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.interaction.callback.InteractionMessageBuilderBase;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import org.javacord.bot.commands.workers.DocsCommandWorker;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The /docs command which is used to show links to Javacord's JavaDocs.
 */
@ApplicationScoped
@Description("Shows a link to the JavaDoc or searches through it")
@Asynchronous
public class DocsSlashCommand extends BaseSlashCommand {
    private static final String SEARCH_TERM = "search-term";
    private static final String SEARCH_TYPE = "search-type";
    private static final String SEARCH_TYPE_CLASSES = "classes";
    private static final String SEARCH_TYPE_MEMBERS = "members";
    private static final String INCLUDE_ALL = "include-all";

    @Inject
    DocsCommandWorker worker;

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
        CompletableFuture<InteractionOriginalResponseUpdater> responseUpdater =
                sendResponseLater(slashCommandInteraction);

        String searchTerm = slashCommandInteraction
                .getArgumentStringValueByName(SEARCH_TERM)
                .orElse(null);
        String searchType = slashCommandInteraction
                .getArgumentStringValueByName(SEARCH_TYPE)
                .orElse(SEARCH_TYPE_MEMBERS);
        boolean includeAll = slashCommandInteraction
                .getArgumentBooleanValueByName(INCLUDE_ALL)
                .orElse(Boolean.FALSE);

        responseUpdater
                .thenCombine(
                        worker.execute(searchTerm, searchType, includeAll),
                        InteractionMessageBuilderBase::addEmbed)
                .thenCompose(InteractionOriginalResponseUpdater::update)
                .join();
    }
}
