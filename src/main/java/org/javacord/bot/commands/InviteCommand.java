package org.javacord.bot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.bot.Constants;
import org.javacord.bot.listeners.CommandCleanupListener;

import java.io.IOException;
import java.io.InputStream;

/**
 * The !invite command which is used to get an invite link to the Javacord Discord server.
 */
public class InviteCommand implements CommandExecutor {

    /**
     * Executes the {@code !invite} command.
     *
     * @param server  The server where the command was issued.
     * @param channel The channel where the command was issued.
     * @param message The message the command was issued in.
     * @throws IOException If the Javacord icon stream cannot be closed properly.
     */
    @Command(aliases = {"!invite"}, async = true, description = "Shows the invite link to the Javacord server")
    public void onCommand(Server server, TextChannel channel, Message message) throws IOException {
        // Only react in #java_javacord channel on Discord API server
        if ((server.getId() == Constants.DAPI_SERVER_ID) && (channel.getId() != Constants.DAPI_JAVACORD_CHANNEL_ID)) {
            return;
        }

        try (InputStream javacord3Icon = getClass().getClassLoader().getResourceAsStream("javacord3_icon.png")) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setThumbnail(javacord3Icon, "png")
                    .setColor(Constants.JAVACORD_ORANGE)
                    .addField("Invite Link", "https://discordapp.com/invite/0qJ2jjyneLEgG7y3");

            CommandCleanupListener.insertResponseTracker(embed, message.getId());
            channel.sendMessage(embed).join();
        }
    }

}
