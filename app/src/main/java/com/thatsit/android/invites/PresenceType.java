
package com.thatsit.android.invites;

import org.jivesoftware.smack.packet.Presence;


/**
 * Utility class to deal with Presence type.
 */
public final class PresenceType {

    /** The user is available to receive messages (default). */
    private static final int AVAILABLE = 100;

    /** The user is unavailable to receive messages. */
    private static final int UNAVAILABLE = 200;

    /** Request subscription to recipient's presence. */

    private static final int SUBSCRIBE = 300;

    /** Grant subscription to sender's presence. */
    private static final int SUBSCRIBED = 400;

    /** Request removal of subscription to sender's presence. */
    private static final int UNSUBSCRIBE = 500;

    /** Grant removal of subscription to sender's presence. */
    private static final int UNSUBSCRIBED = 600;

    /** The presence packet contains an error message. */
    private static final int ERROR = 701;

    /**
     * Private default constructor.
     */
    private PresenceType() {
    }

    /**
     * Get the presence type from a presence packet.
     * @param presence the presence type
     * @return an int representing the presence type
     */
    public static int getPresenceType(final Presence presence) {
    int res;
    switch (presence.getType()) {
        case available:
        res = PresenceType.AVAILABLE;
        break;
        case unavailable:
        res = PresenceType.UNAVAILABLE;
        break;
        case subscribe:
        res = PresenceType.SUBSCRIBE;
        break;
        case subscribed:
        res = PresenceType.SUBSCRIBED;
        break;
        case unsubscribe:
        res = PresenceType.UNSUBSCRIBE;
        break;
        case unsubscribed:
        res = PresenceType.UNSUBSCRIBED;
        break;
        case error:
        default:
        res = PresenceType.ERROR;
    }
    return res;
    }

    /**
     * Get the smack presence mode for a status.
     * @param type the status type in beem
     * @return the presence mode to use in presence packet or null if there is no mode to use
     */
    public static Presence.Type getPresenceTypeFrom(final int type) {
    Presence.Type res;
    switch (type) {
        case AVAILABLE:
        res = Presence.Type.available;
        break;
        case UNAVAILABLE:
        res = Presence.Type.unavailable;
        break;
        case SUBSCRIBE:
        res = Presence.Type.subscribe;
        break;
        case SUBSCRIBED:
        res = Presence.Type.subscribed;
        break;
        case UNSUBSCRIBE:
        res = Presence.Type.unsubscribe;
        break;
        case UNSUBSCRIBED:
        res = Presence.Type.unsubscribed;
        break;
        case ERROR:
        res = Presence.Type.error;
        break;
        default:
        return null;
    }
    return res;
    }
}
