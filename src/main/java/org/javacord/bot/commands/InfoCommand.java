package org.javacord.bot.commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.Version;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import net.kautler.command.api.parameter.Parameters;
import org.javacord.api.DiscordApi;
import org.javacord.api.Javacord;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;
import org.javacord.bot.listeners.CommandCleanupListener;

@ApplicationScoped
@Description("Shows information about this bot")
@Asynchronous
public class InfoCommand extends BaseTextCommand {
    @Inject
    Version commandFrameworkVersion;

    /**
     * Executes the {@code !info} command.
     */
    @Override
    protected void doExecute(CommandContext<? extends Message> commandContext, Message message,
                             TextChannel channel, Parameters<String> parameters) {
        DiscordApi api = channel.getApi();

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Constants.JAVACORD_ORANGE)
                .setTitle(api.getYourself().getName() + " - Official Javacord Bot")
                .setThumbnail(api.getYourself().getAvatar())
                .setDescription("The official bot for the Javacord Server\n\n"
                        + "Powered by Javacord and command-framework")
                .addInlineField("GitHub", "https://github.com/Javacord/Javacord-Bot")
                .addInlineField("Javacord Version", String.format(
                        "[%s](https://github.com/Javacord/Javacord/releases/tag/v%s)",
                        Javacord.DISPLAY_VERSION, Javacord.VERSION))
                .addInlineField("command-framework Version", String.format(
                        "[%s](https://github.com/Vampire/command-framework/releases/tag/v%s)",
                        commandFrameworkVersion.getDisplayVersion(), commandFrameworkVersion.getVersion()));

        CommandCleanupListener.insertResponseTracker(embed, message.getId());
        channel.sendMessage(embed).join();
    }
}
