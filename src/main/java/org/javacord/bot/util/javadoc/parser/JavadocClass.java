package org.javacord.bot.util.javadoc.parser;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a javadoc class.
 */
public class JavadocClass {

    private final String baseUrl;
    private final String name;
    private final String packageName;

    /**
     * Creates a new javadoc class.
     *
     * @param baseUrl The base url of the javadocs.
     * @param node The node with the information about the class.
     */
    public JavadocClass(String baseUrl, JsonNode node) {
        this.baseUrl = baseUrl;
        name = node.get("l").asText();
        packageName = node.get("p").asText();
    }

    /**
     * Gets the name of the class.
     *
     * @return The name of the class.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets name of the class' package.
     *
     * @return The name of the class' package.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Gets the full url of the class.
     *
     * @return The full url of the class.
     */
    public String getFullUrl() {
        return baseUrl + packageName.replace(".", "/") + "/" + name + ".html";
    }

}
