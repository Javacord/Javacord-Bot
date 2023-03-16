package org.javacord.bot.commands.text;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import net.kautler.command.api.parameter.Parameters;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.commands.workers.GradleCommandWorker;
import org.javacord.bot.listeners.TextCommandCleanupListener;

/**
 * The !gradle command which is used to get information about Javacord with Gradle.
 */
@ApplicationScoped
@Description("Shows the Gradle dependency")
@Asynchronous
public class GradleTextCommand extends BaseTextCommand {
    @Inject
    GradleCommandWorker worker;

    /**
     * Executes the {@code !gradle} command.
     */
    @Override
    protected void doExecute(CommandContext<? extends Message> commandContext,
                             Message message, Parameters<String> parameters) {
        EmbedBuilder embed = worker.execute();
        TextCommandCleanupListener.insertResponseTracker(embed, message.getId());
        message.reply(embed).join();
    }
}
