package org.javacord.bot.listeners;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * A listener to clean up our responses to slash user commands if the wastebasket reaction was clicked.
 */
@ApplicationScoped
public class SlashCommandCleanupListener implements ReactionAddListener {
    public static final String WASTEBASKET = "\uD83D\uDDD1️";

    @Inject
    Logger logger;

    @Inject
    DiscordApi discordApi;

    void registerListener(@Observes @Initialized(ApplicationScoped.class) Object unused) {
        discordApi.addReactionAddListener(this);
    }

    @Override
    public void onReactionAdd(ReactionAddEvent event) {
        if (event.getUserId() == discordApi.getYourself().getId()) {
            return;
        }

        if (!event.getEmoji().equalsEmoji(WASTEBASKET)) {
            return;
        }

        event
                .requestMessage()
                .thenCompose(message -> {
                    if (message.getAuthor().getId() != discordApi.getYourself().getId()) {
                        return completedFuture(null);
                    }

                    if (!message.getReactionByEmoji(WASTEBASKET).orElseThrow(AssertionError::new).containsYou()) {
                        return completedFuture(null);
                    }

                    return message.delete("Wastebasket reaction has been clicked");
                })
                .whenComplete((__, throwable) -> {
                    if (throwable != null) {
                        logger
                                .atError()
                                .withThrowable(throwable)
                                .log("Exception while deleting response");
                    }
                });
    }
}
