package org.javacord.bot.commands.text;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.kautler.command.api.CommandContext;
import net.kautler.command.api.annotation.Asynchronous;
import net.kautler.command.api.annotation.Description;
import net.kautler.command.api.annotation.Usage;
import net.kautler.command.api.parameter.Parameters;
import org.javacord.api.entity.message.Message;
import org.javacord.bot.commands.workers.DocsCommandWorker;
import org.javacord.bot.listeners.CommandCleanupListener;

import java.util.Set;

/**
 * The !docs command which is used to show links to Javacord's JavaDocs.
 */
@ApplicationScoped
@Description("Shows a link to the JavaDoc or searches through it")
@Usage("[[('classes' | 'class' | 'c' | 'methods' | 'method' | 'm')] [('all' | 'a')] <search...>]")
@Asynchronous
public class DocsTextCommand extends BaseTextCommand {
    /**
     * The parameters that indicate searching for class names only.
     */
    private static final Set<String> CLASS_PARAMS = Set.of("classes", "class", "c");

    /**
     * The parameters that indicate searching for method names only.
     */
    private static final Set<String> METHOD_PARAMS = Set.of("methods", "method", "m");

    /**
     * The parameters that indicate also searching internal packages and the core docs.
     */
    private static final Set<String> INCLUDE_ALL_PARAMS = Set.of("all", "a");

    @Inject
    DocsCommandWorker worker;

    /**
     * Executes the {@code !docs} command.
     */
    @Override
    protected void doExecute(CommandContext<? extends Message> commandContext,
                             Message message, Parameters<String> parameters) {
        String searchTerm;
        String searchType = DocsCommandWorker.SEARCH_TYPE_MEMBERS;
        boolean includeAll = false;
        switch (parameters.size()) {
            case 0:
                searchTerm = null;
                break;

            case 1:
                searchTerm = parameters.get("search").orElseThrow(AssertionError::new);
                break;

            case 2: {
                searchTerm = parameters.get("search").orElseThrow(AssertionError::new);
                includeAll = includeAll(parameters);
                if (!includeAll) {
                    if (classSearch(parameters)) {
                        searchType = DocsCommandWorker.SEARCH_TYPE_CLASSES;
                    } else if (!methodSearch(parameters)) {
                        throw new AssertionError("Unexpected state while parameter count 2");
                    }
                }
                break;
            }

            case 3: {
                includeAll = includeAll(parameters);
                if (!includeAll) {
                    throw new AssertionError("Unexpected includeAll false while parameter count 3");
                }
                searchTerm = parameters.get("search").orElseThrow(AssertionError::new);
                if (classSearch(parameters)) {
                    searchType = DocsCommandWorker.SEARCH_TYPE_CLASSES;
                } else if (!methodSearch(parameters)) {
                    throw new AssertionError("Unexpected state while parameter count 3");
                }
                break;
            }

            default:
                throw new AssertionError(String.format("Missing case for parameter count '%s'", parameters.size()));
        }

        worker
                .execute(searchTerm, searchType, includeAll)
                .thenCompose(embed -> {
                    CommandCleanupListener.insertResponseTracker(embed, message.getId());
                    return message.reply(embed);
                })
                .join();
    }

    private boolean classSearch(Parameters<String> parameters) {
        return parameters.getParameterNames().stream().anyMatch(CLASS_PARAMS::contains);
    }

    private boolean methodSearch(Parameters<String> parameters) {
        return parameters.getParameterNames().stream().anyMatch(METHOD_PARAMS::contains);
    }

    private boolean includeAll(Parameters<String> parameters) {
        return parameters.getParameterNames().stream().anyMatch(INCLUDE_ALL_PARAMS::contains);
    }
}
