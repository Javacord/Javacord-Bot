package org.javacord.bot.commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.bot.Constants;

import java.io.IOException;
import java.io.InputStream;

/**
 * The /invite command which is used to get an invite link to the Javacord Discord server.
 */
@ApplicationScoped
@Description("Shows the invite link to the Javacord server")
@Asynchronous
public class InviteCommand extends BaseSlashCommand {
    @Inject
    Logger logger;

    /**
     * Executes the {@code /invite} command.
     */
    @Override
    public void execute(CommandContext<? extends SlashCommandInteraction> commandContext) {
        try (InputStream javacord3Icon = getClass().getResourceAsStream("/javacord3_icon.png")) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setThumbnail(javacord3Icon)
                    .setColor(Constants.JAVACORD_ORANGE)
                    .addField("Invite Link", "https://discord.gg/javacord");
            sendResponse(commandContext.getMessage(), embed).join();
        } catch (IOException ioe) {
            logger
                    .atError()
                    .withThrowable(ioe)
                    .log("Exception while closing image stream");
        }
    }
}
