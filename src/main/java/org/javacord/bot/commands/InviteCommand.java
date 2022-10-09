package org.javacord.bot.commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
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
 * The !invite command which is used to get an invite link to the Javacord Discord server.
 */
@ApplicationScoped
@Description("Shows the invite link to the Javacord server")
@Asynchronous
public class InviteCommand extends BaseTextCommand {
    @Inject
    Logger logger;

    /**
     * Executes the {@code !invite} command.
     */
    @Override
    protected void doExecute(CommandContext<? extends Message> commandContext, Message message,
                             TextChannel channel, Parameters<String> parameters) {
        try (InputStream javacord3Icon = getClass().getResourceAsStream("/javacord3_icon.png")) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setThumbnail(javacord3Icon)
                    .setColor(JAVACORD_ORANGE)
                    .addField("Invite Link", "https://discord.gg/javacord");

            CommandCleanupListener.insertResponseTracker(embed, message.getId());
            channel.sendMessage(embed).join();
        } catch (IOException ioe) {
            logger
                    .atError()
                    .withThrowable(ioe)
                    .log("Exception while closing image stream");
        }
    }
}
