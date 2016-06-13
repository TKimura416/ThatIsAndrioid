package com.thatsit.android.fragement;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.thatsit.android.MainService;
import com.thatsit.android.R;
import com.thatsit.android.Utility;
import com.thatsit.android.activities.ContactActivity;
import com.thatsit.android.activities.SelectAudioActivity;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.encryption.helper.EncryptionManager;
import com.thatsit.android.interfaces.ChangeChatPasswordInterface;
import com.thatsit.android.xmpputils.Constants;
import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.seasia.myquick.asyncTasks.ChangeChatPasswordAsync;
import com.seasia.myquick.model.AppSinglton;
import com.seasia.myquick.model.UpdateMessagePaswordTemplate;

import java.net.URLEncoder;

@SuppressLint("ValidFragment")
public class FragmentChatSetting extends Fragment implements OnClickListener{
	final String TAG = "FragmentChatSetting";
	private View mView;
	private Button mBtnBasic_Setting,mBtnChat_Setting,mBtnPayment_Setting,mBtn_Done;
	private RadioButton mRadio_Available,mRadio_Busy,radioStatusButton,radioVibrateButton;
	private EditText mEdt_OldPasswrd,mEdt_NewPasswrd,mEdt_ConfirmNewPasswrd;
	private TextView edt_tagLine;
	private FragmentManager mFragmentManager;
	private FragmentTransaction mFragmentTransaction;
	private FragmentBasicSetting mFragmentBasicSetting;
	private FragmentPaymentSetting mFragmentPaymentSetting;
	private String OldPassword,NewPassword,ConfirmNewPassword,RetVal;
	private String RadioStatus,RadioVibrate;
	private SharedPreferences mSharedPreferences;
	private TextView text_PseudoName,tv_setNotificationTone;
	private RadioGroup mRgroupStatus,mRgroupVibrate;
	private static boolean IsvibrateOn = false;
	private static boolean isAvailable=false;
	private static RadioButton mRadio_VibrateOn;
	private static RadioButton mRadio_VibrateOff;
	private SharedPreferences mSharedPreferencesVibrate,mSharedPreferencesVibRead,
			mSharedPreferences_reg,mSharedPreferencesAvailable,mSharedPreferencesAvlRead;
	private MainService mService;
	private static final Intent SERVICE_INTENT = new Intent();
	private EncryptionManager encryptionManager;
	private LinearLayout fragInvite_tabs_lnrlayout;
	private ContactActivity hostActivity;
	static {
		SERVICE_INTENT.setComponent(new ComponentName(Constants.MAINSERVICE_PACKAGE,  Constants.MAINSERVICE_PACKAGE + Constants.MAINSERVICE_NAME ));
	}
	public FragmentChatSetting(MainService mainService){
		mService = mainService;
	}
	public FragmentChatSetting(){
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		((ContactActivity)getActivity()).iconStateChanger(false, false, false, true);
		Utility.fragPaymentSettingsOpen = false;

	}
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		hostActivity = (ContactActivity) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		mView= inflater.inflate(R.layout.fragment_account_tab_chat_settings, container, false);

