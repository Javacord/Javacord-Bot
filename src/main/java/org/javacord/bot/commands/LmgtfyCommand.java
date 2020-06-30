package org.javacord.bot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.bot.Constants;
import org.javacord.bot.listeners.CommandCleanupListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * The !lmgtfy command which is used to help others searching the internet.
 */
public class LmgtfyCommand implements CommandExecutor {

    /**
     * Executes the {@code !lmgtfy} command.
     *
     * @param server  The server where the command was issued.
     * @param channel The channel where the command was issued.
     * @param message The message the command was issued in.
     * @param args The command's arguments.
     */
    @Command(aliases = {"!lmgtfy"}, async = true, description = "Helps users searching the internet with DuckDuckGo")
    public void onCommand(Server server, TextChannel channel, Message message, String[] args) {
        // Only react in #java_javacord channel on Discord API server
        if ((server.getId() == Constants.DAPI_SERVER_ID) && (channel.getId() != Constants.DAPI_JAVACORD_CHANNEL_ID)) {
            return;
        }

        EmbedBuilder embed = new EmbedBuilder();
        if (args.length == 0) {
            embed.setColor(Constants.ERROR_COLOR)
                    .setTitle("Error")
                    .setDescription("The `!lmgtfy` command needs arguments!");
        } else {
            try {
                embed.setColor(Constants.JAVACORD_ORANGE)
                        .setTitle("Lmgtfy")
                        .setDescription("https://lmgtfy.com/?s=d&iie=1&q="
                                + URLEncoder.encode(String.join(" ", args), StandardCharsets.UTF_8.toString()));
            } catch (UnsupportedEncodingException ignored) { }
        }

        CommandCleanupListener.insertResponseTracker(embed, message.getId());
        channel.sendMessage(embed).join();
    }
}
