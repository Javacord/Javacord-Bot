package org.javacord.bot.commands.text;

import jakarta.inject.Inject;
import net.kautler.command.api.Command;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.parameter.ParameterParseException;
import net.kautler.command.api.parameter.ParameterParser;
import net.kautler.command.api.parameter.Parameters;
import net.kautler.command.api.restriction.RestrictionChainElement;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;
import org.javacord.bot.listeners.TextCommandCleanupListener;
import org.javacord.bot.restrictions.JavacordChannelOnDapiServer;

import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseTextCommand implements Command<Message> {
    private static final String TEXT_SUFFIX = "Text";
    private static final int TEXT_SUFFIX_LENGTH = TEXT_SUFFIX.length();

    @Inject
    Logger logger;

    @Inject
    ParameterParser parameterParser;

    @Override
    public List<String> getAliases() {
        return Command
                .super
                .getAliases()
                .stream()
                .map(alias -> alias.endsWith(TEXT_SUFFIX)
                        ? alias.substring(0, alias.length() - TEXT_SUFFIX_LENGTH)
                        : alias)
                .collect(Collectors.toList());
    }

    @Override
    public final RestrictionChainElement getRestrictionChain() {
        // Only react in #java_javacord channel on Discord API server
        return doGetRestrictionChain().and(JavacordChannelOnDapiServer.class);
    }

    protected RestrictionChainElement doGetRestrictionChain() {
        return Command.super.getRestrictionChain();
    }

    @Override
    public final void execute(CommandContext<? extends Message> commandContext) {
        Message message = commandContext.getMessage();
        Parameters<String> parameters;
        try {
            parameters = parameterParser.parse(commandContext);
        } catch (ParameterParseException ppe) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription(String.format("%s: %s", message.getAuthor().getDisplayName(), ppe.getMessage()))
                    .setColor(Constants.ERROR_COLOR);
            TextCommandCleanupListener.insertResponseTracker(embed, message.getId());
            message
                    .reply(embed)
                    .whenComplete((__, throwable) -> {
                        if (throwable != null) {
                            logger
                                    .atError()
                                    .withThrowable(throwable)
                                    .log("Exception while sending parameter parse failure to Discord");
                        }
                    });
            return;
        }
        doExecute(commandContext, message, parameters);
    }

    protected abstract void doExecute(CommandContext<? extends Message> commandContext,
                                      Message message, Parameters<String> parameters);
}
