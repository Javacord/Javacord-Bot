package org.javacord.bot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;

/**
 * The !github command which is used to link to Javacord related GitHub repositories.
 */
public class GitHubCommand implements CommandExecutor {

    /**
     * Executes the {@code !github} command.
     *
     * @param channel The channel where the command was issued.
     */
    @Command(aliases = {"!github"}, async = true)
    public void onCommand(TextChannel channel) {
        EmbedBuilder embed = new EmbedBuilder()
                .addField("Javacord", "https://github.com/Javacord/Javacord")
                .addField("Example Bot", "https://github.com/Javacord/Example-Bot")
                .setThumbnail(getClass().getClassLoader().getResourceAsStream("javacord3_icon.png"), "png")
                .setColor(Constants.JAVACORD_ORANGE);
        channel.sendMessage(embed).join();
    }

}
