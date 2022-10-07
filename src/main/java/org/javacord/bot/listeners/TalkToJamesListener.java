package org.javacord.bot.listeners;

import de.btobastian.sdcf4j.CommandHandler;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.util.logging.ExceptionLogger;
import org.javacord.bot.Constants;

import java.util.Arrays;

public class TalkToJamesListener implements MessageCreateListener {

    private final CommandHandler commandHandler;

    /**
     * Initializes the listener.
     *
     * @param commandHandler The command handler used to extract commands.
     */
    public TalkToJamesListener(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getChannel().getId() != Constants.TALK_TO_JAMES_CHANNEL_ID) {
            return;
        }
        if (event.getMessageAuthor().isBotUser()) {
            return;
        }

        boolean messageIsCommand = commandHandler.getCommands().stream()
                .flatMap(command -> Arrays.stream(command.getCommandAnnotation().aliases()))
                .anyMatch(alias -> event.getMessageContent().startsWith(alias));

        if (!messageIsCommand) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Information")
                    .setDescription(
                            "This channel is solely used to talk to me.\nYou can see all my commands with `!help`.")
                    .setColor(Constants.JAVACORD_ORANGE);
            CommandCleanupListener.insertResponseTracker(embed, event.getMessageId());
            event.getChannel().sendMessage(embed).exceptionally(ExceptionLogger.get());
        }
    }

}
