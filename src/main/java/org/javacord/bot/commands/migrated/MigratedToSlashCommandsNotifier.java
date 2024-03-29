package org.javacord.bot.commands.migrated;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Alias;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.RestrictedTo;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.bot.Constants;
import org.javacord.bot.commands.text.HiddenTextCommand;
import org.javacord.bot.listeners.TextCommandCleanupListener;
import org.javacord.bot.restrictions.Before2023;
import org.javacord.bot.util.JavacordIconProvider;

import java.util.Comparator;

@ApplicationScoped
@Alias("migrated/docs")
@Alias("migrated/example")
@Alias("migrated/github")
@Alias("migrated/gradle")
@Alias("migrated/info")
@Alias("migrated/help")
@Alias("migrated/invite")
@Alias("migrated/maven")
@Alias("migrated/sdcf4j")
@Alias("migrated/setup")
@Alias("migrated/wiki")
@RestrictedTo(Before2023.class)
@Asynchronous
//TODO: After 2022 delete this class, and Before2023 restriction class
public class MigratedToSlashCommandsNotifier implements HiddenTextCommand {
    @Inject
    JavacordIconProvider iconProvider;

    @Override
    public void execute(CommandContext<? extends Message> commandContext) {
        Message message = commandContext.getMessage();

        EmbedBuilder embed = new EmbedBuilder()
                .setThumbnail(iconProvider.getIcon())
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

        TextCommandCleanupListener.insertResponseTracker(embed, message.getId());
        message.reply(embed).join();
    }
}
