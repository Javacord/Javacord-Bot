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
import org.javacord.bot.Constants;
import org.javacord.bot.listeners.CommandCleanupListener;

import java.io.IOException;
import java.io.InputStream;

/**
 * The !example command which is used to get a link to the example bot.
 */
@ApplicationScoped
@Description("Shows a link to the example bot")
@Asynchronous
public class ExampleCommand extends BaseTextCommand {
    @Inject
    Logger logger;

    /**
     * Executes the {@code !example} command.
     */
    @Override
    protected void doExecute(CommandContext<? extends Message> commandContext, Message message,
                             TextChannel channel, Parameters<String> parameters) {
        try (InputStream javacord3Icon = getClass().getResourceAsStream("/javacord3_icon.png")) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setThumbnail(javacord3Icon)
                    .setColor(Constants.JAVACORD_ORANGE)
                    .addField("Example Bot", "https://github.com/Javacord/Example-Bot");
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
