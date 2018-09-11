package org.javacord.bot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;
import org.javacord.bot.listeners.CommandCleanupListener;

public class Sdcf4jCommand implements CommandExecutor {

    /**
     * Executes the {@code !sdcf4j} command.
     *
     * @param channel The channel where the command was issued.
     * @param message The message the command was issued in.
     */
    @Command(aliases = {"!sdcf4j", "!commands"}, async = true)
    public void onCommand(TextChannel channel, Message message) {

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("SDCF4J")
                .setDescription("A simple discord command framework, compatible with Javacord, JDA and Discord4J")
                .addInlineField("GitHub", "https://github.com/Bastian/sdcf4j")
                .addInlineField("Wiki", "https://github.com/Bastian/sdcf4j/wiki")
                .setColor(Constants.JAVACORD_ORANGE);
        CommandCleanupListener.insertResponseTracker(embed, message.getId());
        channel.sendMessage(embed).join();
    }

}
