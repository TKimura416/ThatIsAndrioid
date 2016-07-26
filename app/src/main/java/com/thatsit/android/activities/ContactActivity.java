package com.thatsit.android.activities;

import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.myquick.socket.ServerThread;
import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.seasia.myquick.asyncTasks.CheckChatPasswordAsync;
import com.seasia.myquick.asyncTasks.GetDataAsyncSubscriptionHistory;
import com.seasia.myquick.asyncTasks.UserStatusIdAsync;
import com.seasia.myquick.model.AppSinglton;
import com.seasia.myquick.model.CheckMessage_ChatPasswrd;
import com.seasia.myquick.model.GetSubscriptionHistoryTemplate;
import com.seasia.myquick.model.ValidateUserLoginStatus;
import com.seasia.myquick.model.ValidateUserStatusID;
import com.thatsit.android.MainService;
import com.thatsit.android.R;
import com.thatsit.android.Utility;
import com.thatsit.android.adapter.CustomExpandAdapter;
import com.thatsit.android.adapter.NavigationAdapter;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.beans.GCMClientManager;
import com.thatsit.android.beans.GcmTokenIQ;
import com.thatsit.android.encryption.helper.EncryptionManager;
import com.thatsit.android.fragement.FragmentBasicSetting;
import com.thatsit.android.fragement.FragmentChatHistoryScreen;
import com.thatsit.android.fragement.FragmentChatSetting;
import com.thatsit.android.fragement.FragmentContact;
import com.thatsit.android.fragement.FragmentInvitationReceive;
import com.thatsit.android.fragement.FragmentInvitationScreen;
import com.thatsit.android.fragement.FragmentInvitationSent;
import com.thatsit.android.fragement.FragmentPaymentSetting;
import com.thatsit.android.interfaces.CheckChatPasswordInterface;
import com.thatsit.android.interfaces.SubscriptionHistoryInterface;
import com.thatsit.android.interfaces.ValidateUserLoginInterface;
import com.thatsit.android.interfaces.ValidateUserStatusIdInterface;
import com.thatsit.android.xmpputils.Constants;
import com.thatsit.android.xmpputils.XmppManager;

import org.apache.commons.net.io.Util;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

/**
 * This is the main container on which fragments are implemented.
 * It is opened once user gets signed in and gets connected to openfire.
 */
