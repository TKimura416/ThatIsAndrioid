
package com.thatsit.android.xmpputils;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.address.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.CloseIQProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.DataPacketProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.OpenIQProvider;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.commands.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.delay.provider.DelayInformationProvider;
import org.jivesoftware.smackx.disco.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.disco.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.iqlast.packet.LastActivity;
import org.jivesoftware.smackx.iqprivate.PrivateDataManager;
import org.jivesoftware.smackx.iqversion.packet.Version;
import org.jivesoftware.smackx.muc.packet.GroupChatInvitation;
import org.jivesoftware.smackx.muc.provider.MUCAdminProvider;
import org.jivesoftware.smackx.muc.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.muc.provider.MUCUserProvider;
import org.jivesoftware.smackx.offline.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.offline.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.privacy.provider.PrivacyProvider;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.sharedgroups.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.si.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.time.packet.Time;
import org.jivesoftware.smackx.vcardtemp.provider.VCardProvider;
import org.jivesoftware.smackx.xdata.provider.DataFormProvider;
import org.jivesoftware.smackx.xevent.provider.MessageEventProvider;
import org.jivesoftware.smackx.xhtmlim.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.xroster.provider.RosterExchangeProvider;
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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.thatsit.android.MainService;
import com.thatsit.android.Utility;
import com.thatsit.android.application.ThatItApplication;

import java.io.IOException;


public class XmppManager {
	private String TAG = getClass().getSimpleName();
	private SharedPreferences mSettings;
	public static final int DISCONNECTED = 1;
	public static final int CONNECTING = 2;
	public static final int CONNECTED = 3;
	public static final int DISCONNECTING = 4;
	public static final int WAITING_TO_CONNECT = 5;
	public static final int WAITING_FOR_NETWORK = 6;
	public static XmppManager sXmppManager;
	private XMPPTCPConnection mConnection=null;

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
//            mSettings = PreferenceManager.getDefaultSharedPreferences(ThatItApplication.getApplication());
//            String login = mSettings.getString(ThatItApplication.ACCOUNT_USERNAME_KEY, "");
//            String password = mSettings.getString(ThatItApplication.ACCOUNT_PASSWORD_KEY, "");
            SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
            SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");
            mConnection = new XMPPTCPConnection(
                    XMPPTCPConnectionConfiguration.builder()
                            .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
//                            .setUsernameAndPassword(login, password)
                            .setHost(Constants.HOST) //HostName HERE
                            .setServiceName(Constants.HOST) //SERVICENAME
                            .setPort(Constants.PORT)
                            .setDebuggerEnabled(true)
                            .build()
            );        }
		return mConnection;
	}

	private void configure() {
		// Private Data Storage
        ProviderManager.addIQProvider("query", "jabber:iq:private",  new PrivateDataManager.PrivateDataIQProvider());

		// Time
		try {
			ProviderManager.addIQProvider("query", "jabber:iq:time", Class.forName("org.jivesoftware.smackx.time.packet.Time"));
		}catch (ClassNotFoundException e) {
            Log.w("TestClient", "Can't load class for org.jivesoftware.smackx.packet.Time");
        }
        catch (Exception e){
            e.printStackTrace();
            Log.e("exception",e.getMessage());
        }


		// Roster Exchange
		ProviderManager.addExtensionProvider("x", "jabber:x:roster", new RosterExchangeProvider());

		// Message Events
		ProviderManager.addExtensionProvider("x", "jabber:x:event", new MessageEventProvider()   );

		// Chat State
		ProviderManager.addExtensionProvider("active", "http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		ProviderManager.addExtensionProvider("composing", "http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		ProviderManager.addExtensionProvider("paused", "http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		ProviderManager.addExtensionProvider("inactive", "http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		ProviderManager.addExtensionProvider("gone", "http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		// XHTML
		ProviderManager.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
				new XHTMLExtensionProvider());

		// Group Chat Invitations
		ProviderManager.addExtensionProvider("x", "jabber:x:conference", new GroupChatInvitation.Provider());

		// Service Discovery # Items
		ProviderManager.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());

		// Service Discovery # Info
		ProviderManager.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());

		// Data Forms
		ProviderManager.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

		// MUC User
		ProviderManager.addExtensionProvider("x", "http://jabber.org/protocol/muc#user", new MUCUserProvider());

		// MUC Admin
		ProviderManager.addIQProvider("query", "http://jabber.org/protocol/muc#admin", new MUCAdminProvider());

		// MUC Owner
		ProviderManager.addIQProvider("query", "http://jabber.org/protocol/muc#owner", new MUCOwnerProvider());

		// Delayed Delivery
		ProviderManager.addExtensionProvider("x", "jabber:x:delay", new DelayInformationProvider());


		// Version
		try {
			ProviderManager.addIQProvider("query", "jabber:iq:version",
					Class.forName("org.jivesoftware.smackx.iqversion.packet.Version"));
		} catch (ClassNotFoundException e) {
			// Not sure what's happening here.
            e.printStackTrace();
		}catch (Exception e) {
            // Not sure what's happening here.
            e.printStackTrace();
        }

		// VCard
		ProviderManager.addIQProvider("vCard", "vcard-temp", new VCardProvider());

		// Offline Message Requests
		ProviderManager.addIQProvider("offline", "http://jabber.org/protocol/offline",
				new OfflineMessageRequest.Provider());

		// Offline Message Indicator
		ProviderManager.addExtensionProvider("offline", "http://jabber.org/protocol/offline",
				new OfflineMessageInfo.Provider());

		// Last Activity
		ProviderManager.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());

		// User Search
		ProviderManager.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

		// SharedGroupsInfo
		ProviderManager.addIQProvider("sharedgroup", "http://www.jivesoftware.org/protocol/sharedgroup",
				new SharedGroupsInfo.Provider());

        try{
            // JEP-33: Extended Stanza Addressing
            ProviderManager.addExtensionProvider("addresses", "http://jabber.org/protocol/address",
                    new MultipleAddressesProvider());
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
		// FileTransfer
		ProviderManager.addIQProvider("si", "http://jabber.org/protocol/si", new StreamInitiationProvider());
		ProviderManager.addIQProvider("query","http://jabber.org/protocol/bytestreams", new BytestreamsProvider());

		ProviderManager.addIQProvider("open","http://jabber.org/protocol/ibb", new OpenIQProvider());
		ProviderManager.addIQProvider("close","http://jabber.org/protocol/ibb", new CloseIQProvider());
		ProviderManager.addExtensionProvider("data","http://jabber.org/protocol/ibb", new DataPacketProvider());
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
		// Privacy
		ProviderManager.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
		ProviderManager.addIQProvider("command", "http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider());
		ProviderManager.addExtensionProvider("malformed-action", "http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.MalformedActionError());
		ProviderManager.addExtensionProvider("bad-locale", "http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadLocaleError());
		ProviderManager.addExtensionProvider("bad-payload", "http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadPayloadError());
		ProviderManager.addExtensionProvider("bad-sessionid", "http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadSessionIDError());
		ProviderManager.addExtensionProvider("session-expired", "http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.SessionExpiredError());

        }catch (Exception e){
            e.printStackTrace();
        }
	}

}
