package org.javacord.bot.commands.slash;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.bot.commands.workers.InviteCommandWorker;

/**
 * The /invite command which is used to get an invite link to the Javacord Discord server.
 */
@ApplicationScoped
@Description("Shows the invite link to the Javacord server")
@Asynchronous
public class InviteSlashCommand extends BaseSlashCommand {
    @Inject
    InviteCommandWorker worker;

    /**
     * Executes the {@code /invite} command.
     */
    @Override
    public void execute(CommandContext<? extends SlashCommandInteraction> commandContext) {
        sendResponse(commandContext.getMessage(), worker.execute()).join();
    }
}
