package org.javacord.bot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;

/**
 * The !gradle command which is used to get information about Javacord with Gradle.
 */
public class GradleCommand implements CommandExecutor {

    /**
     * Executes the {@code !gradle} command.
     *
     * @param channel The channel where the command was issued.
     */
    @Command(aliases = {"!gradle"}, async = true)
    public void onCommand(TextChannel channel) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Constants.JAVACORD_ORANGE)
                .addField("Dependency",
                        "```groovy\n"
                                + "repositories { \n"
                                + "  mavenCentral()\n"
                                + "}\n"
                                + "dependencies { \n"
                                // TODO Always use the latest version
                                + "  implementation 'org.javacord:javacord:3.0.0'\n"
                                + "}\n"
                                + "```")
                .addField("Setup Guide", "• [IntelliJ](https://javacord.org/wiki/getting-started/intellij-gradle/)");

        channel.sendMessage(embed).join();
    }

}
