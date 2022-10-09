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
 * The /gradle command which is used to get information about Javacord with Gradle.
 */
@ApplicationScoped
@Description("Shows the Gradle dependency")
@Asynchronous
public class GradleCommand extends BaseSlashCommand {
    @Inject
    LatestVersionFinder versionFinder;

    /**
     * Executes the {@code /gradle} command.
     */
    @Override
    public void execute(CommandContext<? extends SlashCommandInteraction> commandContext) {
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
        sendResponse(commandContext.getMessage(), embed).join();
    }
}
