package org.javacord.bot.commands.workers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.Version;
import org.javacord.api.DiscordApi;
import org.javacord.api.Javacord;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.bot.Constants;

@ApplicationScoped
public class InfoCommandWorker {
    @Inject
    DiscordApi api;

    @Inject
    Version commandFrameworkVersion;

    /**
     * Executes the {@code info} commands.
     */
    public EmbedBuilder execute() {
        User yourself = api.getYourself();

        return new EmbedBuilder()
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
    }
}
