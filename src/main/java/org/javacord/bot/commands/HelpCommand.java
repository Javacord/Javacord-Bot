package org.javacord.bot.commands;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.bot.Constants;
import org.javacord.bot.listeners.CommandCleanupListener;

/**
 * The !help command which is used to list all commands.
 */
public class HelpCommand implements CommandExecutor {

    private final CommandHandler commandHandler;

    /**
     * Initializes the command.
     *
     * @param commandHandler The command handler used to extract command usages and descriptions.
     */
    public HelpCommand(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    /**
     * Executes the {@code !help} command.
     *
     * @param api The Discord api.
     * @param server  The server where the command was issued.
     * @param channel The channel where the command was issued.
     * @param message The message triggering the command.
     * @param args The command's arguments.
     */
    @Command(aliases = {"!help"}, async = true, description = "Shows the help page")
    public void onCommand(DiscordApi api, Server server, TextChannel channel, Message message, String[] args) {
        // Only react in #java_javacord channel on Discord API server
        if ((server.getId() == Constants.DAPI_SERVER_ID) && (channel.getId() != Constants.DAPI_JAVACORD_CHANNEL_ID)) {
            return;
        }

        if (args.length >= 1) { // TODO Provide help for specific commands (e.g., "!help wiki")
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription("The `!help` command does not accept arguments!")
                    .setColor(Constants.ERROR_COLOR);
            CommandCleanupListener.insertResponseTracker(embed, message.getId());
            channel.sendMessage(embed).join();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setThumbnail(getClass().getClassLoader().getResourceAsStream("javacord3_icon.png"), "png")
                .setTitle("Commands")
                .setColor(Constants.JAVACORD_ORANGE);

        for (CommandHandler.SimpleCommand simpleCommand : commandHandler.getCommands()) {
            if (!simpleCommand.getCommandAnnotation().showInHelpPage()) {
                continue; // skip command
            }
            String usage = simpleCommand.getCommandAnnotation().usage();
            if (usage.isEmpty()) { // no usage provided, using the first alias
                usage = simpleCommand.getCommandAnnotation().aliases()[0];
            }
            String description = simpleCommand.getCommandAnnotation().description();
            embed.addField("**__" + usage + "__**", description);
        }

        CommandCleanupListener.insertResponseTracker(embed, message.getId());
        channel.sendMessage(embed).join();
    }
}
