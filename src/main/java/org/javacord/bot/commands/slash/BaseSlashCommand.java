package org.javacord.bot.commands.slash;

import net.kautler.command.api.CommandContext;
import net.kautler.command.api.slash.javacord.SlashCommandJavacord;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.SelectMenu;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import org.javacord.api.interaction.callback.InteractionMessageBuilderBase;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.javacord.api.entity.message.MessageFlag.EPHEMERAL;
import static org.javacord.bot.listeners.PublishResponseListener.SHOW_TO_EVERYONE_AND_MENTION_ID;
import static org.javacord.bot.listeners.PublishResponseListener.SHOW_TO_EVERYONE_ID;
import static org.javacord.bot.listeners.PublishResponseListener.setContentToHiddenCommandString;

public abstract class BaseSlashCommand implements SlashCommandJavacord {
    private static final String SLASH_SUFFIX = "Slash";
    private static final int SLASH_SUFFIX_LENGTH = SLASH_SUFFIX.length();

    @Override
    public List<String> getAliases() {
        return SlashCommandJavacord
                .super
                .getAliases()
                .stream()
                .map(alias -> alias.endsWith(SLASH_SUFFIX)
                        ? alias.substring(0, alias.length() - SLASH_SUFFIX_LENGTH)
                        : alias)
                .collect(Collectors.toList());
    }

    protected CompletableFuture<InteractionOriginalResponseUpdater> sendResponse(
            CommandContext<? extends SlashCommandInteraction> commandContext, EmbedBuilder embed) {
        InteractionImmediateResponseBuilder responder = commandContext.getMessage().createImmediateResponder();
        responder.setFlags(EPHEMERAL);
        setContentToHiddenCommandString(commandContext, responder);
        responder.addEmbed(embed);
        addShowToEveryoneRows(responder);
        return responder.respond();
    }

    protected CompletableFuture<InteractionOriginalResponseUpdater> sendResponseLater(
            CommandContext<? extends SlashCommandInteraction> commandContext) {
        return commandContext
                .getMessage()
                .respondLater(true)
                .thenApply(messageBuilder -> {
                    setContentToHiddenCommandString(commandContext, messageBuilder);
                    return addShowToEveryoneRows(messageBuilder);
                });
    }

    private static <T extends InteractionMessageBuilderBase<T>> T addShowToEveryoneRows(T messageBuilder) {
        return messageBuilder.addComponents(
                ActionRow.of(Button.primary(
                        SHOW_TO_EVERYONE_ID,
                        "Show response to all users"
                )),
                ActionRow.of(SelectMenu.createUserMenu(
                        SHOW_TO_EVERYONE_AND_MENTION_ID,
                        "Show response to all users and mention one"
                ))
        );
    }
}
