package org.javacord.bot.commands.slash;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.bot.commands.workers.InfoCommandWorker;

@ApplicationScoped
@Description("Shows information about this bot")
@Asynchronous
public class InfoSlashCommand extends BaseSlashCommand {
    @Inject
    InfoCommandWorker worker;

    /**
     * Executes the {@code /info} command.
     */
    @Override
    public void execute(CommandContext<? extends SlashCommandInteraction> commandContext) {
        sendResponse(commandContext.getMessage(), worker.execute()).join();
    }
}
