package org.javacord.bot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;

import java.io.IOException;
import java.io.InputStream;

/**
 * The !invite command which is used to get an invite link to the Javacord Discord server.
 */
public class InviteCommand implements CommandExecutor {

    /**
     * Executes the {@code !invite} command.
     *
     * @param channel The channel where the command was issued.
     * @throws IOException If the Javacord icon stream cannot be closed properly.
     */
    @Command(aliases = {"!invite"}, async = true)
    public void onCommand(TextChannel channel) throws IOException {
        try (InputStream javacord3Icon = getClass().getClassLoader().getResourceAsStream("javacord3_icon.png")) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setThumbnail(javacord3Icon, "png")
                    .setColor(Constants.JAVACORD_ORANGE)
                    .addField("Invite Link", "https://discordapp.com/invite/0qJ2jjyneLEgG7y3");

            channel.sendMessage(embed).join();
        }
    }

}
