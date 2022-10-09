package org.javacord.bot.commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Alias;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import net.kautler.command.api.parameter.Parameters;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.listeners.CommandCleanupListener;

import java.io.IOException;
import java.io.InputStream;

import static org.javacord.bot.Constants.JAVACORD_ORANGE;

/**
 * The !github command which is used to link to Javacord related GitHub repositories.
 */
@ApplicationScoped
@Alias("github")
@Description("Shows links to the most important GitHub pages")
@Asynchronous
public class GitHubCommand extends BaseTextCommand {
    @Inject
    Logger logger;

    /**
     * Executes the {@code !github} command.
     */
    @Override
    protected void doExecute(CommandContext<? extends Message> commandContext, Message message,
                             TextChannel channel, Parameters<String> parameters) {
        try (InputStream javacord3Icon = getClass().getResourceAsStream("/javacord3_icon.png")) {
            EmbedBuilder embed = new EmbedBuilder()
                    .addField("Javacord", "https://github.com/Javacord/Javacord")
                    .addField("Example Bot", "https://github.com/Javacord/Example-Bot")
                    .addField("James", "https://github.com/Javacord/Javacord-Bot")
                    .setThumbnail(javacord3Icon)
                    .setColor(JAVACORD_ORANGE);
            CommandCleanupListener.insertResponseTracker(embed, message.getId());
            channel.sendMessage(embed).join();
        } catch (IOException e) {
            logger
                    .atError()
                    .withThrowable(e)
                    .log("Exception while closing image stream");
        }
    }
}
