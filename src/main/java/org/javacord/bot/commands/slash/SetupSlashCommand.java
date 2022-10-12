package org.javacord.bot.commands.slash;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.bot.commands.workers.SetupCommandWorker;

/**
 * The /setup command which is used to get information useful for first setup.
 */
@ApplicationScoped
@Description("Shows useful information to setup a Javacord bot")
@Asynchronous
public class SetupSlashCommand extends BaseSlashCommand {
    @Inject
    SetupCommandWorker worker;

    /**
     * Executes the {@code /setup} command.
     */
    @Override
    public void execute(CommandContext<? extends SlashCommandInteraction> commandContext) {
        sendResponse(commandContext.getMessage(), worker.execute()).join();
    }
}
