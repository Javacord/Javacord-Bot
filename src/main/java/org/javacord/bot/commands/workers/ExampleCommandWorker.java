package org.javacord.bot.commands.workers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;
import org.javacord.bot.util.JavacordIconProvider;

/**
 * The example command worker which is used to get a link to the example bot.
 */
@ApplicationScoped
public class ExampleCommandWorker {
    @Inject
    Logger logger;

    @Inject
    JavacordIconProvider iconProvider;

    /**
     * Executes the {@code example} commands.
     */
    public EmbedBuilder execute() {
        return new EmbedBuilder()
                .setThumbnail(iconProvider.getIcon())
                .setColor(Constants.JAVACORD_ORANGE)
                .addField("Example Bot", "https://github.com/Javacord/Example-Bot");
    }
}
