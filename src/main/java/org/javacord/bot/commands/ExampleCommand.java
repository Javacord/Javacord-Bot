package org.javacord.bot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;

/**
 * The !example command which is used to get an link to the example bot.
 */
public class ExampleCommand implements CommandExecutor {

    @Command(aliases = {"!example"}, async = true)
    public void onCommand(TextChannel channel) {
        EmbedBuilder embed = new EmbedBuilder()
                .setThumbnail(getClass().getClassLoader().getResourceAsStream("javacord3_icon.png"), "png")
                .setColor(Constants.JAVACORD_ORANGE)
                .addField("Example Bot", "https://github.com/Javacord/Example-Bot");

        channel.sendMessage(embed).join();
    }
}
