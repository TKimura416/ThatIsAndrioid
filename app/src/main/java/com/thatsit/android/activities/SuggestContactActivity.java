package com.thatsit.android.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedVignetteBitmapDisplayer;
import com.thatsit.android.MainService;
import com.thatsit.android.R;
import com.thatsit.android.RefreshApplicationListener;
import com.thatsit.android.Utility;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.xmpputils.Constants;
import com.thatsit.android.xmpputils.XmppManager;
import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

@SuppressLint("UseSparseArrays")
public class SuggestContactActivity  extends Activity implements RefreshApplicationListener{
	final String TAG = "SuggestContact";
	private MainService mService;
	private XmppManager mXmppManager;
	private XMPPConnection mConnection;
	private ThatItApplication myApplication;
	private ArrayList<RosterEntry> rosterEntries;
	private MyRosterListnerSuggest myRosterListner = new MyRosterListnerSuggest();
	private UsersAdapter usersAdapter;
	private Handler handler;
	private ListView mlistView_Contacts;
	private boolean mBinded;
	private HashMap<Integer,View> viewContainer = new HashMap<>();
	private Handler hand = new Handler();
	private static final Intent SERVICE_INTENT = new Intent();
	private Hashtable<String, VCard> rosterVCardsHash = new Hashtable<>();

	static {
		SERVICE_INTENT.setComponent(new ComponentName(Constants.MAINSERVICE_PACKAGE,  Constants.MAINSERVICE_PACKAGE + Constants.MAINSERVICE_NAME ));
	}

	@SuppressLint("ValidFragment")
	public SuggestContactActivity(MainService mService) {
		this.mService = mService;
		handler = new Handler();
	}
	public SuggestContactActivity() {

	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.suggest_contact_activity);

		Utility.setDeviceTypeAndSecureFlag(SuggestContactActivity.this);
		myApplication = ThatItApplication.getApplication();
		mXmppManager = XmppManager.getInstance();
		mConnection = mXmppManager.getXMPPConnection();
		rosterEntries = new ArrayList<>();

