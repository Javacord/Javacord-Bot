package org.javacord.bot.util.wiki.parser;

/**
 * A class representing a page on the wiki.
 */
public class WikiPage implements Comparable<WikiPage> {

    private final String title;
    private final String[] keywords;
    private final String path;
    private final String content;

    /**
     * Creates a new wiki page.
     *
     * @param title The title of the page.
     * @param keywords The keywords the page is tagged with.
     * @param path The path of the page, relative to the wiki's base URL.
     * @param content The content of the page.
     */
    public WikiPage(String title, String[] keywords, String path, String content) {
        this.title = title;
        this.keywords = keywords;
        this.path = path;
        this.content = content;
    }

    /**
     * Gets the title.
     *
     * @return The title of the page.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the keywords.
     *
     * @return The keywords for the page.
     */
    public String[] getKeywords() {
        return keywords;
    }

    /**
     * Gets the relative path.
     *
     * @return The page path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the content.
     *
     * @return The content of the page.
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets a markdown-formatted link to the page.
     *
     * @return The markdown for a link to the page.
     */
    public String asMarkdownLink() {
        return String.format("[%s](%s)", title, WikiParser.BASE_URL + path);
    }

    @Override
    public int compareTo(WikiPage that) {
        return this.title.compareTo(that.title);
    }

}
