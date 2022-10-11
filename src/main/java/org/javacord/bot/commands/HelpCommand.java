package org.javacord.bot.commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import net.kautler.command.api.Command;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import net.kautler.command.api.parameter.Parameters;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;
import org.javacord.bot.listeners.CommandCleanupListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The !help command which is used to list all commands.
 */
@ApplicationScoped
@Description("Shows the help page")
@Asynchronous
public class HelpCommand extends BaseTextCommand {
    @Inject
    Logger logger;

    @Inject
    Instance<Command<Message>> textCommands;

    /**
     * Executes the {@code !help} command.
     */
    @Override
    protected void doExecute(CommandContext<? extends Message> commandContext, Message message,
                             TextChannel channel, Parameters<String> parameters) {
        // TODO Provide help for specific commands (e.g., "!help wiki")
        try (InputStream javacord3Icon = getClass().getResourceAsStream("/javacord3_icon.png")) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setThumbnail(javacord3Icon)
                    .setTitle("Commands")
                    .setColor(Constants.JAVACORD_ORANGE);

            String prefix = commandContext.getPrefix().orElseThrow(AssertionError::new);
            textCommands
                    .stream()
                    .sorted(Comparator.comparing(command -> command.getAliases().get(0)))
                    .forEachOrdered(command -> {
                        List<String> lines = new ArrayList<>();
                        if (command.getAliases().size() > 1) {
                            lines.add(String.format(
                                    "**Aliases:** %s",
                                    command
                                            .getAliases()
                                            .stream()
                                            .skip(1)
                                            .collect(Collectors.joining(", " + prefix, prefix, ""))));
                        }
                        command
                                .getDescription()
                                .ifPresent(description -> lines.add(String.format("**Description:** %s", description)));
                        command.getUsage().ifPresent(usage -> lines.add(String.format("**Usage:** `%s`", usage)));
                        String commandInfo = String.join("\n", lines);
                        if (commandInfo.isBlank()) {
                            commandInfo = "\u200B";
                        }
                        embed.addField(String.format("**__%s%s__**", prefix, command.getAliases().get(0)), commandInfo);
                    });

            CommandCleanupListener.insertResponseTracker(embed, message.getId());
            channel.sendMessage(embed).join();
        } catch (IOException ioe) {
            logger
                    .atError()
                    .withThrowable(ioe)
                    .log("Exception while closing image stream");
        }
    }
}
