package org.javacord.bot;

import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.JavacordHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.bot.commands.DocsCommand;
import org.javacord.bot.commands.ExampleCommand;
import org.javacord.bot.commands.GitHubCommand;
import org.javacord.bot.commands.GradleCommand;
import org.javacord.bot.commands.InviteCommand;
import org.javacord.bot.commands.MavenCommand;
import org.javacord.bot.commands.SetupCommand;
import org.javacord.bot.commands.WikiCommand;

public class Main {

    private static Logger logger = LogManager.getLogger(Main.class);

    /**
     * The entrance point of the bot.
     *
     * @param args The first argument should be the bot's token. Every other argument gets ignored.
     */
    public static void main(String[] args) {
        // Login
        DiscordApi api = new DiscordApiBuilder()
                .setToken(args[0])
                .login().join();

        // Register commands
        CommandHandler handler = new JavacordHandler(api);
        handler.registerCommand(new DocsCommand());
        handler.registerCommand(new ExampleCommand());
        handler.registerCommand(new GitHubCommand());
        handler.registerCommand(new GradleCommand());
        handler.registerCommand(new InviteCommand());
        handler.registerCommand(new MavenCommand());
        handler.registerCommand(new SetupCommand());
        handler.registerCommand(new WikiCommand());
    }

}
