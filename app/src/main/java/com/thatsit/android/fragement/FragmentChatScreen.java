package com.thatsit.android.fragement;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.seasia.myquick.asyncTasks.Validate_ThatsItId_Async;
import com.seasia.myquick.model.ValidateThatsItID;
import com.thatsit.android.FileDispatcherAsync;
import com.thatsit.android.FileReceiveraAsync_;
import com.thatsit.android.MainService;
import com.thatsit.android.MainService.MyMessageListner;
import com.thatsit.android.R;
import com.thatsit.android.RefreshApplicationListener;
import com.thatsit.android.UpdateFTPStatus;
import com.thatsit.android.Utility;
import com.thatsit.android.Utils;
import com.thatsit.android.activities.ContactActivity;
import com.thatsit.android.activities.GridViewActivity;
import com.thatsit.android.activities.MUCActivity;
import com.thatsit.android.activities.SuggestContactActivity;
import com.thatsit.android.adapter.DiscussArrayAdapter;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.beans.OneBubble;
import com.thatsit.android.db.DbOpenHelper;
import com.thatsit.android.db.One2OneChatDb;
import com.thatsit.android.encryption.helper.EncryptionManager;
import com.thatsit.android.interfaces.ValidateThatsItIdInterface;
import com.thatsit.android.xmpputils.Constants;
import com.thatsit.android.xmpputils.XmppManager;
import com.myquick.filebrowser.AndroidExplorer;
import com.myquick.socket.ClientChatThread;
import com.myquick.socket.ServerThread;
import com.myquickapp.receivers.ConnectionBroadcastReceiver;
import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.seasia.myquick.model.AppSinglton;

/**
 * in this fragment, one2one chat is performed with the roster entry.
 */
public class FragmentChatScreen extends Fragment implements OnClickListener,RefreshApplicationListener {
	private static final String TAG = FragmentChatScreen.class.getSimpleName();
	private ImageView mImageVw_ChatScreen,fragChat_img_smiley;
	private RelativeLayout fragChat_btn_MsgSend_rel;
	public  ListView mListView_Chat;
	private TextView fragChat_txt_UserName,fragChat_txt_profileDescription;
	private View mView;
	private FragmentManager mFragmentManager;
	private FragmentTransaction mFragmentTransaction;
	private FragmentChatHistoryScreen mFragmentChatHistory;
	public static EditText mEdtTxtChat;
	private Uri selectedImageUriFromGallery,selectedVideoUriFromGallery;
	private String picturePath,videoPath;
	private String LastID;
	private final StringBuilder copyValue = new StringBuilder();
	private static final ConnectionBroadcastReceiver connectionBroadcastReceiver = new ConnectionBroadcastReceiver();
	private final One2OneChatReceiver one2OneChatReceiver = new One2OneChatReceiver();
	private static final Intent SERVICE_INTENT = new Intent();
	private final Handler mHandler = new Handler();
	private XmppManager mXmppManager;
	private XMPPConnection mConnection;
	private ThatItApplication myApplication;
	private MainService mService;
	private RosterEntry mRosterEntry;
	private One2OneChatDb one2OneChatDb;
	private ContactActivity hostActivity;
	public static DiscussArrayAdapter adapter;
	private RelativeLayout rel_overflowImage,fragChat_clipboard;
	private final ArrayList<Integer> ListItem_position_new = new ArrayList<>();
	private final ArrayList<String> ListItem_position_all = new ArrayList<>();
	private final ArrayList<Integer> ListItem_position_deletes = new ArrayList<>();
	private ImageView /*img_copy,img_cut,*/img_accept;
	private TextView img_delete;
	public RelativeLayout activityRootView;
	private ClipData clip;
	private View View_Color;
	private String chatRecipientPhoto;
	public static byte[] byteArray;
	private String msg;
	private ProgressBar mprgBarFtpStatus;
	private TextView mTxtVwPercentageCompletion;
	private final int FILE_SELECT_CODE = 13;
	private final int PHOTO_SELECT_CODE=14;
	private final int VIDEO_SELECT_CODE=15;
	public static ProgressDialog progressDialog;
	public static String messageStatus;
	private String personFirstName,personLastName;
	private static String subject;
	private static String subject_socket;
	private EncryptionManager encryptionManager;
	private NotificationManager notificationManager = null;
	private Handler handler;
	private Presence presence;
	private AlertDialog alertDialog;
	private Button btn_loadMore;
	private int itemSize = 20;
	private Cursor cursor;
	private boolean onItemLongClickCalled;

	static {
		SERVICE_INTENT.setComponent(new ComponentName(Constants.MAINSERVICE_PACKAGE,  Constants.MAINSERVICE_PACKAGE + Constants.MAINSERVICE_NAME ));
	}

