package org.javacord.bot.commands.text;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import net.kautler.command.api.parameter.Parameters;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.commands.workers.MavenCommandWorker;
import org.javacord.bot.listeners.CommandCleanupListener;

/**
 * The !maven command which is used to get information about Javacord with Maven.
 */
@ApplicationScoped
@Description("Shows the Maven dependency")
@Asynchronous
public class MavenTextCommand extends BaseTextCommand {
    @Inject
    MavenCommandWorker worker;

    /**
     * Executes the {@code !maven} command.
     */
    @Override
    protected void doExecute(CommandContext<? extends Message> commandContext,
                             Message message, Parameters<String> parameters) {
        EmbedBuilder embed = worker.execute();
        CommandCleanupListener.insertResponseTracker(embed, message.getId());
        message.reply(embed).join();
    }
}
