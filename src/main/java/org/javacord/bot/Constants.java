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
     * The ID of the #talk-to-james channel on the Javacord server.
     */
    public static final long TALK_TO_JAMES_CHANNEL_ID = 712366253603422209L;

    /**
     * The API URL where to obtain the latest release version for Javacord.
     */
    public static final String LATEST_VERSION_URL = "https://ci.javacord.org/app/rest/builds/buildType:(id:Javacord_Release),status:SUCCESS?guest=1";

    /**
     * The URL to the Javacord docs on docs.javacord.org, with a placeholder for api/core.
     */
    public static final String JAVACORD_DOCS_URL_1 = "https://docs.javacord.org/%s/";

    /**
     * The URL to the Javacord docs on javadoc.io, with a placeholder for api/core and another for the version.
     */
    public static final String JAVACORD_DOCS_URL_2 = "https://javadoc.io/static/org.javacord/javacord-%s/%s/";

    private Constants() { /* nope */ }

}
