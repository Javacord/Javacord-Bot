package org.javacord.bot;

import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.JavacordHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.bot.commands.DocsCommand;
import org.javacord.bot.commands.GitHubCommand;
import org.javacord.bot.commands.TimeoutCommand;
import org.javacord.bot.commands.WikiCommand;

import java.util.List;

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
        handler.registerCommand(new GitHubCommand());
        handler.registerCommand(new TimeoutCommand());
        handler.registerCommand(new WikiCommand());

        // Add "moderation" Permission to Bastian and Vampire; used by !timeout command
        handler.addPermission("157862224206102529", "moderation");
        handler.addPermission("341505207341023233", "moderation");

        // Remove all members from the Timeout role, to prevent eternal mutes from bot restarts.
        api.getServerById(151037561152733184L)
                .ifPresent(server -> {
                    List<Role> rolesByName = server.getRolesByName("Timeout");

                    if (rolesByName.size() == 1) {
                        Role timeout = rolesByName.get(0);

                        timeout.getUsers()
                                .forEach(usr -> usr.removeRole(timeout));
                    }
                });
    }

}
