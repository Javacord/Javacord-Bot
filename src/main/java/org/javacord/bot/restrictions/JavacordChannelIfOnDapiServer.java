package org.javacord.bot.restrictions;

import jakarta.enterprise.context.ApplicationScoped;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.restriction.Restriction;
import org.javacord.api.entity.message.Message;

import static java.lang.Boolean.TRUE;
import static org.javacord.bot.Constants.DAPI_JAVACORD_CHANNEL_ID;
import static org.javacord.bot.Constants.DAPI_SERVER_ID;

@ApplicationScoped
public class JavacordChannelIfOnDapiServer implements Restriction<Message> {
    @Override
    public boolean allowCommand(CommandContext<? extends Message> commandContext) {
        Message message = commandContext.getMessage();
        return message
                .getServer()
                .map(server ->
                        (server.getId() != DAPI_SERVER_ID)
                                || (message.getChannel().getId() == DAPI_JAVACORD_CHANNEL_ID))
                .orElse(TRUE);
    }
}
