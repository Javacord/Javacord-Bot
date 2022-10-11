package org.javacord.bot.util;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.bot.Constants;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class LatestVersionFinder {
    @Inject
    Logger logger;

    @Inject
    DiscordApi api;

    private static final OkHttpClient client = new OkHttpClient();

    private static final Pattern XML_VERSION = Pattern
            .compile("<latest>(\\d+\\.\\d+\\.\\d+)<\\/latest>", Pattern.MULTILINE);

    private volatile String latestVersion = "";

    /**
     * Initialize the latest version finder.
     */
    @PostConstruct
    void populateWithLatestVersion() {
        ExecutorService executorService = api.getThreadPool().getExecutorService();
        CompletableFuture
                .supplyAsync(this::getAndUpdateVersionSync, executorService)
                .whenComplete((__, throwable) -> {
                    if (throwable != null) {
                        logger
                                .atError()
                                .withThrowable(throwable)
                                .log("Exception while populating the latest version finder");
                    }
                });
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
        return CompletableFuture
                .supplyAsync(this::getAndUpdateVersionSync, executorService)
                .exceptionally(throwable -> {
                    logger
                            .atError()
                            .withThrowable(throwable)
                            .log("Exception while finding the latest version");
                    return latestVersion;
                });
    }

    private String getAndUpdateVersionSync() {
        Request request = new Request.Builder()
                .url(Constants.LATEST_VERSION_URL)
                .build();
        try (ResponseBody body = client.newCall(request).execute().body()) {
            if (body == null) {
                throw new RuntimeException("Error while requesting the latest version: No response body.");
            }

            // Response format is an XML object with a <latest>...</latest> tag with the version
            Matcher matcher = XML_VERSION.matcher(body.string());
            if (!matcher.find()) {
                throw new RuntimeException("Failed to match latest version!");
            }
            String latestVersion = matcher.group(1);
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
