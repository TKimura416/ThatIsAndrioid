package com.thatsit.android.fragement;

import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.Form;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.thatsit.android.MainService;
import com.thatsit.android.R;
import com.thatsit.android.Utility;
import com.thatsit.android.activities.ContactActivity;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.interfaces.ValidateThatsItIdInterface;
import com.thatsit.android.parseutil.ParseCallbackListener;
import com.thatsit.android.parseutil.ParseUtil;
import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.seasia.myquick.asyncTasks.Validate_ThatsItId_Async;
import com.seasia.myquick.model.AppSinglton;
import com.seasia.myquick.model.ValidateThatsItID;

@SuppressLint("ValidFragment")
public class FragmentInvitationScreen extends Fragment implements OnClickListener {

	private Button mBtnReceived, mBtnSent, mBtnInvites, mBtnSentInvitation,
			mBtnCancel;
	private View mView;
	private FragmentManager mFragmentManager;
	private FragmentTransaction mFragmentTransaction;
	private FragmentInvitationReceive mFragmentInvitationReceive;
	private FragmentInvitationSent mFragmentInvitationSent;
	private EditText mEdit_Id, mEdit_Message;
	private String Ids, Messages;
	private static String idExtension;
	private ContactActivity hostActivity;
	private static final Intent SERVICE_INTENT = new Intent();
	private final ParseUtil parseUtil = new ParseUtil();
	private LinearLayout fragInvite_tabs_lnrlayout;
	static {
		SERVICE_INTENT.setComponent(new ComponentName("com.thatsit.android",
				"com.thatsit.android.MainService"));
	}

	private Handler handler;
	private MainService mService;

	public FragmentInvitationScreen(MainService mService) {
		this.mService = mService;
	}

