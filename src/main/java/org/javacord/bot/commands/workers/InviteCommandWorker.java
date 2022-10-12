package org.javacord.bot.commands.workers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;
import org.javacord.bot.util.JavacordIconProvider;

/**
 * The invite command worker which is used to get an invite link to the Javacord Discord server.
 */
@ApplicationScoped
public class InviteCommandWorker {
    @Inject
    JavacordIconProvider iconProvider;

    /**
     * Executes the {@code invite} commands.
     */
    public EmbedBuilder execute() {
        return new EmbedBuilder()
                .setThumbnail(iconProvider.getIcon())
                .setColor(Constants.JAVACORD_ORANGE)
                .addField("Invite Link", "https://discord.gg/javacord");
    }
}
