package org.javacord.bot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;

/**
 * The !docs command which is used to show links to Javacord's JavaDocs.
 */
public class DocsCommand implements CommandExecutor {

    @Command(aliases = {"!docs"})
    public void onDocsCommand(TextChannel channel) {
        EmbedBuilder embed = new EmbedBuilder()
                .addField("Overview", "https://docs.javacord.org/")
                .addField("Latest release version", "https://docs.javacord.org/api/v/latest")
                .addField("Latest snapshot", "https://docs.javacord.org/api/build/latest")
                .setThumbnail(getClass().getClassLoader().getResourceAsStream("javacord3_icon.png"), "png")
                .setColor(Constants.JAVACORD_ORANGE);
        channel.sendMessage(embed);
    }

}
