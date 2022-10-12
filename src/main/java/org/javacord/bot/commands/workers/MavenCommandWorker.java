package org.javacord.bot.commands.workers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;
import org.javacord.bot.util.LatestVersionFinder;

/**
 * The maven command worker which is used to get information about Javacord with Maven.
 */
@ApplicationScoped
public class MavenCommandWorker {
    @Inject
    LatestVersionFinder versionFinder;

    /**
     * Executes the {@code maven} commands.
     */
    public EmbedBuilder execute() {
        String latestVersion = versionFinder.findLatestVersion().join();
        return new EmbedBuilder()
                .setColor(Constants.JAVACORD_ORANGE)
                .addField("Dependency",
                        "```xml\n"
                                + "<dependency>\n"
                                + "    <groupId>org.javacord</groupId>\n"
                                + "    <artifactId>javacord</artifactId>\n"
                                + "    <version>" + latestVersion + "</version>\n"
                                + "    <type>pom</type>\n"
                                + "</dependency>\n"
                                + "```")
                .addField("Setup Guides",
                        "• [IntelliJ](https://javacord.org/wiki/getting-started/intellij-maven/)\n"
                                + "• [Eclipse](https://javacord.org/wiki/getting-started/eclipse-maven/)");
    }
}
