package org.javacord.bot.commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.Command;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Alias;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.RestrictedTo;
import net.kautler.command.api.annotation.RestrictionPolicy;
import net.kautler.command.api.annotation.RestrictionPolicy.Policy;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.bot.Constants;
import org.javacord.bot.listeners.CommandCleanupListener;
import org.javacord.bot.restrictions.Before2023;
import org.javacord.bot.restrictions.JavacordChannelIfOnDapiServer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;

@ApplicationScoped
@Alias("docs")
@Alias("example")
@Alias("github")
@Alias("gradle")
@Alias("info")
@Alias("help")
@Alias("invite")
@Alias("maven")
@Alias("sdcf4j")
@Alias("setup")
@Alias("wiki")
@RestrictedTo(JavacordChannelIfOnDapiServer.class)
@RestrictedTo(Before2023.class)
@RestrictionPolicy(Policy.ALL_OF)
@Asynchronous
//TODO: After 2022 delete this class, command cleanup listener and restriction classes
public class MigratedToSlashCommandsNotifier implements Command<Message> {
    @Inject
    Logger logger;

    @Override
    public void execute(CommandContext<? extends Message> commandContext) {
        Message message = commandContext.getMessage();
        try (InputStream javacord3Icon = getClass().getResourceAsStream("/javacord3_icon.png")) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setThumbnail(javacord3Icon)
                    .setColor(Constants.JAVACORD_ORANGE)
                    .setTitle("Migrated to Slash Commands")
                    .setDescription("This bot was migrated to slash commands, just type `/` to discover them");

            message
                    .getApi()
                    .getGlobalSlashCommands()
                    .thenAccept(slashCommands -> slashCommands
                            .stream()
                            .sorted(Comparator.comparing(SlashCommand::getName))
                            .forEachOrdered(slashCommand -> embed.addField(
                                    String.format("</%s:%d>", slashCommand.getName(), slashCommand.getId()),
                                    slashCommand.getDescription())
                            ))
                    .join();

            CommandCleanupListener.insertResponseTracker(embed, message.getId());
            message.reply(embed).join();
        } catch (IOException ioe) {
            logger
                    .atError()
                    .withThrowable(ioe)
                    .log("Exception while closing image stream");
        }
    }
}
