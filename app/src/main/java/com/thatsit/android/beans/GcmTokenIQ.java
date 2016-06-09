package com.thatsit.android.beans;

import org.jivesoftware.smack.packet.IQ;

/**
 * Created by toltori on 6/4/16.
 * Customized IQ class to send GCM token to the openfire server.
 */
public class GcmTokenIQ extends IQ {
    private final String m_strXmlns = "urn:xmpp:apns";
    private String m_strGcmToken = "";

    /**
     * Set GCM token to send.
     * @param p_strGcmToken Token string
     */
    public void setGcmToken(String p_strGcmToken) {
        m_strGcmToken = p_strGcmToken;
    }

    public String getChildElementXML() {
        String w_strRequest = "<query xmlns='" + m_strXmlns + "'><token>" + m_strGcmToken + "</token></query>";
        return w_strRequest;
    }
}
