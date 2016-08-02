package com.thatsit.android.beans;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.util.XmlStringBuilder;

/**
 * Created by toltori on 6/4/16.
 * Customized IQ class to send GCM token to the openfire server.
 */
public class GcmTokenIQ extends IQ {
    private String m_strGcmToken = "";

    public GcmTokenIQ(String childElementName, String childElementNamespace) {
        super(childElementName, "urn:xmpp:apns");
        m_strGcmToken=childElementNamespace;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.element("token",m_strGcmToken);
        return xml;
    }
}