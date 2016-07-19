
package com.thatsit.android.ping;

import org.jivesoftware.smack.packet.IQ;

/**
 * This extension represents a iq ping.
 */
public class PingExtension extends IQ {

    /** Namespace of the Ping XEP. */
    private static final String NAMESPACE = "urn:xmpp:ping";

    /** Xml element name for the ping. */
    private static final String ELEMENT = "ping";

    public PingExtension(IQ iq) {
        super(iq);
    }

//
//    @Override
//    public String getChildElementXML() {
//        if (getType() == Type.result)
//            return null;
//        return "<" + ELEMENT + " xmlns=\"" + NAMESPACE + "\" />";
////    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        return null;
    }

}
