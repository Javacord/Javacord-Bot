package org.javacord.bot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;
import org.javacord.bot.util.CommandHelper;

/**
 * The !invite command which is used to get an invite link to the Javacord Discord server.
 */
public class InviteCommand implements CommandExecutor {

    @Command(aliases = {"!invite"}, async = true)
    public void onCommand(TextChannel channel, Message commandMessage) {
        EmbedBuilder embed = new EmbedBuilder()
                .setThumbnail(getClass().getClassLoader().getResourceAsStream("javacord3_icon.png"), "png")
                .setColor(Constants.JAVACORD_ORANGE)
                .addField("Invite Link", "https://discordapp.com/invite/0qJ2jjyneLEgG7y3");
        CommandHelper.messageCleanup(commandMessage, channel.sendMessage(embed).join());
    }
}