	public FragmentChatScreen(MainService mainService, RosterEntry entry, String profilePicDrawable,boolean lastFragment,
							  String personFirstName,String personLastName){
		try {
			mService = mainService;
			mRosterEntry = entry;
			myApplication = ThatItApplication.getApplication();
			mXmppManager = XmppManager.getInstance();
			mConnection = mXmppManager.getXMPPConnection();
			this.chatRecipientPhoto=profilePicDrawable;
			this.personFirstName=personFirstName;
			this.personLastName=personLastName;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utility.fragmentChatScreen = FragmentChatScreen.this;
		handler = new Handler();
		Utility.fragChatIsOpen = true;
		Utility.smileyScreenOpened = false;
		one2OneChatDb = new One2OneChatDb(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		try {
			Utility.fragmentChatScreen = FragmentChatScreen.this;
			setAdapter();
			NotificationManager notificationManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(MainService.NOTIFICATION_MESSAGE_RECEIVED);
			ThatItApplication.getApplication().getIncomingPings().remove(mRosterEntry.getUser());
			ThatItApplication.getApplication().getIncomingFilePings().remove(mRosterEntry.getUser());
			Utility.stopDialog();

			if(mRosterEntry !=null && mRosterEntry.getUser().contains("@") && Utility.googleServicesUnavailable == true){
				String entryWithoutHost = mRosterEntry.getUser().replace("@190.97.163.145","");
				new Validate_ThatsItId_Async(getActivity(),entryWithoutHost,mValidateThatsItIdInterface).execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public FragmentChatScreen() {
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		hostActivity = (ContactActivity) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		mView= inflater.inflate(R.layout.fragment_chat_screen, container, false);

		encryptionManager = new EncryptionManager();

		if(getResources().getBoolean(R.bool.isTablet)){
			ContactActivity.textView_toolbar_title.setText("Chats");
		}
		ContactActivity.tabs_lnrlayout.setVisibility(View.GONE);

		setWindowType();
		initialiseNotificationManager();
		initialise_Variable();
		initialise_Listeners();
		setUserInformation();
		startServerThreadForSocketFileTransfer();

		return mView;
	}

	/**
	 * Start thread for file transfer via socket.
	 */
	private void startServerThreadForSocketFileTransfer() {

		ServerThread serverThread = new ServerThread(getActivity());
		new Thread(serverThread).start();
	}

	private void initialiseNotificationManager() {

		notificationManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(MainService.NOTIFICATION_MESSAGE_RECEIVED);
	}

	private void setUserInformation() {

		try {
			if(personFirstName.equals("")){
				fragChat_txt_UserName.setText("My Name");
			}
			else {
				fragChat_txt_UserName.setText(personFirstName);
			}
			if (personLastName.equals("")){
				fragChat_txt_profileDescription.setText("Hi I Am Using That's It");
			}
			else{
				fragChat_txt_profileDescription.setText(personLastName);
			}
			if(chatRecipientPhoto == null){
				ContactActivity.options = new DisplayImageOptions.Builder()
						.displayer(new RoundedBitmapDisplayer(200))
								//.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
						.showImageOnFail(R.drawable.no_img)
						.showImageForEmptyUri(R.drawable.no_img)
						.cacheOnDisk(true)
						.cacheInMemory(true)
						.build();
				ContactActivity.loader.displayImage("", mImageVw_ChatScreen, ContactActivity.options);
				//mImageVw_ChatScreen.setImageResource(R.drawable.no_img);
			}else{

				ContactActivity.options = new DisplayImageOptions.Builder()
						.displayer(new RoundedBitmapDisplayer(200))
								//.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
						.showImageOnFail(R.drawable.no_img)
						.showImageForEmptyUri(R.drawable.no_img)
						.cacheOnDisk(true)
						.cacheInMemory(true)
						.build();
				ContactActivity.loader.displayImage(chatRecipientPhoto, mImageVw_ChatScreen, ContactActivity.options);
				//mImageVw_ChatScreen.setImageDrawable(chatRecipientPhoto);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setWindowType() {

		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
						| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if(mRosterEntry!=null ){
			ThatItApplication.getApplication().getIncomingPings().remove(mRosterEntry.getUser());
			ThatItApplication.getApplication().getIncomingFilePings().remove(mRosterEntry.getUser());
		}
	}

	public void setAdapter(){
		try {
			adapter  = new DiscussArrayAdapter(hostActivity,chatRecipientPhoto,mListView_Chat);
			mListView_Chat.setAdapter(adapter);  //Set empty adapter

			showPreviousChat(itemSize);
			NotificationManager notificationManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(MainService.NOTIFICATION_MESSAGE_RECEIVED);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get entire chat for jID from database.
	 */
	private void showPreviousChat(int minusVal) {
		try {
			ThatItApplication.getApplication().openDatabase();
			cursor = One2OneChatDb.getAllMessagesOfParticipant(mRosterEntry.getUser());
			DatabaseUtils.dumpCursor(cursor);
			if(cursor.getCount() < 20){
				showAllValues();
				btn_loadMore.setVisibility(View.GONE);
			}
			else {
				btn_loadMore.setVisibility(View.VISIBLE);
				showRequiredValues(minusVal);
			}
			if (LastID==null) {
				LastID="0";
			}
			cursor.close();

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	private void showRequiredValues(int minusVal) {
		int startPoint;
		startPoint = cursor.getCount() - minusVal;
		if (startPoint <0) {
			startPoint = 0;
			btn_loadMore.setVisibility(View.GONE);
		}
		if (cursor.moveToPosition(startPoint)) {
			int position = 0;
			//do{
			for (int i = startPoint; i < cursor.getCount(); i++) {
				String id = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_ID));
				LastID = id;
				String msg = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_MESSAGE));
				String userType = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_OWNER_OR_PARTICIPANT));
				String time = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_TIMESTAMP));
				String msgStatus = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_MESSAGE_STATUS));
				String msgSubject = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_MESSAGE_SUBJECT));
				boolean isOwner = userType.equalsIgnoreCase(DbOpenHelper.USER_TYPE_OWNER) ? true : false;
				adapter.add(new OneBubble(isOwner, id, msg, msgSubject, false, Long.valueOf(time), personFirstName, msgStatus));
				adapter.notifyDataSetChanged();
				position = position + 1;
				cursor.moveToNext();
			}
			//}while(cursor.moveToNext());
		}
	}
	private void showAllValues(){
		if (cursor.moveToFirst()) {
			int position = 0;
			do {
				String id = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_ID));
				LastID = id;
				String msg = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_MESSAGE));
				String userType = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_OWNER_OR_PARTICIPANT));
				String time = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_TIMESTAMP));
				String msgStatus = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_MESSAGE_STATUS));
				String msgSubject = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_MESSAGE_SUBJECT));
				boolean isOwner = userType.equalsIgnoreCase(DbOpenHelper.USER_TYPE_OWNER) ? true : false;
				adapter.add(new OneBubble(isOwner, id, msg, msgSubject, false, Long.valueOf(time), personFirstName, msgStatus));
				adapter.notifyDataSetChanged();
				position = position + 1;
			} while (cursor.moveToNext());
		}
	}
	@Override
	public void onStart() {
		super.onStart();
		try {
			IntentFilter filter = new IntentFilter();
			filter.addAction(MainService.CONNECTION_CLOSED);
			hostActivity.registerReceiver(connectionBroadcastReceiver, filter);
			hostActivity.registerReceiver(one2OneChatReceiver, new IntentFilter(MainService.CHAT));

			addIncommingChatListner();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		try {
			hostActivity.unregisterReceiver(connectionBroadcastReceiver);
			hostActivity.unregisterReceiver(one2OneChatReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialse class variables.
	 */
	private void initialise_Variable() {

		mListView_Chat = (ListView)mView.findViewById(R.id.fragChat_list_contacts);
		mEdtTxtChat = (EditText)mView.findViewById(R.id.fragChat_edt_enterMessage);
		mFragmentChatHistory = new FragmentChatHistoryScreen(mService);
		mImageVw_ChatScreen =(ImageView) mView.findViewById(R.id.fragChat_User_profilepic);
		fragChat_txt_UserName=(TextView)mView.findViewById(R.id.fragChat_txt_UserName);
		fragChat_txt_profileDescription=(TextView)mView.findViewById(R.id.fragChat_txt_UserName2);
		fragChat_img_smiley=(ImageView) mView.findViewById(R.id.fragChat_img_smiley);
		rel_overflowImage=(RelativeLayout) mView.findViewById(R.id.fragChat_rltv_img2);
		fragChat_btn_MsgSend_rel=(RelativeLayout)mView.findViewById(R.id.fragChat_btn_MsgSend_rel);
		fragChat_clipboard=(RelativeLayout) mView.findViewById(R.id.fragChat_clipboard);
		//img_copy=(ImageView) mView.findViewById(R.id.img_copy);
		mprgBarFtpStatus=(ProgressBar)mView.findViewById(R.id.prgBarFtpProgress);
		mTxtVwPercentageCompletion=(TextView)mView.findViewById(R.id.txtvwPercentageFeild);
		img_delete=(TextView) mView.findViewById(R.id.img_delete);
		//img_cut=(ImageView) mView.findViewById(R.id.img_cut);
		img_accept=(ImageView) mView.findViewById(R.id.img_tick);
		activityRootView =(RelativeLayout)mView.findViewById(R.id.RelativeLayout1);
		btn_loadMore = (Button)mView.findViewById(R.id.btn_loadMore);
	}

	/**
	 * Send message to jID.
	 */
	protected void sendChatMessage() {
		try {
			final String to = mRosterEntry.getUser();
			msg = mEdtTxtChat.getText().toString().trim();
			try {
				presence = MainService.mService.connection.getRoster().getPresence(to);

				if (!presence.getType().equals(Presence.Type.unavailable)) {
					messageStatus = "R";

					new Thread(new Runnable() {
						@Override
						public void run() {

							one2OneChatDb.updateMessagestatus(to, "R");
						}
					}).start();

				} else {
					messageStatus = "D";
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			if(LastID==null)LastID="";
			if (!LastID.equals("") && !LastID.equals("0")) {
				int updateId = Integer.parseInt(LastID);
				updateId = updateId+1;
				LastID=""+updateId;
				adapter.add(new OneBubble(true, ""+updateId, msg,null, false, System
						.currentTimeMillis(), mConnection.getUser(),messageStatus));

			}else{
				adapter.add(new OneBubble(true, ""+1, msg,null, false, System
						.currentTimeMillis(),mConnection.getUser(),messageStatus));
				LastID = "1";
			}
			mEdtTxtChat.setText("");
			new Thread(new Runnable() {

				@Override
				public void run() {
					String revisedMessage = encryptionManager.encryptPayload(Utility.processSmileyCodes(msg));

					sendMessage(to, revisedMessage, mService.mMessageListner);

					if(TextUtils.isEmpty(chatRecipientPhoto)){
						byteArray = getBitmapFromImageview();
					}else {
						byteArray = chatRecipientPhoto.getBytes();
					}
					saveOwnerChat(mRosterEntry.getUser(), mRosterEntry.getName(), revisedMessage,null,byteArray,messageStatus);

				}
			}).start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private byte[] getBitmapFromImageview() {
		Bitmap bitmap = ((BitmapDrawable)mImageVw_ChatScreen.getDrawable()).getBitmap();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] image_bytes = baos.toByteArray();
		try {
			baos.flush();
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Base 64 to convert image to string to send to the server
		return image_bytes;
	}

	/**
	 * Send message with file to jID.
	 */
	public void sendMessageWithFileSubject(String fileName){

		try {
			String to = mRosterEntry.getUser();
			msg = "I have sent you a file :"+fileName;
			String msgToBeSent = "I have sent you a file: "+fileName+" . Tap to download.";
			String revisedMsgToBeSaved = encryptionManager.encryptPayload(Utility.processSmileyCodes(msg));
			String revisedMsgToBeSent = encryptionManager.encryptPayload(Utility.processSmileyCodes(msgToBeSent));

			if(TextUtils.isEmpty(chatRecipientPhoto)){
				byteArray = getBitmapFromImageview();
			}else {
				byteArray = chatRecipientPhoto.getBytes();
			}
			if (mConnection.isConnected()) {
				sendMessageWithFile(to, revisedMsgToBeSent, mService.mMessageListner, fileName);

				try {
					Presence presence = mConnection.getRoster().getPresence(to);

					if (!presence.getType().equals(Presence.Type.unavailable)) {
						messageStatus = "R";
						one2OneChatDb = new One2OneChatDb(ThatItApplication.getApplication());
						one2OneChatDb.updateMessagestatus(to, "R");
					} else {
						messageStatus = "D";
					}
				} catch(Exception e) {
					e.printStackTrace();
				}

				if(LastID==null)LastID="";
				if (!LastID.equals("") && !LastID.equals("0")) {
					int updateId = Integer.parseInt(LastID);
					updateId = updateId+1;
					LastID=""+updateId;
					adapter.add(new OneBubble(true, ""+updateId, msg,null, false, System
							.currentTimeMillis(), mConnection.getUser(),messageStatus));

				}else{
					adapter.add(new OneBubble(true, ""+1, msg,null, false, System
							.currentTimeMillis(), mConnection.getUser(),messageStatus));
					LastID = "1";
				}
				mEdtTxtChat.setText("");

				saveOwnerChat(mRosterEntry.getUser(), mRosterEntry.getName(), revisedMsgToBeSaved,null,byteArray,messageStatus);

			}else {
				Toast.makeText(myApplication, "Connection Error!", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send message with file to jID.
	 */
	public  void sendMessageWithFileSubjectViaSocket(String fileName, long id){

		try {
			String to = mRosterEntry.getUser();
			msg = "I have sent you a file :"+fileName;
			String msgToBeSent = "I have sent you a file: "+fileName+" . Tap to download.";
			String revisedMsgToBeSaved = encryptionManager.encryptPayload(Utility.processSmileyCodes(msg));
			String revisedMsgToBeSent = encryptionManager.encryptPayload(Utility.processSmileyCodes(msgToBeSent));

			if(TextUtils.isEmpty(chatRecipientPhoto)){
				byteArray = getBitmapFromImageview();
			}else {
				byteArray = chatRecipientPhoto.getBytes();
			}
			if (mConnection.isConnected()) {
				sendMessageWithFileViaSocket(to, revisedMsgToBeSent, mService.mMessageListner, fileName, id);

				try {
					Presence presence = mConnection.getRoster().getPresence(to);

					if (!presence.getType().equals(Presence.Type.unavailable)) {
						messageStatus = "R";
						one2OneChatDb = new One2OneChatDb(ThatItApplication.getApplication());
						one2OneChatDb.updateMessagestatus(to, "R");
					} else {
						messageStatus = "D";
					}
				} catch(Exception e) {
					e.printStackTrace();
				}

				if(LastID==null)LastID="";
				if (!LastID.equals("") && !LastID.equals("0")) {
					int updateId = Integer.parseInt(LastID);
					updateId = updateId+1;
					LastID=""+updateId;
					adapter.add(new OneBubble(true, ""+updateId, msg,null, false, System
							.currentTimeMillis(), mConnection.getUser(),messageStatus));

				}else{
					adapter.add(new OneBubble(true, ""+1, msg,null, false, System
							.currentTimeMillis(), mConnection.getUser(),messageStatus));
					LastID = "1";
				}
				mEdtTxtChat.setText("");

				saveOwnerChat(mRosterEntry.getUser(), mRosterEntry.getName(), revisedMsgToBeSaved,null,byteArray,messageStatus);
			}else {
				Toast.makeText(myApplication, "Connection Error!", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Note: jid is participant jid  It's not owner jid because we want to save owner chat and his
	 * participant chat into participant id
	 * */
	void saveOwnerChat(String jid, String name, String msg,String subject, byte[] Images,String messageStatus){
		try {
			one2OneChatDb = new One2OneChatDb(ThatItApplication.getApplication());
			One2OneChatDb.addMessage(jid, name, msg,subject,byteArray, DbOpenHelper.USER_TYPE_OWNER,messageStatus);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressLint("NewApi")
	private void initialise_Listeners() {

		fragChat_btn_MsgSend_rel.setOnClickListener(this);
		fragChat_img_smiley.setOnClickListener(this);
		img_accept.setOnClickListener(this);
		//img_copy.setOnClickListener(this);
		img_delete.setOnClickListener(this);
		//img_cut.setOnClickListener(this);
		rel_overflowImage.setOnClickListener(this);
		btn_loadMore.setOnClickListener(this);

		mListView_Chat.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
									int position, long id) {

				//onItemClickCalled = true;
				//onItemLongClickCalled = false;

				if (!onItemLongClickCalled) {
					if (!(v.getTag(R.string.msg_subject_key)).equals("")) {
						subject = (String) v.getTag(R.string.msg_subject_key);

						JSONObject json; /*File.separator /*/
						try {
							json = new JSONObject(subject);
							if (json.has("file_name")) {

								if (!com.myquick.socket.Constants.isDownloading) {
									subject_socket = subject;
									showSocketFileDownloadPrompt(json.getString("file_name"), json.getString("ip"), json.getString("file_path"));
								} else {
									Toast.makeText(getActivity(), "Please Wait while Current file is downloaded.", Toast.LENGTH_SHORT).show();
									;
								}
							} else {
								showFTPFileDownloadPrompt(subject);
							}
						} catch (JSONException e) {
							showFTPFileDownloadPrompt(subject);
							e.printStackTrace();
						}
						adapter.notifyDataSetChanged();
					}
				}
			}
		});

		mListView_Chat.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position,
										   long arg3) {
				try {
					onItemLongClickCalled = true;

					if(onItemLongClickCalled) {
						OneBubble oneBubble = (OneBubble) arg0.getAdapter().getItem(position);
						fragChat_clipboard.setVisibility(View.VISIBLE);
						ListItem_position_new.add(Integer.valueOf(oneBubble.id));
						OneBubble bubble = (OneBubble) mListView_Chat.getAdapter().getItem(position);

						copyValue.append(new EncryptionManager().decryptPayload(bubble.message));

						arg1.setBackgroundColor(Color.parseColor("#002E3E"));
						View_Color = arg1;
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				return false;
			}
		});
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {

			case R.id.fragChat_btn_MsgSend_rel:

				if(NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())){

					if(MainService.mService.connection.isConnected()
							&& MainService.mService.connection.isAuthenticated()){

						if(mEdtTxtChat.getText().toString().trim().equals("")){
							Toast.makeText(getActivity(), "No Message To Send", Toast.LENGTH_SHORT).show();
							return;
						}
						else{
							try {
								if(mConnection.getRoster().contains(mRosterEntry.getUser())){
									onItemLongClickCalled = false;
									sendChatMessage();
								}
								else{
									Toast.makeText(getActivity(), "User does not exists in your list", Toast.LENGTH_SHORT).show();
									return;
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						try {
							adapter.notifyDataSetChanged();
							fragChat_clipboard.setVisibility(View.GONE);
							View_Color.setBackgroundColor(Color.TRANSPARENT);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}else{
						Utility.showMessage("Please wait while connection gets restored");
					}
				}
				else{
					Utility.showMessage(getResources().getString(R.string.Network_Availability));
				}
				break;

			case R.id.fragChat_img_smiley:

				onItemLongClickCalled = false;
				if(Utility.smileyScreenOpened == false){
					Utility.smileyScreenOpened = true;
					MUCActivity.chek_Activity = false;
					Intent intent = new Intent(getActivity(),GridViewActivity.class);
					startActivity(intent);
				}
				break;

			case R.id.img_tick:

				try {
					onItemLongClickCalled = false;
					setAdapter();
					fragChat_clipboard.setVisibility(View.GONE);
					View_Color.setBackgroundColor(Color.TRANSPARENT);
					ListItem_position_new.clear();
				} catch (Exception e) {
					e.printStackTrace();
				}

				break;

			case R.id.img_delete:

				try {
					onItemLongClickCalled = false;
					fragChat_clipboard.setVisibility(View.GONE);
					View_Color.setBackgroundColor(Color.TRANSPARENT);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				try {
					boolean isDeleted = true;
					copyValue.setLength(0);

					One2OneChatDb chatDb = new One2OneChatDb(getActivity());
					Log.d("coulmn_id", "coulmn_id   " + ListItem_position_new);
					for (int i = 0; i < ListItem_position_new.size(); i++) {
						isDeleted=chatDb.deleteMessage(ListItem_position_new.get(i));
					}
					if (isDeleted == true) {
						LastID="0";
						ListItem_position_new.clear();
						setAdapter();
					}
					else{
						ListItem_position_new.clear();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case R.id.fragChat_rltv_img2:

				try {
					selectOverflowOption();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case R.id.btn_loadMore:
				adapter  = new DiscussArrayAdapter(hostActivity,chatRecipientPhoto,mListView_Chat);
				mListView_Chat.setAdapter(adapter);  //Set empty adapter
				itemSize = itemSize + 20;
				showPreviousChat(itemSize);
				break;

		}
	}

	void addIncommingChatListner(){
		try {
			if (mConnection.isConnected()) {
				ChatManager chatmanager = mConnection.getChatManager();
				chatmanager.addChatListener(mService.mIncomingChatManagerListener);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void sendMessage(String to, String msg, MyMessageListner mMessageListner) {

		if (!mConnection.isConnected())   return;
		ChatManager chatmanager = mConnection.getChatManager();
		Chat newChat = chatmanager.createChat(to, mMessageListner);
		try {
			Message newMessage = new Message();
			newMessage.setBody(msg);
			newChat.sendMessage(newMessage);
		} catch (XMPPException e) {
			Log.d(TAG, "Error Delivering block");
		}
	}

	public void sendMessageWithFile(String to,String revisedMsgToBeSent, MyMessageListner mMessageListner,String fileName) {

		if (!mConnection.isConnected())   return;
		ChatManager chatmanager = mConnection.getChatManager();
		Chat newChat = chatmanager.createChat(to, mMessageListner);
		try {
			Message newMessage = new Message();
			newMessage.setBody(revisedMsgToBeSent);
			newMessage.addSubject("isFile", fileName);
			newChat.sendMessage(newMessage);
		} catch (XMPPException e) {
			Log.d(TAG, "Error Delivering block");
		}
	}

	public void sendMessageWithFileViaSocket(String to,String revisedMsgToBeSent, MyMessageListner mMessageListner,String fileName, long id) {

		if (!mConnection.isConnected())   return;
		ChatManager chatmanager = mConnection.getChatManager();
		Chat newChat = chatmanager.createChat(to, mMessageListner);
		try {
			Message newMessage = new Message();
			newMessage.setBody(revisedMsgToBeSent);

			String URI = com.myquick.socket.Constants.File_URI.getPath();

			JSONObject com = new JSONObject();
			try {
				com.put("file_name", fileName);
				com.put("id", id);
				com.put("ip", Utils.getLocalIpv4Address());
				com.put("file_path", URI);

				String message = com.toString();

				newMessage.addSubject("isFile", message);
				newChat.sendMessage(newMessage);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		} catch (XMPPException e) {
			Log.d(TAG, "Error Delivering block");
		}
	}

	/**
	 * Broadcast receiver to receive incoming message.
	 */
	class One2OneChatReceiver extends BroadcastReceiver{
		@TargetApi(Build.VERSION_CODES.KITKAT)
		@Override
		public void onReceive(Context arg0, final Intent arg1) {
			if(Objects.equals(arg1.getAction(), MainService.CHAT)){
				updateChatList(arg0, arg1);
				ThatItApplication.getApplication().getIncomingPings().remove(mRosterEntry.getUser());
				ThatItApplication.getApplication().getIncomingFilePings().remove(mRosterEntry.getUser());
			}if (arg1.getAction().equals("File Received")) {

			}
		}
	}

	public static void update(){
		new One2OneChatDb(ThatItApplication.getApplication()).updateFileDownloadstatus(subject_socket);
	}

	/**
	 * Update chat list when new message received.
	 */
	public void updateChatList(Context arg0, final Intent arg1) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				try {
					if (arg1.getStringExtra("jid").equalsIgnoreCase(mRosterEntry.getUser())) {
						adapter.add(new OneBubble(false, "", arg1.getStringExtra("msg"), arg1.getStringExtra("msg_subject"), false, System.currentTimeMillis(), arg1.getStringExtra("name"), messageStatus));
						System.out.println("MEassge" + arg1.getStringExtra("msg"));
						//						setReverseList();
						fragChat_clipboard.setVisibility(View.GONE);
						//setAdapter();
						adapter.notifyDataSetChanged();
						new Thread(new Runnable() {

							@Override
							public void run() {
								notificationManager.cancel(MainService.NOTIFICATION_MESSAGE_RECEIVED);
							}
						}).start();

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void selectOverflowOption() {

		final CharSequence[] options = { "Suggest a Contact",
				"Transfer Photo",
				"Transfer Video",
				"Transfer Other Files",
				"End Chat",
				"Cancel" };

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Make your selection");
		builder.setCancelable(false);
		builder.setItems(options, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int item) {
				if (options[item].equals("Suggest a Contact")) {

					if(NetworkAvailabilityReceiver.isInternetAvailable(getActivity())){
						if(MainService.mService.connection.isConnected()
								&& MainService.mService.connection.isAuthenticated()){
							Intent intent  = new Intent(getActivity(),SuggestContactActivity.class);
							startActivity(intent);
						}else{
							Toast.makeText(getActivity(),"Please wait while connection gets restored", Toast.LENGTH_SHORT).show();
						}
					}else{
						Toast.makeText(getActivity(), getResources().getString(R.string.Network_Availability), Toast.LENGTH_SHORT).show();
					}
				}

				else if (options[item].equals("Transfer Photo")) {

					try {
						if(NetworkAvailabilityReceiver.isInternetAvailable(getActivity())){
							if(MainService.mService.connection.isConnected()
									&& MainService.mService.connection.isAuthenticated()){

								dialog.dismiss();
								sendRequestIPMessage();
								launchPhotoPicker();
							}else{
								Toast.makeText(getActivity(),"Please wait while connection gets restored", Toast.LENGTH_SHORT).show();
							}
						}else{
							Toast.makeText(getActivity(), getResources().getString(R.string.Network_Availability), Toast.LENGTH_SHORT).show();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}else if (options[item].equals("Transfer Video")) {

					if(NetworkAvailabilityReceiver.isInternetAvailable(getActivity())){
						if(MainService.mService.connection.isConnected()
								&& MainService.mService.connection.isAuthenticated()){

							dialog.dismiss();
							sendRequestIPMessage();
							launchVideoPicker();
						}else{
							Toast.makeText(getActivity(),"Please wait while connection gets restored", Toast.LENGTH_SHORT).show();
						}
					}else{
						Toast.makeText(getActivity(), getResources().getString(R.string.Network_Availability), Toast.LENGTH_SHORT).show();
					}

				}else if (options[item].equals("Transfer Other Files")) {

					if(NetworkAvailabilityReceiver.isInternetAvailable(getActivity())){
						if(MainService.mService.connection.isConnected()
								&& MainService.mService.connection.isAuthenticated()){

							dialog.dismiss();
							sendRequestIPMessage();
							launchFilePicker();
						}else{
							Toast.makeText(getActivity(),"Please wait while connection gets restored", Toast.LENGTH_SHORT).show();
						}
					}else{
						Toast.makeText(getActivity(), getResources().getString(R.string.Network_Availability), Toast.LENGTH_SHORT).show();
					}
				}
				else if (options[item].equals("End Chat")) {

					InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
							Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mEdtTxtChat.getWindowToken(), 0);

					// get Value from databse
					Utility.startDialog(getActivity());

					new Thread(new Runnable() {
						@Override
						public void run() {
							deleteEntireChat();
							handler.post(new Runnable() {
								@Override
								public void run() {
									try {
										setAdapter();
										Utility.stopDialog();
										replaceFragment();
										//displayContactSection();
										if(!getResources().getBoolean(R.bool.isTablet)){
											ContactActivity.tabs_lnrlayout.setVisibility(View.VISIBLE);
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});
						}
					}).start();
				}
				else if (options[item].equals("Cancel")) {
					dialog.dismiss();
				}
			}
		});
		builder.show();
	}


	/**
	 * Clear all chat.
	 */
	private void deleteEntireChat() {

		try {
			myApplication.openDatabase();
			Cursor cursor = One2OneChatDb.getAllMessagesOfParticipant(mRosterEntry.getUser());
			DatabaseUtils.dumpCursor(cursor);

			if (cursor.moveToFirst()){
				int position = 0;
				do{
					ListItem_position_all.add(cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_ID)));
					ListItem_position_deletes.add(Integer.parseInt(ListItem_position_all.get(position)));
					position = position+1;
				}
				while(cursor.moveToNext());
			}
			cursor.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		boolean isDeleted = true;
		fragChat_clipboard.setVisibility(View.GONE);
		try {
			One2OneChatDb chatDb = new One2OneChatDb(getActivity());

			for (int i = 0; i < ListItem_position_deletes.size(); i++) {
				isDeleted=chatDb.deleteMessage(ListItem_position_deletes.get(i));
			}
			if (isDeleted == true) {
				LastID="0";
				ListItem_position_new.clear();
				ListItem_position_all.clear();
				ListItem_position_deletes.clear();
				//setAdapter();
			}
			else{
				Toast.makeText(getActivity(),"Not able to End Chat", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void replaceFragment() {

		mFragmentManager = getActivity().getSupportFragmentManager();
		mFragmentTransaction = mFragmentManager.beginTransaction();
		mFragmentTransaction.replace(R.id.fragmentContainer, mFragmentChatHistory);
		mFragmentTransaction.commit();
	}

	public void displayContactSection() {

		ContactActivity.mBtn_Contact.setBackgroundResource(R.drawable.contact_icon_hover);
		ContactActivity.mBtn_Chat.setBackgroundResource(R.drawable.comment_icon_sm);
		ContactActivity.mBtn_Invite.setBackgroundResource(R.drawable.message_icon_sm);
		ContactActivity.mBtn_Account.setBackgroundResource(R.drawable.user_icon_sm);
	}

	/**
	 * Select file to be sent to jID.
	 */
	private void launchFilePicker(){

		try {
			startActivityForResult(new Intent(getActivity(),
					AndroidExplorer.class), FILE_SELECT_CODE);

			progressDialog = ProgressDialog.show(getActivity(),"", "Please Wait...");
			progressDialog.setCancelable(false);

			new Thread() {
				public void run() {
					try {
						sleep(2000);
					} catch (Exception e) {
						progressDialog.dismiss();
						Log.e("tag", e.getMessage());
					}
				}
			}.start();
		} catch (ActivityNotFoundException ignored) {
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {

			case FILE_SELECT_CODE:

				if (resultCode == Activity.RESULT_OK) {

					File f;
					try {
						String path = data.getStringExtra("path");
						f = new File(path);

						if(AppSinglton.IpAddress  != null){
							String myIp  = Utils.getLocalIpv4Address();
							if(myIp.substring(0,myIp.lastIndexOf(".")).equals(AppSinglton.IpAddress.substring(0,AppSinglton.IpAddress.lastIndexOf("."))))	{
								com.myquick.socket.Constants.File_URI  = Uri.parse(f.getAbsolutePath());

								long id = System.currentTimeMillis();

								ClientChatThread chatThread = new ClientChatThread(AppSinglton.IpAddress,getActivity(),com.myquick.socket.Constants.SENDING_CODE+"$"+id);
								new Thread(chatThread).start();
								File file = new File(com.myquick.socket.Constants.File_URI.toString());
							}
							else{
								processOtherFiles(f);
							}
						}
						else{
							processOtherFiles(f);
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				break;

			case PHOTO_SELECT_CODE:
				if (resultCode == Activity.RESULT_OK) {
					//	Get URI Path from Gallery
					try {

						selectedImageUriFromGallery = data.getData();
						getImageFromGallery();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;

			case VIDEO_SELECT_CODE:
				if (resultCode == Activity.RESULT_OK) {
					//	Get URI Path from Gallery
					try {
						selectedVideoUriFromGallery = data.getData();
						getVideoFromGallery();

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
		}
	}

	/**
	 * Select file to be sent to jID.
	 */
	private void processOtherFiles(File f) {

		new FileDispatcherAsync(f, mTxtVwPercentageCompletion,
				mprgBarFtpStatus,mService.getFileTransferListener(),
				mRosterEntry.getUser().split("@")[0],getActivity(),new UpdateFTPStatus(){

			@Override
			public void showProgress(int currentProgress) {
			}

			@Override
			public void markStatus(boolean isComplete,String name) {

				if(isComplete)
					sendMessageWithFileSubject(name);
			}}).processFileDispatch();
	}

	private void sendRequestIPMessage() {

		try {
			String to = mRosterEntry.getUser();
			msg = "What is your ip";
			if (mConnection.isConnected()) {
				sendMessage(to, msg, mService.mMessageListner);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Select video file to be sent to jID.
	 */
	private void getVideoFromGallery() {

		try {
			String[] filePath = { MediaStore.Video.Media.DATA };
			Cursor c = getActivity().getContentResolver().query(
					selectedVideoUriFromGallery, filePath, null, null, null);
			c.moveToFirst();
			int columnIndex = c.getColumnIndex(filePath[0]);
			videoPath = c.getString(columnIndex);
			c.close();

			sendSelectedVideoFileFromGallery();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Select image file to be sent to jID.
	 */
	private void getImageFromGallery() {

		try {

			String[] filePath = { MediaStore.Images.Media.DATA };
			Cursor c = getActivity().getContentResolver().query(
					selectedImageUriFromGallery, filePath, null, null, null);
			c.moveToFirst();
			int columnIndex = c.getColumnIndex(filePath[0]);
			picturePath = c.getString(columnIndex);
			c.close();
			sendSelectedImageFileFromGallery();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendSelectedVideoFileFromGallery() {

		File f;
		try {
			f = new File(videoPath);

			if (f.exists()) {
				if(checkValidIp(AppSinglton.IpAddress)){
					String myIp  = Utils.getLocalIpv4Address();
					if(myIp.substring(0,myIp.lastIndexOf(".")).equals(AppSinglton.IpAddress.substring(0,AppSinglton.IpAddress.lastIndexOf("."))))	{
						com.myquick.socket.Constants.File_URI  = Uri.parse(f.getAbsolutePath());

						long id = System.currentTimeMillis();

						ClientChatThread chatThread = new ClientChatThread(AppSinglton.IpAddress,getActivity(),com.myquick.socket.Constants.SENDING_CODE+"$"+id);
						new Thread(chatThread).start();
						File file = new File(com.myquick.socket.Constants.File_URI.toString());

						Utility.fragmentChatScreen.sendMessageWithFileSubjectViaSocket(file.getName(),id);
					}
					else{
						processVideoFile(f);
					}
				}
				else{
					processVideoFile(f);
				}
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void processVideoFile(File f) {
		new FileDispatcherAsync(f, mTxtVwPercentageCompletion,
				mprgBarFtpStatus,
				mService.getFileTransferListener(),
				mRosterEntry.getUser().split("@")[0],getActivity(), new UpdateFTPStatus(){

			@Override
			public void showProgress(int currentProgress) {
			}

			@Override
			public void markStatus(boolean isComplete, String name) {
				if(isComplete)
					sendMessageWithFileSubject(name);
			}}).processFileDispatch();

	}

	// Sending image from gallery

	private void sendSelectedImageFileFromGallery() {

		File f;
		try {
			f = new File(picturePath);
			if (f.exists()) {
				if(checkValidIp(AppSinglton.IpAddress)){
					String myIp  = Utils.getLocalIpv4Address();
					if(myIp.substring(0,myIp.lastIndexOf(".")).equals(AppSinglton.IpAddress.substring(0,AppSinglton.IpAddress.lastIndexOf("."))))	{
						com.myquick.socket.Constants.File_URI  = Uri.parse(f.getAbsolutePath());

						long id = System.currentTimeMillis();

						ClientChatThread chatThread = new ClientChatThread(AppSinglton.IpAddress,getActivity(),com.myquick.socket.Constants.SENDING_CODE+"$"+id);
						new Thread(chatThread).start();
						File file = new File(com.myquick.socket.Constants.File_URI.toString());

						Utility.fragmentChatScreen.sendMessageWithFileSubjectViaSocket(file.getName(),id);
					}
					else{
						processFile(f);
					}
				}
				else{
					processFile(f);
				}
			}
		}
		catch (Exception ignored) {
		}
	}

	/**
	 *
	 * @param ip of jID to whom file is to be sent
	 * @return
	 */
	private boolean checkValidIp(String ip){

		if(ip == null) return false;
		ip = ip.replace(".", "");

		long ipL = 0;
		try{
			ipL  = Long.parseLong(ip);
			return true;
		}catch(NumberFormatException e){
			//			NOT VALID IP
			return false;
		}catch(Exception e){
			return false;
		}
	}

	public void processFile(File f){
		new FileDispatcherAsync(f, mTxtVwPercentageCompletion,
				mprgBarFtpStatus,
				mService.getFileTransferListener(),
				mRosterEntry.getUser().split("@")[0],getActivity(), new UpdateFTPStatus(){

			@Override
			public void showProgress(int currentProgress) {
			}

			@Override
			public void markStatus(boolean isComplete,
								   String name) {
				if(isComplete)
					sendMessageWithFileSubject(name);
			}}).processFileDispatch();
	}

	/**
	 * Open images in gallery
	 */
	private void launchPhotoPicker(){
		try {
			startActivityForResult(new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), PHOTO_SELECT_CODE);

		} catch (Exception ignored) {
		}

	}

	/**
	 * Open videos in gallery
	 */
	private void launchVideoPicker(){

		try {
			startActivityForResult(new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI), VIDEO_SELECT_CODE);

		} catch (Exception e) {
			Log.e("tag","No activity can handle picking a file. Showing alternatives.");
		}
	}


	private void showFTPFileDownloadPrompt(final String fileName){

		if(new One2OneChatDb(hostActivity).isFileDownloaded(fileName)){

			try {
				NotificationManager notificationManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.cancel(FileReceiveraAsync_.notificaion_id);

				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

				String name=fileName;
				File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Thats It/"+name);
				//			String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
				String extension = fileName.substring(fileName.lastIndexOf(".")+1, fileName.length());
				String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
				intent.setDataAndType(Uri.fromFile(file),mimetype);
				startActivity(intent);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}else{
			try {
				final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
				//	alertDialogBuilder.setTitle("");
				alertDialogBuilder
						.setMessage("Are you sure you want to download file"+fileName)
						.setPositiveButton("Done",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int id) {
									}
								})
						.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
							}
						});

				final AlertDialog dialog = alertDialogBuilder.create();
				dialog.show();
				dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

				dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							try {
								dialog.dismiss();
								if (NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {

									mService.acceptCurrentincomingFileRequest(fileName,new FileDownloadStatusCallback() {

										@Override
										public void onDownloadComplete() {
										}
									});

								} else {
									Toast.makeText(getActivity(),getResources().getString(R.string.Network_Availability),Toast.LENGTH_SHORT).show();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
						getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void showSocketFileDownloadPrompt(final String fileName, final String ip,final String file_path){
		Log.e("@@@@@", "###"+ip);
		if(new One2OneChatDb(hostActivity).isFileDownloaded(subject_socket)){

			try {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

				String name=fileName;
				File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Thats It/"+name);
				//			String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
				String extension = fileName.substring(fileName.lastIndexOf(".")+1, fileName.length());
				String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
				intent.setDataAndType(Uri.fromFile(file),mimetype);
				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}else{
			try {
				final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
				//	alertDialogBuilder.setTitle("");
				alertDialogBuilder
						.setMessage("Are you sure you want to download file"+fileName)
						.setPositiveButton("Done",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
														int id) {
									}
								})
						.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
							}
						});

				final AlertDialog dialog = alertDialogBuilder.create();
				dialog.show();
				dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

				dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							try {
								if (NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {
									if(!ip.equals("")){
										Log.e("@@@@@", "ip"+ip);
										ClientChatThread send_message =  new ClientChatThread(ip,getActivity(),com.myquick.socket.Constants.ACCEPT+"$"+file_path+"#");
										new Thread(send_message).start();
									}
									dialog.dismiss();

								} else {
									Toast.makeText(getActivity(),"Internet Connection Unavailable",Toast.LENGTH_SHORT).show();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
						getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void refreshApplication() {
		if(!mConnection.isConnected())
			try {
				mConnection.connect();
			} catch (XMPPException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
		setAdapter();
		onStart();
	}

	/**
	 *  Check if roster entry exists on Admin
	 */

	final ValidateThatsItIdInterface mValidateThatsItIdInterface = new ValidateThatsItIdInterface() {
		@Override
		public void validateThatsItId(ValidateThatsItID mValidateThatsItID) {

			if(mValidateThatsItID != null){

				String status = mValidateThatsItID.getValidateUserPincodeResult().getStatus().getStatus();
				if(!status.equalsIgnoreCase("1")){
					openAlert("This contact has been disabled by Admin and will be removed from your contact list");
				}else{
					Utility.UserPauseStatus(getActivity());
				}
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
										Utility.startDialog(getActivity());
										new Thread(new Runnable() {
											@Override
											public void run() {
												try {
													Utility.removeFriendIfExists(mRosterEntry.getUser());
													removeContactFromRoster(mRosterEntry);
													ThatItApplication.getApplication().getSentInvites().remove(mRosterEntry.getUser().toUpperCase());
													ThatItApplication.getApplication().getSentInvites().remove(mRosterEntry.getUser().toLowerCase());
													// get Value from databse
													deleteEntireChat();
													handler.post(new Runnable() {
														@Override
														public void run() {
															try {
																setAdapter();
																Utility.stopDialog();
																replaceFragment();
																displayContactSection();
																if (!getResources().getBoolean(R.bool.isTablet)) {
																	ContactActivity.tabs_lnrlayout.setVisibility(View.VISIBLE);
																}
															} catch (Exception e) {
																e.printStackTrace();
															}
														}
													});
												} catch (Exception e) {
													e.printStackTrace();
												}
											}
										}).start();
									}
								});

						/*alertDialogBuilder.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0, int arg1) {

										alertDialog.dismiss();
										alertDialog = null;
										Utility.stopDialog();
									}
								});*/
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

	/**
	 *  DELETE ROSTER FROM OPENFIRE.............................................................................................
	 */
	private void removeContactFromRoster(RosterEntry entryToBeRemoved) {
		try {
			MainService.mService.connection.getRoster().removeEntry(entryToBeRemoved);
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}
}

