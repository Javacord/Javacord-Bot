package org.javacord.bot.restrictions;

import jakarta.enterprise.context.ApplicationScoped;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.restriction.Restriction;
import org.javacord.api.entity.message.Message;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;

@ApplicationScoped
public class Before2023 implements Restriction<Message> {
    private static final Instant startOf2023 = LocalDateTime
            .MIN
            .with(ChronoField.YEAR, 2023)
            .with(ChronoField.DAY_OF_YEAR, 1)
            .with(ChronoField.NANO_OF_DAY, 0)
            .toInstant(ZoneOffset.UTC);

    @Override
    public boolean allowCommand(CommandContext<? extends Message> commandContext) {
        Message message = commandContext.getMessage();
        return message.getCreationTimestamp().isBefore(startOf2023);
    }
}
