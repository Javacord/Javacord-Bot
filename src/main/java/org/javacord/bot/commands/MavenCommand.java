package org.javacord.bot.commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.bot.Constants;
import org.javacord.bot.util.LatestVersionFinder;

/**
 * The /maven command which is used to get information about Javacord with Maven.
 */
@ApplicationScoped
@Description("Shows the Maven dependency")
@Asynchronous
public class MavenCommand extends BaseSlashCommand {
    @Inject
    LatestVersionFinder versionFinder;

    /**
     * Executes the {@code /maven} command.
     */
    @Override
    public void execute(CommandContext<? extends SlashCommandInteraction> commandContext) {
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
        sendResponse(commandContext.getMessage(), embed).join();
    }
}
