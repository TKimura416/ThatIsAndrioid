package com.thatsit.android.activities;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;

import org.jivesoftware.smack.XMPPConnection;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.seasia.myquick.asyncTasks.GetSubscriptionFeeAsync;
import com.seasia.myquick.asyncTasks.GetUserIdAsync;
import com.seasia.myquick.asyncTasks.RegisterUserOnChatServerAsyncTask;
import com.seasia.myquick.asyncTasks.RegistrationAsyncTask;
import com.seasia.myquick.asyncTasks.ValidatePincodeAsync;
import com.seasia.myquick.model.AppSinglton;
import com.seasia.myquick.model.GenerateKeyIdResponse;
import com.seasia.myquick.model.InsertUserResponseTemplate;
import com.seasia.myquick.model.SubcriptionFeeTemplate;
import com.seasia.myquick.model.ValidatePincode;
import com.thatsit.android.MainService;
import com.thatsit.android.R;
import com.thatsit.android.Utility;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.encryption.helper.EncryptionManager;
import com.thatsit.android.interfaces.OrientationListener;
import com.thatsit.android.interfaces.RegisterInterface;
import com.thatsit.android.interfaces.SubscriptionFeeInterface;
import com.thatsit.android.interfaces.UserIdInterface;
import com.thatsit.android.interfaces.ValidatePincodeInterface;
import com.thatsit.android.view.OrientationManager;
import com.thatsit.android.xmpputils.XmppManager;

/**
 *
 * In this class, user info is being registered on Admin website as well as Openfire
 */

