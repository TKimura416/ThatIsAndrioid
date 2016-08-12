package com.thatsit.android.fragement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.stringprep.XmppStringprepException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.thatsit.android.MainService;
import com.thatsit.android.R;
import com.thatsit.android.Utility;
import com.thatsit.android.activities.ContactActivity;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.db.DbOpenHelper;
import com.thatsit.android.db.One2OneChatDb;
import com.thatsit.android.parseutil.ParseCallbackListener;
import com.thatsit.android.parseutil.ParseUtil;
import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.seasia.myquick.model.AppSinglton;

@SuppressLint({ "ValidFragment", "NewApi" })
public class FragmentInvitationSent extends Fragment implements OnClickListener{
	private Button mBtnReceived,mBtnSent,mBtnInvites;
	private View mView;
	private FragmentManager mFragmentManager;
	private FragmentTransaction mFragmentTransaction;
	private FragmentInvitationScreen mFragmentInvitationScreen;
	private FragmentInvitationReceive mFragmentInvitationReceive;
	private LinearLayout mLinLytInviationsSentContainer;
	private MainService  mService;
	private final VCard card = new VCard();
	private final ParseUtil parseUtil = new ParseUtil();
	private ContactActivity hostActivity;
	private Handler handler;
	private final ArrayList<String> jids = new ArrayList<>();
	private LinearLayout fragInvSent_tabs_lnrlayout;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utility.fragmentInvitationsent= FragmentInvitationSent.this;
		((ContactActivity)getActivity()).iconStateChanger(false, false, true, false);
		Utility.fragPaymentSettingsOpen = false;
	}


	@Override
	public void onResume() {
		super.onResume();
		Utility.fragmentInvitationsent = FragmentInvitationSent.this;
		if (getResources().getBoolean(R.bool.isTablet)) {
			if (hostActivity.mDrawerLayout != null && hostActivity.navDrawerView != null) {
				hostActivity.mDrawerLayout.closeDrawer(hostActivity.navDrawerView);
			}
		}
		Utility.UserPauseStatus(getActivity());
	}

	public FragmentInvitationSent(MainService mainService){
		mService = mainService;
	}
	public FragmentInvitationSent(){

	}
	@Override
	public void onAttach(Context activity) {
		super.onAttach(activity);
		hostActivity = (ContactActivity) activity;
	}
	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		mView= inflater.inflate(R.layout.fragment_invitations_sent, container, false);

		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN|WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		initialise_Variables();
		initialise_Listener();
		if(getResources().getBoolean(R.bool.isTablet)){
			ContactActivity.textView_toolbar_title.setText("Sent Invitations");
			fragInvSent_tabs_lnrlayout.setVisibility(View.GONE);
		}
		handler = new Handler();
		return mView;
	}

	private void initialise_Variables() {
		mFragmentInvitationScreen =new FragmentInvitationScreen(mService);
		mFragmentInvitationReceive=new FragmentInvitationReceive(mService);

		fragInvSent_tabs_lnrlayout = (LinearLayout)mView.findViewById(R.id.fragInvSent_tabs_lnrlayout);
		mBtnReceived =(Button)mView.findViewById(R.id.fragInvSent_btn_Received);
		mBtnSent=(Button)mView.findViewById(R.id.fragInvSent_btn_Sent);
		mBtnInvites=(Button)mView.findViewById(R.id.fragInvSent_btn_invitaions);
		mLinLytInviationsSentContainer=(LinearLayout)mView.findViewById(R.id.fragInvRec_list_invitationsSent);
	}

	private void initialise_Listener() {
		mBtnReceived.setOnClickListener(this);
		mBtnSent.setOnClickListener(this);
		mBtnInvites.setOnClickListener(this);
	}


	@Override
	public void onStart(){
		super.onStart();
		try {
			getActivity().registerReceiver(new IncomingReceiver(),new IntentFilter("SUBSCRIBE_ACTION_CALLBACK"));
			populateSentInvitations();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * Broadcast Receiver to receive incoming friend request.
	 */
	public class IncomingReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("SUBSCRIBE_ACTION_CALLBACK")) {
				populateSentInvitations();
			}
		}
	}



	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.fragInvSent_btn_invitaions:
				try {
					mFragmentManager = getActivity().getSupportFragmentManager();
					mFragmentTransaction = mFragmentManager.beginTransaction();
					mFragmentTransaction.replace(R.id.fragmentContainer, mFragmentInvitationScreen);
					mFragmentTransaction.commit();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case R.id.fragInvSent_btn_Received:
				try {
					Utility.startDialog(hostActivity);

					new Thread(new Runnable() {
						@Override
						public void run() {
							handler.post(new Runnable() {
								@Override
								public void run() {
									mFragmentManager = getActivity().getSupportFragmentManager();
									mFragmentTransaction = mFragmentManager.beginTransaction();
									mFragmentTransaction.replace(R.id.fragmentContainer, mFragmentInvitationReceive);
									mFragmentTransaction.commit();
								}
							});
						}
					}).start();

				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
		}
	}

	/**
	 * Display list of sent invites
	 */
	@SuppressLint("DefaultLocale")
	public void populateSentInvitations(){
		try {

			ThatItApplication.getApplication().openDatabase();
			Cursor cursor = One2OneChatDb.getAllRoster();
			cursor.moveToFirst();
			jids.clear();

			if (cursor.moveToFirst()) {
				do {
					String jid = cursor.getString(cursor
							.getColumnIndex(DbOpenHelper.JABBER_ID));
					jids.add(jid);
				} while (cursor.moveToNext());
			}
			cursor.close();

			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {

					LayoutInflater inflater = LayoutInflater.from(getActivity());
					mLinLytInviationsSentContainer.removeAllViews();

					Iterator<Entry<String, Boolean>> it = ThatItApplication.getApplication().getSentInvites().entrySet().iterator();

					while (it.hasNext()) {
						View currentRow = new View(getActivity());
						currentRow = inflater.inflate(R.layout.adapter_fragment_invitations_sent, null);
						InvitationSentView invitationSentView = new InvitationSentView();
						invitationSentView.mtxtVwInvitationRecipient = (TextView)currentRow.findViewById(R.id.invRec_txt_invitationMessage);
						invitationSentView.mtxtVwInvitationMessage = (TextView)currentRow.findViewById(R.id.invRec_txt_invitationMessageInfo);
						invitationSentView.mtxtVwInvitationRecipientID = (TextView)currentRow.findViewById(R.id.invRec_txt_pinNo);
						invitationSentView.imagesUserPic=(ImageView)currentRow.findViewById(R.id.invRec_img_profilePic);
						invitationSentView.btn_removeSentInvite = (ImageButton) currentRow.findViewById(R.id.btn_removeSentInvite);

						final Entry<String, Boolean> entry = it.next();

						invitationSentView.btn_removeSentInvite.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {

								if (NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {

									if (MainService.mService.connection.isConnected() && MainService.mService.connection.isAuthenticated()) {

										Utility.startDialog(getActivity());
										parseUtil.removeRequest(ThatItApplication.getApplication(), AppSinglton.thatsItPincode, entry.getKey(), new ParseCallbackListener() {

											@Override
											public void done(List<ParseObject> receipients, ParseException e,
															 int requestId) {

											}

											@SuppressLint("DefaultLocale")
											@Override
											public void done(ParseException parseException, int requestId) {

												if (parseException == null) {

													removeFriend(entry.getKey());
													populateSentInvitations();
												} else {
													Toast.makeText(ThatItApplication.getApplication(), "Error while cancellation", Toast.LENGTH_SHORT).show();
												}

											}
										}, ParseCallbackListener.OPERATION_FRIEND_REQUEST_DELETED);
									} else {
										Utility.showMessage("Please wait while connection gets restored");
									}
								} else {
									Utility.showMessage(getResources().getString(R.string.Network_Availability));
								}
							}
						});

						try {
							if(Roster.getInstanceFor(MainService.mService.connection).contains(entry.getKey() )){

                                if(!jids.contains(entry.getKey().toLowerCase().split("@")[0])) {

                                    try {
                                        if (MainService.mService.connection != null && MainService.mService.connection.isAuthenticated() && MainService.mService.connection.isConnected()) {
											VCardManager.getInstanceFor(MainService.mService.connection).loadVCard(entry.getKey());
//											card.load(MainService.mService.connection, entry.getKey());
                                        } else {
                                            Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                                        }

                                        if (card != null && card.getFirstName() != null && !Objects.equals(card.getFirstName(), ""))
                                            invitationSentView.mtxtVwInvitationRecipient.setText(card.getFirstName());

                                        else {
                                            invitationSentView.mtxtVwInvitationRecipient.setText("My Name");
                                        }

                                        if (card != null && card.getLastName() != null && !Objects.equals(card.getLastName(), "")) {
                                            invitationSentView.mtxtVwInvitationMessage.setText(card.getLastName());
                                        } else {
                                            invitationSentView.mtxtVwInvitationMessage.setText("Hi there! i would like to add you ");
                                        }
                                        if (card.getField("profile_picture_url") != null) {

                                            ContactActivity.options = new DisplayImageOptions.Builder()
                                                    .displayer(new RoundedBitmapDisplayer(200))
                                                    .showImageOnFail(R.drawable.no_img)
                                                    .showImageForEmptyUri(R.drawable.no_img)
                                                    .cacheInMemory(true)
                                                    .cacheOnDisk(true)
                                                    .build();
                                            ContactActivity.loader.displayImage(card.getField("profile_picture_url"), invitationSentView.imagesUserPic, ContactActivity.options);

                                        }
                                    } catch (XMPPException e) {
                                        e.printStackTrace();
                                    }
                                    invitationSentView.mtxtVwInvitationRecipientID.setText(entry.getKey().split("@")[0]);

                                    mLinLytInviationsSentContainer.addView(currentRow);
                                }
                            }else{
                                Utility.stopDialog();
                            }
						} catch (SmackException.NoResponseException e) {
							e.printStackTrace();
						} catch (SmackException.NotConnectedException e) {
							e.printStackTrace();
						}
					}
					Utility.stopDialog();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove sent invite
	 * @param key
	 */
	private void removeFriend(final String key) {

		ThatItApplication.getApplication().getSentInvites().remove(key.toLowerCase());
		ThatItApplication.getApplication().getSentInvites().remove(key.toUpperCase());
		Utility.removeFriendXMPP(ThatItApplication.getApplication(), key, MainService.mService.connection, true);

	}

	private class InvitationSentView{
		TextView mtxtVwInvitationRecipient;
		TextView mtxtVwInvitationRecipientID;
		TextView mtxtVwInvitationMessage;
		ImageView imagesUserPic;
		ImageButton btn_removeSentInvite;
	}
}
