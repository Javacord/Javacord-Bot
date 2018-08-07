package org.javacord.bot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;
import org.javacord.bot.util.CommandHelper;

/**
 * The !github command which is used to link to Javacord related GitHub repositories.
 */
public class GitHubCommand implements CommandExecutor {

    @Command(aliases = {"!github"}, async = true)
    public void onCommand(TextChannel channel, Message commandMessage) {
        EmbedBuilder embed = new EmbedBuilder()
                .addField("Javacord", "https://github.com/Javacord/Javacord")
                .addField("Example Bot", "https://github.com/Javacord/Example-Bot")
                .setThumbnail(getClass().getClassLoader().getResourceAsStream("javacord3_icon.png"), "png")
                .setColor(Constants.JAVACORD_ORANGE);
        CommandHelper.messageCleanup(commandMessage, channel.sendMessage(embed).join());
    }

}
