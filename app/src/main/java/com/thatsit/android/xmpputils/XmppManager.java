
package com.thatsit.android.xmpputils;

//import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration;
//import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
//import org.jivesoftware.smack.provider.PrivacyProvider;
//import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
//import org.jivesoftware.smackx.GroupChatInvitation;
//import org.jivesoftware.smackx.PrivateDataManager;
//import org.jivesoftware.smackx.bytestreams.ibb.provider.CloseIQProvider;
//import org.jivesoftware.smackx.bytestreams.ibb.provider.DataPacketProvider;
//import org.jivesoftware.smackx.bytestreams.ibb.provider.OpenIQProvider;
//import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
//import org.jivesoftware.smackx.packet.ChatStateExtension;
//import org.jivesoftware.smackx.packet.LastActivity;
//import org.jivesoftware.smackx.packet.OfflineMessageInfo;
//import org.jivesoftware.smackx.packet.OfflineMessageRequest;
//import org.jivesoftware.smackx.packet.SharedGroupsInfo;
//import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
//import org.jivesoftware.smackx.provider.DataFormProvider;
//import org.jivesoftware.smackx.provider.DelayInformationProvider;
//import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
//import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
//import org.jivesoftware.smackx.provider.MUCAdminProvider;
//import org.jivesoftware.smackx.provider.MUCOwnerProvider;
//import org.jivesoftware.smackx.provider.MUCUserProvider;
//import org.jivesoftware.smackx.provider.MessageEventProvider;
//import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
//import org.jivesoftware.smackx.provider.RosterExchangeProvider;
//import org.jivesoftware.smackx.provider.StreamInitiationProvider;
//import org.jivesoftware.smackx.provider.VCardProvider;
//import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
//import org.jivesoftware.smackx.search.UserSearch;
import android.util.Log;
import com.thatsit.android.Utility;


public class XmppManager {
	private String TAG = getClass().getSimpleName();

	public static final int DISCONNECTED = 1;
	public static final int CONNECTING = 2;
	public static final int CONNECTED = 3;
	public static final int DISCONNECTING = 4;
	public static final int WAITING_TO_CONNECT = 5;
	public static final int WAITING_FOR_NETWORK = 6;
	private static XMPPTCPConnectionConfiguration connConfig = null;
	public static XmppManager sXmppManager;
	private XMPPTCPConnection mConnection;

	public synchronized static XmppManager getInstance() {
		if (sXmppManager == null) {
			sXmppManager = new XmppManager();
			Log.e("XmppManager","getting Instance");
		}
		return sXmppManager;
	}

	public void setXMPPConnection(XMPPTCPConnection connection){
		this.mConnection = connection;
	}

	// XMPP Connection
	public XMPPTCPConnection getXMPPConnection() {
		if (mConnection == null) {
			setConnectionConfig();
			mConnection = new XMPPTCPConnection(connConfig);
		}
		return mConnection;
	}