		encryptionManager = new EncryptionManager();
		initialise_Variables();
		if(getResources().getBoolean(R.bool.isTablet)){
			ContactActivity.textView_toolbar_title.setText("Chat Settings");
			fragInvite_tabs_lnrlayout.setVisibility(View.GONE);
		}
		readSharedPreferenceValues();
		checkVibrateOptions();
		checkAvailavibilityOptions();
		initialise_Listener();
		initialise_RadioButtons();
		setWindowState();
		return mView;
	}

	private void setWindowState() {

		try {
			if(getResources().getBoolean(R.bool.isTablet)){
				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			}else{
				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN|WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if(hostActivity.mDrawerLayout != null && hostActivity.navDrawerView != null) {
			hostActivity.mDrawerLayout.closeDrawer(hostActivity.navDrawerView);
		}
		Utility.UserPauseStatus(getActivity());
	}

	/**
	 * Get user stored info from shared preference
	 */
	private void readSharedPreferenceValues() {

		mSharedPreferencesVibRead=getActivity().getSharedPreferences("mSharedPreferencesVibrateValue",1);
		RadioVibrate = mSharedPreferencesVibRead.getString("Vibrate_Value", "");
		mSharedPreferencesAvlRead=getActivity().getSharedPreferences("mSharedPreferencesAvlValue",1);
		RadioStatus = mSharedPreferencesAvlRead.getString("Avl_Value", "");

		mSharedPreferences_reg = getActivity().getSharedPreferences("register_data", 0);

		mSharedPreferences = getActivity().getSharedPreferences("USERID", getActivity().MODE_WORLD_READABLE);
		AppSinglton.userId = mSharedPreferences.getString("USERID", "");

		String psedoname=mSharedPreferences_reg.getString("PseudoName","");
		String profiledescription=mSharedPreferences_reg.getString("Pseudodescription", "");
		if(psedoname.equals("")){
			text_PseudoName.setText("My Name");
		}
		else{
			text_PseudoName.setText(psedoname);
		}
		if(profiledescription.equals("")){
			edt_tagLine.setText("Hi, I am using That's It");
		}
		else{
			edt_tagLine.setText(profiledescription);
		}
	}


	/**
	 * Initialise class variables.
	 */
	private void initialise_Variables() {

		mFragmentBasicSetting=new FragmentBasicSetting(mService);
		mFragmentPaymentSetting=new FragmentPaymentSetting(mService);

		fragInvite_tabs_lnrlayout = (LinearLayout)mView.findViewById(R.id.fragInvite_tabs_lnrlayout);
		mBtnBasic_Setting =(Button)mView.findViewById(R.id.fragActChatSet_btn_basicSet);
		mBtnChat_Setting=(Button)mView.findViewById(R.id.fragActChatSet_btn_chatSet);
		mBtnPayment_Setting=(Button)mView.findViewById(R.id.fragActChatSet_btn_paymentSet);
		mBtn_Done = (Button) mView.findViewById(R.id.fragActBasicSet_btn_Done);
		mRadio_Available = (RadioButton) mView.findViewById(R.id.fragActChatSet_Radio_avl);
		mRadio_Busy = (RadioButton) mView.findViewById(R.id.fragActChatSet_Radio_busy);
		mRadio_VibrateOn = (RadioButton) mView.findViewById(R.id.fragActChatSet_Radio_VibrateOn);
		mRadio_VibrateOff = (RadioButton) mView.findViewById(R.id.fragActChatSet_Radio_VibtareOff);
		mRgroupStatus = (RadioGroup) mView.findViewById(R.id.radio_grpStatus);
		mRgroupVibrate = (RadioGroup) mView.findViewById(R.id.radio_grpVibrate);
		mEdt_OldPasswrd = (EditText) mView.findViewById(R.id.fragActChatSet_edt_Oldpwd);
		mEdt_NewPasswrd = (EditText) mView.findViewById(R.id.fragActChatSet_edt_Newpwd);
		mEdt_ConfirmNewPasswrd = (EditText) mView.findViewById(R.id.fragActChatSet_edt_confNewPwd);
		text_PseudoName=(TextView)mView.findViewById(R.id.text_PseudoName);
		edt_tagLine=(TextView)mView.findViewById(R.id.edt_tagLine);
		tv_setNotificationTone=(TextView)mView.findViewById(R.id.tv_setNotificationTone);
	}

	/**
	 * Initialise click listeners.
	 */
	private void initialise_RadioButtons() {
		mRadio_Available.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if(NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())){
					if(MainService.mService.connection.isConnected()
							&& MainService.mService.connection.isAuthenticated()){

						try {
							isAvailable=true;
							Utility.isBusy = false;
							Presence p = new Presence(Type.available);
							MainService.mService.connection.sendPacket(p);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						try {
							// get the selected radio button from the group
							int selectedOption = mRgroupStatus.getCheckedRadioButtonId();
							radioStatusButton = (RadioButton) mView.findViewById(selectedOption);
							RadioStatus = radioStatusButton.getText().toString();

							mSharedPreferencesAvailable=getActivity().getSharedPreferences("mSharedPreferencesAvlValue",0);
							mSharedPreferencesAvailable.edit().putString("Avl_Value",RadioStatus).commit();

							switch (RadioStatus) {
								case "Available":
									mRadio_Available.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tick, 0, 0, 0);
									mRadio_Busy.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);

									break;
								case "Busy":
									mRadio_Available.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
									mRadio_Busy.setCompoundDrawablesWithIntrinsicBounds(R.drawable.busy, 0, 0, 0);

									break;
								default:
									mRadio_Available.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tick, 0, 0, 0);
									mRadio_Busy.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
									break;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}else{
						Utility.showMessage("Please wait while connection is restored");
					}
				}else{
					Utility.showMessage(getResources().getString(R.string.Network_Availability));
				}
			}
		});
		mRadio_Busy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					if (NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {

						if (MainService.mService.connection.isConnected()
								&& MainService.mService.connection.isAuthenticated()) {

							isAvailable = false;
							Utility.isBusy = true;
							Presence p = new Presence(Type.available, "I am busy", 42, Mode.dnd);
							MainService.mService.connection.sendPacket(p);

							try {
								// get the selected radio button from the group
								int selectedOption = mRgroupStatus.getCheckedRadioButtonId();
								radioStatusButton = (RadioButton) mView.findViewById(selectedOption);
								RadioStatus = radioStatusButton.getText().toString();

								mSharedPreferencesAvailable = getActivity().getSharedPreferences("mSharedPreferencesAvlValue", 0);
								mSharedPreferencesAvailable.edit().putString("Avl_Value", RadioStatus).commit();

								switch (RadioStatus) {
									case "Available":
										mRadio_Available.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tick, 0, 0, 0);
										mRadio_Busy.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);

										break;
									case "Busy":
										mRadio_Available.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
										mRadio_Busy.setCompoundDrawablesWithIntrinsicBounds(R.drawable.busy, 0, 0, 0);

										break;
									default:
										mRadio_Available.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tick, 0, 0, 0);
										mRadio_Busy.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
										break;
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							Utility.showMessage("Please wait while connection is restored");
						}
					} else {
						Utility.showMessage(getResources().getString(R.string.Network_Availability));
					}

				} catch (Exception e1) {
					e1.printStackTrace();
				}

			}
		});

		mRadio_VibrateOn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					IsvibrateOn = true;
					// get the selected radio button from the group
					int selectedOption = mRgroupVibrate.getCheckedRadioButtonId();
					radioVibrateButton = (RadioButton) mView.findViewById(selectedOption);
					RadioVibrate = radioVibrateButton.getText().toString();

					mSharedPreferencesVibrate = getActivity().getSharedPreferences("mSharedPreferencesVibrateValue", 0);
					mSharedPreferencesVibrate.edit().putString("Vibrate_Value", RadioVibrate).commit();

					switch (RadioVibrate) {
						case "On":
							if (!checkIfVibratorExists()) {
								Utility.showMessage("Your device does not support vibration");
							}
							mRadio_VibrateOn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);
							mRadio_VibrateOff.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);

							break;
						case "Off":
							mRadio_VibrateOn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
							mRadio_VibrateOff.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);

							break;
						default:
							mRadio_VibrateOn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
							mRadio_VibrateOff.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);
							break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		mRadio_VibrateOff.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					IsvibrateOn = false;

					// get the selected radio button from the group
					int selectedOption = mRgroupVibrate.getCheckedRadioButtonId();
					radioVibrateButton = (RadioButton) mView.findViewById(selectedOption);
					RadioVibrate = radioVibrateButton.getText().toString();

					mSharedPreferencesVibrate=getActivity().getSharedPreferences("mSharedPreferencesVibrateValue",0);
					mSharedPreferencesVibrate.edit().putString("Vibrate_Value",RadioVibrate).commit();

					switch (RadioVibrate) {
						case "On":
							mRadio_VibrateOn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);
							mRadio_VibrateOff.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);

							break;
						case "Off":
							mRadio_VibrateOn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
							mRadio_VibrateOff.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);

							break;
						default:
							mRadio_VibrateOn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
							mRadio_VibrateOff.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);
							break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		mEdt_ConfirmNewPasswrd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					submitPassword();
					handled = true;
				}
				return handled;
			}
		});
	}

	private void submitPassword() {

		if (NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {
			check_Validation();
		} else {
			Toast.makeText(getActivity(),getResources().getString(R.string.Network_Availability), Toast.LENGTH_SHORT).show();
		}
	}

	private boolean checkIfVibratorExists() {
		String vs = Context.VIBRATOR_SERVICE;
		Vibrator mVibrator = (Vibrator)getActivity().getSystemService(vs);
		return mVibrator.hasVibrator();
	}

	/**
	 * Check Vibration mode - On or Off
	 */
	private void checkVibrateOptions(){
		IsvibrateOn = false;
		switch (RadioVibrate) {
			case "On":
				mRadio_VibrateOn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);
				mRadio_VibrateOff.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);

				break;
			case "Off":
				mRadio_VibrateOn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
				mRadio_VibrateOff.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);

				break;
			default:
				mRadio_VibrateOn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
				mRadio_VibrateOff.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);
				break;
		}
	}

	/**
	 * Check Availability Options- Available or Busy
	 */
	private void checkAvailavibilityOptions(){
		isAvailable=true;

		switch (RadioStatus) {
			case "Available":
				mRadio_Available.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tick, 0, 0, 0);
				mRadio_Busy.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);

				break;
			case "Busy":
				mRadio_Available.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
				mRadio_Busy.setCompoundDrawablesWithIntrinsicBounds(R.drawable.busy, 0, 0, 0);

				break;
			default:
				mRadio_Available.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tick, 0, 0, 0);
				mRadio_Busy.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
				break;
		}
	}

	private void initialise_Listener() {
		mBtnBasic_Setting.setOnClickListener(this);
		mBtnChat_Setting.setOnClickListener(this);
		mBtnPayment_Setting.setOnClickListener(this);
		mBtn_Done.setOnClickListener(this);
		tv_setNotificationTone.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.fragActChatSet_btn_basicSet:
				try {
					//mFragmentBasicSetting.setRetainInstance(false);
					mFragmentManager = getActivity().getSupportFragmentManager();
					mFragmentTransaction = mFragmentManager.beginTransaction();
					mFragmentTransaction.replace(R.id.fragmentContainer, mFragmentBasicSetting);
					mFragmentTransaction.commit();
					//ContactActivity.mStackChatAccount.pop();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case R.id.fragActChatSet_btn_paymentSet:
				try {
					//	mFragmentPaymentSetting.setRetainInstance(true);
					mFragmentManager = getActivity().getSupportFragmentManager();
					mFragmentTransaction = mFragmentManager.beginTransaction();
					mFragmentTransaction.replace(R.id.fragmentContainer, mFragmentPaymentSetting);
					mFragmentTransaction.commit();

				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case R.id.fragActBasicSet_btn_Done:
				try {
					InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mEdt_OldPasswrd.getWindowToken(), 0);
					submitPassword();

				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case R.id.tv_setNotificationTone:
				Intent intent = new Intent(getActivity(),SelectAudioActivity.class);
				startActivity(intent);
				break;
		}
	}

	/**
	 * Check if all fields are filled and are correct.
	 */
	private void check_Validation() {
		try {
			OldPassword = encryptionManager.encryptPayload(mEdt_OldPasswrd.getText().toString());
			NewPassword = encryptionManager.encryptPayload(mEdt_NewPasswrd.getText().toString());
			ConfirmNewPassword = encryptionManager.encryptPayload(mEdt_ConfirmNewPasswrd.getText().toString());

			if (mEdt_OldPasswrd.getText().toString().trim().toCharArray().length == 0){
				Toast.makeText(getActivity(),"Please Enter Old Password", Toast.LENGTH_SHORT).show();
			}
			else if (mEdt_NewPasswrd.getText().toString().trim().toCharArray().length == 0){
				Toast.makeText(getActivity(),"Please Enter New Password", Toast.LENGTH_SHORT).show();
			}
			else if (mEdt_NewPasswrd.getText().toString().trim().toCharArray().length>15){
				Toast.makeText(getActivity(),"Maximum Chat Password Length Should be 15 Characters", Toast.LENGTH_SHORT).show();
			}
			else if (mEdt_ConfirmNewPasswrd.getText().toString().trim().toCharArray().length == 0){
				Toast.makeText(getActivity(),"Please Confirm New Password", Toast.LENGTH_SHORT).show();
			}
			else if (!NewPassword.equals(ConfirmNewPassword)){
				Toast.makeText(getActivity(),"Passwords Do Not Match", Toast.LENGTH_SHORT).show();
			}
			else if (mEdt_NewPasswrd.getText().toString().trim().toCharArray().length<6){
				Toast.makeText(getActivity(),"Minimum Chat Password Length Should be 6 Characters", Toast.LENGTH_SHORT).show();
			}
			else {
				Utility.startDialog(getActivity());

				OldPassword = URLEncoder.encode(OldPassword, "UTF-8");
				if (OldPassword.contains("%")) {
					OldPassword = OldPassword.replace("%", "2");
				}
				NewPassword = URLEncoder.encode(NewPassword, "UTF-8");
				if (NewPassword.contains("%")) {
					NewPassword = NewPassword.replace("%", "2");
				}

				if (NewPassword.equalsIgnoreCase(Utility.getPassword())) {
					Utility.stopDialog();
					Toast.makeText(getActivity()," Login and Chat Passwords must be different ",Toast.LENGTH_SHORT).show();
				}
				else {
					new ChangeChatPasswordAsync(getActivity(), AppSinglton.userId,
							OldPassword, NewPassword, mChangeChatPasswordInterface).execute();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 *  Change chat password interface.
	 */
	private final ChangeChatPasswordInterface mChangeChatPasswordInterface = new ChangeChatPasswordInterface() {

		@Override
		public void changeChatPassword(
				UpdateMessagePaswordTemplate mUpdateMessagePaswordTemplate) {

			try {
				mEdt_OldPasswrd.setText("");
				mEdt_NewPasswrd.setText("");
				mEdt_ConfirmNewPasswrd.setText("");

				RetVal=mUpdateMessagePaswordTemplate.getmUpdateMessagePasswordResult().getmUpdateMessagePasswordParams()[0].getRetVal();
				Utility.stopDialog();

				switch (RetVal) {
					case "1":
						SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
						edit.putString(ThatItApplication.ACCOUNT_CHAT_PASSWORD_KEY, NewPassword);
						edit.commit();
						Toast.makeText(getActivity(), "Password Successfully Changed", Toast.LENGTH_LONG).show();
						break;
					case "2":
						Toast.makeText(getActivity(), "Old Password Does Not Match", Toast.LENGTH_LONG).show();
						break;
					default:
						Toast.makeText(getActivity(), "Password could not be changed. Please try again", Toast.LENGTH_LONG).show();
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
}
