package com.thatsit.android.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.provider.VCardProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.thatsit.android.MainService;
import com.thatsit.android.MainService.MyMessageListner;
import com.thatsit.android.MyGroupsHelper;
import com.thatsit.android.R;
import com.thatsit.android.RefreshApplicationListener;
import com.thatsit.android.Utility;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.beans.TemplateGroupMessageHolder;
import com.thatsit.android.db.DbOpenHelper;
import com.thatsit.android.db.One2OneChatDb;
import com.thatsit.android.encryption.helper.EncryptionManager;
import com.thatsit.android.parseutil.ParseCallbackListener;
import com.thatsit.android.parseutil.ParseUtil;
import com.thatsit.android.xmpputils.Constants;
import com.thatsit.android.xmpputils.XmppManager;
import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.seasia.myquick.model.AppSinglton;

public class InviteContactsToRoster  extends Activity implements RefreshApplicationListener{
	final String TAG = "SuggestContact";
	private MainService mService;
	private XmppManager mXmppManager;
	private XMPPConnection mConnection;
	private ThatItApplication myApplication;
	private MyRosterListnerInvite myRosterListner = new MyRosterListnerInvite();
	private boolean isSomeoneInvited = false;
	private UsersAdapter usersAdapter;
	private Handler handler=new Handler();
	private ListView mlistView_Contacts;
	private static final Intent SERVICE_INTENT = new Intent();
	private String invitationSentUser;
	private ArrayList<String> jids= new ArrayList<>();
	private ArrayList<String> listcardname = new ArrayList<>();
	private ArrayList<String> listcardlastname = new ArrayList<>();
	private ArrayList<String> listcardprofilepic = new ArrayList<>();
	private	ParseUtil parseUtil = new ParseUtil();
	private VCard card = new VCard();
	private static String nicknameToJoin;
	private static String groupNameWithMessage;
	static {
		SERVICE_INTENT.setComponent(new ComponentName(Constants.MAINSERVICE_PACKAGE,  Constants.MAINSERVICE_PACKAGE + Constants.MAINSERVICE_NAME ));
	}
	private HashMap<Integer,View> viewContainer = new HashMap<>();
	@SuppressLint("ValidFragment")
	public InviteContactsToRoster(MainService mService) {
		this.mService = mService;
		handler = new Handler();
	}
	public InviteContactsToRoster() {
	}

	@Override
	protected void onResume() {
		super.onResume();
		Utility.UserPauseStatus(InviteContactsToRoster.this);
	}

	/**
	 * Initialise class variables.
	 */
	private void initialise_Variable() {
		mlistView_Contacts = (ListView) findViewById(R.id.lst_contacts);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Utility.inviteContactsToRoster = null;
		groupNameWithMessage = "";
		groupNameWithMessage = null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.suggest_contact_activity);

		Utility.inviteContactsToRoster = InviteContactsToRoster.this;
		groupNameWithMessage = getIntent().getStringExtra("GROUP NAME");
		if(groupNameWithMessage.contains("%2b")){
			groupNameWithMessage = groupNameWithMessage.replace("%2b"," ");
		}
		Utility.setDeviceTypeAndSecureFlag(InviteContactsToRoster.this);
		initialise_Variable();

