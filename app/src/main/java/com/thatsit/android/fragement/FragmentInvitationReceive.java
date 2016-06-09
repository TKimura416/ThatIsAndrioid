package com.thatsit.android.fragement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.net.io.Util;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smackx.packet.VCard;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.thatsit.android.MainService;
import com.thatsit.android.R;
import com.thatsit.android.RefreshApplicationListener;
import com.thatsit.android.Utility;
import com.thatsit.android.activities.ContactActivity;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.parseutil.ParseCallbackListener;
import com.thatsit.android.parseutil.ParseOperationDecider;
import com.thatsit.android.parseutil.ParseUtil;
import com.thatsit.android.xmpputils.Constants;
import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.seasia.myquick.model.AppSinglton;

@SuppressLint("ValidFragment")
public class FragmentInvitationReceive extends Fragment implements OnClickListener {

	private VCard card = new VCard();
	private Handler handler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utility.fragmentInvitationReceive = FragmentInvitationReceive.this;
		((ContactActivity)getActivity()).iconStateChanger(false,false, true,false);
		Utility.fragPaymentSettingsOpen = false;
	}


	@Override
	public void onResume() {
		super.onResume();
		Utility.fragmentInvitationReceive = FragmentInvitationReceive.this;

		if (getResources().getBoolean(R.bool.isTablet)) {
			if (hostActivity.mDrawerLayout != null && hostActivity.navDrawerView != null) {
				hostActivity.mDrawerLayout.closeDrawer(hostActivity.navDrawerView);
			}
		}
		Utility.UserPauseStatus(getActivity());
	}

	private Button mBtnReceived, mBtnSent, mBtnInvites;
	private View mView;
	private FragmentManager mFragmentManager;
	private FragmentTransaction mFragmentTransaction;
	private FragmentInvitationScreen mFragmentInvitationScreen;
	private FragmentInvitationReceive mFragmentInvitationReceive;
	private FragmentInvitationSent mFragmentInvitationSent;
	private static final Intent SERVICE_INTENT = new Intent();
	private MainService mService;
	private LinearLayout invitationsContainer;
	private SharedPreferences mSharedPreferences_Pincode;
	private ContactActivity hostActivity;
	private LinearLayout fragInvRec_tabs_lnrlayout;

	static {
		SERVICE_INTENT.setComponent(new ComponentName("com.thatsit.android",
				"com.thatsit.android.MainService"));
	}

	/**
	 * Constructor.
	 */
	public FragmentInvitationReceive() {
	}

	public FragmentInvitationReceive(MainService service) {
		mService = service;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

		mView = inflater.inflate(R.layout.fragment_invitations_received,container, false);

		initialise_Variables();
		initialise_Listener();
		if(getResources().getBoolean(R.bool.isTablet)){
			ContactActivity.textView_toolbar_title.setText("Received Invitations");
			fragInvRec_tabs_lnrlayout.setVisibility(View.GONE);
		}
		handler = new Handler();
		mSharedPreferences_Pincode = getActivity().getSharedPreferences("THATSITID", 0);
		AppSinglton.thatsItPincode = mSharedPreferences_Pincode.getString("THATSITID", "");
		try {
			getActivity().getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
					| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
					| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return mView;
	}

	private void initialise_Variables() {

		mFragmentInvitationScreen = new FragmentInvitationScreen(mService);
		mFragmentInvitationReceive = new FragmentInvitationReceive(mService);
		mFragmentInvitationSent = new FragmentInvitationSent(mService);
		fragInvRec_tabs_lnrlayout = (LinearLayout)mView.findViewById(R.id.fragInvRec_tabs_lnrlayout);
		invitationsContainer = (LinearLayout) mView.findViewById(R.id.fragInvRec_list_invitationsSent);
		mBtnReceived = (Button) mView.findViewById(R.id.fragInvRec_btn_Received);
		mBtnSent = (Button) mView.findViewById(R.id.fragInvRec_btn_Sent);
		mBtnInvites = (Button) mView.findViewById(R.id.fragInvRec_btn_invitaions);
	}

	private void initialise_Listener() {

		mBtnReceived.setOnClickListener(this);
		mBtnSent.setOnClickListener(this);
		mBtnInvites.setOnClickListener(this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		hostActivity = (ContactActivity) activity;
	}

	@Override
	public void onStart() {
		super.onStart();

		populateIncomingInvitation();
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.fragInvRec_btn_invitaions:
			try {
				mFragmentManager = getActivity().getSupportFragmentManager();
				mFragmentTransaction = mFragmentManager.beginTransaction();
				mFragmentTransaction.replace(R.id.fragmentContainer,mFragmentInvitationScreen);
				mFragmentTransaction.commit();

			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.fragInvRec_btn_Sent:
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
								mFragmentTransaction.replace(R.id.fragmentContainer,mFragmentInvitationSent);
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
	 * Add all incoming invitaions.
	 */
	public void populateIncomingInvitation() {

		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				try {
					LayoutInflater inflater = LayoutInflater.from(getActivity());
					invitationsContainer.removeAllViews();

					if(ThatItApplication.getApplication().getIncomingRequestHash().keySet() != null) {

						for (final String key : ThatItApplication.getApplication().getIncomingRequestHash().keySet()) {


							Roster roster = MainService.mService.connection.getRoster();
							Collection<RosterEntry> entries = roster.getEntries();
							List<RosterEntry> userList = new ArrayList<>(entries);

							ArrayList<String> existIds = new ArrayList<>();
							for (int i = 0; i < userList.size(); i++) {
								String userId = userList.get(i).getUser(); 
								Log.v(""+i, "userId->"+userId);
								existIds.add(userId);
							}


							if(!existIds.contains(key)){ 

								final InvitationView invitationView = new InvitationView();
								View currentinvitationView = new View(getActivity());
								currentinvitationView = inflater.inflate(R.layout.adapter_fragment_invitations_received, null);

								invitationView.txtvwInvitationSender = (TextView) currentinvitationView.findViewById(R.id.invRec_txt_invitationMessage);
								invitationView.txtvwinvitationMessage = (TextView) currentinvitationView.findViewById(R.id.invRec_txt_invitationMessageInfo);
								invitationView.txtvwinvitationSenderID = (TextView) currentinvitationView.findViewById(R.id.invRec_txt_pinNo);
								invitationView.imagesUserPic = (ImageView) currentinvitationView.findViewById(R.id.invRec_img_profilePic);
								invitationView.btnAccept = (Button) currentinvitationView.findViewById(R.id.invRec_btn_accept);
								invitationView.btnDeny = (Button) currentinvitationView.findViewById(R.id.invRec_btn_decline);

								try {
									card.load(MainService.mService.connection, key);
									if (card.getFirstName() == null) {
										invitationView.txtvwInvitationSender.setText("My Name");
									} else {
										invitationView.txtvwInvitationSender.setText(card.getFirstName());
									}

									if (card.getField("profile_picture_url") != null) {

										ContactActivity.options = new DisplayImageOptions.Builder().displayer(new RoundedBitmapDisplayer(200))
										.showImageOnFail(R.drawable.no_img)
										.showImageForEmptyUri(R.drawable.no_img)
										.cacheInMemory(true)
										.cacheOnDisk(true)
										.build();
										ContactActivity.loader.displayImage(card.getField("profile_picture_url"), invitationView.imagesUserPic, ContactActivity.options);

									}
								} catch (XMPPException e) {
									invitationView.txtvwInvitationSender.setText(key);
									e.printStackTrace();
								}

								invitationView.txtvwinvitationSenderID.setText(key.split("@")[0]);
								Presence p = (Presence) ThatItApplication.getApplication().getIncomingRequestHash().get(key);

								String getStatus = p.getStatus();
								if (getStatus == null) {
									invitationView.txtvwinvitationMessage.setText("Hi,can we be friends");
								} else {
									invitationView.txtvwinvitationMessage.setText(getStatus);
								}
								invitationView.btnAccept.setOnClickListener(new View.OnClickListener() {

									@Override
									public void onClick(View v) {

										if(NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())){
											try {
												Utility.startDialog(getActivity());

												parseUtil.updateOperation(getActivity(), key, AppSinglton.thatsItPincode, new ParseCallbackListener() {

													@Override
													public void done(List<ParseObject> receipients, ParseException e,
															int requestId) {
													}

													@Override
													public void done(ParseException parseException, int requestId) {

														if (parseException == null) {

															ThatItApplication.getApplication().getIncomingRequestHash().remove(key);
															sendUserPresence(key);
															populateIncomingInvitation();
															Toast.makeText(getActivity(), "Friend Request Accepted", Toast.LENGTH_SHORT).show();
															NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
															notificationManager.cancel(MainService.NOTIFICATION_FRIEND_REQUEST);

														} else {
															Utility.stopDialog();
															Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.error_sendingRequest), Toast.LENGTH_SHORT).show();
														}
													}
												}, ParseCallbackListener.OPERATION_FRIEND_REQUEST_ACCEPTED, ParseOperationDecider.FRIEND_REQUEST_ACCEPTED);
											} catch (Exception e) {
												e.printStackTrace();
												Utility.stopDialog();
											}
										}else{
											Utility.showMessage(getResources().getString(R.string.Network_Availability));
										}
									}
								});

								invitationView.btnDeny.setOnClickListener(new View.OnClickListener() {

									@Override
									public void onClick(View v) {

										if(NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())){
											try {

												Utility.startDialog(getActivity());
												parseUtil.removeRequest(getActivity(), key, AppSinglton.thatsItPincode, new ParseCallbackListener() {

													@Override
													public void done(List<ParseObject> receipients, ParseException e,
															int requestId) {
													}

													@Override
													public void done(ParseException parseException, int requestId) {

														ParseUtil parseUtil = new ParseUtil();
														parseUtil.removeRequest(ThatItApplication.getApplication(), AppSinglton.thatsItPincode, key, new ParseCallbackListener() {

															@Override
															public void done(List<ParseObject> receipients, ParseException e,
																	int requestId) {
															}

															@Override
															public void done(ParseException parseException, int requestId) {

																if (parseException == null) {
																	removeFriendRequest(key);
																	populateIncomingInvitation();
																	NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
																	notificationManager.cancel(mService.NOTIFICATION_FRIEND_REQUEST);

																} else {
																	Utility.stopDialog();
																	Toast.makeText(getActivity(), "Error in cancelling request", Toast.LENGTH_SHORT).show();
																}
															}
														}, requestId);

													}
												}, ParseCallbackListener.OPERATION_FRIEND_REQUEST_DELETED);
												;

											} catch (Exception e1) {
												e1.printStackTrace();
												Utility.stopDialog();
											}
										}else{
											Utility.showMessage(getResources().getString(R.string.Network_Availability));
										}
									}
								});
								invitationsContainer.addView(currentinvitationView);
							}
						}
						Utility.stopDialog();
					}else{
						Utility.stopDialog();
					}
				} catch (Exception e) {
					Utility.stopDialog();
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 *
	 * @param key - jID to be removed
	 */
	private void removeFriendRequest(final String key) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				Utility.removeFriendXMPP(ThatItApplication.getApplication(),key, MainService.mService.connection,true);

			}
		}).start();

	}

	/**
	 * send presence available to jID
	 * @param key   - JID
	 */
	private void sendUserPresence(final String key) {

		new Thread(new Runnable() {

			@Override
			public void run() {

				Utility.onFriendAdded(key);
				Presence presence = new Presence(Type.subscribe);
				presence.setTo(key);
				Utility.sendPresence(presence, key, MainService.mService.connection);

				Presence presence_ = new Presence(Type.subscribed);
				presence_.setTo(key);
				Utility.sendPresence(presence_, key, MainService.mService.connection);

			}
		}).start();
	}

	ParseUtil parseUtil = new ParseUtil();

	private class InvitationView {
		TextView txtvwInvitationSender;
		TextView txtvwinvitationMessage;
		TextView txtvwinvitationSenderID;
		Button btnAccept, btnDeny;
		ImageView imagesUserPic;
	}
}
