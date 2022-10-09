package org.javacord.bot.listeners;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import net.kautler.command.api.Command;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.javacord.bot.Constants.JAVACORD_ORANGE;
import static org.javacord.bot.Constants.TALK_TO_JAMES_CHANNEL_ID;

@ApplicationScoped
public class TalkToJamesListener implements MessageCreateListener {
    @Inject
    Logger logger;

    @Inject
    DiscordApi discordApi;

    @Inject
    Instance<Command<Message>> textCommands;

    private List<String> textCommandAliases;

    void registerListener(@Observes @Initialized(ApplicationScoped.class) Object __) {
        textCommandAliases = textCommands
                .stream()
                .map(Command::getAliases)
                .flatMap(Collection::stream)
                .map(alias -> format("!%s", alias))
                .collect(toList());

        discordApi.addMessageCreateListener(this);
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getChannel().getId() != TALK_TO_JAMES_CHANNEL_ID) {
            return;
        }
        if (!event.getMessageAuthor().isRegularUser()) {
            return;
        }

        boolean messageIsCommand = textCommandAliases
                .stream()
                .anyMatch(alias -> event.getMessageContent().startsWith(alias));

        if (!messageIsCommand) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Information")
                    .setDescription("This channel is solely used to talk to me.\n" +
                            "You can see all my commands with `!help`.")
                    .setColor(JAVACORD_ORANGE);
            CommandCleanupListener.insertResponseTracker(embed, event.getMessageId());
            event
                    .getChannel()
                    .sendMessage(embed)
                    .whenComplete((__, throwable) -> {
                        if (throwable != null) {
                            logger
                                    .atError()
                                    .withThrowable(throwable)
                                    .log("Exception while sending only-talk-to-me message");
                        }
                    });
        }
    }
}
