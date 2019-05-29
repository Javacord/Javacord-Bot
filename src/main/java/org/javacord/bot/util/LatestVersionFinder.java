package org.javacord.bot.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.javacord.api.DiscordApi;
import org.javacord.api.util.logging.ExceptionLogger;
import org.javacord.bot.Constants;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class LatestVersionFinder {

    private final DiscordApi api;

    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    private volatile String latestVersion = "";

    /**
     * Initialize the Version finder.
     * @param api The api object of which to obtain the Scheduler.
     */
    public LatestVersionFinder(DiscordApi api) {
        this.api = api;
        // Populate with latest version
        CompletableFuture.supplyAsync(this::getAndUpdateVersionSync, api.getThreadPool().getExecutorService())
                .exceptionally(ExceptionLogger.get());
    }

    /**
     * Obtain the latest release version of Javacord.
     *
     * <p>If the version cannot be obtained, the last successfully retrieved version will be used instead.
     *
     * @return The most recent release version.
     */
    public CompletableFuture<String> findLatestVersion() {
        ExecutorService executorService = api.getThreadPool().getExecutorService();
        return CompletableFuture.supplyAsync(this::getAndUpdateVersionSync, executorService)
                .exceptionally(ExceptionLogger.get().andThen(value -> latestVersion));
    }

    private String getAndUpdateVersionSync() {
        Request request = new Request.Builder()
                .url(Constants.LATEST_VERSION_URL)
                .build();
        try (ResponseBody body = client.newCall(request).execute().body()) {
            if (body == null) {
                throw new RuntimeException("Error while requesting the latest version: No response body.");
            }
            JsonNode response = mapper.readTree(body.charStream());
            // Response format is a JSON object {"version":"x.y.z"}
            if (!response.isObject()) {
                throw new AssertionError("Latest Version API result differs from expectation");
            }
            String latestVersion = response.get("version").asText();
            if (latestVersion == null || latestVersion.isEmpty()) {
                throw new AssertionError("Latest Version API result differs from expectation");
            }
            // Set cached version
            this.latestVersion = latestVersion;
            // Eventually clean up update task
            return latestVersion;
        } catch (NullPointerException | IOException e) {
            throw new RuntimeException("Error while requesting the latest version", e);
        }
    }

}
