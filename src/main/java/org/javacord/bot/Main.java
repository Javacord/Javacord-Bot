package org.javacord.bot;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.se.SeContainerInitializer;
import jakarta.inject.Named;
import org.apache.logging.log4j.LogManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.Boolean.FALSE;
import static java.lang.Thread.setDefaultUncaughtExceptionHandler;

@ApplicationScoped
public class Main {
    @Produces
    @Named
    static String discordToken;

    /**
     * The entry point of the bot.
     *
     * @param args The bot requires exactly one argument, either a file with the token as content or the token directly.
     *             If the argument is a relative file path, it is relative to the working directory.
     * @throws IOException If there is an error when reading the token file or writing the default log4j2.xml.
     */
    public static void main(String[] args) throws IOException {
        setupLogging();

        if (args.length != 1) {
            System.err.println("This bot requires exactly one argument, "
                    + "either a file with the token as content or the token directly.\n"
                    + "If the argument is a relative file path, it is relative to the working directory");
            System.exit(1);
        }

        Path tokenFile = Paths.get(args[0]);
        if (Files.isRegularFile(tokenFile)) {
            try (BufferedReader tokenFileReader = Files.newBufferedReader(tokenFile)) {
                discordToken = tokenFileReader.readLine();
            }
        } else {
            discordToken = args[0];
        }

        SeContainerInitializer
                .newInstance()
                .addProperty("org.jboss.weld.construction.relaxed", FALSE)
                .addPackages(true, Main.class)
                .initialize();
    }

    private static void setupLogging() throws IOException {
        System.setProperty("java.util.logging.manager", org.apache.logging.log4j.jul.LogManager.class.getName());
        System.setProperty("org.jboss.logging.provider", "log4j2");

        String log4jConfigurationFileProperty = System.getProperty("log4j.configurationFile");
        if (log4jConfigurationFileProperty != null) {
            Path log4jConfigurationFile = Paths.get(log4jConfigurationFileProperty);
            if (!Files.exists(log4jConfigurationFile)) {
                try (InputStream fallbackLog4j2ConfigStream = Main.class.getResourceAsStream("/log4j2.xml")) {
                    Files.copy(fallbackLog4j2ConfigStream, log4jConfigurationFile);
                }
            }
        }

        setDefaultUncaughtExceptionHandler((thread, throwable) -> LogManager
                .getLogger(throwable.getStackTrace()[0].getClassName())
                .atError()
                .withThrowable(throwable)
                .log("Caught unhandled exception on thread '{}'!", thread::getName));
    }
}
