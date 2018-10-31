package org.javacord.bot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;

/**
 * The !setup command which is used to get information useful for first setup.
 */
public class SetupCommand implements CommandExecutor {

    /**
     * Executes the {@code !setup} command.
     *
     * @param channel The channel where the command was issued.
     */
    @Command(aliases = {"!setup"}, async = true)
    public void onCommand(TextChannel channel) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Constants.JAVACORD_ORANGE)
                .addField("Gradle Dependency",
                        "```groovy\n"
                                + "repositories { \n"
                                + "  mavenCentral()\n"
                                + "}\n"
                                + "dependencies { \n"
                                // TODO Always use the latest version
                                + "  implementation 'org.javacord:javacord:3.0.0'\n"
                                + "}\n"
                                + "```")
                .addField("Maven Dependency",
                        "```xml\n"
                                + "<dependency>\n"
                                + "    <groupId>org.javacord</groupId>\n"
                                + "    <artifactId>javacord</artifactId>\n"
                                // TODO Always use the latest version
                                + "    <version>3.0.0</version>\n"
                                + "    <type>pom</type>\n"
                                + "</dependency>\n"
                                + "```")
                .addField("Setup Guides",
                        "• [IntelliJ + Gradle](https://javacord.org/wiki/getting-started/intellij-gradle/) (recommended)\n"
                                + "• [IntelliJ + Maven](https://javacord.org/wiki/getting-started/intellij-maven/)\n"
                                + "• [Eclipse + Maven](https://javacord.org/wiki/getting-started/eclipse-maven/)");

        channel.sendMessage(embed).join();
    }

}