public class RegisterActivity extends Activity implements OnClickListener,
		OnTouchListener,OrientationListener {

	private Button mBtn_Register;
	public static TextView mTxt_ThatsItId;
	private EditText mEdt_Password, mEdt_ConfrmPassword, mEdt_MeesagePasswd,
			mEdt_ConfirmMeesagePasswd,edt_Register_email,edt_enterPincode;
	private String country, age, gender;
	private Spinner mCountryListSpinner, mYearOfBirthSpinner;
	private RadioGroup mRgroup;
	private RadioButton radioGenderButton, mRadio_Male, mRadio_Female;
	boolean mBinded;
	private String chatUserName = "";
	private EncryptionManager encryptionManager;
	private String login_password = "";
	private String email;
	private String RetVal;
	private SharedPreferences settings;
	private XmppManager mXmppManager;
	private XMPPConnection mConnection;
	private Utility mUtility = new Utility();
	private String Chat_password;
	private SharedPreferences mSharedPreferences;
	private RelativeLayout rltv_top;
	private String subcriptionFee;
	private int SCREEN_ORIENTATION=0;
	private OrientationManager orientationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_register);

		orientationManager = new OrientationManager(RegisterActivity.this, SensorManager.SENSOR_DELAY_NORMAL, this);
		orientationManager.enable();
		try {
			Utility.setDeviceTypeAndSecureFlag(RegisterActivity.this);
			encryptionManager = new EncryptionManager();
			initialiseXmppConnection();
			//getValueFromBundle();
			initialise_Variables();
			initialiseSharedPreferences();
			if(Utility.hasPincode == true){
				rltv_top.setVisibility(View.GONE);
				edt_enterPincode.setVisibility(View.VISIBLE);
			}else{
				rltv_top.setVisibility(View.VISIBLE);
				edt_enterPincode.setVisibility(View.GONE);
				get16DigitUserID();
			}
			initialise_Listeners();
			initialise_Spinner_Age();
			restoreState();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void restoreState() {

		if (Utility.radioBtnValue == 1 || Utility.radioBtnValue == 2) {
			if (Utility.radioBtnValue == 1) {
				gender = "Female";
				mRadio_Female.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);
				mRadio_Male.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
			} else if (Utility.radioBtnValue == 2) {
				gender = "Male";
				mRadio_Female.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
				mRadio_Male.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);
			}
		}
	}

	private void get16DigitUserID() {

		Utility.startDialog(RegisterActivity.this);
		new	GetUserIdAsync(RegisterActivity.this,mUserIdInterface).execute();
	}

	private void getSubscriptionFee() {

		new GetSubscriptionFeeAsync(RegisterActivity.this,mSubscriptionFeeInterface).execute();
	}

	/**
	 * Method to set visibility of pincode.
	 */
	private void setPincodeVisibility() {

		mTxt_ThatsItId.setText(AppSinglton.thatsItPincode);
		Utility.stopDialog();
	}

	/**
	 * Method to initialise Xmpp manager and connection.
	 */

	private void initialiseXmppConnection() {
		mXmppManager = XmppManager.getInstance();
		mConnection = mXmppManager.getXMPPConnection();
	}

	/**
	 * Method to initialise sharedprefence to local data storage .
	 */
	private void initialiseSharedPreferences() {
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		mSharedPreferences = getSharedPreferences("USERID", 0);
	}

	public RegisterActivity() {
	}


	/**
	 * Initialise class variables.
	 */
	private void initialise_Variables() {
		mBtn_Register = (Button) findViewById(R.id.btn_Register);
		mTxt_ThatsItId = (TextView) findViewById(R.id.txt_pincode_Value);
		mEdt_Password = (EditText) findViewById(R.id.Register_edttxt_enterPwd);
		mEdt_ConfrmPassword = (EditText) findViewById(R.id.Register_edttxt_confirmPwd);
		mEdt_MeesagePasswd = (EditText) findViewById(R.id.edt_createChatPass);
		mEdt_ConfirmMeesagePasswd = (EditText) findViewById(R.id.edt_confirmChatPass);
		mCountryListSpinner = (Spinner) findViewById(R.id.Register_spinr_country);
		mYearOfBirthSpinner = (Spinner) findViewById(R.id.Register_spinr_DOB);
		mRadio_Female = (RadioButton) findViewById(R.id.btnRadio_female);
		mRadio_Male = (RadioButton) findViewById(R.id.btnRadio_male);
		mRgroup = (RadioGroup) findViewById(R.id.radio_grpGender);
		edt_Register_email = (EditText) findViewById(R.id.edt_Register_email);
		edt_enterPincode = (EditText) findViewById(R.id.edt_enterPincode);
		rltv_top = (RelativeLayout) findViewById(R.id.rltv_top);
	}
	/**
	 * Initialise Listeners.
	 */
	private void initialise_Listeners() {
		try {
			mRadio_Female.setOnClickListener(this);
			mRadio_Male.setOnClickListener(this);
			mBtn_Register.setOnClickListener(this);
			mCountryListSpinner.setOnTouchListener(this);
			mYearOfBirthSpinner.setOnTouchListener(this);

			mCountryListSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0,View arg1, int position, long arg3) {
					mCountryListSpinner.setSelection(position);
					country = (String) mCountryListSpinner.getSelectedItem();
				}
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					Toast.makeText(getApplicationContext(),"Please Select Country", Toast.LENGTH_SHORT).show();
				}
			});

			mYearOfBirthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0,View arg1, int position, long arg3) {
					try {
						mYearOfBirthSpinner.setSelection(position);
						age = (String) mYearOfBirthSpinner.getSelectedItem();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

			case R.id.btnRadio_female:
				try {
					Utility.radioBtnValue = 1;
					// get the selected radio button from the group
					int selectedOption = mRgroup.getCheckedRadioButtonId();
					radioGenderButton = (RadioButton) findViewById(selectedOption);
					gender = radioGenderButton.getText().toString();
					switch (gender) {
						case "Female":
							mRadio_Female.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);
							mRadio_Male.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
							break;
						case "Male":
							mRadio_Female.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
							mRadio_Male.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);
							break;
						default:
							mRadio_Female.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
							mRadio_Male.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
							break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				break;

			case R.id.btnRadio_male:
				try {
					Utility.radioBtnValue = 2;
					// get the selected radio button from the group
					int selectedOption = mRgroup.getCheckedRadioButtonId();
					radioGenderButton = (RadioButton) findViewById(selectedOption);
					gender = radioGenderButton.getText().toString();
					switch (gender) {
						case "Female":
							mRadio_Female.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);
							mRadio_Male.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
							break;
						case "Male":
							mRadio_Female.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
							mRadio_Male.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);
							break;
						default:
							mRadio_Female.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
							mRadio_Male.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
							break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case R.id.btn_Register:
				try {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mEdt_ConfirmMeesagePasswd.getWindowToken(), 0);

					if (NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {
						check_Validation();
					} else {
						Toast.makeText(RegisterActivity.this,getResources().getString(R.string.Network_Availability), Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			default:
				break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {

			case R.id.Register_spinr_country:
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mEdt_ConfrmPassword.getWindowToken(), 0);
				break;

			case R.id.Register_spinr_DOB:
				InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				manager.hideSoftInputFromWindow(mEdt_ConfrmPassword.getWindowToken(), 0);
				break;

			default:
				break;
		}

		return false;
	}
	/**
	 * Method to save user login credentials into local sharedprefences database.
	 */
	void saveCredential() {
		try {
			SharedPreferences.Editor edit = settings.edit();
			edit.putString(ThatItApplication.ACCOUNT_USERNAME_KEY, chatUserName);
			edit.putString(ThatItApplication.ACCOUNT_PASSWORD_KEY, login_password);
			edit.putString(ThatItApplication.ACCOUNT_CHAT_PASSWORD_KEY, Chat_password);
			edit.putString(ThatItApplication.ACCOUNT_EMAIL_ID,email );
			edit.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Check if all fields are filled and are correct.
	 */
	private void check_Validation() {

		try {
			login_password = encryptionManager.encryptPayload(mEdt_Password.getText().toString().trim());
			String confirmPassword = encryptionManager.encryptPayload(mEdt_ConfrmPassword.getText().toString().trim());
			Chat_password = encryptionManager.encryptPayload(mEdt_MeesagePasswd.getText().toString().trim());
			String Chat_confirmPassword = encryptionManager.encryptPayload(mEdt_ConfirmMeesagePasswd.getText().toString().trim());
			email = edt_Register_email.getText().toString().trim();

			if (mEdt_Password.getText().toString().trim().toCharArray().length == 0) {

				Toast.makeText(RegisterActivity.this,"Please Enter Login Password", Toast.LENGTH_SHORT).show();

			} else if (mEdt_ConfrmPassword.getText().toString().trim().toCharArray().length == 0) {
				Toast.makeText(RegisterActivity.this,"Please Confirm Login Password", Toast.LENGTH_SHORT).show();

			} else if (mEdt_MeesagePasswd.getText().toString().trim().toCharArray().length == 0) {
				Toast.makeText(RegisterActivity.this,"Please Enter Chat Password", Toast.LENGTH_SHORT).show();

			} else if (mEdt_ConfirmMeesagePasswd.getText().toString().trim().toCharArray().length == 0) {
				Toast.makeText(RegisterActivity.this,"Please Confirm Chat Password", Toast.LENGTH_SHORT).show();

			} else if (!login_password.equals(confirmPassword)) {
				Toast.makeText(RegisterActivity.this,"Login Passwords Do Not Match", Toast.LENGTH_SHORT).show();

			} else if (!Chat_password.equals(Chat_confirmPassword)) {
				Toast.makeText(RegisterActivity.this,"Chat Passwords Do Not Match", Toast.LENGTH_SHORT).show();

			} else if (country.equals("Select Your Country")) {
				Toast.makeText(RegisterActivity.this,"Please Select Your Country", Toast.LENGTH_SHORT).show();

			} else if (age.equals("Select Your Birth Year")) {
				Toast.makeText(RegisterActivity.this,"Please Select Your Birth Year", Toast.LENGTH_SHORT).show();

			}else if (email.trim().toCharArray().length == 0) {
				Toast.makeText(RegisterActivity.this,"Please Enter Your Email Address", Toast.LENGTH_SHORT).show();

			} else if (!mUtility.isEmailValid(email)) {
				Toast.makeText(RegisterActivity.this,"Please Enter Valid Email Address", Toast.LENGTH_SHORT).show();

			} else if (gender == null) {
				Toast.makeText(RegisterActivity.this,"Please Select Your Gender", Toast.LENGTH_SHORT).show();

			} else if (login_password.trim().toCharArray().length < 6) {
				Toast.makeText(RegisterActivity.this,"Minimum Password Length Should be 6 Characters ", Toast.LENGTH_SHORT).show();

			} else if (Chat_password.trim().toCharArray().length < 6) {
				Toast.makeText(RegisterActivity.this,"Minimum Chat Password Length Should be 6 Characters ",Toast.LENGTH_SHORT).show();

			} else if (Utility.hasPincode == true
					&& edt_enterPincode.getText().toString().trim().toCharArray().length == 0) {
				Toast.makeText(RegisterActivity.this,"Enter Promotional Code",Toast.LENGTH_SHORT).show();
			} else if(mEdt_MeesagePasswd.getText().toString().trim()
					.equalsIgnoreCase(mEdt_Password.getText().toString().trim())){
				Toast.makeText(RegisterActivity.this," Login and Chat Passwords must be different ",Toast.LENGTH_SHORT).show();
			}
			else {
				Utility.startDialog(RegisterActivity.this);
				Utility.lockScreenRotation(RegisterActivity.this, SCREEN_ORIENTATION);
				login_password = URLEncoder.encode(login_password,"UTF-8");
				Chat_password = URLEncoder.encode(Chat_password,"UTF-8");
				if(login_password.contains("%")){
					login_password = login_password.replace("%","2");
				}
				if(Chat_password.contains("%")){
					Chat_password = Chat_password.replace("%","2");
				}

				if(Utility.hasPincode == true){
					chatUserName = edt_enterPincode.getText().toString().trim().toUpperCase();
					AppSinglton.thatsItPincode = chatUserName;

					Utility.saveThatsItPincode(RegisterActivity.this,AppSinglton.thatsItPincode);

					new ValidatePincodeAsync(RegisterActivity.this,
							chatUserName,mValidatePincodeInterface).execute();
				}
				else{
					chatUserName = mTxt_ThatsItId.getText().toString();

					new RegistrationAsyncTask(
							chatUserName, login_password, "In-App Purchase","1.99", "Fresh",
							gender, country, "Android","","", age,Chat_password ,email,RegisterActivity.this,
							mRegisterInterface).execute();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Save user credentials in shared preference.
	 */

	private void saveUserCredentials() {
		try {
			if ((TextUtils.isEmpty(chatUserName) && TextUtils.isEmpty(login_password)))
				return;
			saveCredential();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onStart() {
		try {
			super.onStart();
			IntentFilter filter = new IntentFilter();
			filter.addAction(MainService.CONNECTION_CLOSED);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Spinner to select date of birth.
	 */

	private void initialise_Spinner_Age() {
		try {
			ArrayList<String> yearsTitle = new ArrayList<>();
			yearsTitle.add("Select Your Birth Year");
			ArrayList<String> years = new ArrayList<>();
			int thisYear = Calendar.getInstance().get(Calendar.YEAR);

			/*for (int i = 1900; i <= thisYear; i++) {
				years.add(Integer.toString(i));
			}*/

			for(int i = thisYear; i >= 1900;i--){
				years.add(Integer.toString(i));
			}
			yearsTitle.addAll(years);
			ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
					R.layout.spinner_items, yearsTitle);
			mYearOfBirthSpinner.setAdapter(adapter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Get register details  for the Thats It ID.
	 */

	RegisterInterface mRegisterInterface = new RegisterInterface() {

		@Override
		public void registerInterfaceMethod(
				InsertUserResponseTemplate mInsertUserResponseTemplate) {

			try {
				RetVal = mInsertUserResponseTemplate.getInsertUserDataResult().getmInsertUserDataParams()[0].getRetVal().toString();
				switch (RetVal) {
					case "1":
						AppSinglton.userId = mInsertUserResponseTemplate.getInsertUserDataResult().getmInsertUserDataParams()[0].getUserId();
						mSharedPreferences.edit().putString("USERID", AppSinglton.userId).commit();

						if (Utility.hasPincode == true) {
							new RegisterUserOnChatServerAsyncTask(RegisterActivity.this, mConnection, chatUserName, login_password).execute();
						}
						break;
					case "0":
						Utility.stopDialog();
						Utility.unlockScreenRotation(RegisterActivity.this);
						Toast.makeText(getApplicationContext(), "Email ID Already Exists.", Toast.LENGTH_SHORT).show();
						break;
					case "2":
						Utility.stopDialog();
						Utility.unlockScreenRotation(RegisterActivity.this);
						Toast.makeText(getApplicationContext(), "Promotion Code Already Exists.", Toast.LENGTH_SHORT).show();
						break;
				}
			} catch (Exception e) {
				Utility.stopDialog();
				Utility.unlockScreenRotation(RegisterActivity.this);
				e.printStackTrace();
			}}
	};


	/**
	 * Check if pincode exists on the server.
	 */

	ValidatePincodeInterface mValidatePincodeInterface = new ValidatePincodeInterface() {

		@Override
		public void validatePincode(ValidatePincode mValidatePincode) {
			try {
				int returnValue = Integer.parseInt(mValidatePincode.getValidatePincodeResult().getStatus().getStatus());

				if(returnValue == 1){

					getSubscriptionFee();

				}else{
					Utility.stopDialog();
					Utility.unlockScreenRotation(RegisterActivity.this);
					Utility.showMessage("Enter Valid Promotional Code");

				}
			} catch (Exception e) {
				Utility.stopDialog();
				Utility.unlockScreenRotation(RegisterActivity.this);
				e.printStackTrace();
			}
		}
	};


	/**
	 * Get Subscription fee.
	 */
	SubscriptionFeeInterface mSubscriptionFeeInterface = new SubscriptionFeeInterface() {

		@Override
		public void subscriptionFee(
				SubcriptionFeeTemplate mSubcriptionFeeTemplate) {

			if (mSubcriptionFeeTemplate != null) {

				int status = Integer.parseInt(mSubcriptionFeeTemplate.getGetSubscriptionFeeResult().getStatus().getStatus());
				if (status == 1) {
					subcriptionFee = mSubcriptionFeeTemplate.getGetSubscriptionFeeResult().getGetSubscriptionFeeParams()[0].getSubscriptionfee();

					SharedPreferences.Editor edit = settings.edit();
					edit.putString(ThatItApplication.ACCOUNT_USERNAME_KEY, chatUserName);
					edit.putString(ThatItApplication.ACCOUNT_PASSWORD_KEY, login_password);
					edit.putString(ThatItApplication.ACCOUNT_CHAT_PASSWORD_KEY, Chat_password);
					edit.putString(ThatItApplication.ACCOUNT_EMAIL_ID, email);
					edit.commit();

					new RegistrationAsyncTask(
							chatUserName, login_password, "Promotion Code",subcriptionFee, "Fresh",
							gender, country, "Android","", "", age,Chat_password ,email,RegisterActivity.this,
							mRegisterInterface).execute();

				} else {
					Utility.showMessage("Unable to get Subscription Fee");
					Utility.unlockScreenRotation(RegisterActivity.this);
				}
			} else {
				Utility.showMessage("Unable to get Subscription Fee");
				Utility.unlockScreenRotation(RegisterActivity.this);
			}
		}
	};

	/**
	 * Get 16-digit user ID.
	 */
	UserIdInterface mUserIdInterface = new UserIdInterface() {

		@Override
		public void getUserId(GenerateKeyIdResponse mGenerateKeyIdResponse) {

			if (mGenerateKeyIdResponse != null) {
				AppSinglton.thatsItPincode = mGenerateKeyIdResponse.getGeneralAlphakeyResult().getGenerateAlphaKeyParam()[0].getKey();
				setPincodeVisibility();
			} else {
				Utility.stopDialog();
				Utility.showMessage("Error while generating pincode");
			}
		}
	};

	@Override
	public void onOrientationChange(OrientationManager.ScreenOrientation screenOrientation) {

		switch (screenOrientation) {

			case PORTRAIT:
				SCREEN_ORIENTATION = 0;
				break;

			case REVERSED_PORTRAIT:
				SCREEN_ORIENTATION = 1;
				break;

			case LANDSCAPE:
				SCREEN_ORIENTATION = 2;
				break;

			case REVERSED_LANDSCAPE:
				SCREEN_ORIENTATION = 3;
				break;

		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			orientationManager.disable();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
