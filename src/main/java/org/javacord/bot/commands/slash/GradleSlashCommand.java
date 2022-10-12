package org.javacord.bot.commands.slash;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.bot.commands.workers.GradleCommandWorker;

/**
 * The /gradle command which is used to get information about Javacord with Gradle.
 */
@ApplicationScoped
@Description("Shows the Gradle dependency")
@Asynchronous
public class GradleSlashCommand extends BaseSlashCommand {
    @Inject
    GradleCommandWorker worker;

    /**
     * Executes the {@code /gradle} command.
     */
    @Override
    public void execute(CommandContext<? extends SlashCommandInteraction> commandContext) {
        sendResponse(commandContext.getMessage(), worker.execute()).join();
    }
}
