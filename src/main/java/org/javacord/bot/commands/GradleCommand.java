package org.javacord.bot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.bot.Constants;
import org.javacord.bot.listeners.CommandCleanupListener;
import org.javacord.bot.util.LatestVersionFinder;

/**
 * The !gradle command which is used to get information about Javacord with Gradle.
 */
public class GradleCommand implements CommandExecutor {

    private final LatestVersionFinder versionFinder;

    /**
     * Initializes the command.
     * @param versionFinder The version finder to use to determine the latest javacord version.
     */
    public GradleCommand(LatestVersionFinder versionFinder) {
        this.versionFinder = versionFinder;
    }

    /**
     * Executes the {@code !gradle} command.
     *
     * @param server  The server where the command was issued.
     * @param channel The channel where the command was issued.
     * @param message The message the command was issued in.
     */
    @Command(aliases = {"!gradle"}, async = true)
    public void onCommand(Server server, TextChannel channel, Message message) {
        // Only react in #java_javacord channel on Discord API server
        if ((server.getId() == Constants.DAPI_SERVER_ID) && (channel.getId() != Constants.DAPI_JAVACORD_CHANNEL_ID)) {
            return;
        }

        String latestVersion = versionFinder.findLatestVersion().join();
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Constants.JAVACORD_ORANGE)
                .addField("Dependency",
                        "```groovy\n"
                                + "repositories { \n"
                                + "  mavenCentral()\n"
                                + "}\n"
                                + "dependencies { \n"
                                + "  implementation 'org.javacord:javacord:" + latestVersion + "'\n"
                                + "}\n"
                                + "```")
                .addField("Setup Guide", "• [IntelliJ](https://javacord.org/wiki/getting-started/intellij-gradle/)");

        CommandCleanupListener.insertResponseTracker(embed, message.getId());
        channel.sendMessage(embed).join();
    }

}
