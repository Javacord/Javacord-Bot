package org.javacord.bot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.SlashCommandBuilder;

import java.util.HashSet;
import java.util.List;

@ApplicationScoped
public class SlashCommandRegisterer {
    @Inject
    Logger logger;

    @Inject
    DiscordApi discordApi;

    @Inject
    List<SlashCommandBuilder> slashCommandBuilders;

    void registerSlashCommands(@Observes @Initialized(ApplicationScoped.class) Object unused) {
        discordApi
                .bulkOverwriteGlobalApplicationCommands(new HashSet<>(slashCommandBuilders))
                .whenComplete((__, throwable) -> {
                    if (throwable != null) {
                        logger
                                .atError()
                                .withThrowable(throwable)
                                .log("Exception while registering slash commands");
                    }
                });
    }
}
