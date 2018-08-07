package org.javacord.bot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;
import org.javacord.bot.util.CommandHelper;

/**
 * The !wiki command which is used to link to Javacord's wiki.
 */
public class WikiCommand implements CommandExecutor {

    @Command(aliases = {"!wiki"}, async = true)
    public void onCommand(TextChannel channel, Message commandMessage) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Javacord Wiki")
                .setDescription("https://javacord.org/wiki")
                .setThumbnail(getClass().getClassLoader().getResourceAsStream("javacord3_icon.png"), "png")
                .setColor(Constants.JAVACORD_ORANGE);
        CommandHelper.messageCleanup(commandMessage, channel.sendMessage(embed).join());
    }

}
