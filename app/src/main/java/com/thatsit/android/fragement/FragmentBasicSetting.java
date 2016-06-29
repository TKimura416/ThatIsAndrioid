package com.thatsit.android.fragement;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.seasia.myquick.controller.WebServiceClient;
import com.seasia.myquick.model.AppSinglton;
import com.seasia.myquick.model.FetchUserSettingTemplates;
import com.seasia.myquick.model.GetSubscriptionHistoryTemplate;
import com.seasia.myquick.model.UpdateUserSettingTemplate;
import com.seasia.myquick.model.ValidateUserLoginStatus;
import com.thatsit.android.MainService;
import com.thatsit.android.R;
import com.thatsit.android.Utility;
import com.thatsit.android.activities.ChangeLoginPassword;
import com.thatsit.android.activities.ContactActivity;
import com.thatsit.android.activities.WelcomeActivity;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.beans.LogFile;
import com.thatsit.android.db.DbOpenHelper;
import com.thatsit.android.interfaces.ValidateUserLoginInterface;

/**
 *
 * @author psingh5
 * This fragment contains user registration data and provision to set his name,
 * description,profile pic.
 */
@SuppressLint({ "ValidFragment", "NewApi" })
public class FragmentBasicSetting extends SuperFragment implements OnClickListener,
		View.OnTouchListener {

	protected static final String TAG = "Fragment Account";
	private View mView;
	private Button mBtnBasic_Setting, mBtnChat_Setting, mBtnPayment_Setting,mBtn_SignOut, mBtn_Done;
	private ImageView mImageVw_BasicSetting;
	private TextView mTxt_UserName, mTxt_PinNo, mTxt_ExpiryDates,tv_changeLoginPassword;
	private EditText mEdt_PsuedoName, mEdt_Profiledescription,mEdt_emailID;
	private RadioButton mRadio_Female, mRadio_Male, radioGenderButton;
	private RadioGroup mRgroup;
	private String mProfileImageString,mPinNo,mPsuedoName,mPseudodescription,
			mGender,mCountry,mAge,mEmailID,RetVal,expiryDate,city,state;
	private Spinner mCountryListSpinner, mYearOfBirthSpinner;
	private String  profile_img64 = "";
	private FragmentManager mFragmentManager;
	private FragmentTransaction mFragmentTransaction;
	private FragmentChatSetting mFragmentChatSetting;
	private FragmentPaymentSetting mFragmentPaymentSetting;
	private TextView tv_retry;
	private SharedPreferences settings,mSharedPreferences,mSharedPreferences_reg;
	private ImageButton fragActBasicSet_btn_copy;
	private DbOpenHelper dbOpenHelper;
	private Uri fileUri,resultFileUri;
	private ImageLoader loader= ImageLoader.getInstance();
	private DisplayImageOptions options;
	private boolean doneClicked = false;
	private ContactActivity hostActivity;
	private int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	private int GALLERY_IMAGE_REQUEST_CODE = 200;
	final int PIC_CROP = 2; //keep track of cropping intent
	public static final int MEDIA_TYPE_IMAGE = 1;
	private static final String IMAGE_DIRECTORY_NAME = "Thats It Profile Images";
	private Bitmap bitmap;
	private LinearLayout fragInvite_tabs_lnrlayout;
	private InputMethodManager imm;
	private ProgressDialog pdDialog;
	private int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3;

	@SuppressLint("ValidFragment")
	public FragmentBasicSetting(MainService service) {
	}

	public FragmentBasicSetting() {
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		hostActivity = (ContactActivity) activity;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		((ContactActivity)getActivity()).iconStateChanger(false, false, false, true);
		Utility.fragPaymentSettingsOpen = false;
		Utility.fragChatIsOpen = false;
		imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		setRetainInstance(true);
		checkServiceStart();
		mView = inflater.inflate(R.layout.fragment_account_tab_basic_settings,container, false);

		Utility.enteredFragmentOnce = false;
		loader.init(ImageLoaderConfiguration.createDefault(getActivity()));
		initialiseSharedPreference();
		initialiseDbHelper();
		setWindowState();
		initialise_Variables();
		if(getResources().getBoolean(R.bool.isTablet)){
			ContactActivity.textView_toolbar_title.setText("Basic Settings");
			fragInvite_tabs_lnrlayout.setVisibility(View.GONE);
		}
		initialise_Listener();
		initialise_Countries();
		initialise_Spinner_Age();
		callWebServiceData();
		return mView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Utility.enteredFragmentOnce = false;
		try {
			if(getResources().getBoolean(R.bool.isTablet)) {
				if (hostActivity.mDrawerLayout != null && hostActivity.navDrawerView != null) {
					hostActivity.mDrawerLayout.closeDrawer(hostActivity.navDrawerView);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Utility.UserPauseStatus(getActivity());
	}

	/**
	 * Initialise shared preference to Obtain user id.
	 */
	private void initialiseSharedPreference() {
		mSharedPreferences_reg = getActivity().getSharedPreferences("register_data", 0);
		mSharedPreferences = getActivity().getSharedPreferences("USERID", 0);
		AppSinglton.userId = mSharedPreferences.getString("USERID", "");
	}

	/**
	 * Get user registration details.
	 */
	private void callWebServiceData() {

		try {
			if(Utility.RegsiterDataFetchedOnce == true){
				// Get Register Data from Shared Preference
				getRegisterDataFromSharedPrefernce();

			}else {
				if (NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {
					new GetDataAsynRegistration().execute();
				} else {
					Toast.makeText(getActivity(),getResources().getString(R.string.Network_Availability), Toast.LENGTH_SHORT).show();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Utility.RegsiterDataFetchedOnce = false;
		}
	}

	/**
	 * Obtain user id from shared preference.
	 */
	private void getRegisterDataFromSharedPrefernce() {

		mProfileImageString = mSharedPreferences_reg.getString("ProfileImageString", "");
		mPinNo = mSharedPreferences_reg.getString("Pincode", "");
		mPsuedoName = mSharedPreferences_reg.getString("PseudoName", "");
		mPseudodescription = mSharedPreferences_reg.getString("Pseudodescription", "");
		mGender = mSharedPreferences_reg.getString("Gender", "");
		mCountry = mSharedPreferences_reg.getString("Country", "");
		mAge = mSharedPreferences_reg.getString("Age", "");
		mEmailID = mSharedPreferences_reg.getString("EmailID", "");
		expiryDate = mSharedPreferences_reg.getString("ExpiryDate", "");
		//	profile_img64 = mSharedPreferences_reg.getString("profile_img64", "");

		setRegisterDataToRespectiveFields();
		mTxt_ExpiryDates.setText(expiryDate);
	}

	/**
	 * Set keyboard state hidden.
	 */
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


	/**
	 * Initialise database.
	 */
	private void initialiseDbHelper() {

		try {
			dbOpenHelper = new DbOpenHelper(getActivity());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialise class variables.
	 */
	private void initialise_Variables() {

		mFragmentChatSetting = new FragmentChatSetting(MainService.mService);
		mFragmentPaymentSetting = new FragmentPaymentSetting(MainService.mService);

		fragInvite_tabs_lnrlayout = (LinearLayout)mView.findViewById(R.id.fragInvite_tabs_lnrlayout);
		mBtnBasic_Setting = (Button) mView.findViewById(R.id.fragActBasicSet_btn_basicSet);
		mBtnChat_Setting = (Button) mView.findViewById(R.id.fragActBasicSet_btn_chatSet);
		mBtnPayment_Setting = (Button) mView.findViewById(R.id.fragActBasicSet_btn_paymentSet);
		mImageVw_BasicSetting = (ImageView) mView.findViewById(R.id.fragActBasicSet_img_contactImage);
		mTxt_UserName = (TextView) mView.findViewById(R.id.fragActBasicSet_tv_UserName);
		tv_changeLoginPassword= (TextView) mView.findViewById(R.id.tv_changeLoginPassword);
		mTxt_PinNo = (TextView) mView.findViewById(R.id.fragActBasicSet_tv_pinNo);
		mTxt_ExpiryDates = (TextView) mView.findViewById(R.id.fragActBasicSet_tv_date);
		mEdt_PsuedoName = (EditText) mView.findViewById(R.id.fragActBasicSet_edt_PseudoName);
		mEdt_Profiledescription = (EditText) mView.findViewById(R.id.fragActBasicSet_edt_ProfileDesc);
		mCountryListSpinner = (Spinner) mView.findViewById(R.id.fragActBasicSet_spinr_Countary);
		mYearOfBirthSpinner = (Spinner) mView.findViewById(R.id.fragActBasicSet_spinr_Age);
		mRadio_Female = (RadioButton) mView.findViewById(R.id.btnRadio_female);
		mRadio_Male = (RadioButton) mView.findViewById(R.id.btnRadio_male);
		mRgroup = (RadioGroup) mView.findViewById(R.id.radio_grpGender);
		mBtn_SignOut = (Button) mView.findViewById(R.id.fragActBasicSet_btn_SignOut);
		mBtn_Done = (Button) mView.findViewById(R.id.fragActBasicSet_btn_Done);
		tv_retry = (TextView) mView.findViewById(R.id.tv_retry);
		fragActBasicSet_btn_copy = (ImageButton) mView.findViewById(R.id.fragActBasicSet_btn_copy);
		mEdt_emailID= (EditText) mView.findViewById(R.id.fragActBasicSet_edt_emailID);
	}

	/**
	 * Initialise click listeners.
	 */
	private void initialise_Listener() {

		try {
			mBtnBasic_Setting.setOnClickListener(this);
			mBtnChat_Setting.setOnClickListener(this);
			mBtnPayment_Setting.setOnClickListener(this);
			mBtn_SignOut.setOnClickListener(this);
			mBtn_Done.setOnClickListener(this);
			mImageVw_BasicSetting.setOnClickListener(this);
			tv_retry.setOnClickListener(this);
			tv_changeLoginPassword.setOnClickListener(this);
			fragActBasicSet_btn_copy.setOnClickListener(this);
			mRadio_Female.setOnClickListener(this);
			mRadio_Male.setOnClickListener(this);
			mCountryListSpinner.setOnTouchListener(this);
			mYearOfBirthSpinner.setOnTouchListener(this);

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		mCountryListSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,int position, long arg3) {
				try {
					mCountryListSpinner.setSelection(position);
					mCountry = (String) mCountryListSpinner.getSelectedItem();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				Toast.makeText(getActivity(), "Please Select Country",Toast.LENGTH_SHORT).show();
			}
		});

		mYearOfBirthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,int position, long arg3) {
				try {
					mYearOfBirthSpinner.setSelection(position);
					mAge = (String) mYearOfBirthSpinner.getSelectedItem();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}


	/**
	 * Select age from spinner.
	 */
	private void initialise_Spinner_Age() {
		try {
			ArrayList<String> yearsTitle = new ArrayList<String>();
			yearsTitle.add("Select Your Birth Year");
			ArrayList<String> years = new ArrayList<String>();
			int thisYear = Calendar.getInstance().get(Calendar.YEAR);
			/*for (int i = 1900; i <= thisYear; i++) {
				years.add(Integer.toString(i));
			}*/
			for(int i = thisYear; i >= 1900;i--){
				years.add(Integer.toString(i));
			}
			yearsTitle.addAll(years);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_items, yearsTitle);
			mYearOfBirthSpinner.setAdapter(adapter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void initialise_Countries() {
		try {
			List<String> country = new ArrayList<String>();
			country = Arrays.asList(getResources().getStringArray(R.array.country_arrays));
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),R.layout.spinner_items,country);
			mCountryListSpinner.setAdapter(adapter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

			case R.id.fragActBasicSet_btn_chatSet:
				try {
					//	mFragmentPaymentSetting.setRetainInstance(false);
					mFragmentManager = getActivity().getSupportFragmentManager();
					mFragmentTransaction = mFragmentManager.beginTransaction();
					mFragmentTransaction.replace(R.id.fragmentContainer,mFragmentChatSetting);
					mFragmentTransaction.commit();
					//ContactActivity.mStackChatAccount.add(mFragmentChatSetting);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case R.id.fragActBasicSet_btn_paymentSet:
				try {
					//mFragmentPaymentSetting.setRetainInstance(true);
					mFragmentManager = getActivity().getSupportFragmentManager();
					mFragmentTransaction = mFragmentManager.beginTransaction();
					mFragmentTransaction.replace(R.id.fragmentContainer,mFragmentPaymentSetting);
					mFragmentTransaction.commit();
					//	ContactActivity.mStackChatAccount.add(mFragmentPaymentSetting);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case R.id.tv_changeLoginPassword:
				try {
					Intent intent = new Intent(getActivity(),ChangeLoginPassword.class);
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case R.id.fragActBasicSet_btn_SignOut:

				openExitDialog();
				break;
			case R.id.fragActBasicSet_btn_Done:

				try {
					InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mEdt_PsuedoName.getWindowToken(), 0);
					Utility.enteredFragmentOnce = false;
					doneClicked = true;
					if(NetworkAvailabilityReceiver.isInternetAvailable(getActivity())){
						check_Validation();
					}else{
						Toast.makeText(getActivity(),getResources().getString(R.string.Network_Availability), Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
			case R.id.tv_retry:
				try {
					if (NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {

						updateWebServiceSharedPreference();
						callWebServiceData();

					} else {
						Toast.makeText(getActivity(),getResources().getString(R.string.Network_Availability), Toast.LENGTH_SHORT).show();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
			case R.id.fragActBasicSet_img_contactImage:
				try {
					selectImage();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case R.id.fragActBasicSet_btn_copy:
				try {
					String copyValue = mTxt_PinNo.getText().toString();
					if (copyValue.equals("")) {
						Toast.makeText(getActivity(), "No Text To Copy", Toast.LENGTH_SHORT).show();
					} else {

						ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("label",copyValue);
						clipboard.setPrimaryClip(clip);
						Toast.makeText(getActivity(),"Text Copied To Clipboard", Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case R.id.btnRadio_female:

				try {
					// get the selected radio button from the group
					int selectedOption = mRgroup.getCheckedRadioButtonId();
					radioGenderButton = (RadioButton) mView.findViewById(selectedOption);
					mGender = radioGenderButton.getText().toString();
					if (mGender.equals("Female")) {
						mRadio_Female.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);
						mRadio_Male.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
					} else if (mGender.equals("Male")) {
						mRadio_Female.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
						mRadio_Male.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);
					} else {
						mRadio_Female.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
						mRadio_Male.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case R.id.btnRadio_male:

				try {
					// get the selected radio button from the group
					int selectedOption = mRgroup.getCheckedRadioButtonId();

					radioGenderButton = (RadioButton) mView.findViewById(selectedOption);
					mGender = radioGenderButton.getText().toString();
					if (mGender.equals("Female")) {
						mRadio_Female.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);
						mRadio_Male.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);

					} else if (mGender.equals("Male")) {
						mRadio_Female.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
						mRadio_Male.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);

					} else {
						mRadio_Female.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
						mRadio_Male.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
		}
	}

	private void updateWebServiceSharedPreference() {

		Utility.RegsiterDataFetchedOnce = false;
		Utility.FragmentHistoryDataFetchedOnce = false;
	}


	/**
	 * Check if all fields are filled to update info.
	 */
	private void check_Validation() {

		try {
			mPsuedoName = mEdt_PsuedoName.getText().toString();
			mPseudodescription = mEdt_Profiledescription.getText().toString();
			state="";
			city="";

			if (mPsuedoName.trim().toCharArray().length == 0) {
				Toast.makeText(getActivity(), "Please Enter Psuedo Name", Toast.LENGTH_SHORT).show();

			} else if (mPseudodescription.trim().toCharArray().length == 0) {
				Toast.makeText(getActivity(),"Please Enter Profile Description", Toast.LENGTH_SHORT).show();

			} else if (mCountry.equals("Select Your Country")) {
				Toast.makeText(getActivity(), "Please Select Your Country", Toast.LENGTH_SHORT).show();

			} else if (mAge.equals("Select Your Birth Year")) {
				Toast.makeText(getActivity(), "Please Select Your Birth Year",Toast.LENGTH_SHORT).show();
			}
			else if (mGender == null) {
				Toast.makeText(getActivity(), "Please Select Your Gender", Toast.LENGTH_SHORT).show();

			} else {
				if(NetworkAvailabilityReceiver.isInternetAvailable(getActivity())){
					new GetDataAsync().execute();
				}else{
					Toast.makeText(getActivity(), getResources().getString(R.string.Network_Availability), Toast.LENGTH_SHORT).show();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
			case R.id.fragActBasicSet_spinr_Countary:
				imm.hideSoftInputFromWindow(mEdt_PsuedoName.getWindowToken(), 0);
				break;

			case R.id.fragActBasicSet_spinr_Age:
				imm.hideSoftInputFromWindow(mEdt_PsuedoName.getWindowToken(), 0);
				break;

			default:
				break;
		}
		return false;
	}

	/**
	 * Update user information on sever - name,description,image
	 */
	private class GetDataAsync extends AsyncTask<Void, Void, UpdateUserSettingTemplate> {

		@Override
		protected void onPreExecute() {

			pdDialog = new ProgressDialog(getActivity());
			pdDialog.setMessage("Updating Data");
			pdDialog.show();
			pdDialog.setCancelable(false);
		}

		@Override
		protected UpdateUserSettingTemplate doInBackground(Void... arg0) {
			try {

				getBitmapFromImageview();

				UpdateUserSettingTemplate obj = new WebServiceClient(
						getActivity()).change_UserBasicSettings(
						AppSinglton.userId, mGender, mCountry, state, city,
						profile_img64, mAge, mPsuedoName, mPseudodescription, mEdt_emailID.getText().toString());
				return obj;
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(UpdateUserSettingTemplate result) {
			super.onPostExecute(result);

			try {
				if (result == null){
					pdDialog.dismiss();
					Utility.showMessage("Error while updation");
					return;
				}
				pdDialog.dismiss();
				RetVal = result.getmUpdateUserSettingsResult().getUpdateUserSettingsParams()[0].getRetVal();

				if (RetVal == null) {
					Toast.makeText(getActivity(),"Data Could Not be Saved Please Try Again",Toast.LENGTH_SHORT).show();
				}

				if (RetVal.equals("1")) {
					updateWebServiceSharedPreference();
					callWebServiceData();

				} else {
					Toast.makeText(getActivity(),"Data Could Not be Saved Please Try Again",Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void getBitmapFromImageview() {
		Bitmap bitmap = ((BitmapDrawable)mImageVw_BasicSetting.getDrawable()).getBitmap();
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
		profile_img64 = Base64.encodeToString(image_bytes, Base64.NO_WRAP);
	}

	/**
	 * Get subscription history for jID.
	 */
	private class GetDataAsyncExpiryDate extends AsyncTask<Void, Void, GetSubscriptionHistoryTemplate> {

		@Override
		protected GetSubscriptionHistoryTemplate doInBackground(Void... arg0) {

			try {
				GetSubscriptionHistoryTemplate obj = new WebServiceClient(
						getActivity()).getSubscriptionHistory(AppSinglton.userId);
				return obj;
			} catch (Exception e) {
				Utility.RegsiterDataFetchedOnce = false;
				return null;
			}
		}

		@Override
		protected void onPostExecute(GetSubscriptionHistoryTemplate result) {
			try {
				super.onPostExecute(result);

				expiryDate = result.getGetSubscriptionHistoryResult().getGetSubscriptionHistoryParams()[0].getSubscriptionExpiryDate();
				mTxt_ExpiryDates.setText(expiryDate);

				mSharedPreferences_reg.edit().putString("ExpiryDate",expiryDate).commit();
				pdDialog.dismiss();
			} catch (Exception e) {
				e.printStackTrace();
				Utility.RegsiterDataFetchedOnce = false;
				pdDialog.dismiss();
			}
			if(doneClicked == true){
				doneClicked = false;
				Utility.showMessage("Profile Successfully Updated");
			}
		}
	}

	/**
	 * Select option to change profile pic.
	 */
	private void selectImage() {

		final CharSequence[] options = { "Add Photo via Camera", "Pick Photo","Cancel" };
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Profile Picture");
		builder.setCancelable(false);
		builder.setItems(options, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int item) {

				if (options[item].equals("Add Photo via Camera")) {

					openCamera();

				} else if (options[item].equals("Pick Photo")) {

					openGallery();

				} else if (options[item].equals("Cancel")) {
					dialog.dismiss();
				}
			}
		});
		builder.show();
	}

	/**
	 * Capturing Camera Image will lauch camera app request image capture
	 */

	private void openCamera() {

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
		startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
	}

	/**
	 * Select image from gallery
	 */

	private void openGallery() {

		if (Build.VERSION.SDK_INT >= 23){

			if (ContextCompat.checkSelfPermission(getActivity(),
					Manifest.permission.READ_EXTERNAL_STORAGE)
					!= PackageManager.PERMISSION_GRANTED) {

				// Should we show an explanation?
				if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
						Manifest.permission.READ_EXTERNAL_STORAGE)) {

					// Show an expanation to the user *asynchronously* -- don't block
					// this thread waiting for the user's response! After the user
					// sees the explanation, try again to request the permission.

				} else {

					// No explanation needed, we can request the permission.

					/*ActivityCompat.requestPermissions(getActivity(),
							new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
							MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);*/
					requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
							MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

					// MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
					// app-defined int constant. The callback method gets the
					// result of the request.
				}
			}else{
				/*ActivityCompat.requestPermissions(getActivity(),
						new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
						MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);*/
				requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
						MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
			}
		}else {
			Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(intent, GALLERY_IMAGE_REQUEST_CODE);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case 3: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					// permission was granted, yay! Get your Photo
					Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(intent, GALLERY_IMAGE_REQUEST_CODE);

				} else {

					// permission denied, boo! Disable the
					// functionality that depends on this permission.
				}
				return;
			}

			// other 'case' lines to check for other
			// permissions this app might request
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			// if the result is capturing image

			if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
				previewCapturedImage();
			}
			// if the result is selecting image from gallery
			else if (requestCode == GALLERY_IMAGE_REQUEST_CODE && resultCode == getActivity().RESULT_OK ) {
				fileUri = data.getData();
				getImageFromGallery();

			} else if (requestCode == PIC_CROP) {

				Uri extras = data.getData();

				//get the cropped bitmap
				if(extras == null){
					Bitmap cropped_bitmap = null;//extras.getParcelable("data");
					try {
						cropped_bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), resultFileUri);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						cropped_bitmap = null;
					}
					if(cropped_bitmap == null){
						cropped_bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), fileUri);//extras.getParcelable("data");
						if(cropped_bitmap != null){
							mImageVw_BasicSetting.setImageBitmap(cropped_bitmap);
						}
					}else {
						mImageVw_BasicSetting.setImageBitmap(cropped_bitmap);
					}
				}
				else {
					ContentResolver cr = getActivity().getContentResolver();
					InputStream in = cr.openInputStream(extras);
					BitmapFactory.Options options = new BitmapFactory.Options();
					Bitmap cropped_bitmap = BitmapFactory.decodeStream(in, null, options);
					mImageVw_BasicSetting.setImageBitmap(cropped_bitmap);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 *  Get user registration info.
	 */
	private class GetDataAsynRegistration extends AsyncTask<Void, Void, FetchUserSettingTemplates> {

		@Override
		protected void onPreExecute() {
			//Utility.startDialog(hostActivity);
			pdDialog = new ProgressDialog(getActivity());
			pdDialog.setMessage("Please Wait");
			pdDialog.show();
			pdDialog.setCancelable(false);
		}

		@Override
		protected FetchUserSettingTemplates doInBackground(Void... arg0) {

			try {
				FetchUserSettingTemplates obj = new WebServiceClient(getActivity())
						.getRegistrationdetails(AppSinglton.userId);

				return obj;
			} catch (Exception e) {
				Utility.RegsiterDataFetchedOnce = false;
				return null;
			}
		}

		@Override
		protected void onPostExecute(FetchUserSettingTemplates result) {
			super.onPostExecute(result);
			if (result == null){
				Utility.showMessage("Error while fetching information");
				pdDialog.dismiss();
				return;
			}
			try {
				Utility.RegsiterDataFetchedOnce = true;
				mPinNo = result.getmFetchUserSettingsResult().getmFetchUserSettingsParams()[0].getUserPINCode().toString();
				mPsuedoName = result.getmFetchUserSettingsResult().getmFetchUserSettingsParams()[0].getPseudoName().toString();
				mPseudodescription = result.getmFetchUserSettingsResult().getmFetchUserSettingsParams()[0].getProfileDescription().toString();
				mGender = result.getmFetchUserSettingsResult().getmFetchUserSettingsParams()[0].getGender().toString();
				mCountry = result.getmFetchUserSettingsResult().getmFetchUserSettingsParams()[0].getCountry().toString();
				mAge = result.getmFetchUserSettingsResult().getmFetchUserSettingsParams()[0].getDOB().toString();
				mEmailID = result.getmFetchUserSettingsResult().getmFetchUserSettingsParams()[0].getEmailId().toString();
				mProfileImageString =result.getmFetchUserSettingsResult().getmFetchUserSettingsParams()[0].getProfileImage().toString();
				// Set Values In Shared Preference
				setRegisterValuesInSharedPrefernce();

				//	Set Register Data to Specific Fields
				setRegisterDataToRespectiveFields();

				// Set User Pic, Name and Status to Vcard
				if(doneClicked == true){
					new Thread(new Runnable() {
						@Override
						public void run() {
							updateDataToVcard();
						}
					}).start();
				}

			}catch (Exception e) {
				e.printStackTrace();
				Utility.RegsiterDataFetchedOnce = false;
				pdDialog.dismiss();
			}
			new GetDataAsyncExpiryDate().execute();
		}
	}

	/**
	 * Load user profile pic, firstname, lastname to Vcard.
	 */
	private void updateDataToVcard() {

		if(MainService.mService.connection.isConnected()
				&& MainService.mService.connection.isAuthenticated()){

			VCard me = new VCard();
			try {
				me.load(MainService.mService.connection); // load own VCard
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Update VCARD
			if(mPsuedoName.equals("")){
				me.setFirstName(mTxt_UserName.getText().toString());
			}else{
				me.setFirstName(mPsuedoName);
			}
			if(mPseudodescription.equals("")){
				me.setLastName("Hi I Am Using That's It");
			}else{
				me.setLastName(mPseudodescription);
			}
			if(mProfileImageString!=null)
				me.setField("profile_picture_url", mProfileImageString);
			else
				me.setField("profile_picture", "");
			try {
				me.save(MainService.mService.connection);
			} catch (XMPPException e) {
				Utility.RegsiterDataFetchedOnce = false;
				pdDialog.dismiss();
				e.printStackTrace();
			}
		}
	}

	/**
	 * Save register values in shared preference.
	 */
	private void setRegisterValuesInSharedPrefernce() {

		mSharedPreferences_reg.edit().putString("ProfileImageString",mProfileImageString).commit();
		mSharedPreferences_reg.edit().putString("Pincode",mPinNo).commit();
		mSharedPreferences_reg.edit().putString("PseudoName",mPsuedoName).commit();
		mSharedPreferences_reg.edit().putString("Pseudodescription",mPseudodescription).commit();
		mSharedPreferences_reg.edit().putString("Gender",mGender).commit();
		mSharedPreferences_reg.edit().putString("Country",mCountry).commit();
		mSharedPreferences_reg.edit().putString("Age",mAge).commit();
		mSharedPreferences_reg.edit().putString("EmailID",mEmailID).commit();
		//mSharedPreferences_reg.edit().putString("profile_img64",profile_img64).commit();
	}

	/**
	 * Dislay user info on fields.
	 */
	private void setRegisterDataToRespectiveFields() {

		try {
			// Set Profile Pic
			options = new DisplayImageOptions.Builder()
					.showImageOnFail(R.drawable.no_img)
					.showImageForEmptyUri(R.drawable.no_img)
					.cacheInMemory(true)
					.cacheOnDisk(true)
					.build();
			loader.displayImage(mProfileImageString, mImageVw_BasicSetting, options);

		} catch (Exception e) {
			e.printStackTrace();
		}
		// Set UserName
		if (mPsuedoName.equals("")) {
			mTxt_UserName.setText("My Name");
			//mEdt_PsuedoName.setText("My Name");
		} else {
			mTxt_UserName.setText(mPsuedoName);
			// Set PseudoName
			mEdt_PsuedoName.setText(mPsuedoName);
		}

		// Set Pin No
		mTxt_PinNo.setText(mPinNo);

		// Set PseudoDescription
		mEdt_Profiledescription.setText(mPseudodescription);

		// Set EmailID
		mEdt_emailID.setText(mEmailID);

		// Set Gender
		if (mGender.equals("M")) {
			mRadio_Male.setSelected(true);
			mRadio_Female.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
			mRadio_Male.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);
		} else {
			mRadio_Female.setSelected(true);
			mRadio_Female.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_btn, 0, 0, 0);
			mRadio_Male.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unradio_btn, 0, 0, 0);
		}

		// Set Country Name
		for (int i = 0; i < mCountryListSpinner.getAdapter().getCount(); i++)
			if (mCountry.equals(mCountryListSpinner.getItemAtPosition(i).toString())) {
				mCountryListSpinner.setSelection(i);
				break;
			}

		// Set Age (Year)
		for (int i = 0; i < mYearOfBirthSpinner.getAdapter().getCount(); i++)
			if (mAge.equals(mYearOfBirthSpinner.getItemAtPosition(i).toString())){
				mYearOfBirthSpinner.setSelection(i);
				break;
			}
	}

	/**
	 * Open dialog to close the app.
	 */
	private void openExitDialog() {

		try {
			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
			// set title
			alertDialogBuilder.setTitle("Do You Want to Exit");
			alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					});

			final AlertDialog dialog = alertDialogBuilder.create();
			dialog.show();

			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					try {
						dialog.dismiss();
						if(!NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())){

							performSignOutTask();

						}else{
							pdDialog = new ProgressDialog(getActivity());
							pdDialog.setMessage("Signing Out...");
							pdDialog.show();
							pdDialog.setCancelable(false);
							Utility.UserLoginStatus(getActivity(), Utility.getEmail(), "False","","", mValidateUserLoginInterface);
						}
					} catch (Exception e) {
						pdDialog.dismiss();
						e.printStackTrace();
					}
				}
			});
			dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						dialog.dismiss();
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

	private void performSignOutTask() {

		if(NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())){
			SharedPreferences mSharedPreferences = getActivity().getSharedPreferences("statusID", getActivity().MODE_PRIVATE);
			String sharedStatusID = mSharedPreferences.getString("statusID", "");
			LogFile.deleteLog(sharedStatusID);
		}
		clearAppSingletonData();
		clearAllSharedPreferences();
		clearDatabase();
		Utility.isAppStarted = false;
		Utility.disconnected = true;
		MainService.mService.connection.disconnect();
		MainService.mService.disconnect();
		if(pdDialog!=null) {
			pdDialog.dismiss();
		}
		getActivity().stopService(new Intent(getActivity(), MainService.class));
		Intent startMain = new Intent(getActivity(), WelcomeActivity.class);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(startMain);
		hostActivity.finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	/**
	 * Clear singleton data.
	 */
	private void clearAppSingletonData() {
		AppSinglton.userId = "";
		AppSinglton.thatsItPincode = "";
	}

	/**
	 * Clear database on sign out.
	 */
	private void clearDatabase() {
		dbOpenHelper = new DbOpenHelper(ThatItApplication.getApplication());
		dbOpenHelper.deleteDatabase();
	}

	/**
	 * Clear shared preference data.
	 */
	private void clearAllSharedPreferences() {

		settings = PreferenceManager.getDefaultSharedPreferences(ThatItApplication.getApplication());
		settings.edit().clear().commit();

		SharedPreferences clearUserID = ThatItApplication.getApplication().getSharedPreferences("USERID",Context.MODE_PRIVATE);
		clearUserID.edit().clear().commit();

		SharedPreferences THATSITID = ThatItApplication.getApplication().getSharedPreferences("THATSITID",Context.MODE_PRIVATE);
		THATSITID.edit().clear().commit();

		SharedPreferences clearVibrateData = ThatItApplication.getApplication().getSharedPreferences("mSharedPreferencesVibrateValue",Context.MODE_PRIVATE);
		clearVibrateData.edit().clear().commit();

		SharedPreferences clearAvlData = ThatItApplication.getApplication().getSharedPreferences("mSharedPreferencesAvlValue",Context.MODE_PRIVATE);
		clearAvlData.edit().clear().commit();

		SharedPreferences clearSetNotificationTone = ThatItApplication.getApplication().getSharedPreferences("AudioPreference",Context.MODE_PRIVATE);
		clearSetNotificationTone.edit().clear().commit();

		SharedPreferences clearRegisterData = ThatItApplication.getApplication().getSharedPreferences("register_data",Context.MODE_PRIVATE);
		clearRegisterData.edit().clear().commit();

		SharedPreferences statusID = ThatItApplication.getApplication().getSharedPreferences("statusID",Context.MODE_PRIVATE);
		statusID.edit().clear().commit();
	}

	/**
	 * Here we store the file url as it will be null after returning from camera
	 * app
	 */

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// save file url in bundle as it will be null on scren orientation
		// changes
		outState.putParcelable("file_uri", fileUri);
	}

	/*@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		// get the file url
		fileUri = savedInstanceState.getParcelable("file_uri");
	}*/

	public Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/**
	 * @param type
	 * @return image
	 */

	private static File getOutputMediaFile(int type) {

		// External sdcard location
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME);

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
				return null;
			}
		}

		// Create a media file name
		//	String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" +".jpg");
		} else {
			return null;
		}
		return mediaFile;
	}

	/**
	 * Display image from a path to ImageView
	 */

	private void previewCapturedImage() {
		try {
			bitmap = decodeBitmap(CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
			rotateImageBy90Degree();
			fileUri = getImageUri(getActivity(),bitmap);
			performCrop(fileUri);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Rotate image vertically by 90 degree
	 */

	private void rotateImageBy90Degree() {

		try {
			ExifInterface exif = null;
			exif = new ExifInterface(fileUri.getPath());
			int exifOrientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			int rotate = 0;
			switch (exifOrientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					rotate = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					rotate = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					rotate = 270;
					break;
			}
			if (rotate != 0) {
				int w = bitmap.getWidth();
				int h = bitmap.getHeight();
				// Setting pre rotate
				Matrix mtx = new Matrix();
				mtx.preRotate(rotate);
				// Rotating Bitmap & convert to ARGB_8888, required by tess
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
			}
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get image grom gallery.
	 */
	private void getImageFromGallery() {
		try {
			bitmap = decodeBitmap(GALLERY_IMAGE_REQUEST_CODE);
			fileUri = getImageUri(getActivity(), bitmap);
			performCrop(fileUri);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Decode bitmap to remove out of memory error.
	 */

	private Bitmap decodeBitmap(int resultcode) {

		try {
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			if (resultcode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
				BitmapFactory.decodeFile(fileUri.getPath(), o);
			} else {
				BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(fileUri), null, o);
			}
			final int REQUIRED_SIZE = 180;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
					break;
				}
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			if (resultcode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
				bitmap = BitmapFactory.decodeFile(fileUri.getPath(), o2);
			} else {
				bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(fileUri), null, o2);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	/**
	 *  Crop the image obtained from gallery
	 */
	private void performCrop(Uri inputUri) {

		try {
			//call the standard crop action intent (the user device may not support it)
			Intent cropIntent = new Intent("com.android.camera.action.CROP");

			//indicate image type and Uri
			cropIntent.setDataAndType(inputUri, "image/*");
			//set crop properties
			cropIntent.putExtra("crop", "true");
			//indicate aspect of desired crop
			cropIntent.putExtra("aspectX", 4);
			cropIntent.putExtra("aspectY", 3);
			//indicate output X and Y
			cropIntent.putExtra("outputX", 256);
			cropIntent.putExtra("outputY", 256);
			//cropIntent.putExtra("scaleUpIfNeeded", true);

			//retrieve data on return
			cropIntent.putExtra("return-data", true);
			File file = getTempFile("IMG_");
			if (file != null)
				resultFileUri = Uri.fromFile(file);
			cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, resultFileUri);
			//start the activity - we handle returning in onActivityResult
			startActivityForResult(cropIntent, PIC_CROP);
		} catch (ActivityNotFoundException anfe) {
			Toast.makeText(getActivity(), "Your device doesn't support the crop action!", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Send Login status to server
	 */

	ValidateUserLoginInterface mValidateUserLoginInterface = new ValidateUserLoginInterface() {
		@Override
		public void validateUserLogin(Context context,ValidateUserLoginStatus mValidateUserLoginStatus) {

			if(mValidateUserLoginStatus != null) {
				performSignOutTask();
			}else{
				pdDialog.dismiss();
			}
		}
	};

	public Uri getImageUri(Context inContext, Bitmap inImage) {
		String root = Environment.getExternalStorageDirectory().toString();
		File myDir = new File(root + "/saved_images");
		myDir.mkdirs();
		String fname = "Image-Profile.jpg";
		File file = new File (myDir, fname);
		if (file.exists ())
			file.delete ();
		try {
			FileOutputStream out = new FileOutputStream(file);
			inImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Uri.fromFile(file);
	}


	private File getTempFile(String tempPhotoFile) {
		if (isSDCARDMounted()) {
			File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME);
			if (!path.exists()) {
				path.mkdir();
			}
			File file = new File(path, (tempPhotoFile + System.currentTimeMillis() + ".jpg"));

			if (file.exists()) {
				file.delete();
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return file;
		} else
			return null;
	}

	public  boolean isSDCARDMounted() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED))
			return true;
		return false;
	}
}
