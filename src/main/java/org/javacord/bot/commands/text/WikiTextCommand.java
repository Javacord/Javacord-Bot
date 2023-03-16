package org.javacord.bot.commands.text;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import net.kautler.command.api.annotation.Usage;
import net.kautler.command.api.parameter.Parameters;
import org.javacord.api.entity.message.Message;
import org.javacord.bot.commands.workers.WikiCommandWorker;
import org.javacord.bot.listeners.TextCommandCleanupListener;

import java.util.function.Predicate;

/**
 * The !wiki command which is used to link to Javacord's wiki.
 */
@ApplicationScoped
@Description("Shows a link to the wiki or searches through it")
@Usage("[[('title' | 't' | 'page' | 'p' | 'full' | 'f' | 'content' | 'c')] <search...>]")
@Asynchronous
public class WikiTextCommand extends BaseTextCommand {
    @Inject
    WikiCommandWorker worker;

    /**
     * Executes the {@code !wiki} command.
     */
    @Override
    protected void doExecute(CommandContext<? extends Message> commandContext,
                             Message message, Parameters<String> parameters) {
        String searchTerm;
        boolean searchInKeywords = true;
        boolean searchInTitles = true;
        boolean searchInContents = false;

        switch (parameters.size()) {
            case 0:
                searchTerm = null;
                break;

            case 1:
                searchTerm = parameters.get("search").orElseThrow(AssertionError::new);
                break;

            case 2:
                searchTerm = parameters.get("search").orElseThrow(AssertionError::new);

                String searchType = parameters
                        .getParameterNames()
                        .stream()
                        .filter(Predicate.not("search"::equals))
                        .findAny()
                        .orElseThrow(AssertionError::new);
                switch (searchType) {
                    case "page":
                    case "p":
                    case "title":
                    case "t":
                        searchInKeywords = false;
                        break;

                    case "full":
                    case "f":
                    case "content":
                    case "c":
                        searchInContents = true;
                        break;

                    default:
                        throw new AssertionError(String.format("Missing case for search type '%s'", searchType));
                }
                break;

            default:
                throw new AssertionError(String.format("Missing case for parameter count '%s'", parameters.size()));
        }

        worker
                .execute(searchTerm, searchInKeywords, searchInTitles, searchInContents)
                .thenCompose(embed -> {
                    TextCommandCleanupListener.insertResponseTracker(embed, message.getId());
                    return message.reply(embed);
                })
                .join();
    }
}
