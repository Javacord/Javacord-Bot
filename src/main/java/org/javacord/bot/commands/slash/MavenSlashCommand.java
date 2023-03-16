package org.javacord.bot.commands.slash;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.bot.commands.workers.MavenCommandWorker;

/**
 * The /maven command which is used to get information about Javacord with Maven.
 */
@ApplicationScoped
@Description("Shows the Maven dependency")
@Asynchronous
public class MavenSlashCommand extends BaseSlashCommand {
    @Inject
    MavenCommandWorker worker;

    /**
     * Executes the {@code /maven} command.
     */
    @Override
    public void execute(CommandContext<? extends SlashCommandInteraction> commandContext) {
        sendResponse(commandContext, worker.execute()).join();
    }
}
