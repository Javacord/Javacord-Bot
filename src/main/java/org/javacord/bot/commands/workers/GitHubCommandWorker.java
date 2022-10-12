package org.javacord.bot.commands.workers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;
import org.javacord.bot.util.JavacordIconProvider;

/**
 * The github command worker which is used to link to Javacord related GitHub repositories.
 */
@ApplicationScoped
public class GitHubCommandWorker {
    @Inject
    JavacordIconProvider iconProvider;

    /**
     * Executes the {@code github} commands.
     */
    public EmbedBuilder execute() {
        return new EmbedBuilder()
                .addField("Javacord", "https://github.com/Javacord/Javacord")
                .addField("Example Bot", "https://github.com/Javacord/Example-Bot")
                .addField("James", "https://github.com/Javacord/Javacord-Bot")
                .setThumbnail(iconProvider.getIcon())
                .setColor(Constants.JAVACORD_ORANGE);
    }
}
