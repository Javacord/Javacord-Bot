package org.javacord.bot.commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.Version;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import org.javacord.api.Javacord;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.bot.Constants;

@ApplicationScoped
@Description("Shows information about this bot")
@Asynchronous
public class InfoCommand extends BaseSlashCommand {
    @Inject
    Version commandFrameworkVersion;

    /**
     * Executes the {@code /info} command.
     */
    @Override
    public void execute(CommandContext<? extends SlashCommandInteraction> commandContext) {
        User yourself = commandContext.getMessage().getApi().getYourself();

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Constants.JAVACORD_ORANGE)
                .setTitle(yourself.getName() + " - Official Javacord Bot")
                .setThumbnail(yourself.getAvatar())
                .setDescription("The official bot for the Javacord Server\n\n"
                        + "Powered by Javacord and command-framework")
                .addInlineField("GitHub", "https://github.com/Javacord/Javacord-Bot")
                .addInlineField("Javacord Version", String.format(
                        "[%s](https://github.com/Javacord/Javacord/releases/tag/v%s)",
                        Javacord.DISPLAY_VERSION, Javacord.VERSION))
                .addInlineField("command-framework Version", String.format(
                        "[%s](https://github.com/Vampire/command-framework/releases/tag/v%s)",
                        commandFrameworkVersion.getDisplayVersion(), commandFrameworkVersion.getVersion()));
        sendResponse(commandContext.getMessage(), embed).join();
    }
}
