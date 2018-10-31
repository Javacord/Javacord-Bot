package org.javacord.bot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;

/**
 * The !invite command which is used to get an invite link to the Javacord Discord server.
 */
public class InviteCommand implements CommandExecutor {

    /**
     * Executes the {@code !invite} command.
     *
     * @param channel The channel where the command was issued.
     */
    @Command(aliases = {"!invite"}, async = true)
    public void onCommand(TextChannel channel) {
        EmbedBuilder embed = new EmbedBuilder()
                .setThumbnail(getClass().getClassLoader().getResourceAsStream("javacord3_icon.png"), "png")
                .setColor(Constants.JAVACORD_ORANGE)
                .addField("Invite Link", "https://discordapp.com/invite/0qJ2jjyneLEgG7y3");

        channel.sendMessage(embed).join();
    }

}
