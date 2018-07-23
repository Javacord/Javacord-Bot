package org.javacord.bot.util.javadoc.parser;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a javadoc method.
 */
public class JavadocMethod {

    private final String baseUrl;
    private final String name;
    private final String className;
    private final String packageName;
    private final String url;

    /**
     * Creates a new javadoc method.
     *
     * @param baseUrl The base url of the javadocs.
     * @param node The node with the information about the method.
     */
    public JavadocMethod(String baseUrl, JsonNode node) {
        this.baseUrl = baseUrl;
        name = node.get("l").asText();
        className = node.get("c").asText();
        packageName = node.get("p").asText();
        url = node.has("url") ? node.get("url").asText() : name.replace("(", "-").replace(")", "-");
    }

    /**
     * Gets the name of the method.
     *
     * @return The name of the method.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the full name of the method, including package and class.
     *
     * @return The full name of the method.
     */
    public String getFullName() {
        return getPackageName() + "." + getClassName() + "#" + getName();
    }

    /**
     * Gets a shorter version of the name.
     *
     * @return A shorter version of the name.
     */
    public String getShortenedName() {
        String name = getName();
        if (name.length() > 40) {
            name = name.replaceAll("\\(.+\\)", "(...)");
        }
        if (name.length() > 45) {
            name = name.substring(0, 42) + "...";
        }
        return name;
    }

    /**
     * Gets name of the method's class.
     *
     * @return The name of the method's class.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Gets name of the method's package.
     *
     * @return The name of the method's package.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Gets the full url of the method.
     *
     * @return The full url of the method.
     */
    public String getFullUrl() {
        return baseUrl + packageName.replace(".", "/") + "/" + className + ".html#" + url;
    }

}
