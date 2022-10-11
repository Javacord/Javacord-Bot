package org.javacord.bot.restrictions;

import jakarta.enterprise.context.ApplicationScoped;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.restriction.Restriction;
import org.javacord.api.entity.message.Message;
import org.javacord.bot.Constants;

@ApplicationScoped
public class JavacordChannelIfOnDapiServer implements Restriction<Message> {
    @Override
    public boolean allowCommand(CommandContext<? extends Message> commandContext) {
        Message message = commandContext.getMessage();
        return message
                .getServer()
                .map(server ->
                        (server.getId() != Constants.DAPI_SERVER_ID)
                                || (message.getChannel().getId() == Constants.DAPI_JAVACORD_CHANNEL_ID))
                .orElse(Boolean.TRUE);
    }
}