	public FragmentInvitationScreen() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		((ContactActivity)getActivity()).iconStateChanger(false, false, true, false);
		Utility.fragPaymentSettingsOpen = false;
		Utility.fragChatIsOpen = false;
	}

	@Override
	public void onResume() {
		super.onResume();
		if(hostActivity.mDrawerLayout != null && hostActivity.navDrawerView != null) {
			hostActivity.mDrawerLayout.closeDrawer(hostActivity.navDrawerView);
		}
		Utility.UserPauseStatus(getActivity());
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		hostActivity = (ContactActivity) activity;
	}

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_invite_screen, container,false);

		initialise_Variables();
		setEditTextCapsFilter();
		initialise_Listener();
		if(getResources().getBoolean(R.bool.isTablet)){
			ContactActivity.textView_toolbar_title.setText("Send Invite");
			fragInvite_tabs_lnrlayout.setVisibility(View.GONE);
		}
		try {
			if(getResources().getBoolean(R.bool.isTablet)){
				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			}else{
				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN|WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			}
			handler = new Handler();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mView;
	}

	private void initialise_Variables() {

		mFragmentInvitationReceive = new FragmentInvitationReceive(mService);
		mFragmentInvitationSent = new FragmentInvitationSent(mService);

		fragInvite_tabs_lnrlayout = (LinearLayout)mView.findViewById(R.id.fragInvite_tabs_lnrlayout2);
		mBtnReceived = (Button) mView.findViewById(R.id.fragInvite_btn_Received);
		mBtnSent = (Button) mView.findViewById(R.id.fragInvite_btn_Sent);
		mBtnInvites = (Button) mView.findViewById(R.id.fragInvite_btn_invitaions);
		mBtnSentInvitation = (Button) mView.findViewById(R.id.fragInvite_SentInvite);
		mBtnCancel = (Button) mView.findViewById(R.id.fragInvite_CancelInvite);
		mEdit_Id = (EditText) mView.findViewById(R.id.fragInvite_edt_ID);
		mEdit_Message = (EditText) mView.findViewById(R.id.fragInvite_edt_Message);
	}


	private void setEditTextCapsFilter() {
		//mEdit_Id.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
	}


	private void initialise_Listener() {

		mBtnReceived.setOnClickListener(this);
		mBtnSent.setOnClickListener(this);
		mBtnInvites.setOnClickListener(this);
		mBtnSentInvitation.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		try {
			switch (v.getId()) {

				case R.id.fragInvite_btn_Sent:  // Open Sent Invites

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
				case R.id.fragInvite_btn_Received:

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
										mFragmentTransaction.replace(R.id.fragmentContainer,mFragmentInvitationReceive);
										mFragmentTransaction.commit();
									}
								});
							}
						}).start();

					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case R.id.fragInvite_SentInvite:   // Send Invitation

					try {
						InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(mEdit_Message.getWindowToken(),	0);

						if (NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {

							Ids = mEdit_Id.getText().toString();
							Messages = mEdit_Message.getText().toString();

							if (Ids.trim().toCharArray().length == 0) {
								Toast.makeText(getActivity(),"Please Enter The ID", Toast.LENGTH_SHORT).show();

							} else if (Messages.trim().toCharArray().length == 0) {
								Toast.makeText(getActivity(),"Please Enter Some Message", Toast.LENGTH_SHORT).show();
							}

							else {
								Utility.startDialog(getActivity());
								new Validate_ThatsItId_Async(getActivity(),Ids,mValidateThatsItIdInterface).execute();
							}
						} else {
							Toast.makeText(getActivity(),getResources().getString(R.string.Network_Availability), Toast.LENGTH_SHORT).show();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case R.id.fragInvite_CancelInvite:
					try {
						InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(mEdit_Message.getWindowToken(),0);
						mEdit_Id.setText("");
						mEdit_Message.setText("");

					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				default:
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send friend request to jID
	 */
	private void onAddNewFriends() {
		try {
			if (!MainService.mService.connection.isConnected()){
				Utility.stopDialog();
				return;
			}


			final String jid = mEdit_Id.getText().toString().trim();
			if (jid.length() <= 0){
				Toast.makeText(getActivity(),"Error while sending request.", Toast.LENGTH_SHORT).show();
				Utility.stopDialog();

			}

			if (isValidID(jid)) {
				updateVCardForInviteMessage(jid);
				sendFriendRequest(jid);
			}else{
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getActivity(),"No user exists with this That's It ID",Toast.LENGTH_SHORT).show();
						Utility.stopDialog();
					}
				});
			}
		}
		catch (XMPPException e) {
			Utility.showMessage("Request Failed");
			Utility.stopDialog();
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
			Utility.stopDialog();
		}
	}

	private boolean isSelfInvite(String ids2) {
		if (MainService.mService.connection != null && MainService.mService.connection.isConnected()) {
			if (ids2.trim().equalsIgnoreCase(MainService.mService.connection.getUser().toString()))
				return true;
		}
		return false;
	}

	/**
	 *
	 * @param jid load vard for the jid
	 */
	private void updateVCardForInviteMessage(String jid) {

		try {
			VCard me = new VCard();
			//ProviderManager.getInstance().addIQProvider("vCard", "vcard-temp",new VCardProvider());
			me.load(MainService.mService.connection);
			me.save(MainService.mService.connection);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendFriendRequest(final String jid) {

		parseUtil.addRequest(getActivity(), jid, new ParseCallbackListener() {
			@Override
			public void done(ParseException parseException,int reqId) {
				if(parseException==null){
					idExtension = jid + "@" + MainService.mService.connection.getServiceName();
					Roster roster = Roster.getInstanceFor(MainService.mService.connection);
					try {
						if (!roster.contains(JidCreate.bareFrom(idExtension))) {
                            try {
                                Presence subscribe = new Presence(Presence.Type.subscribe);
                                subscribe.setTo(idExtension);
                                subscribe.setFrom(MainService.mService.connection.getUser());
                                subscribe.setStatus(Messages);
                                MainService.mService.connection.sendPacket(subscribe);
                                ThatItApplication.getApplication().getSentInvites().put(idExtension.toUpperCase(), true);
                                handler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        mEdit_Id.setText("");
                                        mEdit_Message.setText("");
                                        Toast.makeText(getActivity(), "Invitation Sent Successfully", Toast.LENGTH_SHORT).show();
                                        Utility.stopDialog();								}
                                });
                            } catch (Exception e) {
                                e.printStackTrace();

                                parseUtil.removeRequest(getActivity(), AppSinglton.thatsItPincode, jid, new ParseCallbackListener() {
                                    @Override
                                    public void done(List<ParseObject> receipients, ParseException e,
                                                     int requestId) {
                                        Utility.stopDialog();
                                    }

                                    @Override
                                    public void done(ParseException parseException, int requestId) {
                                        Log.e("operation", "Fragment invitation - friend request send operation cancelled");
                                        Utility.stopDialog();
                                    }
                                }, 0);
                            }
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    Toast.makeText(getActivity(), "User already exists in your list", Toast.LENGTH_SHORT).show();

                                    parseUtil.removeRequest(getActivity(), AppSinglton.thatsItPincode,jid, new ParseCallbackListener() {
                                        @Override
                                        public void done(ParseException parseException,int i) {
                                            Utility.stopDialog();
                                        }
                                        @Override
                                        public void done(
                                                List<ParseObject> phoneList,
                                                ParseException e,int i) {
                                            Utility.stopDialog();
                                        }
                                    },0);
                                }
                            });
                        }
					} catch (XmppStringprepException e) {
						e.printStackTrace();
					}

				}else{
					Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.error_sendingRequest), Toast.LENGTH_SHORT).show();
					Utility.stopDialog();
				}
			}

			@Override
			public void done(List<ParseObject> phoneList, ParseException e,int i) {

			}
		},0);
	}

	/**
	 *
	 * @param jidToAdd
	 * @return true if jID is valid
	 * @throws XMPPException
	 */
	private boolean isValidID(String jidToAdd) throws XMPPException {

		if(jidToAdd.length()!=11){
			return false;
		}
		try {

		UserSearchManager search = new UserSearchManager(MainService.mService.connection);
		Form searchForm = search.getSearchForm( MainService.mService.connection.getServiceName());
		Form answerForm = searchForm.createAnswerForm();
		answerForm.setAnswer("Username", true);
		answerForm.setAnswer("search", jidToAdd);
//		ReportedData data = search.getSearchResults(answerForm, "search." + MainService.mService.connection.getXMPPServiceDomain());
		ReportedData data = null;
			data = search.getSearchResults(answerForm, MainService.mService.connection.getServiceName());

		if (data.getRows() != null ) {
//			Iterator<Row> it = data.getRows();
			List<ReportedData.Row> it = data.getRows();
			if (it.size()>0) {
				return true;

			}
		}
		} catch (SmackException.NoResponseException e) {
			e.printStackTrace();
		} catch (SmackException.NotConnectedException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
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

					if (!isSelfInvite(Ids)){

						try {
							ThatItApplication.getApplication().getSentInvitesMessages().put(Ids, Messages.trim());
							Utility.startDialog(FragmentInvitationScreen.this.getActivity());

							new Thread(new Runnable() {
								@Override
								public void run() {

									onAddNewFriends();
								}
							}).start();

						} catch (Exception e) {
							e.printStackTrace();
							Utility.stopDialog();
						}
					}
					else{
						mEdit_Id.setText("");
						mEdit_Message.setText("");
						Utility.stopDialog();
						Toast.makeText(getActivity(),"You Cannot Send Request to Yourself ",Toast.LENGTH_SHORT).show();
					}

				}else{
					// open remove roster alert
					Utility.stopDialog();
					Utility.showMessage("That's It ID does not exist");
				}
			}else{
				Utility.stopDialog();
			}
		}
	};

}
