package org.javacord.bot.commands.text;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import net.kautler.command.api.parameter.Parameters;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.commands.workers.ExampleCommandWorker;
import org.javacord.bot.listeners.TextCommandCleanupListener;

/**
 * The !example command which is used to get a link to the example bot.
 */
@ApplicationScoped
@Description("Shows a link to the example bot")
@Asynchronous
public class ExampleTextCommand extends BaseTextCommand {
    @Inject
    ExampleCommandWorker worker;

    /**
     * Executes the {@code !example} command.
     */
    @Override
    protected void doExecute(CommandContext<? extends Message> commandContext,
                             Message message, Parameters<String> parameters) {
        EmbedBuilder embed = worker.execute();
        TextCommandCleanupListener.insertResponseTracker(embed, message.getId());
        message.reply(embed).join();
    }
}
