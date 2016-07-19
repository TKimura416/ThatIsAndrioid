
package com.thatsit.android.activities;

import java.util.ArrayList;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.thatsit.android.MainService;
import com.thatsit.android.R;
import com.thatsit.android.Utility;
import com.thatsit.android.adapter.MyGroupAdapter;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.beans.TemplateGroupMessageHolder;
import com.thatsit.android.db.DbOpenHelper;
import com.thatsit.android.db.One2OneChatDb;
import com.thatsit.android.encryption.helper.EncryptionManager;
import com.seasia.myquick.model.AppSinglton;

@SuppressLint("NewApi")
public class MUCActivity extends Activity {
	private static ArrayList<TemplateGroupMessageHolder> groupMessageHolders = new ArrayList<>();
	private MultiUserChat muc;
	private ListView listMUCMessages;
	public static EditText edtChat;
	private ImageView sendBtn,fragChat_img_smiley;
	private SharedPreferences mSharedPreferences;
	private String nicknameToJoin;
	private TextView mTxtVwGroupName;
	private MyGroupAdapter groupadapter;
	public static boolean chek_Activity = false;
	private EncryptionManager encryptionManager;
	private String group_name,group_name_compelete  ;
	private ThatItApplication myApplication;
	private NotificationManager notificationManager = null;
	private final IncomingReceiver one2OneChatReceiver = new IncomingReceiver();
	private String psedoname;
	private String revisedMessage;
	private static boolean isPromtAllowed=true;

	@Override
	protected void onStop() {
		super.onStop();
		if(isPromtAllowed){
			Utility.taskPromtOnStop(true,MUCActivity.this);
		}else{
			isPromtAllowed=false;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_muc_chat);

		Utility.setDeviceTypeAndSecureFlag(MUCActivity.this);
		Utility.allowAuthenticationDialog=false;
		notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		myApplication = ThatItApplication.getApplication();
		encryptionManager = new EncryptionManager();
		setWindowType();
		initialiseSharedPreference();
		initialiseVariable();
		ThatItApplication.getApplication().registerReceiver(one2OneChatReceiver, new IntentFilter("GROUP MESSAGE"));
		muc =  ThatItApplication.getApplication().getCurrentGroupChatCoversation();
		getRoomName();
		setAdapter();
		setReverseList();
		setClickListners();
	}

	/**
	 * Returns room name.
	 */
	private void getRoomName() {
		try {
			if(psedoname.equalsIgnoreCase("")){
				nicknameToJoin = "My Name" +" ("+MainService.mService.connection.getUser().asUnescapedString().split("@")[0]+")";
			}else{
				nicknameToJoin = psedoname +" ("+MainService.mService.connection.getUser().asUnescapedString().split("@")[0]+")";
			}
			group_name_compelete =muc.getRoom().asUnescapedString().split("@")[0];
			group_name = muc.getRoom().asUnescapedString().split("@")[0].split("__")[1];
			group_name = group_name.replaceAll("%2b", " ");
			mTxtVwGroupName.setText(group_name);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set keyboard hidden.
	 */
	private void setWindowType() {
		
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
				| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}

	/**
	 *  Read data from shared preference.
	 */
	private void initialiseSharedPreference() {

		mSharedPreferences = getSharedPreferences("register_data", 0);
		psedoname=mSharedPreferences.getString("PseudoName", "");
	}

	/**
	 * Initialise class variables.
	 */
	private void initialiseVariable() {

		listMUCMessages =(ListView) findViewById(R.id.listMUCMessages);
		edtChat =(EditText) findViewById(R.id.edtChat);
		sendBtn =(ImageView) findViewById(R.id.sendBtn);
		mTxtVwGroupName =(TextView)findViewById(R.id.txtvwGroupName);
		fragChat_img_smiley=(ImageView) findViewById(R.id.fragChat_img_smiley);
	}

	/**
	 *
	 * @param message - incoming group message
	 */
	private void setListAdapter(TemplateGroupMessageHolder message){
		try {
			groupMessageHolders.add(message);
			MUCActivity.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					groupadapter.notifyDataSetChanged();	
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *  Set adapter to display values in listview.
	 */
	private void setAdapter() {
		groupMessageHolders = new ArrayList<>();
		if(MainService.mService.connection.isConnected()){
			showPreviousChat();
		}else{
			groupadapter = new MyGroupAdapter(ThatItApplication.getApplication(),MainService.mService.connection, groupMessageHolders,listMUCMessages);
			listMUCMessages.setAdapter(groupadapter);
		} 
	}

	/**
	 * Get entire room chat from database.
	 */
	private void showPreviousChat() {
		try {
			myApplication.openDatabase();
			Cursor cursor = One2OneChatDb.getAllMessagesOfRoom(group_name_compelete);

			DatabaseUtils.dumpCursor(cursor);

			if (cursor.moveToFirst()){
				int position = 0;
				do{
					TemplateGroupMessageHolder templateGroupMessageHolder = new TemplateGroupMessageHolder();
					String jid =cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_JID));
					String msg = cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_MESSAGE));
					templateGroupMessageHolder.setJid(jid);
					templateGroupMessageHolder.setMessage(msg);

					groupMessageHolders.add(templateGroupMessageHolder);
					position = position+1;

				}while(cursor.moveToNext());
			}
			groupadapter = new MyGroupAdapter(ThatItApplication.getApplication(), MainService.mService.connection, groupMessageHolders,listMUCMessages);
			listMUCMessages.setAdapter(groupadapter);
			cursor.close();
			setReverseList();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialise click listeners.
	 */
	private void setClickListners(){

		fragChat_img_smiley.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if(!Utility.smileyScreenOpened){
					Utility.smileyScreenOpened = true;
					chek_Activity = true;
					Intent intent = new Intent(MUCActivity.this,GridViewActivity.class);
					startActivity(intent);
				}
			}
		});

		sendBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				String text = edtChat.getText().toString().trim();

				if(text.equals("")){
					Toast.makeText(getApplicationContext(), "No Message To Send", Toast.LENGTH_SHORT).show();
					return;
				}

				text=nicknameToJoin+"\n\n"+text;

				try {
					revisedMessage = encryptionManager.encryptPayload(Utility.processSmileyCodes(text));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				if (MainService.mService.connection.isConnected()) {
					Message newMessage = muc.createMessage();
					newMessage.setBody(revisedMessage);
					newMessage.setType(Type.groupchat);

					TemplateGroupMessageHolder groupMessageHolder = new TemplateGroupMessageHolder();
					groupMessageHolder.setJid(AppSinglton.thatsItPincode);
					groupMessageHolder.setMessage(revisedMessage);
					setListAdapter(groupMessageHolder);
					try {
						muc.sendMessage(newMessage);
						edtChat.setText("");
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (SmackException.NotConnectedException e) {
						e.printStackTrace();
					}
					One2OneChatDb.addGroupMessage(group_name_compelete,AppSinglton.thatsItPincode, revisedMessage);

				}else {
					Toast.makeText(getBaseContext(), "Not Connected", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	@Override
	public void onBackPressed(){
		Utility.allowAuthenticationDialog=false;
		ThatItApplication.getApplication().unregisterReceiver(one2OneChatReceiver);
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.e("isOpened", Utility.allowAuthenticationDialog+"");

		if(Utility.allowAuthenticationDialog){
			Utility.showLock(MUCActivity.this);
		}
		try {
			ThatItApplication.getApplication().getIncomingGroupPings().remove(group_name_compelete);
			notificationManager.cancel(MainService.NOTIFICATION_MESSAGE_RECEIVED);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Utility.UserPauseStatus(MUCActivity.this);
	}

	/**
	 * Scroll screen to the latest message received.
	 */
	private void setReverseList() {
		try {
			listMUCMessages.post(new Runnable() {
				@Override
				public void run() {
					listMUCMessages.setSelection(groupadapter.getCount() - 1);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Register receiver to handle incoming group message.
	 */
	class IncomingReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context arg0, final Intent arg1) {
			if(arg1.getAction().equalsIgnoreCase("GROUP MESSAGE")){
				try {

					String name =  arg1.getExtras().getString("group_name").replace("%2b",  " ");
					if(name.equalsIgnoreCase(group_name_compelete.replace("%2b", " "))){

						Bundle bundle= arg1.getExtras();

						TemplateGroupMessageHolder message = new TemplateGroupMessageHolder();
						message.setJid(bundle.getString("jid"));
						message.setMessage(bundle.getString("message"));
						message.setRoomName(group_name);

						groupMessageHolders.add(message);
						MUCActivity.this.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								groupadapter.notifyDataSetChanged();	
							}
						});

						KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
						if( !myKM.inKeyguardRestrictedInputMode() && MainService.mService.isRunningInForeground()) {
						// Screen is not locked
						ThatItApplication.getApplication().getIncomingGroupPings().remove(group_name_compelete);
						notificationManager.cancel(MainService.NOTIFICATION_MESSAGE_RECEIVED);
							} 
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
