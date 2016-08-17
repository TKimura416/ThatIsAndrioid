package com.thatsit.android.fragement;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

//import org.jivesoftware.smack.ChatManager;
//import org.jivesoftware.smack.Roster;
//import org.jivesoftware.smack.RosterEntry;
//import org.jivesoftware.smack.RosterGroup;
//import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.roster.RosterListener;
//import org.jivesoftware.smackx.packet.*;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.stringprep.XmppStringprepException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.seasia.myquick.asyncTasks.Validate_ThatsItId_Async;
import com.seasia.myquick.model.ValidateThatsItID;
import com.thatsit.android.MainService;
import com.thatsit.android.MyGroupsHelper;
import com.thatsit.android.R;
import com.thatsit.android.Utility;
import com.thatsit.android.activities.ContactActivity;
import com.thatsit.android.adapter.ChatGroupsAdapter;
import com.thatsit.android.adapter.UsersAdapter;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.db.DbOpenHelper;
import com.thatsit.android.db.One2OneChatDb;
import com.thatsit.android.encryption.helper.EncryptionManager;
import com.thatsit.android.interfaces.CheckChatPasswordInterface;
import com.thatsit.android.interfaces.ValidateThatsItIdInterface;
import com.thatsit.android.parseutil.ParseCallbackListener;
import com.thatsit.android.parseutil.ParseUtil;
import com.thatsit.android.xmpputils.Constants;
import com.thatsit.android.xmpputils.XmppManager;
import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.seasia.myquick.asyncTasks.CheckChatPasswordAsync;
import com.seasia.myquick.controller.WebServiceClient;
import com.seasia.myquick.model.AppSinglton;
import com.seasia.myquick.model.CheckMessage_ChatPasswrd;
import com.seasia.myquick.model.FetchUserSettingTemplates;

public class FragmentContact extends Fragment implements OnClickListener {

	private final boolean LastFragment = false;
	private final String TAG = "FragmentContact";
	private ListView mlistView_Contacts;
	private View mView;
	private Handler handler = new Handler();
	private FragmentManager mFragmentManager;
	private FragmentTransaction mFragmentTransaction;
	private EditText mEdt_Search;
	private ImageView profile_pic;
	private TextView txt_pseudoName, txt_desciption,txt_friendlist;
	private String profileImageString,entryWithoutHost;
	private final One2OneChatReceiver one2OneChatReceiver = new One2OneChatReceiver();
	private static final Intent SERVICE_INTENT = new Intent();
	private String psedoName, profiledescription, RetriveVal_Chat_pass,Chatpassword;
	private MainService mService;
	private XMPPTCPConnection mConnection;
	private XmppManager mXmppManager;
	private ThatItApplication myApplication;
	private final MyRosterListner myRosterListner = new MyRosterListner();
	public static UsersAdapter usersAdapter;
	public static ImageView imgVwAddNewGroup;
	private Button btnToggleContacts, btnToggleGroups;
	private ListView lstvwChatGroups;
	private ChatGroupsAdapter chatGroupsAdapter;
	private ArrayList<RosterGroup> list = null;
	public ProgressBar progressBar_groups;
	private RosterEntry entry,entryToBeRemoved;
	private String profilePicDrawable;
	private boolean itemLongClickPressed = false;
	public static boolean groupPressed = false;
	private ArrayList<String> getRosterHistoryList;
	private ArrayList<Integer> rosterHistoryToBeDeleted;
	private VCard card;
	private ImageView refresh_icon;
	private final ArrayList<String> jids = new ArrayList<>();
	private final ArrayList<String> listcardname = new ArrayList<>();
	private final ArrayList<String> listcardlastname = new ArrayList<>();
	private final ArrayList<String> listcardprofilepic = new ArrayList<>();
	private String personFirstName, personLastName;
	private SharedPreferences mSharedPreferences,mSharedPreferences_reg;
	private final ParseUtil parseUtil = new ParseUtil();
	private ResetRoster resetRoster = null;
	private boolean dialogOpen = false;
	public boolean areGroupsReady;
	private boolean groupsEmpty = false;
	private Activity activity;
	private Dialog dialogChatPassword = null;
	private EncryptionManager encryptionManager;
	private ContactActivity hostActivity;
	private ProgressBar progressBar;
	private int ACTION ; // 1 == OPEN CHAT SCREEN && 2== DIASPLAY GROUPS
	private AlertDialog alertDialog,connectionErrorAlert;
	static {
		SERVICE_INTENT.setComponent(new ComponentName(
				Constants.MAINSERVICE_PACKAGE, Constants.MAINSERVICE_PACKAGE
				+ Constants.MAINSERVICE_NAME));
	}
	private final RostListener rostListener = new RostListener();
	private ProgressDialog progressDialog;
	private SharedPreferences mSettings;

	@SuppressLint("ValidFragment")
	public FragmentContact(MainService mService) {
		this.mService = MainService.mService;
		handler = new Handler();
	}

	public FragmentContact() {
	}

	@Override
	public void onAttach(Context activity) {
		super.onAttach(activity);
		hostActivity = (ContactActivity) activity;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(savedInstanceState == null){
			initialiseFragmentManager();
			if(!getResources().getBoolean(R.bool.isTablet)){
				ContactActivity.tabs_lnrlayout.setVisibility(View.VISIBLE);
			}
			Utility.enteredFragmentOnce = true;
			Utility.fragmentContact = FragmentContact.this;
			((ContactActivity)getActivity()).iconStateChanger(true, false, false,false);
			encryptionManager = new EncryptionManager();
			mSettings = PreferenceManager.getDefaultSharedPreferences(ThatItApplication.getApplication());
			SmackConfiguration.setDefaultPacketReplyTimeout(60000);
			Utility.fragPaymentSettingsOpen = false;
			Utility.fragChatIsOpen = false;
			Utility.disAllowSubscription = true;
			initialiseXmppConnection();
		}
	}

