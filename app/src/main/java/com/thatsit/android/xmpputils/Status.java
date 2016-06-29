
package com.thatsit.android.xmpputils;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;

/**
 * Utility class to deal with status and presence value.
 */
public final class Status {

    /** Status of a disconnected contact. */
    public static final int CONTACT_STATUS_DISCONNECT = 100;

    /** Status of a unavailable (long away) contact. */
    public static final int CONTACT_STATUS_UNAVAILABLE = 200;

    /** Status of a away contact. */
    public static final int CONTACT_STATUS_AWAY = 300;

    /** Status of a busy contact. */
    public static final int CONTACT_STATUS_BUSY = 400;

    /** Status of a available contact. */
    public static final int CONTACT_STATUS_AVAILABLE = 500;

    /** Status of a available for chat contact. */
    public static final int CONTACT_STATUS_AVAILABLE_FOR_CHAT = 600;

    /**
     * Default constructor masked.
     */
    private Status() {
    }

    /**
     * Get the smack presence mode for a status.
     * @param status the status in beem
     * @return the presence mode to use in presence packet or null if there is no mode to use
     */
    public static Presence.Mode getPresenceModeFromStatus(final int status) {
    Presence.Mode res;
    switch (status) {
        case CONTACT_STATUS_AVAILABLE:
        res = Presence.Mode.available;
        break;
        case CONTACT_STATUS_AVAILABLE_FOR_CHAT:
        res = Presence.Mode.chat;
        break;
        case CONTACT_STATUS_AWAY:
        res = Presence.Mode.away;
        break;
        case CONTACT_STATUS_BUSY:
        res = Presence.Mode.dnd;
        break;
        case CONTACT_STATUS_UNAVAILABLE:
        res = Presence.Mode.xa;
        break;
        default:
        return null;
    }
    return res;
    }

    /**
     * Get the status of from a presence packet.
     * @param presence the presence containing status
     * @return an int representing the status
     */
    public static int getStatusFromPresence(final Presence presence) {
    int res = Status.CONTACT_STATUS_DISCONNECT;
    if (presence.getType().equals(Presence.Type.unavailable)) {
        res = Status.CONTACT_STATUS_DISCONNECT;
    } else {
        Mode mode = presence.getMode();
        if (mode == null) {
        res = Status.CONTACT_STATUS_AVAILABLE;
        } else {
        switch (mode) {
            case available:
            res = Status.CONTACT_STATUS_AVAILABLE;
            break;
            case away:
            res = Status.CONTACT_STATUS_AWAY;
            break;
            case chat:
            res = Status.CONTACT_STATUS_AVAILABLE_FOR_CHAT;
            break;
            case dnd:
            res = Status.CONTACT_STATUS_BUSY;
            break;
            case xa:
            res = Status.CONTACT_STATUS_UNAVAILABLE;
            break;
            default:
            res = Status.CONTACT_STATUS_DISCONNECT;
            break;
        }
        }
    }
    return res;
    }

    /**
     * Check if contact is online by his status.
     * @param status contact status
     * @return is online
     */
    public static boolean statusOnline(final int status) {
    return status != Status.CONTACT_STATUS_DISCONNECT;
    }

    /**
     * Get icon resource from status.
     * @param status the status
     * @return the resource icon
     */
}