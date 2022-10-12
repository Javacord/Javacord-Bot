package org.javacord.bot.commands.workers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;
import org.javacord.bot.util.LatestVersionFinder;

/**
 * The gradle command worker which is used to get information about Javacord with Gradle.
 */
@ApplicationScoped
public class GradleCommandWorker {
    @Inject
    LatestVersionFinder versionFinder;

    /**
     * Executes the {@code gradle} commands.
     */
    public EmbedBuilder execute() {
        String latestVersion = versionFinder.findLatestVersion().join();
        return new EmbedBuilder()
                .setColor(Constants.JAVACORD_ORANGE)
                .addField("Dependency",
                        "```groovy\n"
                                + "repositories { \n"
                                + "  mavenCentral()\n"
                                + "}\n"
                                + "dependencies { \n"
                                + "  implementation 'org.javacord:javacord:" + latestVersion + "'\n"
                                + "}\n"
                                + "```")
                .addField("Setup Guide", "• [IntelliJ](https://javacord.org/wiki/getting-started/intellij-gradle/)");
    }
}
