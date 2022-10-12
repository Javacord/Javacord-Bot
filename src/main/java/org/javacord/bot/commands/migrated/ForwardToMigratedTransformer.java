package org.javacord.bot.commands.migrated;

import jakarta.enterprise.context.ApplicationScoped;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.CommandContextTransformer;
import net.kautler.command.api.CommandContextTransformer.InPhase;
import net.kautler.command.api.CommandContextTransformer.Phase;
import org.javacord.api.entity.message.Message;
import org.javacord.bot.Constants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
@InPhase(Phase.BEFORE_ALIAS_AND_PARAMETER_STRING_COMPUTATION)
public class ForwardToMigratedTransformer implements CommandContextTransformer<Message> {
    @Override
    public <T extends Message> CommandContext<T> transform(CommandContext<T> commandContext, Phase phase) {
        if (commandContext.getMessage().getChannel().getId() != Constants.DAPI_JAVACORD_CHANNEL_ID) {
            String prefix = commandContext.getPrefix().orElseThrow(AssertionError::new);
            return commandContext
                    .withMessageContent(commandContext
                            .getMessageContent()
                            .replaceFirst(
                                    String.format("^%s", Pattern.quote(prefix)),
                                    Matcher.quoteReplacement(String.format("%smigrated/", prefix))))
                    .build();
        }
        return commandContext;
    }
}