	private void initialiseFragmentManager() {
		mFragmentManager = getActivity().getSupportFragmentManager();
	}

	/**
	 * Establish XMPP connection
	 */
	private void initialiseXmppConnection() {
		try {
			myApplication = ThatItApplication.getApplication();
			mXmppManager = XmppManager.getInstance();
			mConnection = mXmppManager.getXMPPConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

		try {
			mView = inflater.inflate(R.layout.fragment_contacts, container, false);
			activity = getActivity();
			initialise_Variable();
			if(savedInstanceState == null){
				if(getResources().getBoolean(R.bool.isTablet)){
					ContactActivity.textView_toolbar_title.setText("Contacts");
				}
				initialise_Listeners();
				//mEdt_Search.setText("");
				checkBooleanValue();
				registerReceivers();
				AppSinglton.currentGroupName = null;
				setWindowType();
				initialiseSharedPreference();
				if(NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())){
					if(mConnection == null || !(mConnection.isConnected()|| mConnection.isAuthenticated())) {
						//ProgressBarStatus("Start");
						showConnectionErrorAlert();
						getRosterfromDatabase();
					}else{
						taskOnCreate();
						ProgressBarStatus("Stop");
					}
				}else{
					getRosterfromDatabase();
					Utility.showMessage("No Network Available");
				}

				joinAndSetGroupAdapter();
				callWebServiceData();
				setView();

				if(Utility.groupNotificationClicked){
					Utility.groupNotificationClicked = false;
					displayGroupSection();
					//Utility.stopDialog();
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return mView;
	}

	private void checkBooleanValue() {
		if(FragmentContact.groupPressed){
			displayContactSection();
			FragmentContact.groupPressed = false;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Utility.fragmentContact = FragmentContact.this;
		Utility.UserPauseStatus(getActivity());
	}

	private void setView() {
		try {
			lstvwChatGroups.setVisibility(View.GONE);
			imgVwAddNewGroup.setVisibility(View.GONE);
			refresh_icon.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void joinAndSetGroupAdapter() {
		setGroupAdapter();
		MainService.mService.joinToGroups();
	}

	/**
	 * Roster list for  jID
	 */
	private void setUserAdapter() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (jids.size() == 0) {
					Utility.stopDialog();
					txt_friendlist.setVisibility(View.VISIBLE);
				} else {
					txt_friendlist.setVisibility(View.GONE);
				}
				usersAdapter = new UsersAdapter(jids, hostActivity, mConnection,
						listcardname, listcardlastname, listcardprofilepic);

				mlistView_Contacts.setAdapter(usersAdapter);

				if (progressDialog != null) {
					progressDialog.dismiss();
					progressDialog = null;
				}
				Utility.stopDialog();
			}
		});
	}

	/**
	 * Set keyboard state hidden.
	 */
	private void setWindowType() {
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
						| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
						| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}

	/**
	 * Get user registration info.
	 */
	private void callWebServiceData() {

		if(Utility.FragmentContactDataFetchedOnce){
			getDataFromSharedPrefernce();
		} else {
			//Utility.startDialog(activity);
			new GetDataAsynRegistration().execute();
		}
	}

	/**
	 * Get profileImage,psedoName,profiledescription for user.
	 */
	private void getDataFromSharedPrefernce() {

		profileImageString = mSharedPreferences_reg.getString("ProfileImageString","");
		psedoName = mSharedPreferences_reg.getString("PseudoName", "");
		profiledescription = mSharedPreferences_reg.getString("Pseudodescription", "");

		setDataToRespectiveFields();
	}

	private void initialiseSharedPreference() {

		mSharedPreferences_reg = getActivity().getSharedPreferences("register_data", 0);

		mSharedPreferences = getActivity().getSharedPreferences("USERID", 0);
		AppSinglton.userId = mSharedPreferences.getString("USERID", "");
	}


	/**
	 * Initialise class variables.
	 */
	private void initialise_Variable() {
		profile_pic = (ImageView) mView.findViewById(R.id.profile_pic);
		mlistView_Contacts = (ListView) mView.findViewById(R.id.lst_contacts);
		mEdt_Search = (EditText) mView.findViewById(R.id.edt_Search);
		txt_pseudoName = (TextView) mView.findViewById(R.id.txt_PersnName);
		txt_desciption = (TextView) mView.findViewById(R.id.txt_PersnName2);
		txt_friendlist = (TextView) mView.findViewById(R.id.txt_friendlist);
		btnToggleContacts = (Button) mView.findViewById(R.id.btnViewContacts);
		btnToggleGroups = (Button) mView.findViewById(R.id.btnViewGroup);
		lstvwChatGroups = (ListView) mView.findViewById(R.id.lst_chat_groups);
		imgVwAddNewGroup = (ImageView) mView.findViewById(R.id.user_status);
		refresh_icon = (ImageView) mView.findViewById(R.id.refresh_icon);
		progressBar_groups = (ProgressBar) mView.findViewById(R.id.progressBar_groups);
		progressBar = (ProgressBar)mView.findViewById(R.id.progressBar);
	}

	private RosterEntry getEntryUsingJid(String jid) throws XmppStringprepException {

		jid = jid.toLowerCase();
		if (!jid.contains("@")) {
			jid = jid + "@" + MainService.mService.connection.getHost();
		}
		return Roster.getInstanceFor(MainService.mService.connection).getEntry(jid);
	}

	private void initialise_Listeners() {

		mlistView_Contacts.setTextFilterEnabled(true);

		mEdt_Search.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {
				try {

					//	if(mEdt_Search.getText().toString().trim().toCharArray().length != 0)

					usersAdapter = new UsersAdapter(arg0.toString(),jids,hostActivity,mConnection,
							listcardname,listcardlastname,listcardprofilepic);
					mlistView_Contacts.setAdapter(usersAdapter);

				} catch (Exception e) {
					Utility.stopDialog();
					e.printStackTrace();
				}
			}
			public void beforeTextChanged(CharSequence arg0, int arg1,int arg2, int arg3) {
				System.out.println("before text change" + arg0.length());
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				System.out.println("after text change" + arg0.length());
			}
		});

		imgVwAddNewGroup.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (jids.size() != 0) {
					imgVwAddNewGroup.setEnabled(false);
					MyGroupsHelper.createNewGroupDialog(getActivity(),MainService.mService.connection);
				} else {
					Utility.showMessage("No Friend To Be Added To The Group");
				}
			}
		});

		mlistView_Contacts.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent,
										   View view, int position, long id) {
				try {
					if (!NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {
						Utility.showMessage(getResources().getString(R.string.Network_Availability));
					}
					else if(!MainService.mService.connection.isConnected() && !MainService.mService.connection.isAuthenticated()){
					//	Utility.showMessage("Please wait while connection is restored");
					}
					else {
						itemLongClickPressed = true;
						entryToBeRemoved = getEntryUsingJid(jids.get(position));
						ThatItApplication.getApplication().getIncomingPings().remove(entryToBeRemoved.getUser());
						ThatItApplication.getApplication().getIncomingFilePings().remove(entryToBeRemoved.getUser());
						openRosterDeleteConfirmationDialog();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}
		});

		mlistView_Contacts.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, final View arg1,
									final int position, long arg3) {
				try {

					if (!NetworkAvailabilityReceiver.isInternetAvailable(getActivity())) {
						Utility.showMessage(getResources().getString(R.string.Network_Availability));
					}

					else if(!MainService.connection.isConnected() && !MainService.connection.isAuthenticated()){
						//Utility.showMessage("Please wait while connection is restored");
					}
					else {
						if(!dialogOpen){

							if (!itemLongClickPressed) {

								//mEdt_Search.setText("");
								ACTION = 1;
								dialogOpen = true;
								entry = getEntryUsingJid(jids.get(position));
								entryWithoutHost = jids.get(position);
								openChatPasswordDialog(activity);

								new Thread(new Runnable() {
									@Override
									public void run() {
										try {
											ThatItApplication.getApplication().getIncomingPings().remove(entry.getUser());
											ThatItApplication.getApplication().getIncomingFilePings().remove(entry.getUser());

											entry.setName(listcardname.get(position));
											profilePicDrawable = listcardprofilepic.get(position);
											personFirstName = listcardname.get(position);
											personLastName = listcardlastname.get(position);

											notifyUserAdapter();

										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}).start();
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		btnToggleContacts.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				displayContactSection();
			}
		});

		btnToggleGroups.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				ACTION = 2;
				if(!groupPressed) {
					groupPressed = true;
					if(!TextUtils.isEmpty(entryWithoutHost)){
						entryWithoutHost = "";
					}
					openChatPasswordDialog(activity);
				}
			}
		});

		refresh_icon.setOnClickListener(FragmentContact.this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	/**
	 * Register broadcast receiver to refresh user and group adapters.
	 */
	private void registerReceivers() {

		ThatItApplication.getApplication().registerReceiver(
				new IncomingReceiver(),new IntentFilter("UPDATE_ROSTER_MANUALLY"));
		ThatItApplication.getApplication().registerReceiver(
				new IncomingReceiver(),new IntentFilter("Refresh_Group_Adapter"));
		ThatItApplication.getApplication().registerReceiver(
				new IncomingReceiver(),new IntentFilter("GROUP NEW MESSAGE RECEIVED"));
		ThatItApplication.getApplication().registerReceiver(
				new IncomingReceiver(),new IntentFilter("Refresh_User_Adapter"));
		ThatItApplication.getApplication().registerReceiver(
				new IncomingReceiver(),new IntentFilter("Refresh_Adapter"));
	}

	/**
	 * Fetch Roster list for jID from Openfire.
	 */
	private void fetchRosterFromOpenfire() {

		if(!Utility.VcardLoadedOnce){
			if(NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())
					&&  MainService.mService.connection.isConnected()
					&&  MainService.mService.connection.isAuthenticated()){

				Log.e("fetchRosterFromOpenfire", "fetchRosterFromOpenfire");
				FilterUsersWithParse(rostListener, ParseCallbackListener.OPERATION_ON_START);
			}else{
				getRosterfromDatabase();

				if(!NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())){
					Utility.showMessage(getResources().getString(R.string.Network_Availability));
				}else{
					joinAndSetGroupAdapter();
				}
			}
		} else {
			getRosterfromDatabase();
			joinAndSetGroupAdapter();
		}
	}

	private void FilterUsersWithParse(RostListener listener, int operation) {
		parseUtil.areExists(ThatItApplication.getApplication(), listener, operation);
	}

	@Override
	public void onStop() {
		super.onStop();
		Utility.disAllowSubscription = false;
		Utility.enteredFragmentOnce = false;
	}


	class ResetRoster extends AsyncTask<Void, Void, Void> {
		ArrayList<RosterEntry> rosterEntries = null;

		ResetRoster(ArrayList<RosterEntry> entries) {
			this.rosterEntries = entries;
		}

		@Override
		protected Void doInBackground(Void... params) {

			if (MainService.mService.connection.isConnected() && MainService.mService.connection.isAuthenticated()
					&& rosterEntries != null) {
				jids.clear();
				listcardname.clear();
				listcardprofilepic.clear();
				listcardlastname.clear();

				//card = new VCard();
				One2OneChatDb oneToOne = new One2OneChatDb(ThatItApplication.getApplication());
				for (int i = 0; i < rosterEntries.size(); i++) {
					try {
						card = null;
						card = new VCard();
						if(mConnection.isConnected() && mConnection.isAuthenticated()) {
							card.load(MainService.mService.connection, rosterEntries.get(i).getUser());
						}
						} catch (XMPPException e) {
						e.printStackTrace();
					} catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    } catch (SmackException.NoResponseException e) {
                        e.printStackTrace();
                    }
                    try {
						String jId = rosterEntries.get(i).getUser().split("@")[0];
						String firstname;
						String lastname = "";
						String profilePic = "";
						if(!TextUtils.isEmpty(card.getFirstName())){
							 firstname = card.getFirstName();
						}else{
							firstname = "My Name";
						}
						if(!TextUtils.isEmpty(card.getLastName())){
							 lastname = card.getLastName();
						}
						if(!TextUtils.isEmpty(card.getField("profile_picture_url"))){
							 profilePic = card.getField("profile_picture_url");
						}

						jids.add(jId);
						listcardname.add(firstname);
						listcardlastname.add(lastname);
						listcardprofilepic.add(profilePic);

						oneToOne.saveRoster(rosterEntries.get(i).getUser(),
								firstname, lastname, profilePic);

					} catch (Exception e) {
						e.printStackTrace();
						if(progressDialog != null){
							progressDialog.dismiss();
							progressDialog = null;
						}
						jids.clear();
						listcardname.clear();
						listcardlastname.clear();
						listcardprofilepic.clear();
						Cursor cursor = One2OneChatDb.getRosterEntry(rosterEntries.get(i).getUser() );
						cursor.moveToFirst();

						if(cursor.getCount()>0){
							String jid = cursor.getString(cursor.getColumnIndex(DbOpenHelper.JABBER_ID));
							String firstname = cursor.getString(cursor.getColumnIndex(DbOpenHelper.FIRSTNAME));
							String lastname = cursor.getString(cursor.getColumnIndex(DbOpenHelper.LASTNAME));
							String profilePic = cursor.getString(cursor.getColumnIndex(DbOpenHelper.PROFILE_PIC_URL));

							if(TextUtils.isEmpty(firstname)){
								firstname = "My Name";
							}
							if(TextUtils.isEmpty(lastname)){
								lastname = "";
							}
							if(TextUtils.isEmpty(profilePic)){
								profilePic = "";
							}

							jids.add(jid);
							listcardname.add(firstname);
							listcardlastname.add(lastname);
							listcardprofilepic.add(profilePic);
						}
						else{
							String jId = rosterEntries.get(i).getUser().split("@")[0];
							String firstname;
							String lastname = "";
							String profilePic = "";

							if(!TextUtils.isEmpty(card.getFirstName())){
								firstname = card.getFirstName();
							}else{
								firstname = "My Name";
							}
							if(!TextUtils.isEmpty(card.getLastName())){
								lastname = card.getLastName();
							}
							if(!TextUtils.isEmpty(card.getField("profile_picture_url"))){
								profilePic = card.getField("profile_picture_url");
							}
							jids.add(jId);
							listcardname.add(firstname);
							listcardlastname.add(lastname);
							listcardprofilepic.add(profilePic);

							oneToOne.saveRoster(rosterEntries.get(i).getUser(),
									firstname, lastname,profilePic);
						}
					}

				}
			}
			return null;
		}

		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			Utility.disAllowSubscription = false;
			setUserAdapter();

		}
	}

	private ArrayList<RosterEntry> getUSerRosters() {
		try {
			if (!NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {
				Utility.showMessage(getResources().getString(R.string.Network_Availability));
				return mService.getRosters(myRosterListner);
			}
		} catch (Exception e) {
			Utility.stopDialog();
			e.printStackTrace();
		}
		try {
			if (myApplication.isConnected() && MainService.mService.connection.isAuthenticated()) {
				return mService.getRosters(myRosterListner);
			}
		} catch (Exception e) {
			Utility.stopDialog();
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Roster callbacks for entry added,deleted,updated,presence changed.
	 */
	public final class MyRosterListner implements RosterListener,ParseCallbackListener{

		@Override
		public void entriesAdded(Collection<String> addresses) {

		}

		@Override
		public void entriesUpdated(Collection<String> addresses) {

		}

		@Override
		public void entriesDeleted(Collection<String> addresses) {

		}

		@Override
		public void presenceChanged(final Presence presence) {

			Intent intent = new Intent();
			intent.setAction("Refresh_Adapter");
			activity.sendBroadcast(intent);
		}

		@Override
		public void done(ParseException parseException, int requestId) {
		}

		@Override
		public void done(List<ParseObject> phoneList, ParseException e,
						 int requestId) {
		}
	}

	/**
	 *  resetRoster()
	 *  Called when user refreshes roster list
	 */
	public void resetRoster() {

		if (NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {

			if(MainService.mService.connection.isConnected() && MainService.mService.connection.isAuthenticated()){
				handler.post(new Runnable() {
					@Override
					public void run() {
						Utility.FragmentContactDataFetchedOnce =false;
						Utility.VcardLoadedOnce = false;
						callWebServiceData();
					}
				});

			}else{
				//Utility.showMessage("Please wait while connection is restored");
			}
		} else {
			Utility.showMessage(getResources().getString(R.string.Network_Availability));
		}
	}

	/**
	 * receive broadcast intents for  incoming chat message
	 */

	class One2OneChatReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, final Intent arg1) {
			try {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					if (Objects.equals(arg1.getAction(), MainService.CHAT)) {
                        displayPingOnList(arg1);
                        if (FragmentContact.this.isVisible()) {
                            displayPingOnList(arg1);
                            mService.setIncomingChatNotification();
                        } else {
                            mService.setIncomingChatNotification();
                        }
                    }
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void onStart() {
		super.onStart();
		addIncommingChatListner();
	}

	private void addIncommingChatListner() {
		try {
			if (MainService.mService.connection.isConnected()) {
				ChatManager chatmanager = ChatManager.getInstanceFor(MainService.mService.connection);
				chatmanager.addChatListener(mService.mIncomingChatManagerListener);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Display star icon for corresponding message
	 */
	private void displayPingOnList(Intent arg1) {

		try {
			String jjj = arg1.getStringExtra("jid");
			if (jjj != null && !jjj.equalsIgnoreCase("")) {
				Log.e("getStringExtra", "getStringExtra");
				setUserAdapter();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Filter roster list with parse.
	 */

	class RostListener implements ParseCallbackListener {
		ArrayList<RosterEntry> rosterEntries = null;

		@Override
		public void done(ParseException parseException, int requestId) {
		}

		@SuppressLint("DefaultLocale")
		@Override
		public void done(List<ParseObject> receipients, ParseException e,int requestId) {
			ArrayList<String> receipients_to_be_rejected = new ArrayList<>();
			rosterEntries = getUSerRosters();

			if (e == null) {
				if (rosterEntries == null)
					rosterEntries = new ArrayList<>();
				ArrayList<RosterEntry> rosterEntries1 = new ArrayList<>();
				for (int i = 0; i < receipients.size(); i++) {
					Resources resources = myApplication.getResources();
					ParseObject object = receipients.get(i);
					if (object.getInt(resources.getString(R.string.column_operation_decider)) != 3){
						// 3 means accepted
						receipients_to_be_rejected.add(object.getString(resources
								.getString(R.string.column_receipient)));
					}
				}
				for (int i = 0; i < rosterEntries.size(); i++) {
					String user = rosterEntries.get(i).getUser();
					user = user.substring(0, user.indexOf("@")).toUpperCase();
					if (!receipients_to_be_rejected.contains(user)) {
						rosterEntries1.add(rosterEntries.get(i));
					}
				}

				rosterEntries = rosterEntries1;
			}
			resetRoster = new ResetRoster(rosterEntries);
			resetRoster.execute();

		}
	}

	/**
	 * Fetch user info from database
	 */
	private void getRosterfromDatabase() {

		try {
			//if (mEdt_Search.isEnabled())
			ThatItApplication.getApplication().openDatabase();
			Cursor cursor = One2OneChatDb.getAllRoster();
			Log.d(TAG, "cursor 111 " + cursor.getCount());
			cursor.moveToFirst();

			jids.clear();
			listcardlastname.clear();
			listcardprofilepic.clear();
			listcardname.clear();

			if (cursor.moveToFirst()) {
				do {
					String jid = cursor.getString(cursor
							.getColumnIndex(DbOpenHelper.JABBER_ID));
					String firstname = cursor.getString(cursor
							.getColumnIndex(DbOpenHelper.FIRSTNAME));
					String lastname = cursor.getString(cursor
							.getColumnIndex(DbOpenHelper.LASTNAME));
					String profilePic = cursor.getString(cursor
							.getColumnIndex(DbOpenHelper.PROFILE_PIC_URL));

					if(TextUtils.isEmpty(firstname)){
						firstname = "My Name";
					}
					if(TextUtils.isEmpty(lastname)){
						lastname = "";
					}
					if(TextUtils.isEmpty(profilePic)){
						profilePic = "";
					}

					jids.add(jid);
					listcardname.add(firstname);
					listcardprofilepic.add(profilePic);
					listcardlastname.add(lastname);
				} while (cursor.moveToNext());
			}
			cursor.close();
			Log.e("Adapter", "" + jids.size());
			Log.e("Adapter", "" + listcardname.size());
			Log.e("Adapter",""+listcardprofilepic.size());
			Log.e("Adapter",""+listcardlastname.size());
			setUserAdapter();
			Log.e("","Thats -> getDatabase");
			Utility.disAllowSubscription = false;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Save user info in shared preference
	 */
	private void setValuesInSharedPrefernce() {

		mSharedPreferences_reg.edit().putString("ProfileImageString", profileImageString).commit();
		mSharedPreferences_reg.edit().putString("PseudoName", psedoName).commit();
		mSharedPreferences_reg.edit().putString("Pseudodescription", profiledescription).commit();
	}

	private void setDataToRespectiveFields() {
		try {
			ContactActivity.options = new DisplayImageOptions.Builder()
					.displayer(new RoundedBitmapDisplayer(200))
					.showImageOnFail(R.drawable.no_img)
					.showImageForEmptyUri(R.drawable.no_img)
					.cacheOnDisk(true)
					.build();
			ContactActivity.loader.displayImage(profileImageString, profile_pic, ContactActivity.options);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			// Set Pseudo Name
			if (psedoName.equals("")) {
				txt_pseudoName.setText("My Name");
			} else {
				txt_pseudoName.setText(psedoName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// Set Pseudo Description
			if (profiledescription.equals("")) {
				txt_desciption.setText("Hi I Am Using That's It");
			} else {
				txt_desciption.setText(profiledescription);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean JidsExist = false;
	private void taskOnCreate() {

		Cursor cursor = One2OneChatDb.getAllRoster();
		cursor.moveToFirst();
		jids.clear();
		listcardlastname.clear();
		listcardprofilepic.clear();
		listcardname.clear();

		if(cursor.getCount()>0){
			JidsExist = true;
			Roster roster = Roster.getInstanceFor(mConnection);
			roster.addRosterListener(myRosterListner);
			do {
				String jid = cursor.getString(cursor
						.getColumnIndex(DbOpenHelper.JABBER_ID));
				String firstname = cursor.getString(cursor
						.getColumnIndex(DbOpenHelper.FIRSTNAME));
				String lastname = cursor.getString(cursor
						.getColumnIndex(DbOpenHelper.LASTNAME));
				String profilePic = cursor.getString(cursor
						.getColumnIndex(DbOpenHelper.PROFILE_PIC_URL));

				if(TextUtils.isEmpty(firstname)){
					firstname = "My Name";
				}
				if(TextUtils.isEmpty(lastname)){
					lastname = "";
				}
				if(TextUtils.isEmpty(profilePic)){
					profilePic = "";
				}
				jids.add(jid);
				listcardname.add(firstname);
				listcardprofilepic.add(profilePic);
				listcardlastname.add(lastname);
			} while (cursor.moveToNext());
		}
		else {
			JidsExist = false;
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setMessage("Loading Contacts...");
			progressDialog.setCancelable(false);
			progressDialog.show();
			fetchRosterFromOpenfire();
		}
		cursor.close();
		if(JidsExist) {
			setUserAdapter();
		}
	}

	/**
	 * Delete roster dialog.
	 */
	private void openRosterDeleteConfirmationDialog() {

		try {
			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					getActivity());
			// set title
			alertDialogBuilder.setTitle("Are You Sure to Remove this Contact");
			alertDialogBuilder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					}).setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					});

			final AlertDialog dialog = alertDialogBuilder.create();
			dialog.show();
			dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							try {
								dialog.dismiss();
								itemLongClickPressed = false;
								new Thread(new Runnable() {
									@Override
									public void run() {
										try {
											try {
												Utility.removeFriendIfExists(entryToBeRemoved.getUser());
												removeContactFromRoster(entryToBeRemoved);
												ThatItApplication.getApplication().getSentInvites().remove(entryToBeRemoved.getUser().toUpperCase());
												ThatItApplication.getApplication().getSentInvites().remove(entryToBeRemoved.getUser().toLowerCase());
												try {
													Utility mUtility = new Utility();
													mUtility.deleteUnsubscribedUserChatHistory(
															getRosterHistoryList,rosterHistoryToBeDeleted,
															entryToBeRemoved.getUser(),mService);
												} catch (Exception e) {
													e.printStackTrace();
												}
												getRosterfromDatabase();
											} catch (Exception e) {
												e.printStackTrace();
											}
										} catch (Exception e) {
											Log.e("tag", e.getMessage());
										}
									}
								}).start();

							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
			dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							try {
								dialog.dismiss();
								itemLongClickPressed = false;
								getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void displayContactSection() {
		if(getResources().getBoolean(R.bool.isTablet)){
			ContactActivity.textView_toolbar_title.setText("Contacts");
		}
		Utility.enteredFragmentOnce = true;
		progressBar_groups.setVisibility(View.GONE);
		mlistView_Contacts.setVisibility(View.VISIBLE);
		lstvwChatGroups.setVisibility(View.GONE);
		imgVwAddNewGroup.setVisibility(View.GONE);
		mEdt_Search.setEnabled(true);
		btnToggleGroups.setBackgroundResource(R.color.custom_grey);
		btnToggleContacts.setBackgroundResource(R.color.custom_red);
		ContactActivity.mBtn_Contact.setBackgroundResource(R.drawable.contact_icon_hover);
		ContactActivity.mBtn_Chat.setBackgroundResource(R.drawable.comment_icon_sm);
		ContactActivity.mBtn_Invite.setBackgroundResource(R.drawable.message_icon_sm);
		ContactActivity.mBtn_Account.setBackgroundResource(R.drawable.user_icon_sm);
		refresh_icon.setVisibility(View.VISIBLE);
		groupPressed = false;
	}

	private void displayGroupSection() {

		try {
			setGroupAdapter();
			if(getResources().getBoolean(R.bool.isTablet)){
				ContactActivity.textView_toolbar_title.setText("Groups");
			}
			Utility.enteredFragmentOnce = false;
			Utility.groupNotificationClicked = false;
			groupPressed = true;
			mEdt_Search.setText("");
			mlistView_Contacts.setVisibility(View.GONE);
			lstvwChatGroups.setVisibility(View.VISIBLE);
			imgVwAddNewGroup.setVisibility(View.VISIBLE);
			mEdt_Search.setEnabled(false);
			btnToggleGroups.setBackgroundResource(R.color.custom_red);
			btnToggleContacts.setBackgroundResource(R.color.custom_grey);
			ContactActivity.mBtn_Contact.setBackgroundResource(R.drawable.contact_icon_sm);
			ContactActivity.mBtn_Chat.setBackgroundResource(R.drawable.comment_icon_hover);
			ContactActivity.mBtn_Invite.setBackgroundResource(R.drawable.message_icon_sm);
			ContactActivity.mBtn_Account.setBackgroundResource(R.drawable.user_icon_sm);
			refresh_icon.setVisibility(View.GONE);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *  DELETE ROSTER FROM OPENFIRE.............................................................................................
	 */
	private void removeContactFromRoster(RosterEntry entryToBeRemoved) {
		try {
			try {
				Roster.getInstanceFor(MainService.mService.connection).removeEntry(entryToBeRemoved);
			} catch (SmackException.NotLoggedInException e) {
				e.printStackTrace();
			} catch (SmackException.NoResponseException e) {
				e.printStackTrace();
			} catch (SmackException.NotConnectedException e) {
				e.printStackTrace();
			}
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}

	public class IncomingReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (intent.getAction().equals("Refresh_User_Adapter")) {
					getRosterfromDatabase();
				}
				if (intent.getAction().equals("Refresh_Adapter")) {
					notifyUserAdapter();
				}
				if (intent.getAction().equals("Group Modified")
						|| intent.getAction().equals("Refresh_Group_Adapter")) {
					setGroupAdapter();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * User group list
	 */
	public void setGroupAdapter() {

		try {
			if (MainService.mService.connection.isConnected() && MainService.mService.connection.isAuthenticated()) {
				areGroupsReady = false;
				groupsEmpty = false;
				Collection<RosterGroup> rGroups = Roster.getInstanceFor(MainService.mService.connection).getGroups();
				list = new ArrayList<>(rGroups);
				if (list != null) {
					if (list.size() == 0) {
						groupsEmpty = true;
						progressBar_groups.setVisibility(View.GONE);
					}
					chatGroupsAdapter = new ChatGroupsAdapter(getActivity(), list);
					handler.post(new Runnable() {
						@Override
						public void run() {
							Log.e("CHAT_ADAPTER", "CHAT_ADAPTER");
							lstvwChatGroups.setAdapter(chatGroupsAdapter);
							Utility.stopDialog();
						}
					});
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void openChatScreen() {

		mFragmentTransaction = mFragmentManager.beginTransaction();
		FragmentChatScreen fragmentChatScreen = new FragmentChatScreen(mService, entry, profilePicDrawable, LastFragment,personFirstName, personLastName);
		mFragmentTransaction.replace(R.id.fragmentContainer, fragmentChatScreen);
		mFragmentTransaction.commit();
		ContactActivity.mBtn_Contact.setBackgroundResource(R.drawable.contact_icon_sm);
		ContactActivity.mBtn_Chat.setBackgroundResource(R.drawable.comment_icon_hover);
		ContactActivity.mBtn_Invite.setBackgroundResource(R.drawable.message_icon_sm);
		ContactActivity.mBtn_Account.setBackgroundResource(R.drawable.user_icon_sm);
	}


	private void notifyUserAdapter(){

		handler.post(new Runnable() {
			@Override
			public void run() {
				try {
					usersAdapter.notifyDataSetChanged();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.refresh_icon:

				//mEdt_Search.setText("");

				if(!NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())){
					Utility.showMessage(getResources().getString(R.string.Network_Availability));
					break;
				}
				if(mConnection.isConnected()
						&& mConnection.isAuthenticated()
						&& NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())){
					try {
						progressDialog = new ProgressDialog(getActivity());
						progressDialog.setMessage("Refreshing Contacts...");
						progressDialog.setCancelable(false);
						progressDialog.show();

						Utility.VcardLoadedOnce = false;
					} catch (Exception e) {
						e.printStackTrace();
					}
					fetchRosterFromOpenfire();
				}
				else{
					if(!NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())){
						Utility.showMessage(getResources().getString(R.string.Network_Availability));
					}
					else if(!(mConnection.isConnected()|| mConnection.isAuthenticated())){
						MainService.mService.connectAsync();
						//Utility.showMessage("Please wait while connection is restored");
					}
				}
				break;
		}
	}

	/**
	 * Enter chat password before going to chat screen or display room list
	 */
	private void openChatPasswordDialog(final Activity activity) {

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

			InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);

			btnAccept.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);

						try {
							if (NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {

								if (!mConnection.isConnected() || !mConnection.isAuthenticated()) {

									//Utility.showMessage("Please wait while connection is restored");
									dialogOpen = false;
									dialogChatPassword.dismiss();
									groupPressed = false;
									return;
								}

								Chatpassword = encryptionManager.encryptPayload(etPassword.getText().toString().trim());
								Chatpassword = URLEncoder.encode(Chatpassword, "UTF-8");
								if (Chatpassword.contains("%")) {
									Chatpassword = Chatpassword.replace("%", "2");
								}

								if (etPassword.getText().toString().trim().toCharArray().length == 0) {
									Toast.makeText(getActivity(), "Enter Chat Password", Toast.LENGTH_SHORT).show();
								} else {
									try {
										dialogChatPassword.dismiss();
										dialogOpen = false;

										if (!TextUtils.isEmpty(Utility.getChatPassword())) {
											if (Chatpassword.equals(Utility.getChatPassword())) {

												if (TextUtils.isEmpty(entryWithoutHost)) {
													if (ACTION == 1) {
														// OPEN CHAT SCREEN
														dialogOpen = false;
														openChatScreen();
													} else if (ACTION == 2) {
														// DISPLAY GROUPS
														displayGroupSection();
													}
												} else {
													if (Utility.googleServicesUnavailable) {
														new Validate_ThatsItId_Async(getActivity(), entryWithoutHost, mValidateThatsItIdInterface).execute();
													} else {
														if (ACTION == 1) {
															// OPEN CHAT SCREEN
															dialogOpen = false;
															openChatScreen();
														} else if (ACTION == 2) {
															// DISPLAY GROUPS
															displayGroupSection();
															Utility.stopDialog();
														}
													}

												}
											} else {
												if (ACTION == 1) {
													dialogOpen = false;
												}
												groupPressed = false;
												Toast.makeText(getActivity(), "Incorrect Password", Toast.LENGTH_SHORT).show();
											}
										} else {
											new CheckChatPasswordAsync(getActivity(), Chatpassword, mCheckChatPasswordInterface).execute();
										}

									} catch (Exception e) {
										e.printStackTrace();
										Utility.stopDialog();
									}
								}
							} else {
								Toast.makeText(getActivity(), getResources().getString(R.string.Network_Availability), Toast.LENGTH_SHORT).show();
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

						InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);
						dialogOpen = false;
						dialogChatPassword.dismiss();
						groupPressed = false;
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
	 * Check Chat password - Correct - Procced else display toast message
	 */
	private final CheckChatPasswordInterface mCheckChatPasswordInterface = new CheckChatPasswordInterface() {

		@Override
		public void checkChatPassword(CheckMessage_ChatPasswrd chat_password) {

			try {
				RetriveVal_Chat_pass = chat_password.getmCheckMessagePasswordResult()
						.getmCheckMessagePasswordParams()[0].getRetVal();

				if (RetriveVal_Chat_pass != null){

					if (RetriveVal_Chat_pass.equals("1")) {

						hostActivity.storeChatPasswordInSharedPreference(Chatpassword);

						if(Utility.googleServicesUnavailable){
							if (TextUtils.isEmpty(entryWithoutHost)) {
								if (ACTION == 1) {
									// OPEN CHAT SCREEN
									dialogOpen = false;
									openChatScreen();
									Utility.stopDialog();
								} else if (ACTION == 2) {
									// DISPLAY GROUPS
									displayGroupSection();
									Utility.stopDialog();
								}
							} else {
								// Check if roster entry exists on Admin
								if (Utility.googleServicesUnavailable) {
									new Validate_ThatsItId_Async(getActivity(), entryWithoutHost, mValidateThatsItIdInterface).execute();
								}
							}
						}else{
							if (ACTION == 1) {
								// OPEN CHAT SCREEN
								dialogOpen = false;
								openChatScreen();
								Utility.stopDialog();
							} else if (ACTION == 2) {
								// DISPLAY GROUPS
								displayGroupSection();
								Utility.stopDialog();
							}
						}
					} else {
						if (ACTION == 1) {
							dialogOpen = false;
						}
						groupPressed = false;
						Toast.makeText(getActivity(), "Incorrect Password", Toast.LENGTH_SHORT).show();
					}
				}else{
					groupPressed = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				groupPressed = false;
				Utility.stopDialog();
			}
		}
	};

	/**
	 * Get user registration info
	 */
	private class GetDataAsynRegistration extends AsyncTask<Void, Void, FetchUserSettingTemplates> {

		@Override
		protected FetchUserSettingTemplates doInBackground(Void... arg0) {
			try {
				Log.e("Fragment", "FetchUserSettingTemplates");
				FetchUserSettingTemplates result = new WebServiceClient(
						getActivity()).getRegistrationdetails(AppSinglton.userId);

				profileImageString = result.getmFetchUserSettingsResult().getmFetchUserSettingsParams()[0].getProfileImage().toString();
				psedoName = result.getmFetchUserSettingsResult().getmFetchUserSettingsParams()[0].getPseudoName().toString();
				profiledescription = result.getmFetchUserSettingsResult().getmFetchUserSettingsParams()[0].getProfileDescription().toString();

				// Set Values In Shared Preference
				setValuesInSharedPrefernce();

				return result;
			} catch (Exception e) {
				Utility.stopDialog();
				return null;
			}
		}

		@Override
		protected void onPostExecute(FetchUserSettingTemplates result) {
			try {
				super.onPostExecute(result);
				setDataToRespectiveFields();
				Utility.FragmentContactDataFetchedOnce = true;
			} catch (Exception e) {
				//Utility.stopDialog();
				e.printStackTrace();
			}
		}
	}


	/**
	 * Connection error alert.
	 */

	private void showConnectionErrorAlert() {

		new CountDownTimer(10000, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
				//this will be done every 1000 milliseconds ( 1 seconds )
			}
			@Override
			public void onFinish() {
				ProgressBarStatus("Stop");
				if (!(MainService.connection.isConnected() || MainService.connection.isAuthenticated())) {
					ProgressBarStatus("Start");
//					Utility.showMessage("Reconnecting to server...");
					showConnectionErrorAlert();
					//There must be an error reconnection
				} else {
					ProgressBarStatus("Stop");
					Roster roster = Roster.getInstanceFor(MainService.connection);
					roster.addRosterListener(myRosterListner);
					setUserAdapter();
				}
			}
		}.start();
	}


	private void ProgressBarStatus(final String message) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (message.equalsIgnoreCase("Start")) {
					progressBar.setVisibility(View.VISIBLE);
				} else {
					progressBar.setVisibility(View.GONE);
				}

			}
		});
	}

	/**
	 *  Check if roster entry exists on Admin
	 */

	private final ValidateThatsItIdInterface mValidateThatsItIdInterface = new ValidateThatsItIdInterface() {
		@Override
		public void validateThatsItId(ValidateThatsItID mValidateThatsItID) {

			if(mValidateThatsItID != null){

				String status = mValidateThatsItID.getValidateUserPincodeResult().getStatus().getStatus();
				if(status.equalsIgnoreCase("1")){

					// Pincode is existing on Admin
					if(ACTION == 1){
						// OPEN CHAT SCREEN
						dialogOpen = false;
						openChatScreen();
						//Utility.stopDialog();
					}
					else if(ACTION == 2){
						// DISPLAY GROUPS
						displayGroupSection();
						Utility.stopDialog();
					}
				}else{
					// open remove roster alert
					//openAlert("This contact has been disabled by Admin. Do you wish to remove it from your contact list?");
					openAlert("This contact has been disabled by Admin and will be removed from your contact list");
				}
			}else{
				Utility.stopDialog();
			}
		}
	};

	private void openAlert(final String Message) {

		handler.post(new Runnable() {

			@Override
			public void run() {

				try {
					if (alertDialog == null) {
						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
						alertDialogBuilder.setTitle("That's It");
						alertDialogBuilder.setMessage(Message);

						alertDialogBuilder.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0, int arg1) {

										alertDialog.dismiss();
										alertDialog = null;

										new Thread(new Runnable() {
											@Override
											public void run() {
												try {
													Utility.removeFriendIfExists(entry.getUser());
													removeContactFromRoster(entry);
													ThatItApplication.getApplication().getSentInvites().remove(entry.getUser().toUpperCase());
													ThatItApplication.getApplication().getSentInvites().remove(entry.getUser().toLowerCase());
													try {
														Utility mUtility = new Utility();
														mUtility.deleteUnsubscribedUserChatHistory(
																getRosterHistoryList,rosterHistoryToBeDeleted,
																entry.getUser(),mService);
													} catch (Exception e) {
														e.printStackTrace();
													}
													getRosterfromDatabase();
												} catch (Exception e) {
													e.printStackTrace();
												}
											}
										}).start();
									}
								});

						alertDialog = alertDialogBuilder.create();
						alertDialog.show();
						alertDialog.setCancelable(false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		AppSinglton.currentGroupName = null;
	}
}
