package com.thatsit.android.beans;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.util.XmlStringBuilder;

/**
 * Created by toltori on 6/4/16.
 * Customized IQ class to send GCM token to the openfire server.
 */
public class GcmTokenIQ extends IQ {
    private final String m_strXmlns = "urn:xmpp:apns";
    private String m_strGcmToken = "";

    public GcmTokenIQ(String m_strGcmToken) {
        super("urn:xmpp:apns");
        m_strGcmToken=m_strGcmToken;
    }

    /**
     * Set GCM token to send.
     * @param p_strGcmToken Token string
     */
    public void setGcmToken(String p_strGcmToken) {
        m_strGcmToken = p_strGcmToken;
    }


//    public XmlStringBuilder getChildElementXML() {
//        String w_strRequest = "<query xmlns='" + m_strXmlns + "'><token>" + m_strGcmToken + "</token></query>";
//        return w_strRequest;
//    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        xml.element("w_strRequest", "<query xmlns='" + m_strXmlns + "'><token>" + m_strGcmToken + "</token></query>");
        return xml;
//        String w_strRequest = "<query xmlns='" + m_strXmlns + "'><token>" + m_strGcmToken + "</token></query>";
//        return null;
    }
}
