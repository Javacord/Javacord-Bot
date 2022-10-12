package org.javacord.bot.commands.workers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.bot.Constants;
import org.javacord.bot.util.LatestVersionFinder;

/**
 * The setup commands worker which is used to get information useful for first setup.
 */
@ApplicationScoped
public class SetupCommandWorker {
    @Inject
    LatestVersionFinder versionFinder;

    /**
     * Executes the {@code setup} commands.
     */
    public EmbedBuilder execute() {
        String latestVersion = versionFinder.findLatestVersion().join();
        return new EmbedBuilder()
                .setColor(Constants.JAVACORD_ORANGE)
                .addField("Gradle Dependency",
                        "```groovy\n"
                                + "repositories { \n"
                                + "  mavenCentral()\n"
                                + "}\n"
                                + "dependencies { \n"
                                + "  implementation 'org.javacord:javacord:" + latestVersion + "'\n"
                                + "}\n"
                                + "```")
                .addField("Maven Dependency",
                        "```xml\n"
                                + "<dependency>\n"
                                + "    <groupId>org.javacord</groupId>\n"
                                + "    <artifactId>javacord</artifactId>\n"
                                + "    <version>" + latestVersion + "</version>\n"
                                + "    <type>pom</type>\n"
                                + "</dependency>\n"
                                + "```")
                .addField("Setup Guides",
                        "• [IntelliJ + Gradle](https://javacord.org/wiki/getting-started/intellij-gradle/) (recommended)\n"
                                + "• [IntelliJ + Maven](https://javacord.org/wiki/getting-started/intellij-maven/)\n"
                                + "• [Eclipse + Maven](https://javacord.org/wiki/getting-started/eclipse-maven/)");
    }
}