public class ContactActivity extends ActionBarActivity implements OnClickListener{
	final String TAG = getClass().getSimpleName();
	public static ImageButton mBtn_Contact,mBtn_Chat,mBtn_Invite,mBtn_Account;
	private FragmentManager mFragmentManager;
	private FragmentTransaction mFragmentTransaction;
	private FragmentChatHistoryScreen mFragmentChatHistoryScreen;
	private FragmentContact mContactFragment;
	private FragmentInvitationScreen mInvitatinFragment;
	private FragmentBasicSetting mBasicSettingFragment;
	private FragmentInvitationSent mFragmentInvitationSent;
	private FragmentInvitationReceive mFragmentInvitationReceive;
	private FragmentChatSetting mFragmentChatSetting;
	private String RetriveVal_Chat_pass,Chatpassword;
	private MainService mService;
	public static LinearLayout tabs_lnrlayout;
	private FragmentPaymentSetting mFragmentPaymentSetting;
	private static final Intent SERVICE_INTENT = new Intent();
	private Dialog dialogChatPassword = null;
	private boolean dialogOpen = false;
	private EncryptionManager encryptionManager;
	private SharedPreferences mSharedPreferences,mSharedPreferences_Pincode,
			mSharedPreferences_Gcm,settings;
	private String ExpiryDate;
	private Handler handler;
	public static TextView textView_toolbar_title;
	public static ImageLoader loader= ImageLoader.getInstance();
	public static DisplayImageOptions options;
	static {
		SERVICE_INTENT.setComponent(new ComponentName(Constants.MAINSERVICE_PACKAGE,  Constants.MAINSERVICE_PACKAGE + Constants.MAINSERVICE_NAME ));
	}
	public DrawerLayout mDrawerLayout;
	private ExpandableListView mDrawerList;
	private CustomExpandAdapter customAdapter;
	private ActionBarDrawerToggle mDrawerToggle;
	public LinearLayout navDrawerView;
	private String[] mInvitationTitles,mAdminTitles;
	private Toolbar mToolBar;
	private List<NavigationAdapter> listParent;
	private HashMap<String, List<String>> listDataChild;
	private View lastColored;
	private String PROJECT_NUMBER="354278772391";//"773007732943";
	private String registrationID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);

		//
		// [2016/06/07 10:16 KSH] Show version & build number.
		//
		try {
			TextView w_txtVersion = (TextView) findViewById(R.id.txt_version);
			String w_strVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			int w_nVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
			w_txtVersion.setText(String.format("Version : %s, Build number : %d", w_strVersionName, w_nVersionCode));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			initialise_SharedPreference();
			startGcmService();
			Utility.setDeviceTypeAndSecureFlag(ContactActivity.this);
			initialiseHandlers();
			registerReceiver();
			Utility.contactActivity = ContactActivity.this;
			initialiseFragmentManager();
			initialise_Variables();
			initialiseLoader();
			initialise_Listener();
			setBooleans();
			checkGroupNotification();
		} catch (Exception e) {
			e.printStackTrace();
		}
		startServerThread();
		if(getResources().getBoolean(R.bool.isTablet)){
			setNavigation(savedInstanceState);
		}
	}

	private void initialiseHandlers() {
		encryptionManager = new EncryptionManager();
		handler = new Handler();
	}

	/**
	 * Server thread for file transfer via socket
	 */
	private void startServerThread() {
		ServerThread serverThread = new ServerThread(ContactActivity.this);
		new Thread(serverThread).start();
	}

	/**
	 * Check if group pending intent fired
	 */
	private void checkGroupNotification() {
		String intentValue = getIntent().getStringExtra("groupNotification");
		if(!TextUtils.isEmpty(intentValue) && intentValue.equalsIgnoreCase("groupNotification")){
			Utility.groupNotificationClicked = true;
			Utility.enteredFragmentOnce = false;
		}
	}

	private void setBooleans() {
		Utility.LoginStarted = false;
		Utility.mBinded = false;
		Utility.enteredFragmentOnce = false;
	}

	/**
	 * Init your Universal Loader
	 */
	private void initialiseLoader() {
		loader.init(ImageLoaderConfiguration.createDefault(this));
	}

	/**
	 * Register your GCM ID
	 */
	private void startGcmService() {

		GCMClientManager pushClientManager = new GCMClientManager(this, PROJECT_NUMBER);
		pushClientManager.registerIfNeeded(new GCMClientManager.RegistrationCompletedHandler() {
			@Override
			public void onSuccess(String registrationId, boolean isNewRegistration) {

				Log.d("Registration id", registrationId);
				//send this registrationId to your server
				registrationID = registrationId;
				new UserStatusIdAsync(ContactActivity.this, mValidateUserStatusIdInterface).execute();

				//
				// [2016/06/04 05:35 KSH]Register GCM token to the openfire server.
				//
				registerGcmTokenToOpenfire(registrationId);
			}

			@Override
			public void onFailure(String ex) {
				super.onFailure(ex);
			}
		});
	}

	/**
	 * Register the GCM token to the openfire xmpp server so that our plugin can use it.
	 */
	private void registerGcmTokenToOpenfire(String p_strGcmToken) {
		try {
			XmppManager w_xmppManager = XmppManager.getInstance();
			XMPPTCPConnection w_xmppConnection = w_xmppManager.getXMPPConnection();
			GcmTokenIQ w_iq = new GcmTokenIQ(p_strGcmToken);
			w_iq.setTo(Constants.HOST);
			w_iq.setType(IQ.Type.set);
			w_iq.setStanzaId("apns68057d6a");
			w_iq.setGcmToken(p_strGcmToken);
			w_xmppConnection.sendStanza(w_iq);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void sendUserLoginStatus(String statusID) {
		if (Utility.getEmail() != null) {

			if(Utility.googleServicesUnavailable == true){
				registrationID = "";
			}
			Utility.UserLoginStatus(ContactActivity.this, Utility.getEmail(), "True", registrationID,statusID, mValidateUserLoginInterface);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		try {
			startAndBindService();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set Navigation Drawer for Tablets
	 * @param savedInstanceState
	 */
	private void setNavigation(Bundle savedInstanceState) {

		mToolBar = (Toolbar) findViewById(R.id.toolbar);
		textView_toolbar_title.setText("Contacts");
		navDrawerView = (LinearLayout) findViewById(R.id.navDrawerView);
		mInvitationTitles = getResources().getStringArray(R.array.Invitations_array);
		mAdminTitles = getResources().getStringArray(R.array.Admin_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ExpandableListView) findViewById(R.id.nav_left_drawer);

		listParent = new ArrayList<NavigationAdapter>();
		listDataChild = new HashMap<String, List<String>>();

		// Navigation Drawer of Flight starts
		listParent.add(new NavigationAdapter(getString(R.string.Contacts), R.drawable.contact_icon_hover));
		listParent.add(new NavigationAdapter(getString(R.string.Chats), R.drawable.comment_icon_hover));
		listParent.add(new NavigationAdapter(getString(R.string.Invitations), R.drawable.message_icon_hover));
		listParent.add(new NavigationAdapter(getString(R.string.Admin), R.drawable.user_icon_hover));

		listDataChild.put(getString(R.string.Contacts), new ArrayList<String>());
		listDataChild.put(getString(R.string.Chats), new ArrayList<String>());
		listDataChild.put(getString(R.string.Invitations), Arrays.asList(mInvitationTitles));
		listDataChild.put(getString(R.string.Admin), Arrays.asList(mAdminTitles));

		customAdapter = new CustomExpandAdapter(this, listParent, listDataChild);
		// setting list adapter
		mDrawerList.setAdapter(customAdapter);
		mDrawerList.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);

		//getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		//getSupportActionBar().setHomeButtonEnabled(true);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolBar, R.string.drawer_open, R.string.drawer_close) {

			@Override
			public void onDrawerClosed(View drawerView) {
				// Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
				super.onDrawerClosed(drawerView);
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				// Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
				if(drawerView != null){
					InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
					inputMethodManager.hideSoftInputFromWindow(drawerView.getWindowToken(), 0);
				}
				super.onDrawerOpened(drawerView);
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			selectItem(0,0);
		}

		mDrawerList.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				int index = parent.getFlatListPosition(ExpandableListView.getPackedPositionForGroup(groupPosition));
				parent.setItemChecked(index, true);
				String parentTitle = ((NavigationAdapter) customAdapter.getGroup(groupPosition)).getTitle();

				if(lastColored != null){
					lastColored.setBackgroundColor(Color.TRANSPARENT);
					lastColored.invalidate();
				}

				if(groupPosition == 0){
					openFragmentContacts();
				}
				else if(groupPosition == 1){
					openFragmentChatDiscussions();
				}

				if (!(parentTitle == getString(R.string.Invitations) || (parentTitle == getString(R.string.Admin)))) {
					mDrawerLayout.closeDrawer(navDrawerView);
				}

				if((parentTitle == getString(R.string.Invitations) || (parentTitle == getString(R.string.Admin)))) {

					RelativeLayout rel_expand =(RelativeLayout) v.findViewById(R.id.rel1);
					RelativeLayout rel_collapse =(RelativeLayout) v.findViewById(R.id.rel2);
					if (rel_expand.getVisibility() == View.VISIBLE) {
						// Its visible
						rel_expand.setVisibility(View.GONE);
						rel_collapse.setVisibility(View.VISIBLE);
					} else {
						// Either gone or invisible
						rel_expand.setVisibility(View.VISIBLE);
						rel_collapse.setVisibility(View.GONE);
					}
					customAdapter.notifyDataSetChanged();
				}
				return false;
			}
		});

		mDrawerList.setOnChildClickListener(new OnChildClickListener() {

			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

				int index = parent.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));
				parent.setItemChecked(index, true);

				if(lastColored != null){
					lastColored.setBackgroundColor(Color.TRANSPARENT);
					lastColored.invalidate();
				}
				lastColored = v;
				v.setBackgroundColor(Color.rgb(229,127,127));

				selectItem(groupPosition,childPosition);

				return false;
			}
		});
	}

	private void selectItem(int groupPosition,int childPosition) {
		//mDrawerLayout.closeDrawer(navDrawerView);

		if(groupPosition == 2){

			if(childPosition == 0) {
				if (!ContactActivity.textView_toolbar_title.getText().equals("Received Invitations")) {
					openFragmentReceivedInvites();
				}else{
					mDrawerLayout.closeDrawer(navDrawerView);
				}
			}

			else if(childPosition == 1) {
				if (!ContactActivity.textView_toolbar_title.getText().equals("Sent Invitations")) {
					openFragmentSentInvites();
				}else{
					mDrawerLayout.closeDrawer(navDrawerView);
				}
			}

			else if(childPosition == 2) {
				if (!ContactActivity.textView_toolbar_title.getText().equals("Send Invite")) {
					openFragmentInvitation();
				}else{
					mDrawerLayout.closeDrawer(navDrawerView);
				}
			}
		}
		else if(groupPosition == 3){
			if(childPosition == 0){
				openFragmentBasicSettings();
			}

			else if(childPosition == 1) {
				if (!ContactActivity.textView_toolbar_title.getText().equals("Chat Settings")) {
					openFragmentChatSettings();
				}else{
					mDrawerLayout.closeDrawer(navDrawerView);
				}
			}

			else if(childPosition == 2) {
				if (!ContactActivity.textView_toolbar_title.getText().equals("Payment Settings")) {
					openFragmentPaymentSettings();
				}else{
					mDrawerLayout.closeDrawer(navDrawerView);
				}
			}
		}
	}

	private void openFragmentContacts() {

		if(Utility.enteredFragmentOnce == false) {
			setContactUI();
		}
	}

	private void openFragmentChatDiscussions() {

		if(NetworkAvailabilityReceiver.isInternetAvailable(ContactActivity.this)){
			FragmentContact.groupPressed = false;

			if(dialogOpen == false){
				dialogOpen = true;
				openChatPasswordDialog(ContactActivity.this);
			}
		}
		else{
			Toast.makeText(ContactActivity.this,getResources().getString(R.string.Network_Availability), Toast.LENGTH_SHORT).show();
		}
	}

	private void openFragmentPaymentSettings() {

		try {
			mFragmentPaymentSetting = new FragmentPaymentSetting(MainService.mService);
			mFragmentManager = getSupportFragmentManager();
			mFragmentTransaction = mFragmentManager.beginTransaction();
			mFragmentTransaction.replace(R.id.fragmentContainer,mFragmentPaymentSetting);
			mFragmentTransaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void openFragmentChatSettings() {

		try {
			mFragmentChatSetting = new FragmentChatSetting(MainService.mService);
			mFragmentManager = getSupportFragmentManager();
			mFragmentTransaction = mFragmentManager.beginTransaction();
			mFragmentTransaction.replace(R.id.fragmentContainer,mFragmentChatSetting);
			mFragmentTransaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void openFragmentBasicSettings() {

		try {
			FragmentContact.groupPressed = false;
			mBasicSettingFragment=new FragmentBasicSetting();
			mFragmentTransaction = mFragmentManager.beginTransaction();
			mFragmentTransaction.replace(R.id.fragmentContainer, mBasicSettingFragment);
			mFragmentTransaction.commit();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void openFragmentInvitation() {

		try {
			mInvitatinFragment=new FragmentInvitationScreen(mService);
			FragmentContact.groupPressed = false;
			mFragmentTransaction = mFragmentManager.beginTransaction();
			mFragmentTransaction.replace(R.id.fragmentContainer, mInvitatinFragment);
			mFragmentTransaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void openFragmentSentInvites() {

		try {
			Utility.startDialog(ContactActivity.this);

			new Thread(new Runnable() {
				@Override
				public void run() {
					handler.post(new Runnable() {
						@Override
						public void run() {
							mFragmentInvitationSent = new FragmentInvitationSent(mService);
							mFragmentTransaction = mFragmentManager.beginTransaction();
							mFragmentTransaction.replace(R.id.fragmentContainer,mFragmentInvitationSent);
							mFragmentTransaction.commit();
						}
					});
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void openFragmentReceivedInvites() {

		try {
			Utility.startDialog(ContactActivity.this);
			new Thread(new Runnable() {
				@Override
				public void run() {
					handler.post(new Runnable() {
						@Override
						public void run() {
							mFragmentInvitationReceive = new FragmentInvitationReceive(mService);
							mFragmentTransaction = mFragmentManager.beginTransaction();
							mFragmentTransaction.replace(R.id.fragmentContainer,mFragmentInvitationReceive);
							mFragmentTransaction.commit();
						}
					});
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if(getResources().getBoolean(R.bool.isTablet)){
			mDrawerToggle.syncState();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		if(getResources().getBoolean(R.bool.isTablet)){
			mDrawerToggle.onConfigurationChanged(newConfig);
		}
	}


	/**
	 * Check for ID Expiration.
	 */
	private void callExpiryWebService() {

		new GetDataAsyncSubscriptionHistory(ContactActivity.this,
				AppSinglton.userId,mSubscriptionHistoryInterface).execute();
	}

	/**
	 * Read values for UserId and Thats It Pincode from shared preference.
	 */
	private void initialise_SharedPreference() {

		settings = PreferenceManager.getDefaultSharedPreferences(this);

		mSharedPreferences = getSharedPreferences("USERID", 0);
		AppSinglton.userId = mSharedPreferences.getString("USERID", "");

		mSharedPreferences_Pincode = getSharedPreferences("THATSITID", 0);
		AppSinglton.thatsItPincode = mSharedPreferences_Pincode.getString("THATSITID", "");

		mSharedPreferences_Gcm = getSharedPreferences("GcmPreference", 0);
		String message = mSharedPreferences_Gcm.getString("GcmPreference", "");

		Utility.alertDialog = null;
		if(!TextUtils.isEmpty(message) && message.equalsIgnoreCase("Account Disabled")){
			Utility.openAlert(ContactActivity.this,"AccountDisabled","Your account seems to get disabled. Kindly Sign in with different account.");
		}
		else if(!TextUtils.isEmpty(message) && message.equalsIgnoreCase("Account Paused")){
			Utility.openAlert(ContactActivity.this,"AccountPaused", "Your account has been suspended by Admin. Kindly Sign in with different account.");
		}
	}

	/**
	 *  Register Broadcast Receivers.
	 */
	private void registerReceiver() {

		this.registerReceiver(mMessageReceiver,new IntentFilter("OpenFragmentPayment"));
		this.registerReceiver(mMessageReceiver,new IntentFilter("Account Disabled"));
		this.registerReceiver(mMessageReceiver,new IntentFilter("Account Paused"));
		this.registerReceiver(mMessageReceiver,new IntentFilter("checkExpiryStatus"));
		this.registerReceiver(mMessageReceiver,new IntentFilter("Offline Xmpp Signal"));

		try {
			this.registerReceiver(one2OneChatReceiver,new IntentFilter(MainService.CHAT));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialise fragment manager for transaction between fragments.
	 */
	private void initialiseFragmentManager() {

		mFragmentManager = getSupportFragmentManager();
	}

	public ContactActivity(){
	}

	@Override
	public void onBackPressed() {
		try {

			if(Utility.fragChatIsOpen == true) {

				if(!NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())){
					Utility.showMessage(getResources().getString(R.string.Network_Availability));
				}
				else{
					openFragmentChatHistory();
				}
			}
			else {
				openExitDialog();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *  Displays chat history screen containing all chat information.
	 */
	private void openFragmentChatHistory() {

		mFragmentChatHistoryScreen=new FragmentChatHistoryScreen(mService);
		mFragmentTransaction = mFragmentManager.beginTransaction();
		mFragmentTransaction.replace(R.id.fragmentContainer, mFragmentChatHistoryScreen);
		mFragmentTransaction.commit();
		if(!getResources().getBoolean(R.bool.isTablet)){
			ContactActivity.tabs_lnrlayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onResume(){
		super.onResume();
		try {
			if(Utility.allowAuthenticationDialog==true) {
				Utility.showLock(ContactActivity.this);
			}else {
				Utility.noNetwork = false;
				callExpiryWebService();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Called to open invitation scrren on incoming request.
	 */
	private void taskOnResume() {
		try {
			Intent i = getIntent();
			if(i==null){
				System.out.println("intent is null");
			}else{
				if(i.getData()==null){
					System.out.println("intent is null");
					if(i.getBooleanExtra("isFileRequest", false)){
						getIntent().removeExtra("isFileRequest");
					}
				}else{
					launchinvitationsRecievedFragment();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *  Initialise class variables.
	 */
	private void initialise_Variables() {

		mBtn_Contact = (ImageButton)findViewById(R.id.btn_contacts);
		mBtn_Chat = (ImageButton)findViewById(R.id.btn_chat);
		mBtn_Invite = (ImageButton)findViewById(R.id.btn_invitaions);
		mBtn_Account = (ImageButton)findViewById(R.id.btn_account);
		tabs_lnrlayout=(LinearLayout) findViewById(R.id.tabs_lnrlayout);
		textView_toolbar_title = (TextView)findViewById(R.id.textView_toolbar_title);
	}

	/**
	 *  Initialise click listeners.
	 */
	private void initialise_Listener() {
		try {
			mBtn_Contact.setOnClickListener(this);
			mBtn_Chat.setOnClickListener(this);
			mBtn_Invite.setOnClickListener(this);
			mBtn_Account.setOnClickListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called to start servive and bind service with the activity.
	 */
	private void startAndBindService() {

		try {
			if(MainService.mService == null)
				startService(SERVICE_INTENT);

			if(!Utility.mBinded) {
				bindService(SERVICE_INTENT, serviceConnection, Context.BIND_AUTO_CREATE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		Utility.taskPromtOnStop(true, ContactActivity.this);
	}

	/**
	 *  Setting up service connection and binding with activity.
	 */
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			try {
				mService = ((MainService.MyBinder) binder).getService();
				Utility.mBinded = true;
				openFragmentContacts();
				taskOnResume();
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			Utility.mBinded = false;
		}
	};

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

			case R.id.btn_contacts:

				openFragmentContacts();
				break;

			case R.id.btn_chat:

				if(NetworkAvailabilityReceiver.isInternetAvailable(ContactActivity.this)){
					openFragmentChatDiscussions();
				}
				else{
					Toast.makeText(ContactActivity.this,getResources().getString(R.string.Network_Availability), Toast.LENGTH_SHORT).show();
				}

				break;

			case R.id.btn_invitaions:
				iconStateChanger(false, false, true, false);
				openFragmentInvitation();
				break;

			case R.id.btn_account:
				iconStateChanger(false, false, false, true);
				openFragmentBasicSettings();
				break;

			default:
				break;
		}
	}

	/**
	 * Set icons' UI on switching between fragments.
	 */
	public void setContactUI() {

		iconStateChanger(true, false, false, false);
		try {
			mContactFragment=new FragmentContact(mService);
			mFragmentTransaction = mFragmentManager.beginTransaction();
			mFragmentTransaction.replace(R.id.fragmentContainer, mContactFragment, FragmentContact.class.getCanonicalName());
			mFragmentTransaction.commit();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set icon state based on icon clicked to open a particular fragment.
	 */
	public void iconStateChanger(boolean contact, boolean chat, boolean invite,boolean account) {

		try {
			if (contact) {
				mBtn_Contact.setBackgroundResource(R.drawable.contact_icon_hover);
			}
			else{
				mBtn_Contact.setBackgroundResource(R.drawable.contact_icon_sm);
			}
			if (chat) {
				mBtn_Chat.setBackgroundResource(R.drawable.comment_icon_hover);
			}
			else {
				mBtn_Chat.setBackgroundResource(R.drawable.comment_icon_sm);
			}

			if (invite)	{
				mBtn_Invite.setBackgroundResource(R.drawable.message_icon_hover);
			}
			else {
				mBtn_Invite.setBackgroundResource(R.drawable.message_icon_sm);
			}
			if (account) {
				mBtn_Account.setBackgroundResource(R.drawable.user_icon_hover);
			}
			else {
				mBtn_Account.setBackgroundResource(R.drawable.user_icon_sm);
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}


	/**
	 * Alert Dialog To Exit The Application.
	 * On ok, close the application.
	 */
	private void openExitDialog() {
		try {
			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ContactActivity.this);
			// set title
			alertDialogBuilder.setTitle("Do You Want to Exit");
			alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					});

			final AlertDialog dialog = alertDialogBuilder.create();
			dialog.show();
			dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					dialog.dismiss();
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_HOME);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
					startActivity(intent);
					Utility.taskPromtOnStop(true, ContactActivity.this);
				}
			});
			dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						dialog.dismiss();
						getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open invitation screen when invite is received.
	 */
	private void launchinvitationsRecievedFragment(){
		try {
			iconStateChanger(false, false,true, false);

			if(MainService.mService==null)
				startService(SERVICE_INTENT);

			if(!Utility.mBinded){
				bindService(SERVICE_INTENT, serviceConnection, Context.BIND_AUTO_CREATE);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try{
			if(mFragmentInvitationReceive==null){
				mFragmentTransaction = mFragmentManager.beginTransaction();
				mFragmentInvitationReceive=new FragmentInvitationReceive(mService);
				mFragmentTransaction.replace(R.id.fragmentContainer, mFragmentInvitationReceive);
				mFragmentTransaction.commit();
			}
		}catch(Exception e){
		}
	}

	/**
	 * Broadcast receiver to display star on incoming message.
	 */

	private BroadcastReceiver one2OneChatReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (intent.getAction() == MainService.CHAT) {
					displayPingOnList(intent);
					mService.setIncomingChatNotification();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};


	// handler for received Intents for the "my-event" event
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (intent.getAction().equals("OpenFragmentPayment")) {
					mFragmentPaymentSetting = new FragmentPaymentSetting(MainService.mService);
					mFragmentTransaction = mFragmentManager.beginTransaction();
					mFragmentTransaction.replace(R.id.fragmentContainer,mFragmentPaymentSetting );
					mFragmentTransaction.commit();

					mBtn_Contact.setEnabled(false);
					mBtn_Chat.setEnabled(false);
					mBtn_Invite.setEnabled(false);
					mBtn_Account.setEnabled(false);
				}
				/*else if(intent.getAction().equals("Connection Error")){
					Utility.showConnectionErrorAlert("ContactActivityError",ContactActivity.this);
				}*/
				else if(intent.getAction().equals("Account Disabled")){
					if(Utility.alertDialog != null){
						Utility.alertDialog = null;
					}
					Utility.openAlert(ContactActivity.this, "AccountDisabled", "Your account seems to get disabled. Kindly Sign in with different account.");
				}
				else if(intent.getAction().equals("Account Paused")){
					if(Utility.alertDialog != null){
						Utility.alertDialog = null;
					}
					Utility.openAlert(ContactActivity.this,"AccountPaused", "Your account has been suspended by Admin. Kindly Sign in with different account.");
				}
				else if(intent.getAction().equals("checkExpiryStatus")){
					callExpiryWebService();
				} else if (intent.getAction().equals("Offline Xmpp Signal")) {
					//
					// [2016/06/06 04:02 KSH] Offline -> online signal received via GCM.
					//
					//if(!(MainService.mService.connection.isConnected()|| MainService.mService.connection.isAuthenticated())){
						MainService.mService.connectAsync();
						Utility.showMessage("Resume connection...");
					//}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};


	/**
	 * Display a star for corresponding jID.
	 * @param arg1 - returns jID for the person who sent the message
	 */
	private void displayPingOnList(Intent arg1) {
		try {
			String jjj = arg1.getStringExtra("jid");
			if (jjj != null && !jjj.equalsIgnoreCase("")) {
				ThatItApplication.getApplication().getIncomingPings().add(jjj);

				String file = arg1.getStringExtra("msg_subject");
				if(file!=null && !file.equalsIgnoreCase("")){
					ThatItApplication.getApplication().getIncomingFilePings().add(jjj);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param activity - Display alert on activity to enter chat password.
	 */
	public void openChatPasswordDialog(final ContactActivity activity ) {

		dialogChatPassword=null;

		if(dialogChatPassword==null ){

			dialogChatPassword = new Dialog(activity);

			dialogChatPassword.setContentView(R.layout.login_promt_authentication);
			dialogChatPassword.setTitle("Enter Chat Password 2");

			Button btnAccept  = (Button)dialogChatPassword.findViewById(R.id.btn_accept);
			Button btnDecline  = (Button)dialogChatPassword.findViewById(R.id.btn_decline);
			final EditText etUsername = (EditText)dialogChatPassword.findViewById(R.id.etUsername);
			TextView signInDifferentUser = (TextView)dialogChatPassword.findViewById(R.id.signInDifferentUser);

			etUsername.setVisibility(View.GONE);
			signInDifferentUser.setVisibility(View.GONE);

			final EditText etPassword = (EditText)dialogChatPassword.findViewById(R.id.etPassword);

			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);

			btnAccept.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {

						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);

						try {
							if (NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {
								Chatpassword = encryptionManager.encryptPayload(etPassword.getText().toString().trim());
								Chatpassword = URLEncoder.encode(Chatpassword, "UTF-8");
								if(Chatpassword.contains("%")){
									Chatpassword = Chatpassword.replace("%","2");
								}

								if (etPassword.getText().toString().trim().toCharArray().length == 0) {
									Toast.makeText(getApplicationContext(),"Enter Chat Password",Toast.LENGTH_SHORT).show();
								} else {
									try {
										dialogChatPassword.dismiss();
										dialogOpen = false;

										if(!TextUtils.isEmpty(Utility.getChatPassword())) {

											if (Chatpassword.equals(Utility.getChatPassword())) {
												iconStateChanger(false, true, false, false);
												getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
												openFragmentChatHistory();
											}else{
												Toast.makeText(getApplicationContext(),"Incorrect Password", Toast.LENGTH_SHORT).show();
											}
										}else {
											new CheckChatPasswordAsync(activity, Chatpassword, mCheckChatPasswordInterface).execute();
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							} else {
								Toast.makeText(ContactActivity.this,getResources().getString(R.string.Network_Availability),Toast.LENGTH_SHORT).show();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			btnDecline.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {

						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);
						dialogChatPassword.dismiss();
						dialogOpen = false;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			dialogChatPassword.setCancelable(false);
			dialogChatPassword.setCanceledOnTouchOutside(false);
		}
		dialogChatPassword.show();
	}

	/**
	 * Save chat password in shared preference
	 */
	public void storeChatPasswordInSharedPreference(String Chatpassword) {
		SharedPreferences.Editor edit = settings.edit();
		edit.putString(ThatItApplication.ACCOUNT_CHAT_PASSWORD_KEY, Chatpassword);
		edit.commit();
	}

	/**
	 *  Check Chat Password Interface - Get result from async to confirm correct password.
	 */

	CheckChatPasswordInterface mCheckChatPasswordInterface = new CheckChatPasswordInterface() {

		@Override
		public void checkChatPassword(CheckMessage_ChatPasswrd chat_password) {

			try {
				RetriveVal_Chat_pass = chat_password.getmCheckMessagePasswordResult()
						.getmCheckMessagePasswordParams()[0].getRetVal();

				if (RetriveVal_Chat_pass.equals("1")) {
					storeChatPasswordInSharedPreference(Chatpassword);
					iconStateChanger(false, true, false, false);
					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
					openFragmentChatHistory();
				} else {
					Toast.makeText(getApplicationContext(),"Incorrect Password", Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 *  Get expiration date for the jID.
	 */

	SubscriptionHistoryInterface mSubscriptionHistoryInterface = new SubscriptionHistoryInterface() {

		@Override
		public void subscriptionHistory(
				GetSubscriptionHistoryTemplate mGetSubscriptionHistoryTemplate) {

			try {
				if(mGetSubscriptionHistoryTemplate!=null){

					if(mGetSubscriptionHistoryTemplate.getGetSubscriptionHistoryResult().getGetSubscriptionHistoryParams() != null) {

						ExpiryDate = mGetSubscriptionHistoryTemplate.getGetSubscriptionHistoryResult().getGetSubscriptionHistoryParams()[0].getSubscriptionExpiryDate();
						ExpiryDate = ExpiryDate.replace(" ", "-");
						ExpiryDate = ExpiryDate.replace(",", "");
						checkTimePassed();
					}else{
						if(MainService.mService.mTimer != null){
							MainService.mService.mTimer.cancel();
							MainService.mService.mTimer = null;
						}
						if(Utility.googleServicesUnavailable == true) {
							Utility.openAlert(ContactActivity.this, "AccountDisabled", "Your account seems to get disabled. Kindly Sign in with different account.");
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};


	/**
	 * Check if current date has exceeded the expiration date.
	 */
	public void checkTimePassed(){

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("MMM-dd-yyyy");
		String currentDate = df.format(cal.getTime());
		Date formattedcurrentDate = null;
		try {
			formattedcurrentDate = df.parse(currentDate);
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		Date expiryDateFormatted = null;
		try {
			expiryDateFormatted = df.parse(ExpiryDate);
		}
		catch (ParseException e) {
			e.printStackTrace();
		}

		if(formattedcurrentDate.compareTo(expiryDateFormatted)>0){
			// Current Date exceeded Expiry Date
			Utility.openAlert(ContactActivity.this,"RenewalError","Your That's It ID has expired. Kindly get it renewed to proceed.");
		}else{
			mBtn_Contact.setEnabled(true);
			mBtn_Chat.setEnabled(true);
			mBtn_Invite.setEnabled(true);
			mBtn_Account.setEnabled(true);
		}
	}

	/**
	 * Send Login status to server
	 */

	ValidateUserLoginInterface mValidateUserLoginInterface = new ValidateUserLoginInterface() {
		@Override
		public void validateUserLogin(Context context,ValidateUserLoginStatus mValidateUserLoginStatus) {
			Log.e("",""+mValidateUserLoginStatus);
		}
	};


	ValidateUserStatusIdInterface mValidateUserStatusIdInterface = new ValidateUserStatusIdInterface() {
		@Override
		public void validateUserStatusId(ValidateUserStatusID mValidateUserStatusID) {

			if(mValidateUserStatusID != null){

				String statusID = mValidateUserStatusID.getGeneateRandomNumberResult().getKey();
				sendUserLoginStatus(statusID);

				//Save StatusId in shared preference
				SharedPreferences mSharedPreferences = getSharedPreferences("statusID", MODE_PRIVATE);
				mSharedPreferences.edit().putString("statusID",statusID).commit();
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(mMessageReceiver);
		this.unregisterReceiver(one2OneChatReceiver);
	//	this.unregisterReceiver(networkChangeReceiver);
		if(MainService.mService.mTimer != null){
			MainService.mService.mTimer.cancel();
			MainService.mService.mTimer = null;
		}
		if(Utility.mBinded) {
			unbindService(serviceConnection);
		}
		if(Utility.contactActivity != null){
			Utility.contactActivity = null;
		}
	}
}
