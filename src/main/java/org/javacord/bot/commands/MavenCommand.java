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
 * The !maven command which is used to get information about Javacord with Maven.
 */
public class MavenCommand implements CommandExecutor {

    private final LatestVersionFinder versionFinder;

    /**
     * Initializes the Command.
     * @param versionFinder The version finder to use to determine the latest javacord version.
     */
    public MavenCommand(LatestVersionFinder versionFinder) {
        this.versionFinder = versionFinder;
    }

    /**
     * Executes the {@code !maven} command.
     *
     * @param server  The server where the command was issued.
     * @param channel The channel where the command was issued.
     * @param message The message the command was issued in.
     */
    @Command(aliases = {"!maven"}, async = true, description = "Shows the Maven dependency")
    public void onCommand(Server server, TextChannel channel, Message message) {
        // Only react in #java_javacord channel on Discord API server
        if ((server.getId() == Constants.DAPI_SERVER_ID) && (channel.getId() != Constants.DAPI_JAVACORD_CHANNEL_ID)) {
            return;
        }

        String latestVersion = versionFinder.findLatestVersion().join();
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Constants.JAVACORD_ORANGE)
                .addField("Dependency",
                        "```xml\n"
                                + "<dependency>\n"
                                + "    <groupId>org.javacord</groupId>\n"
                                + "    <artifactId>javacord</artifactId>\n"
                                + "    <version>" + latestVersion + "</version>\n"
                                + "    <type>pom</type>\n"
                                + "</dependency>\n"
                                + "```")
                .addField("Setup Guides",
                        "• [IntelliJ](https://javacord.org/wiki/getting-started/intellij-maven/)\n"
                                + "• [Eclipse](https://javacord.org/wiki/getting-started/eclipse-maven/)");

        CommandCleanupListener.insertResponseTracker(embed, message.getId());
        channel.sendMessage(embed).join();
    }

}
