package org.javacord.bot;

import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.JavacordHandler;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import org.javacord.bot.commands.DocsCommand;
import org.javacord.bot.commands.ExampleCommand;
import org.javacord.bot.commands.GitHubCommand;
import org.javacord.bot.commands.GradleCommand;
import org.javacord.bot.commands.InfoCommand;
import org.javacord.bot.commands.InviteCommand;
import org.javacord.bot.commands.MavenCommand;
import org.javacord.bot.commands.Sdcf4jCommand;
import org.javacord.bot.commands.SetupCommand;
import org.javacord.bot.commands.WikiCommand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    /**
     * The entry point of the bot.
     *
     * @param args The bot requires exactly one argument, either a file with the token as content or the token directly.
     *             If the argument is a relative file path, it is relative to the working directory.
     * @throws IOException If there is an error when reading the token file or writing the default log4j2.xml.
     */
    public static void main(String[] args) throws IOException {
        String log4jConfigurationFileProperty = System.getProperty("log4j.configurationFile");
        if (log4jConfigurationFileProperty != null) {
            Path log4jConfigurationFile = Paths.get(log4jConfigurationFileProperty);
            if (!Files.exists(log4jConfigurationFile)) {
                Files.copy(Main.class.getResourceAsStream("/log4j2.xml"), log4jConfigurationFile);
            }
        }

        Thread.setDefaultUncaughtExceptionHandler(ExceptionLogger.getUncaughtExceptionHandler());

        if (args.length != 1) {
            System.err.println("This bot requires exactly one argument, "
                    + "either a file with the token as content or the token directly.\n"
                    + "If the argument is a relative file path, it is relative to the working directory");
            System.exit(1);
        }

        DiscordApiBuilder apiBuilder = new DiscordApiBuilder();

        // Token
        Path tokenFile = Paths.get(args[0]);
        if (Files.isRegularFile(tokenFile)) {
            apiBuilder.setToken(Files.newBufferedReader(tokenFile).readLine());
        } else {
            apiBuilder.setToken(args[0]);
        }

        // Login
        DiscordApi api = apiBuilder
                .setWaitForServersOnStartup(false)
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
        handler.registerCommand(new Sdcf4jCommand());
        handler.registerCommand(new InfoCommand());
    }

}