	// XMPP Connection Config
	private void setConnectionConfig() {
		try {
            XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration
                    .builder();
            config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
            config.setHost(Constants.HOST);
            config.setPort(Constants.PORT);
            config.setDebuggerEnabled(true);
            config.setSendPresence(true);

//			connConfig.setReconnectionAllowed(true);
//			connConfig.setRosterLoadedAtLogin(true);
//			connConfig.setSendPresence(true);
//			connConfig.setSASLAuthenticationEnabled(false);
//			Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
//			configure(ProviderManager.getInstance());
            Log.e("XmppManager","getting Instance");

        } catch (Exception e) {
            Log.e("XmppManager","getting Instance" + e.getMessage() + e.toString());
            Utility.stopDialog();
			e.printStackTrace();
		}
	}


	

//	private void configure(ProviderManager pm) {
//
//		// Private Data Storage
//		pm.addIQProvider("query", "jabber:iq:private", new PrivateDataManager.PrivateDataIQProvider());
//
//		// Time
//		try {
//			pm.addIQProvider("query", "jabber:iq:time", Class.forName("org.jivesoftware.smackx.packet.Time"));
//		} catch (ClassNotFoundException e) {
//			Log.w("TestClient", "Can't load class for org.jivesoftware.smackx.packet.Time");
//		}
//
//		// Roster Exchange
//		pm.addExtensionProvider("x", "jabber:x:roster", new RosterExchangeProvider());
//
//		// Message Events
//		pm.addExtensionProvider("x", "jabber:x:event", new MessageEventProvider()   );
//
//		// Chat State
//		pm.addExtensionProvider("active", "http://jabber.org/protocol/chatstates",
//				new ChatStateExtension.Provider());
//		pm.addExtensionProvider("composing", "http://jabber.org/protocol/chatstates",
//				new ChatStateExtension.Provider());
//		pm.addExtensionProvider("paused", "http://jabber.org/protocol/chatstates",
//				new ChatStateExtension.Provider());
//		pm.addExtensionProvider("inactive", "http://jabber.org/protocol/chatstates",
//				new ChatStateExtension.Provider());
//		pm.addExtensionProvider("gone", "http://jabber.org/protocol/chatstates",
//				new ChatStateExtension.Provider());
//
//		// XHTML
//		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
//				new XHTMLExtensionProvider());
//
//		// Group Chat Invitations
//		pm.addExtensionProvider("x", "jabber:x:conference", new GroupChatInvitation.Provider());
//
//		// Service Discovery # Items
//		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
//				new DiscoverItemsProvider());
//
//		// Service Discovery # Info
//		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
//				new DiscoverInfoProvider());
//
//		// Data Forms
//		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
//
//		// MUC User
//		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user", new MUCUserProvider());
//
//		// MUC Admin
//		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin", new MUCAdminProvider());
//
//		// MUC Owner
//		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());
//
//		// Delayed Delivery
//		pm.addExtensionProvider("x", "jabber:x:delay", new DelayInformationProvider());
//
//		// Version
//		try {
//			pm.addIQProvider("query", "jabber:iq:version",
//					Class.forName("org.jivesoftware.smackx.packet.Version"));
//		} catch (ClassNotFoundException e) {
//			// Not sure what's happening here.
//		}
//
//		// VCard
//		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
//
//		// Offline Message Requests
//		pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
//				new OfflineMessageRequest.Provider());
//
//		// Offline Message Indicator
//		pm.addExtensionProvider("offline", "http://jabber.org/protocol/offline",
//				new OfflineMessageInfo.Provider());
//
//		// Last Activity
//		pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());
//
//		// User Search
//		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());
//
//		// SharedGroupsInfo
//		pm.addIQProvider("sharedgroup", "http://www.jivesoftware.org/protocol/sharedgroup",
//				new SharedGroupsInfo.Provider());
//
//		// JEP-33: Extended Stanza Addressing
//		pm.addExtensionProvider("addresses", "http://jabber.org/protocol/address",
//				new MultipleAddressesProvider());
//
//		// FileTransfer
//		pm.addIQProvider("si", "http://jabber.org/protocol/si", new StreamInitiationProvider());
//		pm.addIQProvider("query","http://jabber.org/protocol/bytestreams", new BytestreamsProvider());
//
//		pm.addIQProvider("open","http://jabber.org/protocol/ibb", new OpenIQProvider());
//		pm.addIQProvider("close","http://jabber.org/protocol/ibb", new CloseIQProvider());
//		pm.addExtensionProvider("data","http://jabber.org/protocol/ibb", new DataPacketProvider());
//
//
//		// Privacy
//		pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
//		pm.addIQProvider("command", "http://jabber.org/protocol/commands",
//				new AdHocCommandDataProvider());
//		pm.addExtensionProvider("malformed-action", "http://jabber.org/protocol/commands",
//				new AdHocCommandDataProvider.MalformedActionError());
//		pm.addExtensionProvider("bad-locale", "http://jabber.org/protocol/commands",
//				new AdHocCommandDataProvider.BadLocaleError());
//		pm.addExtensionProvider("bad-payload", "http://jabber.org/protocol/commands",
//				new AdHocCommandDataProvider.BadPayloadError());
//		pm.addExtensionProvider("bad-sessionid", "http://jabber.org/protocol/commands",
//				new AdHocCommandDataProvider.BadSessionIDError());
//		pm.addExtensionProvider("session-expired", "http://jabber.org/protocol/commands",
//				new AdHocCommandDataProvider.SessionExpiredError());
//	}

}
