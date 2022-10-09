package org.javacord.bot.commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Alias;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.bot.Constants;

import java.io.IOException;
import java.io.InputStream;

/**
 * The /github command which is used to link to Javacord related GitHub repositories.
 */
@ApplicationScoped
@Alias("github")
@Description("Shows links to the most important GitHub pages")
@Asynchronous
public class GitHubCommand extends BaseSlashCommand {
    @Inject
    Logger logger;

    /**
     * Executes the {@code /github} command.
     */
    @Override
    public void execute(CommandContext<? extends SlashCommandInteraction> commandContext) {
        try (InputStream javacord3Icon = getClass().getResourceAsStream("/javacord3_icon.png")) {
            EmbedBuilder embed = new EmbedBuilder()
                    .addField("Javacord", "https://github.com/Javacord/Javacord")
                    .addField("Example Bot", "https://github.com/Javacord/Example-Bot")
                    .addField("James", "https://github.com/Javacord/Javacord-Bot")
                    .setThumbnail(javacord3Icon)
                    .setColor(Constants.JAVACORD_ORANGE);
            sendResponse(commandContext.getMessage(), embed).join();
        } catch (IOException e) {
            logger
                    .atError()
                    .withThrowable(e)
                    .log("Exception while closing image stream");
        }
    }
}
