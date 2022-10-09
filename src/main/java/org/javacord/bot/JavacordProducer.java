package org.javacord.bot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

@ApplicationScoped
public class JavacordProducer {
    @Inject
    Logger logger;

    @Inject
    @Named
    String discordToken;

    @Produces
    @ApplicationScoped
    DiscordApi produceDiscordApi() {
        return new DiscordApiBuilder()
                .setToken(discordToken)
                .setWaitForServersOnStartup(false)
                .login()
                .whenComplete((__, throwable) -> {
                    if (throwable != null) {
                        logger
                                .atError()
                                .withThrowable(throwable)
                                .log("Exception while logging in to Discord");
                    }
                })
                .join();
    }

    private void disposeDiscordApi(@Disposes DiscordApi discordApi) {
        discordApi.disconnect();
    }
}
