package com.thatsit.android.application;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.parse.Parse;
import com.thatsit.android.db.One2OneChatDb;

public class ThatItApplication extends MultiDexApplication {
	private final String TAG = getClass().getSimpleName();

	/** Preference key for account username. */
	public static final String ACCOUNT_USERNAME_KEY = "account_username";
	/** Preference key for account password. */
	public static final String ACCOUNT_PASSWORD_KEY = "account_password";
	public static final String ACCOUNT_CHAT_PASSWORD_KEY = "account_chat_password";

	public static final String ACCOUNT_EMAIL_ID = "account_email";
	private boolean mIsConnected;
	private boolean mIsAccountConfigured;
	private static ThatItApplication app;
	private SharedPreferences mSettings;
	private final PreferenceListener mPreferenceListener = new PreferenceListener();
	private Hashtable<String, Packet> incomingRequestHash = new Hashtable<>();
	private RosterGroup currentRosterGroupReference=null;
	private MultiUserChat currentMUCRefernece=null;
	private MultiUserChat currentGroupChatCoversation=null;
	private static SecretKey myDesKey;
	private final String keyValue="SecretKeySpec@d1";
	private Set<String> incomingPings = new HashSet<>();
	private Set<String> incomingGroupPings = new HashSet<>();
	
	
	public Set<String> getIncomingGroupPings() {
		return incomingGroupPings;
	}

	public void setIncomingGroupPings(Set<String> incomingGroupPings) {
		this.incomingGroupPings = incomingGroupPings;
	}

	private Set<String> incomingFilePings = new HashSet<>();
	private Hashtable<String, Boolean> sentInvites = new Hashtable<>();
	private Hashtable<String, String> sentInvitesMessages = new Hashtable<>();
	
	@Override
	public void onCreate() {
		//ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
		try {
			super.onCreate();
			app = ThatItApplication.this;

			try{
				//Mobiprobe.activate(this,"49e3851c");
				//Mint.initAndStartSession(this,"5a5bd024");
			}catch(Exception e){
				e.printStackTrace();
			}

			configureParse();
			
			mSettings = PreferenceManager.getDefaultSharedPreferences(this);
			String login = mSettings.getString(ThatItApplication.ACCOUNT_USERNAME_KEY, "");
			String password = mSettings.getString(ThatItApplication.ACCOUNT_PASSWORD_KEY, "");
			String chat_password = mSettings.getString(ThatItApplication.ACCOUNT_CHAT_PASSWORD_KEY, "");
			mIsAccountConfigured = !TextUtils.isEmpty(login) && !TextUtils.isEmpty((password));
			mSettings.registerOnSharedPreferenceChangeListener(mPreferenceListener);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		byte[] encodedKey  = Base64.decode(keyValue, Base64.DEFAULT);
		myDesKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "DES"); 
	}

	/**
	 * Configure App with Parse.
	 */
	private void configureParse() {
		Parse.initialize(this, "3jG99eKHFTblm6scRdKQWJ3veL4m9DnEaEDASZN6", "Fvaaw7zL7n4NTPHofcUDO5LpxmaIwWo8RFPCi0Bw");
	}

	public RosterGroup getCurrentRosterGroupReference() {
		return currentRosterGroupReference;
	}

	public Hashtable<String, String> getSentInvitesMessages() {
		return sentInvitesMessages;
	}

	public void setSentInvitesMessages(Hashtable<String, String> sentInvitesMessages) {
		this.sentInvitesMessages = sentInvitesMessages;
	}

	public Hashtable<String, Boolean> getSentInvites() {
		return sentInvites;
	}

	public void setSentInvites(Hashtable<String, Boolean> sentInvites) {
		this.sentInvites = sentInvites;
	}

	public MultiUserChat getCurrentGroupChatCoversation() {
		return currentGroupChatCoversation;
	}

	public void setCurrentGroupChatCoversation(MultiUserChat currentGroupChatCoversation) {
		this.currentGroupChatCoversation = currentGroupChatCoversation;
	}

	public void setCurrentRosterGroupReference(RosterGroup currentRosterGroupReference) {
		this.currentRosterGroupReference = currentRosterGroupReference;
	}

	public MultiUserChat getCurrentMUCRefernece() {
		return currentMUCRefernece;
	}

	public void setCurrentMUCRefernece(MultiUserChat currentMUCRefernece) {
		this.currentMUCRefernece = currentMUCRefernece;
	}

	public static ThatItApplication getApplication() {
		return app;
	}

	public Set<String> getIncomingPings() {
		return incomingPings;
	}

	public void setIncomingPings(Set<String> incomingPings) {
		this.incomingPings = incomingPings;
	}

	public Set<String> getIncomingFilePings() {
		return incomingFilePings;
	}

	public void setIncomingFilePings(Set<String> incomingFilePings) {
		this.incomingFilePings = incomingFilePings;
	}

	/**
	 * Tell if App is connected to a XMPP server.
	 * 
	 * @return false if not connected.
	 */
	public boolean isConnected() {
		Log.i(TAG, mIsConnected + "    isConnected ");
		return mIsConnected;
	}

	/**
	 * Set the status of the connection to a XMPP server of BEEM.
	 * 
	 * @param isConnected
	 *            set for the state of the connection.
	 */
	public void setConnected(boolean isConnected) {
		mIsConnected = isConnected;
		Log.i(TAG, isConnected + "    setConnected ");
	}

	public Hashtable<String, Packet> getIncomingRequestHash() {
		return incomingRequestHash;
	}

	public void setIncomingRequestHash(
			Hashtable<String, Packet> incomingRequestHash) {
		this.incomingRequestHash = incomingRequestHash;
	}

	/**
	 * Tell if a XMPP account is configured.
	 * 
	 * @return false if there is no account configured.
	 */
	public boolean isAccountConfigured() {
		return mIsAccountConfigured;
	}

	/**
	 * A listener for all the change in the preference file. It is used to
	 * maintain the global state of the application.
	 */
	private class PreferenceListener implements	SharedPreferences.OnSharedPreferenceChangeListener {

		/**
		 * Constructor.
		 */
		public PreferenceListener() {
		}

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			try {
				if (ThatItApplication.ACCOUNT_USERNAME_KEY.equals(key)
						|| ThatItApplication.ACCOUNT_PASSWORD_KEY.equals(key)) {
					String login = mSettings.getString(
							ThatItApplication.ACCOUNT_USERNAME_KEY, "");
					String password = mSettings.getString(
							ThatItApplication.ACCOUNT_PASSWORD_KEY, "");
					mIsAccountConfigured = !TextUtils.isEmpty(login)
							&& !TextUtils.isEmpty((password));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public One2OneChatDb openDatabase() {
		return new One2OneChatDb(app);
	}

	public void closeDatabase() {
		One2OneChatDb.closeDb();
	}

}
