
package com.thatsit.android.ping;

import org.jivesoftware.smack.packet.IQ;

/**
 * This extension represents a iq ping.
 */
public class PingExtension extends IQ {

    /** Namespace of the Ping XEP. */
    public static final String NAMESPACE = "urn:xmpp:ping";

    /** Xml element name for the ping. */
    public static final String ELEMENT = "ping";

    /**
     * Create a ping iq packet.
     */
    public PingExtension() {
    }

    @Override
    public String getChildElementXML() {
        if (getType() == IQ.Type.RESULT)
            return null;
        return "<" + ELEMENT + " xmlns=\"" + NAMESPACE + "\" />";
    }

}
