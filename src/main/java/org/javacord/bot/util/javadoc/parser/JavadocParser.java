package org.javacord.bot.util.javadoc.parser;

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
 * Parses JavaDocs of a given url.
 */
public class JavadocParser {

    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    private final DiscordApi api;
    private final String url;

    /**
     * Creates a new Javadoc parser.
     *
     * @param api A discord api instance.
     * @param url The url of the JavaDocs.
     */
    public JavadocParser(DiscordApi api, String url) {
        this.api = api;
        this.url = url.endsWith("/") ? url : url + "/";
    }

    /**
     * Gets a set with all methods.
     *
     * @return A set with all methods.
     */
    public CompletableFuture<Set<JavadocMethod>> getMethods() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getMethodsBlocking();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, api.getThreadPool().getExecutorService());
    }

    /**
     * Gets a set with all classes.
     *
     * @return A set with all classes.
     */
    public CompletableFuture<Set<JavadocClass>> getClasses() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getClassesBlocking();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, api.getThreadPool().getExecutorService());
    }

    /**
     * Creates a blocking request to get the methods.
     *
     * @return A set with all methods.
     * @throws IOException If something went wrong.
     */
    private Set<JavadocMethod> getMethodsBlocking() throws IOException {
        Request request = new Request.Builder()
                .url(url + "member-search-index.js")
                .build();

        Response response = client.newCall(request).execute();
        ResponseBody body = response.body();
        Set<JavadocMethod> methods = new HashSet<>();
        if (body == null) {
            return methods;
        }
        for (JsonNode node : mapper.readTree(body.string().replaceFirst("memberSearchIndex = ", ""))) {
            methods.add(new JavadocMethod(url, node));
        }
        return methods;
    }

    /**
     * Creates a blocking request to get the classes.
     *
     * @return A set with all classes.
     * @throws IOException If something went wrong.
     */
    private Set<JavadocClass> getClassesBlocking() throws IOException {
        Request request = new Request.Builder()
                .url(url + "type-search-index.js")
                .build();

        Response response = client.newCall(request).execute();
        ResponseBody body = response.body();
        Set<JavadocClass> classes = new HashSet<>();
        if (body == null) {
            return classes;
        }
        for (JsonNode node : mapper.readTree(body.string().replaceFirst("typeSearchIndex = ", ""))) {
            classes.add(new JavadocClass(url, node));
        }
        return classes;
    }

}
