package org.javacord.bot.commands.slash;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.bot.commands.workers.ExampleCommandWorker;

/**
 * The /example command which is used to get a link to the example bot.
 */
@ApplicationScoped
@Description("Shows a link to the example bot")
@Asynchronous
public class ExampleSlashCommand extends BaseSlashCommand {
    @Inject
    ExampleCommandWorker worker;

    /**
     * Executes the {@code /example} command.
     */
    @Override
    public void execute(CommandContext<? extends SlashCommandInteraction> commandContext) {
        sendResponse(commandContext, worker.execute()).join();
    }
}
