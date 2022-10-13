package org.javacord.bot.commands.text;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Alias;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import net.kautler.command.api.parameter.Parameters;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.commands.workers.GitHubCommandWorker;
import org.javacord.bot.listeners.CommandCleanupListener;

/**
 * The !github command which is used to link to Javacord related GitHub repositories.
 */
@ApplicationScoped
@Alias("github")
@Description("Shows links to the most important GitHub pages")
@Asynchronous
public class GitHubTextCommand extends BaseTextCommand {
    @Inject
    GitHubCommandWorker worker;

    /**
     * Executes the {@code !github} command.
     */
    @Override
    protected void doExecute(CommandContext<? extends Message> commandContext,
                             Message message, Parameters<String> parameters) {
        EmbedBuilder embed = worker.execute();
        CommandCleanupListener.insertResponseTracker(embed, message.getId());
        message.reply(embed).join();
    }
}
