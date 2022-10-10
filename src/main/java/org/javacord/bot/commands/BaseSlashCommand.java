package org.javacord.bot.commands;

import net.kautler.command.api.slash.javacord.SlashCommandJavacord;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class BaseSlashCommand implements SlashCommandJavacord {
    public static final String SHOW_TO_EVERYONE = "show-to-everyone";

    @Override
    public final List<SlashCommandOption> getOptions() {
        ArrayList<SlashCommandOption> result = new ArrayList<>(doGetOptions());
        result.add(SlashCommandOption.createBooleanOption(SHOW_TO_EVERYONE, "Show response to all users", false));
        return result;
    }

    protected List<SlashCommandOption> doGetOptions() {
        return SlashCommandJavacord.super.getOptions();
    }

    protected CompletableFuture<InteractionOriginalResponseUpdater> sendResponse(
            SlashCommandInteraction slashCommandInteraction, EmbedBuilder embed) {
        InteractionImmediateResponseBuilder responder = slashCommandInteraction.createImmediateResponder();
        if (!slashCommandInteraction.getOptionBooleanValueByName(SHOW_TO_EVERYONE).orElse(Boolean.FALSE)) {
            responder.setFlags(MessageFlag.EPHEMERAL);
        }
        responder.addEmbed(embed);
        return responder.respond();
    }

    protected CompletableFuture<InteractionOriginalResponseUpdater> sendResponseLater(
            SlashCommandInteraction slashCommandInteraction) {
        return slashCommandInteraction.respondLater(
                !slashCommandInteraction.getOptionBooleanValueByName(SHOW_TO_EVERYONE).orElse(Boolean.FALSE));
    }
}