		try {
			Utility.stopDialog(InviteContactsToRoster.this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		myApplication = ThatItApplication.getApplication();
		mXmppManager = XmppManager.getInstance();
		mConnection = mXmppManager.getXMPPConnection();
		usersAdapter = new UsersAdapter();
		getRosterfromDatabase();

	}

	/**
	 * Get friend list from database for the corresponding jID.
	 */
	private void getRosterfromDatabase() {
		try {
			Utility.startDialog(this);
			ThatItApplication.getApplication().openDatabase();
			Cursor cursor = One2OneChatDb.getAllRoster();
			Log.d(TAG, "cursor 111 " + cursor.getCount());
			cursor.moveToFirst();

			jids.clear();

			listcardlastname.clear();
			listcardprofilepic.clear();
			listcardname.clear();

			if (cursor.moveToFirst()){
				do{
					String jid = cursor.getString(cursor.getColumnIndex(DbOpenHelper.JABBER_ID));
					String firstname = cursor.getString(cursor.getColumnIndex(DbOpenHelper.FIRSTNAME));
					String lastname = cursor.getString(cursor.getColumnIndex(DbOpenHelper.LASTNAME));
					String profilePic = cursor.getString(cursor.getColumnIndex(DbOpenHelper.PROFILE_PIC_URL));

					jids.add(jid);
					listcardname.add(firstname);
					listcardprofilepic.add(profilePic);
					listcardlastname.add( lastname );
				}while(cursor.moveToNext());
			}
			cursor.close();
			setListAdapter();
			Utility.stopDialog(InviteContactsToRoster.this);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set roster information in listview.
	 */
	public void setListAdapter( ) {
		try {
			usersAdapter = new UsersAdapter();
			mlistView_Contacts.setAdapter(usersAdapter);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param jid - jID obtained from Roster list
	 * @return - Returns jID
	 */
	private RosterEntry getEntryUsingJid(String jid){

		jid = jid.toLowerCase();

		if(!jid.contains("@")){
			jid = jid +"@" + mConnection.getHost();
		}

		return mConnection.getRoster().getEntry(jid);
	}

	/**
	 * The Adapter class to provide access to the data items.
	 */
	class UsersAdapter extends BaseAdapter {
		Handler vCardHandler=new Handler();

		@SuppressLint("NewApi")
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			final RosterEntry entry = getEntryUsingJid(jids.get(position));

			View convertviews;

			final RosterListViewHolder viewHolder;
			if (viewContainer.isEmpty() || !viewContainer.containsKey(position)) {

				viewHolder = new RosterListViewHolder();
				convertviews = LayoutInflater.from(InviteContactsToRoster.this).inflate(R.layout.adapter_invite_group, null);
				viewHolder.tvName = (TextView) convertviews.findViewById(R.id.txt_UserName);
				viewHolder.tvStatus = (TextView) convertviews.findViewById(R.id.txt_UserDescrption);
				viewHolder.tvMyQuickID = (TextView) convertviews.findViewById(R.id.txt_profileDescrption4);
				viewHolder.tvPersence = (ImageView) convertviews.findViewById(R.id.profile_picture);
				viewHolder.inviteContact = (Button) convertviews.findViewById(R.id.btn_copyId);
				convertviews.setTag(viewHolder);
				viewContainer.put(position, convertviews);
			}else{
				convertviews=viewContainer.get(position);
				viewHolder=(RosterListViewHolder) convertviews.getTag();
			}

			try {
				if (listcardname.get(position) != null && listcardname.size()!= 0) {
					if(!listcardname.get(position).equalsIgnoreCase("")){
						viewHolder.tvName.setText(listcardname.get(position));
					}else{
						viewHolder.tvName.setText("My Name");
					}
				}

				if (listcardlastname.get(position) != null && listcardlastname.size()!= 0) {

					if(!listcardlastname.get(position).equalsIgnoreCase("")){
						viewHolder.tvStatus.setText(listcardlastname.get(position));
					}else{
						viewHolder.tvStatus.setText("Hi I Am Using That's It");
					}
				}

				if (listcardprofilepic.get(position) != null && listcardprofilepic.size()!= 0) {
					Log.d("images url",	""+ card.getField("profile_picture_url"));

					ContactActivity.options = new DisplayImageOptions.Builder()
							.displayer(new RoundedBitmapDisplayer(200))
							.showImageOnFail(R.drawable.no_img)
							.showImageForEmptyUri(R.drawable.no_img)
							.showImageOnLoading(R.drawable.no_img)
							.cacheInMemory(true)
							.cacheOnDisk(true)
							.build();
					ContactActivity.loader.displayImage(listcardprofilepic.get(position).replaceAll(" ", "%20"), viewHolder.tvPersence, ContactActivity.options);

				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			try {
				viewHolder.tvMyQuickID.setText(entry.getUser().split("@")[0]);
			} catch (NotFoundException e) {
				e.printStackTrace();
			}

			viewHolder.inviteContact.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					try
					{
						if(NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())){

							Utility.startDialog(InviteContactsToRoster.this, "Sending Invitation...");

							final ParseUtil parseUtil = new ParseUtil();
							parseUtil.joinGroup(
									ThatItApplication.getApplication() .getCurrentMUCRefernece().getRoom()
									,jids.get(position)+"@"+XmppManager.getInstance().getXMPPConnection().getHost()
									, new ParseCallbackListener() {

										@Override
										public void done(List<ParseObject> receipients, ParseException e,
														 int requestId) {
										}

										@Override
										public void done(final ParseException parseException, int requestId) {

											new Thread(new Runnable() {
												@Override
												public void run() {
													if(parseException==null){
														//	send invite

														invitationSentUser = jids.get(position)+"@"+mConnection.getHost();

														Message message = new Message(invitationSentUser);
														if(TextUtils.isEmpty(groupNameWithMessage)){
															message.setBody("Join me for a group chat!");
														}else{
															message.setBody("Join me for a group chat in " + groupNameWithMessage+" !");
														}
														message.addExtension(new GroupChatInvitation(ThatItApplication.getApplication().getCurrentMUCRefernece().getRoom()));
														if(XmppManager.getInstance().getXMPPConnection()!=null	&&	XmppManager.getInstance().getXMPPConnection().isAuthenticated()){
															XmppManager.getInstance().getXMPPConnection().sendPacket(message);
														}
														try {
															ThatItApplication.getApplication().getCurrentRosterGroupReference().addEntry(entry);
															ThatItApplication.getApplication().getCurrentMUCRefernece().invite(invitationSentUser, "please join my group");
															isSomeoneInvited=true;
															handler.post(new Runnable() {

																@Override
																public void run() {
																	v.setEnabled(false);
																}
															});

															sendGroupInviteMessage();

														} catch (XMPPException e) {
															e.printStackTrace();
															parseUtil.leaveGroup(ThatItApplication.getApplication()
																	.getCurrentMUCRefernece().getRoom(),invitationSentUser , null, ParseCallbackListener.OPERATION_LEAVE_GROUP);
														}
														addRemove(chat_option.ADD_PERSON, invitationSentUser.subSequence(0, invitationSentUser.indexOf("@"))+"", ThatItApplication.getApplication()
																.getCurrentMUCRefernece().getRoom());

													}
													Utility.stopDialog();
													Utility.showMessage("Invitation Sent");
												}

											}).start();
										}
									}, ParseCallbackListener.OPERATION_JOIN_GROUP);

						}
					} catch (Exception e) {
						isSomeoneInvited=false;
						e.printStackTrace();
					}
				}
			});
			return convertviews;
		}
		@Override
		public int getCount() {
			return jids.size();
		}

		@Override
		public String getItem(int arg0) {
			return jids.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
	}

	/**
	 * Send message along with invitation to corresponding jID.
	 */
	private void sendGroupInviteMessage() {
		try {
			if (mConnection.isConnected()) {
				sendMessage(invitationSentUser, "Group Invitation", mService.mMessageListner);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param user - jID to whom message is to be sent
	 * @param msg - Text message to be sent
	 * @param mMessageListner - Listener to receive incoming message
	 */
	private void sendMessage(String user, String msg,MyMessageListner mMessageListner) {

		if (!mConnection.isConnected())   return;
		ChatManager chatmanager = mConnection.getChatManager();
		Chat newChat = chatmanager.createChat(invitationSentUser, mMessageListner);
		try {
			Message newMessage = new Message();
			newMessage.setBody(msg);
			newChat.sendMessage(newMessage);
		} catch (XMPPException e) {
			Log.d(TAG, "Error Delivering block");
		}
	}

	/**
	 * ViewHolder - enables you to access each list item view without the need for the look up, saving valuable processor cycles.
	 */
	private class RosterListViewHolder{
		TextView tvName  ;
		TextView tvStatus ;
		TextView tvMyQuickID ;
		ImageView tvPersence ;
		Button inviteContact  ;
	}

	@Override
	public void onBackPressed(){

		Utility.stopDialog(InviteContactsToRoster.this);

		if(!isSomeoneInvited){

			parseUtil.getGroupMembers(ThatItApplication.getApplication() .getCurrentMUCRefernece().getRoom(), new ParseCallbackListener() {

				@Override
				public void done(List<ParseObject> receipients, ParseException e,
								 int requestId) {

					if(e==null){
						StringTokenizer stringTokenizer = new StringTokenizer(receipients.get(0).getString(ThatItApplication.getApplication().getString(R.string.column_group_members))," ");
						int size = stringTokenizer.countTokens();
						if(size<=1){

							openInviteSentDialog();

						}else{
							InviteContactsToRoster.this.finish();
						}
					}
				}

				@Override
				public void done(ParseException parseException, int requestId) {
				}
			}, 0);

		}else{
			finish();
		}
	}

	/**
	 * Roster Listener.
	 * Called when any entry is added, or deleted, or updated.
	 */
	public final class MyRosterListnerInvite implements RosterListener {

		@Override
		public void entriesAdded(Collection<String> arg0) {
			getRosterfromDatabase();
		}

		@Override
		public void entriesDeleted(Collection<String> arg0) {
			getRosterfromDatabase();
		}

		@Override
		public void entriesUpdated(Collection<String> arg0) {
			getRosterfromDatabase();
		}

		@Override
		public void presenceChanged(Presence presence) {
		}

		void resetRoster() {
			getRosterfromDatabase();
		}
	}

	/**
	 *
	 * @return - Roster List
	 */
	private ArrayList<RosterEntry> getRosters1() {

		try {
			if (myApplication.isConnected() && mConnection.isAuthenticated()) {
				return mService.getRostersInvite(myRosterListner);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Alert to add jID to the current room.
	 */
	private void openInviteSentDialog() {
		try {
			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(InviteContactsToRoster.this);
			// set title
			alertDialogBuilder.setMessage("Do you want to add the person to the group.");

			alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {}
					});

			final AlertDialog dialog = alertDialogBuilder.create();
			dialog.show();
			dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						askForGroupDeletionDialog();
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
	 * Alert to remove any group from the server.
	 */
	private void askForGroupDeletionDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(InviteContactsToRoster.this);
		builder.setMessage("This is an empty group , it will be automatically removed from ThatsIt!")
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						Utility.stopDialog();
						finish();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public enum chat_option{
		ADD_PERSON,LEAVE
	};

	static MultiUserChat muc =null;

	/**
	 * @param option - Add or remove jID to or from room.
	 * @param jid - id of group member
	 * @param GroupName - Room name
	 */
	public static void addRemove(final chat_option option ,final String jid,String GroupName){

		final XMPPConnection connection =XmppManager.getInstance().getXMPPConnection();
		final SharedPreferences mSharedPreferences = ThatItApplication.getApplication().getSharedPreferences("UpdatePseudoName", 0);
		try {
			nicknameToJoin = mSharedPreferences.getString("pseudoName", "anonymous") +" ("+connection.getUser().split("@")[0]+")";
		} catch (Exception e3) {
			e3.printStackTrace();
		}

		if(!GroupName.contains("@"))
			GroupName = GroupName+"@conference."+connection.getHost();

		try {
			if(muc!=null && muc.getRoom().equalsIgnoreCase(GroupName) ){
				muc.leave();

			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		if (!connection.isConnected()) {
			try {
				connection.connect();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		muc = new MultiUserChat(connection, GroupName);

		System.out.println(nicknameToJoin);
		new Thread(new Runnable() {

			@Override
			public void run() {
				EncryptionManager encryptionManager = new EncryptionManager();
				DiscussionHistory history = new DiscussionHistory();
				history.setMaxStanzas(0);
				if(!muc.isJoined())
					try {
						muc.join(nicknameToJoin, "", history, SmackConfiguration.getPacketReplyTimeout());
					} catch (XMPPException e1) {
						e1.printStackTrace();
					}

				String revisedMessage ="";
				String post = Constants.post+AppSinglton.thatsItPincode;
				String personName = jid;

				try {
					VCard card = new VCard();

					String complete_jid=jid;
					if(!jid.contains("@")){
						complete_jid = complete_jid+"@"+connection.getHost();
					}
					card.load(connection, complete_jid);
					personName= card.getFirstName() +" ("+jid+")";

				} catch (XMPPException e1) {
					e1.printStackTrace();
				}

				switch (option) {
					case ADD_PERSON:
						try {
							revisedMessage = encryptionManager.encryptPayload(Utility.processSmileyCodes(Constants.ADD_PERSON+personName+" is added to the group"));
						} catch (Exception e2) {
							e2.printStackTrace();
						}
						break;

					case LEAVE:
						try {
							revisedMessage = encryptionManager.encryptPayload(Utility.processSmileyCodes(Constants.LEFT_PERSON+personName +" has left the group"));
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						break;
				}

				Message newMessage = muc.createMessage();
				newMessage.setBody(revisedMessage);
				newMessage.setType(Type.groupchat);

				TemplateGroupMessageHolder groupMessageHolder = new TemplateGroupMessageHolder();
				groupMessageHolder.setJid(AppSinglton.thatsItPincode);
				groupMessageHolder.setMessage(revisedMessage);
				try {
					muc.sendMessage(newMessage);

				} catch (XMPPException e) {
					e.printStackTrace();
				}

				muc.addMessageListener(new PacketListener() {

					@Override
					public void processPacket(Packet arg0) {
						System.out.println(arg0.getFrom() );
					}
				});

			}
		}).start();
	}
	@Override
	public void refreshApplication() {

		if(!mConnection.isConnected() || !mConnection.isAuthenticated()){

			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						mConnection.connect();
					} catch (XMPPException e) {
						e.printStackTrace();
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();}
	}
}