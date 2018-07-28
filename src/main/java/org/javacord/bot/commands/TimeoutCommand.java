package org.javacord.bot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.bot.Constants;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The !timeout command which will be used to temporarily mute users in the Javacord server.
 */
public class TimeoutCommand implements CommandExecutor {

    @Command(aliases = {"!timeout"}, usage = "!timeout @User 1d2h3m",
            description = "Define a duration using this Format: 1d12h30m = 1 Day, 12 Hours, 30 Minutes",
            requiredPermissions = "moderation", async = true, privateMessages = false)
    public void onCommand(DiscordApi api, TextChannel channel, Server server, Message commandMessage, String[] args) {
        EmbedBuilder embed = new EmbedBuilder()
                .setThumbnail(getClass().getClassLoader().getResourceAsStream("javacord3_icon.png"), "png")
                .setColor(Constants.JAVACORD_ORANGE);
        if (args.length == 2) { // assuming correct usage, because only a few users can actually use this.
            List<User> mentionedUsers = commandMessage.getMentionedUsers();
            if (mentionedUsers.size() == 1) {
                long duration = convertSimpleDuration(args[1], TimeUnit.SECONDS);
                User user = mentionedUsers.get(0);
                List<Role> rolesByName = server.getRolesByName("Timeout");
                if (rolesByName.size() == 1) {
                    Role timeoutRole = rolesByName.get(0);
                    user.addRole(timeoutRole);
                    api.getThreadPool().getScheduler().schedule(
                            () -> user.removeRole(timeoutRole),
                            duration,
                            TimeUnit.SECONDS
                    ); // schedule the role being removed after the given duration
                    embed.setTitle("User muted");
                    embed.setDescription("User " + user.getDisplayName(server) + " has been muted for " +
                            TimeUnit.SECONDS.toMinutes(duration) + " minutes.");
                    channel.sendMessage(embed);
                }
            } // stays silent if there was no or too many user mention/s
        }
    }

    private long convertSimpleDuration(String durationString, TimeUnit toUnit) {
        String upperDurationString = durationString.toUpperCase();
        if (upperDurationString.indexOf('D') >= 0) {
            return toUnit.convert(
                    Duration.parse("P" + upperDurationString.replace("D", "DT")).toMillis(),
                    TimeUnit.MILLISECONDS);
        } else {
            return toUnit.convert(
                    Duration.parse("PT" + upperDurationString).toMillis(),
                    TimeUnit.MILLISECONDS);
        }
    }
}
