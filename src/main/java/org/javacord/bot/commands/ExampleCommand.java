package org.javacord.bot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;
import org.javacord.bot.listeners.CommandCleanupListener;

import java.io.IOException;
import java.io.InputStream;

/**
 * The !example command which is used to get an link to the example bot.
 */
public class ExampleCommand implements CommandExecutor {

    /**
     * Executes the {@code !example} command.
     *
     * @param channel The channel where the command was issued.
     * @param message The message the command was issued in.
     * @throws IOException If the Javacord icon stream cannot be closed properly.
     */
    @Command(aliases = {"!example"}, async = true)
    public void onCommand(TextChannel channel, Message message) throws IOException {
        try (InputStream javacord3Icon = getClass().getClassLoader().getResourceAsStream("javacord3_icon.png")) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setThumbnail(javacord3Icon, "png")
                    .setColor(Constants.JAVACORD_ORANGE)
                    .addField("Example Bot", "https://github.com/Javacord/Example-Bot");
            CommandCleanupListener.insertResponseTracker(embed, message.getId());
            channel.sendMessage(embed).join();
        }
    }

}
