package org.javacord.bot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;

/**
 * The !wiki command which is used to link to Javacord's wiki.
 */
public class WikiCommand implements CommandExecutor {

    @Command(aliases = {"!wiki"})
    public void onDocsCommand(TextChannel channel) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Javacord Wiki")
                .setDescription("https://github.com/Javacord/Javacord/wiki")
                .setThumbnail(getClass().getClassLoader().getResourceAsStream("javacord3_icon.png"), "png")
                .setColor(Constants.JAVACORD_ORANGE);
        channel.sendMessage(embed);
    }

}
