package org.javacord.bot.listeners;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.entity.message.component.SelectMenu;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.event.interaction.SelectMenuChooseEvent;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.interaction.MessageComponentInteractionBase;
import org.javacord.api.interaction.SelectMenuInteraction;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionFollowupMessageBuilder;
import org.javacord.api.interaction.callback.InteractionMessageBuilderBase;
import org.javacord.api.listener.interaction.ButtonClickListener;
import org.javacord.api.listener.interaction.SelectMenuChooseListener;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Integer.parseUnsignedInt;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.javacord.api.entity.message.MessageDecoration.CODE_SIMPLE;
import static org.javacord.bot.listeners.SlashCommandCleanupListener.WASTEBASKET;

/**
 * A listener to publish our response to a user command if the users presses a button component.
 */
@ApplicationScoped
public class PublishResponseListener implements ButtonClickListener, SelectMenuChooseListener {
    public static final String SHOW_TO_EVERYONE_ID = "show-to-everyone";
    public static final String SHOW_TO_EVERYONE_AND_MENTION_ID = "show-to-everyone-and-mention";

    @Inject
    Logger logger;

    @Inject
    DiscordApi discordApi;

    void registerListener(@Observes @Initialized(ApplicationScoped.class) Object unused) {
        discordApi.addListener(this);
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        ButtonInteraction interaction = event.getButtonInteraction();
        if (!SHOW_TO_EVERYONE_ID.equals(interaction.getCustomId())) {
            return;
        }

        publishResponse(interaction, interaction.createFollowupMessageBuilder());
    }

    @Override
    public void onSelectMenuChoose(SelectMenuChooseEvent event) {
        SelectMenuInteraction interaction = event.getSelectMenuInteraction();
        if (!SHOW_TO_EVERYONE_AND_MENTION_ID.equals(interaction.getCustomId())) {
            return;
        }

        InteractionFollowupMessageBuilder responder = interaction.createFollowupMessageBuilder();
        User mentionUser = interaction.getSelectedUsers().get(0);
        responder.setContent(mentionUser.getMentionTag());
        publishResponse(interaction, responder);
    }

    private void publishResponse(MessageComponentInteractionBase interaction, InteractionFollowupMessageBuilder responder) {
        removePublishButtons(interaction);
        responder
                .getStringBuilder()
                .append("\n")
                .append(CODE_SIMPLE.applyToText(extractHiddenCommandString(interaction)));
        responder
                .addEmbeds(interaction
                        .getMessage()
                        .getEmbeds()
                        .stream()
                        .map(Embed::toBuilder)
                        .collect(toList()))
                .send()
                .thenCompose(message -> message.addReaction(WASTEBASKET))
                .whenComplete((__, throwable) -> {
                    if (throwable != null) {
                        logger
                                .atError()
                                .withThrowable(throwable)
                                .log("Exception while publishing response");
                    }
                });
    }

    private void removePublishButtons(MessageComponentInteractionBase interaction) {
        List<ActionRow> rows = interaction
                .getMessage()
                .getComponents()
                .stream()
                .map(ActionRow.class::cast)
                .flatMap(row -> Stream.of(row
                                        .getComponents()
                                        .stream()
                                        .filter(component -> component
                                                .asButton()
                                                .map(Button::getCustomId)
                                                .orElseGet(() -> component.asSelectMenu().map(SelectMenu::getCustomId))
                                                .filter(customId -> !SHOW_TO_EVERYONE_ID.equals(customId)
                                                        && !SHOW_TO_EVERYONE_AND_MENTION_ID.equals(customId))
                                                .isPresent()
                                        )
                                        .collect(toList())
                                )
                                .filter(components -> !components.isEmpty())
                )
                .map(ActionRow::of)
                .collect(toList());

        interaction
                .createOriginalMessageUpdater()
                .removeAllComponents()
                .addComponents(rows.toArray(HighLevelComponent[]::new))
                .update()
                .whenComplete((__, throwable) -> {
                    if (throwable != null) {
                        logger
                                .atError()
                                .withThrowable(throwable)
                                .log("Exception while removing publish buttons");
                    }
                });
    }

    public static void setContentToHiddenCommandString(
            CommandContext<? extends SlashCommandInteraction> commandContext,
            InteractionMessageBuilderBase<?> responder) {
        byte[] commandBytes = format(
                "/%s %s",
                commandContext.getAlias().orElseThrow(AssertionError::new),
                commandContext
                        .getMessage()
                        .getArguments()
                        .stream()
                        .map(option -> format("%s: %s", option.getName(), option.getStringRepresentationValue().orElse("")))
                        .collect(joining(" "))
        ).trim().getBytes(UTF_8);

        responder.setContent(IntStream
                .range(0, commandBytes.length)
                .map(i -> commandBytes[i])
                .mapToObj(Integer::toBinaryString)
                .collect(joining("\u200D"))
                .replace('0', '\u200B')
                .replace('1', '\u200C')
                + '\u200E');
    }

    private static String extractHiddenCommandString(MessageComponentInteractionBase interaction) {
        String messageContent = interaction.getMessage().getContent();
        int endMarker = messageContent.indexOf('\u200E');
        if (endMarker < 0) {
            throw new AssertionError("End marker not found");
        }
        return Arrays
                .stream(messageContent
                        .substring(0, endMarker)
                        .replace('\u200C', '1')
                        .replace('\u200B', '0')
                        .split(Pattern.quote("\u200D"))
                )
                .mapToInt(commandByte -> parseUnsignedInt(commandByte, 2))
                .collect(
                        ByteArrayOutputStream::new,
                        (baos, i) -> baos.write((byte) i),
                        (baos1, baos2) -> baos1.write(baos2.toByteArray(), 0, baos2.size())
                )
                .toString(UTF_8);
    }
}
