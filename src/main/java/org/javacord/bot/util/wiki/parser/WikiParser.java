package org.javacord.bot.util.wiki.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.javacord.api.DiscordApi;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * A parser for the Javacord wiki.
 */
public class WikiParser {

    public static final String API_URL = "https://javacord.org/api/wiki.json";
    public static final String BASE_URL = "https://javacord.org"; // the /wiki/ part of the url will be returned by the API

    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    private final DiscordApi discordApi;
    private final String apiUrl;

    /**
     * Creates a new wiki parser.
     *
     * @param api The Discord Api of which to use the HTTP client.
     */
    public WikiParser(DiscordApi api) {
        this(api, API_URL);
    }

    /**
     * Creates a new Wiki parser.
     *
     * @param api The Discord Api of which to use the HTTP client.
     * @param apiUrl The URL for the json file with the page list.
     */
    public WikiParser(DiscordApi api, String apiUrl) {
        this.discordApi = api;
        this.apiUrl = apiUrl;
    }

    /**
     * Gets the pages asynchronously.
     *
     * @return The pages of the wiki.
     */
    public CompletableFuture<Set<WikiPage>> getPages() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getPagesBlocking();
            } catch (Throwable t) {
                throw new CompletionException(t);
            }
        }, discordApi.getThreadPool().getExecutorService());
    }

    /**
     * Gets the pages synchronously.
     *
     * @return The pages of the wiki.
     * @throws IOException If the connection to the wiki failed.
     */
    public Set<WikiPage> getPagesBlocking() throws IOException {
        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        Response response = client.newCall(request).execute();
        ResponseBody body = response.body();
        Set<WikiPage> pages = new HashSet<>();
        if (body == null) {
            return pages;
        }
        JsonNode array = mapper.readTree(body.charStream());
        if (!array.isArray()) {
            throw new AssertionError("Format of wiki page list not as expected");
        }
        for (JsonNode node : array) {
            if (node.has("title") && node.has("keywords") && node.has("url") && node.has("content")) {
                pages.add(new WikiPage(
                        node.get("title").asText(),
                        asStringArray(node.get("keywords")),
                        node.get("url").asText(),
                        node.get("content").asText()
                ));
            } else {
                throw new AssertionError("Format of wiki page list not as expected");
            }
        }
        return pages;
    }

    private String[] asStringArray(JsonNode arrayNode) {
        if (!arrayNode.isArray()) {
            return new String[] {};
        }
        String[] result = new String[arrayNode.size()];
        int i = 0;
        for (JsonNode node : arrayNode) {
            result[i++] = node.asText();
        }
        return result;
    }

}
