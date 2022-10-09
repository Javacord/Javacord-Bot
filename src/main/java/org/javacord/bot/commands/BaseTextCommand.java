package org.javacord.bot.commands;

import jakarta.inject.Inject;
import net.kautler.command.api.Command;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.parameter.ParameterParseException;
import net.kautler.command.api.parameter.ParameterParser;
import net.kautler.command.api.parameter.Parameters;
import net.kautler.command.api.restriction.RestrictionChainElement;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.listeners.CommandCleanupListener;
import org.javacord.bot.restrictions.JavacordChannelIfOnDapiServer;

import static java.lang.String.format;
import static org.javacord.bot.Constants.ERROR_COLOR;

public abstract class BaseTextCommand implements Command<Message> {
    @Inject
    Logger logger;

    @Inject
    ParameterParser parameterParser;

    @Override
    public final RestrictionChainElement getRestrictionChain() {
        // Only react in #java_javacord channel on Discord API server
        return doGetRestrictionChain().and(JavacordChannelIfOnDapiServer.class);
    }

    protected RestrictionChainElement doGetRestrictionChain() {
        return Command.super.getRestrictionChain();
    }

    @Override
    public final void execute(CommandContext<? extends Message> commandContext) {
        Message message = commandContext.getMessage();
        TextChannel channel = message.getChannel();

        Parameters<String> parameters;
        try {
            parameters = parameterParser.parse(commandContext);
        } catch (ParameterParseException ppe) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription(format("%s: %s", message.getAuthor().getDisplayName(), ppe.getMessage()))
                    .setColor(ERROR_COLOR);
            CommandCleanupListener.insertResponseTracker(embed, message.getId());
            channel
                    .sendMessage(embed)
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

        doExecute(commandContext, message, channel, parameters);
    }

    protected abstract void doExecute(CommandContext<? extends Message> commandContext, Message message,
                                      TextChannel channel, Parameters<String> parameters);
}
