package org.javacord.bot.util;

import org.javacord.api.entity.message.Message;

import java.util.concurrent.TimeUnit;

public class CommandHelper {

    /**
     * Helps with deleting messages that correspond to commands and their responses.
     *
     * @param commandMessage The message that issued the command.
     * @param sentMessage The message that is the commands response.
     */
    public static void messageCleanup(Message commandMessage, Message sentMessage) {
        applyListenerCleanup(commandMessage)
                .addMessageDeleteListener(event -> sentMessage.delete())
                .removeAfter(24, TimeUnit.HOURS) // remove the listener and both messages after 24 hours
                .addRemoveHandler(() -> {
                    sentMessage.delete();
                    commandMessage.delete();
                });
    }

    /**
     * Applies a listener to the message which removes all listeners when the message is deleted.
     *
     * @param message The message to handle.
     * @return The message that has been handled; usable for chaining methods.
     */
    private static Message applyListenerCleanup(Message message) {
        message.addMessageDeleteListener(event ->
            message.getMessageAttachableListeners()
                    .forEach((key, value) ->
                        message.removeMessageAttachableListener(key)
                    )
        );

        return message;
    }
}
