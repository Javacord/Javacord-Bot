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

    /**
     * Executes the {@code !wiki} command.
     *
     * @param channel The channel where the command was issued.
     */
    @Command(aliases = {"!wiki"}, async = true)
    public void onCommand(TextChannel channel) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Javacord Wiki")
                .setDescription("https://javacord.org/wiki")
                .setThumbnail(getClass().getClassLoader().getResourceAsStream("javacord3_icon.png"), "png")
                .setColor(Constants.JAVACORD_ORANGE);
        channel.sendMessage(embed).join();
    }

}
