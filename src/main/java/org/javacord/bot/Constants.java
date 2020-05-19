package org.javacord.bot;

import java.awt.Color;

/**
 * Contains some useful constants like colors.
 */
public final class Constants {

    /**
     * The "Javacord orange".
     */
    public static final Color JAVACORD_ORANGE = new Color(243, 189, 30);

    /**
     * The color used for error messages.
     */
    public static final Color ERROR_COLOR = Color.RED;

    /**
     * The ID of the "Discord API" server.
     */
    public static final long DAPI_SERVER_ID = 81384788765712384L;

    /**
     * The ID of the "#java_javacord" channel on the "Discord API" server.
     */
    public static final long DAPI_JAVACORD_CHANNEL_ID = 381889796785831936L;

    /**
     * The API URL where to obtain the latest release version for Javacord.
     */
    public static final String LATEST_VERSION_URL = "https://docs.javacord.org/rest/latest-version/release";

    private Constants() { /* nope */ }

}
