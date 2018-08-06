package org.javacord.bot.util.discord;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.reaction.SingleReactionEvent;
import org.javacord.api.util.logging.ExceptionLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class PagedEmbed {
    public final static int FIELD_MAX_CHARS = 1024;
    public final static int MAX_CHARS_PER_PAGE = 4500;
    public final static int MAX_FIELDS_PER_PAGE = 8;
    public final static String PREV_PAGE_EMOJI = "⬅";
    public final static String NEXT_PAGE_EMOJI = "➡";

    private final Messageable messageable;
    private final EmbedBuilder embed;

    private ConcurrentHashMap<Integer, List<Field>> pages = new ConcurrentHashMap<>();
    private List<Field> fields = new ArrayList<>();
    private int page;
    private AtomicReference<Message> sentMessage = new AtomicReference<>();

    /**
     * Creates a new PagedEmbed object.
     *
     * @param messageable The Messageable in which the embed should be sent.
     * @param embed       An EmbedBuilder the sent embed should be based on.
     */
    public PagedEmbed(Messageable messageable, EmbedBuilder embed) {
        this.messageable = messageable;
        this.embed = embed;
    }

    /**
     * Adds a new non-inline field to the paged embed.
     *
     * @param title The title of the field.
     * @param text  The text of the field.
     * @return The new, modified PagedEmbed object.
     */
    public PagedEmbed addField(String title, String text) {
        return addField(title, text, false);
    }

    /**
     * Adds a new field to the pages embed.
     *
     * @param title  The title of the field.
     * @param text   The text of the field.
     * @param inline If the field should be inline.
     * @return The new, modified PageEmbed object.
     */
    public PagedEmbed addField(String title, String text, boolean inline) {
        fields.add(
                new Field(
                        title,
                        text,
                        inline
                )
        );

        return this;
    }

    /**
     * Builds & sends the PagedEmbed.
     *
     * @return A {@code CompletableFuture} that will contain the sent message.
     */
    public CompletableFuture<Message> build() {
        page = 1;
        refreshPages();

        CompletableFuture<Message> future = messageable.sendMessage(embed);

        future.thenAcceptAsync(message -> {
            sentMessage.set(message);
            if (pages.size() != 1) {
                message.addReaction(PREV_PAGE_EMOJI);
                message.addReaction(NEXT_PAGE_EMOJI);
                message.addReactionAddListener(this::onReactionClick);
                message.addReactionRemoveListener(this::onReactionClick);
            }

            message.addMessageDeleteListener(delete -> message.getMessageAttachableListeners()
                    .forEach((a, b) -> message.removeMessageAttachableListener(a)))
                    .removeAfter(3, TimeUnit.HOURS)
                    .addRemoveHandler(() -> {
                        sentMessage.get()
                                .removeAllReactions();
                        sentMessage.get()
                                .getMessageAttachableListeners()
                                .forEach((a, b) -> message.removeMessageAttachableListener(a));
                    });
        }).exceptionally(ExceptionLogger.get());

        return future;
    }

    /**
     * Re-creates the map containing the pages.
     */
    private void refreshPages() {
        int fieldCount = 0, pageChars = 0, totalChars = 0, thisPage = 1;
        pages.clear();

        for (Field field : fields) {
            pages.putIfAbsent(thisPage, new ArrayList<>());

            if (fieldCount <= MAX_FIELDS_PER_PAGE &&
                    pageChars <= FIELD_MAX_CHARS * fieldCount &&
                    totalChars < MAX_CHARS_PER_PAGE) {
                pages.get(thisPage)
                        .add(field);

                fieldCount++;
                pageChars = pageChars + field.getTotalChars();
                totalChars = totalChars + field.getTotalChars();
            } else {
                thisPage++;
                pages.putIfAbsent(thisPage, new ArrayList<>());

                pages.get(thisPage)
                        .add(field);

                fieldCount = 1;
                pageChars = field.getTotalChars();
                totalChars = field.getTotalChars();
            }
        }

        refreshMessage();
    }

    /**
     * Updates the embed in the message according to the page number.
     */
    private void refreshMessage() {
        // Refresh the embed to the current page
        embed.removeAllFields();

        pages.get(page)
                .forEach(field -> {
                    embed.addField(
                            field.getTitle(),
                            field.getText(),
                            field.getInline()
                    );
                });
        if (pages.size() > 1)
            embed.setFooter("Page " + page + " of " + pages.size());

        // Edit sent message
        if (sentMessage.get() != null) {
            sentMessage.get()
                    .edit(embed);
        }
    }

    private void onReactionClick(SingleReactionEvent event) {
        event.getEmoji().asUnicodeEmoji().ifPresent(emoji -> {
            if (!event.getUser().isYourself()) {
                switch (emoji) {
                    case PREV_PAGE_EMOJI:
                        if (page > 1)
                            page--;
                        else if (page == 1)
                            page = pages.size();

                        this.refreshMessage();
                        break;
                    case NEXT_PAGE_EMOJI:
                        if (page < pages.size())
                            page++;
                        else if (page == pages.size())
                            page = 1;

                        this.refreshMessage();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public EmbedBuilder getEmbed() {
        return embed;
    }

    /**
     * This subclass represents an embed field for the PagedEmbed.
     */
    class Field {
        private final String title;
        private final String text;
        private final boolean inline;

        Field(String title, String text, boolean inline) {
            this.title = title;
            this.text = text;
            this.inline = inline;
        }

        /**
         * Gets the title of the field.
         *
         * @return The title of the field.
         */
        String getTitle() {
            return title;
        }

        /**
         * Gets the text of the field.
         *
         * @return The text of the field.
         */
        String getText() {
            return text;
        }

        /**
         * Gets if the field is inline.
         *
         * @return If the field is inline.
         */
        boolean getInline() {
            return inline;
        }

        /**
         * Returns the total characters of the field.
         *
         * @return The total characters of the field.
         */
        int getTotalChars() {
            return title.length() + text.length();
        }
    }
}