		initialise_Variable();
	}

	/**
	 * Initialise class variables.
	 */
	private void initialise_Variable() {
		mlistView_Contacts = (ListView) findViewById(R.id.lst_contacts);
	}

	@Override
	public void onStart() {
		super.onStart();
		try {

			startService(SERVICE_INTENT);
			if(!mBinded){
				mBinded =  bindService(SERVICE_INTENT, serviceConnection, Context.BIND_AUTO_CREATE);
			}
			rosterEntries=getRostersSuggest();

			new Thread(new Runnable() {
				@Override
				public void run() {
					hand.post(new Runnable() {
						@Override
						public void run() {
							showDataOfVcards();
						}
					});
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		Utility.allowAuthenticationDialog = false;
		try {
//			unregisterReceiver(connectionBroadcastReceiver);
			if(mBinded){
				try {
					unbindService(serviceConnection);
					mBinded =false;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param arrayList - Roster list for particular jID
	 */
	private void setListAdapter(ArrayList<RosterEntry> arrayList) {
		try {
			rosterEntries = arrayList;

			if (arrayList != null && arrayList.size() > 0) {
				Log.d(TAG, "NO. of Rosters" + arrayList.size());
				usersAdapter = new UsersAdapter(arrayList);
				mlistView_Contacts.setAdapter(usersAdapter);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private ArrayList<RosterEntry> getRostersSuggest() {
		try {
			if (myApplication.isConnected() && mConnection.isAuthenticated()) {
				return mService.getRostersSuggest(myRosterListner);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * The Adapter class to provide access to the data items.
	 */
	class UsersAdapter extends BaseAdapter {
		ArrayList<RosterEntry> arrayList;
		Handler vCardHandler=new Handler();

		UsersAdapter(ArrayList<RosterEntry> arrayList) {
			this.arrayList = arrayList;
		}

		@SuppressLint({ "NewApi", "InflateParams" })
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final RosterEntry entry = getItem(position);
			View convertviews;

			final RosterListViewHolder viewHolder;
			if (viewContainer.isEmpty() || !viewContainer.containsKey(position)) {

				viewHolder = new RosterListViewHolder();
				convertviews = LayoutInflater.from(SuggestContactActivity.this).inflate(R.layout.adapter_suggest_contact, null);
				viewHolder.tvName = (TextView) convertviews.findViewById(R.id.txt_UserName);
				viewHolder.tvStatus = (TextView) convertviews.findViewById(R.id.txt_UserDescrption);
				viewHolder.tvMyQuickID = (TextView) convertviews.findViewById(R.id.txt_profileDescrption4);
				viewHolder.tvPersence = (ImageView) convertviews.findViewById(R.id.profile_picture);
				viewHolder.copyImage = (ImageButton) convertviews.findViewById(R.id.img_copyIdBtn);
				viewHolder.linLyBg =(RelativeLayout) convertviews.findViewById(R.id.rel_cotyId);
				convertviews.setTag(viewHolder);
				viewContainer.put(position, convertviews);
			}else{
				convertviews=viewContainer.get(position);
				viewHolder=(RosterListViewHolder) convertviews.getTag();
			}

			viewHolder.copyImage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						String copyValue=viewHolder.tvMyQuickID.getText().toString();
						if(copyValue.equals("")){
							Toast.makeText(SuggestContactActivity.this, "No Text To Copy", Toast.LENGTH_SHORT).show();
							finish();
						}
						else{
							ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
							ClipData clip = ClipData.newPlainText("label",copyValue);
							clipboard.setPrimaryClip(clip);
							Toast.makeText(SuggestContactActivity.this, "Text Copied To Clipboard", Toast.LENGTH_SHORT).show();
							finish();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			final VCard card = new VCard();
			viewHolder.tvMyQuickID.setText(entry.getUser().split("@")[0]);

			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						card.load(mConnection, entry.getUser());
						rosterVCardsHash.put(entry.getUser(), card);
					} catch (XMPPException e) {
						e.printStackTrace();
					}

					vCardHandler.post(new Runnable() {

						@Override
						public void run() {

							if(card!=null &&card.getFirstName()!=null){
								viewHolder.tvName.setText(card.getFirstName());
							}
							if(card!=null && card.getLastName()!=null){
								viewHolder.tvStatus.setText(card.getLastName());
							}
							if(card.getField("profile_picture_url")!=null){

								ContactActivity.options = new DisplayImageOptions.Builder()
										.displayer(new RoundedBitmapDisplayer(200))
										.showImageOnFail(R.drawable.no_img)
										.showImageForEmptyUri(R.drawable.no_img)
										.cacheInMemory(true)
										.cacheOnDisk(true)
										.build();
								ContactActivity.loader.displayImage(card.getField("profile_picture_url").replaceAll(" ", "%20"), viewHolder.tvPersence, ContactActivity.options);
							}
						}
					});
				}
			}).start();

			try {
				viewHolder.tvMyQuickID.setText(entry.getUser().split("@")[0]);
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
			return convertviews;
		}
		@Override
		public int getCount() {
			return arrayList.size();
		}

		@Override
		public RosterEntry getItem(int arg0) {
			return arrayList.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
	}

	/**
	 * Chat listener to encounter incoming message.
	 */
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

	/**
	 * Bind service with activity.
	 */
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.d(TAG, "ServiceConnected   ********************");
			mService = ((MainService.MyBinder) binder).getService();
			addIncommingChatListner();
			mBinded = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBinded = false;
		}
	};

	/**
	 * ViewHolder - enables you to access each list item view without the need for the look up, saving valuable processor cycles.
	 */
	static class RosterListViewHolder{
		TextView tvName  ;
		TextView tvStatus ;
		TextView tvMyQuickID ;
		ImageView tvPersence ;
		ImageView copyImage  ;
		RelativeLayout linLyBg ;
	}

	/**
	 * Roster Listener to determine entry added,deleted or updated
	 */
	public final class MyRosterListnerSuggest implements RosterListener {

		@Override
		public void entriesAdded(Collection<String> arg0) {
			resetRoster();
		}

		@Override
		public void entriesDeleted(Collection<String> arg0) {
			resetRoster();
		}

		@Override
		public void entriesUpdated(Collection<String> arg0) {
			resetRoster();
		}

		@Override
		public void presenceChanged(Presence presence) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					if(usersAdapter!=null)
						usersAdapter.notifyDataSetChanged();
				}
			});
		}

		/**
		 * Refresh roster list and get updated information
		 */
		void resetRoster() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					setListAdapter(getRostersSuggest().size() > 0 ? getRostersSuggest()
							: null);
				}
			});
		}
	}

	/**
	 * Get jID information from Vcard.
	 */
	private void showDataOfVcards(){
		try {
			if (NetworkAvailabilityReceiver.isInternetAvailable(SuggestContactActivity.this)) {
				if (getRostersSuggest() != null || !(getRostersSuggest().size() == 0)) {
					setListAdapter(getRostersSuggest().size() > 0 ? getRostersSuggest() : null);
				} else {
					Toast.makeText(SuggestContactActivity.this, "No Friend Available", Toast.LENGTH_SHORT).show();
				}
			} else {
				if(SuggestContactActivity.this!=null)
					Toast.makeText(SuggestContactActivity.this,getResources().getString(R.string.Network_Availability), Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			if(SuggestContactActivity.this!=null)
				Toast.makeText(SuggestContactActivity.this, "No Friend Available", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
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