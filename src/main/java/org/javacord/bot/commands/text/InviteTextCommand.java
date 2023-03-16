package org.javacord.bot.commands.text;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import net.kautler.command.api.parameter.Parameters;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.commands.workers.InviteCommandWorker;
import org.javacord.bot.listeners.TextCommandCleanupListener;

/**
 * The !invite command which is used to get an invite link to the Javacord Discord server.
 */
@ApplicationScoped
@Description("Shows the invite link to the Javacord server")
@Asynchronous
public class InviteTextCommand extends BaseTextCommand {
    @Inject
    InviteCommandWorker worker;

    /**
     * Executes the {@code !invite} command.
     */
    protected void doExecute(CommandContext<? extends Message> commandContext,
                             Message message, Parameters<String> parameters) {
        EmbedBuilder embed = worker.execute();
        TextCommandCleanupListener.insertResponseTracker(embed, message.getId());
        message.reply(embed).join();
    }
}
