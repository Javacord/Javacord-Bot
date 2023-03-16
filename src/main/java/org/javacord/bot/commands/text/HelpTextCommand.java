package org.javacord.bot.commands.text;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import net.kautler.command.api.Command;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import net.kautler.command.api.parameter.Parameters;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;
import org.javacord.bot.listeners.TextCommandCleanupListener;
import org.javacord.bot.util.JavacordIconProvider;

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
public class HelpTextCommand extends BaseTextCommand {
    @Inject
    Instance<Command<Message>> textCommands;

    @Inject
    JavacordIconProvider iconProvider;

    /**
     * Executes the {@code !help} command.
     */
    @Override
    protected void doExecute(CommandContext<? extends Message> commandContext,
                             Message message, Parameters<String> parameters) {
        EmbedBuilder embed = new EmbedBuilder()
                .setThumbnail(iconProvider.getIcon())
                .setTitle("Commands")
                .setColor(Constants.JAVACORD_ORANGE);

        List<? extends Command<Message>> hiddenTextCommands = textCommands
                .select(HiddenTextCommand.class)
                .stream()
                .collect(Collectors.toList());
        String prefix = commandContext.getPrefix().orElseThrow(AssertionError::new);
        textCommands
                .stream()
                .filter(textCommand -> !hiddenTextCommands.contains(textCommand))
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

        TextCommandCleanupListener.insertResponseTracker(embed, message.getId());
        message.reply(embed).join();
    }
}
