package org.javacord.bot.commands.slash;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Alias;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.bot.commands.workers.GitHubCommandWorker;

/**
 * The /github command which is used to link to Javacord related GitHub repositories.
 */
@ApplicationScoped
@Alias("github")
@Description("Shows links to the most important GitHub pages")
@Asynchronous
public class GitHubSlashCommand extends BaseSlashCommand {
    @Inject
    GitHubCommandWorker worker;

    /**
     * Executes the {@code /github} command.
     */
    @Override
    public void execute(CommandContext<? extends SlashCommandInteraction> commandContext) {
        sendResponse(commandContext, worker.execute()).join();
    }
}
