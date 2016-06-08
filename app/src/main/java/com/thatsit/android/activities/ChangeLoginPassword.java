package com.thatsit.android.activities;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.thatsit.android.R;
import com.thatsit.android.Utility;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.encryption.helper.EncryptionManager;
import com.thatsit.android.xmpputils.XmppManager;
import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.seasia.myquick.controller.WebServiceClient;
import com.seasia.myquick.model.AppSinglton;
import com.seasia.myquick.model.UpdateUserPasswordTemplate;

import java.net.URLEncoder;

/**
 * 
 *This class includes work to change the login password for particular jID.
 */
public class ChangeLoginPassword extends Activity implements OnClickListener{

	public static boolean isPromtAllowed=true;
	private EditText edt_enterOldPwd,edt_enterNewPwd,edt_confirmNewPwd;
	private Button createPass_btn_submit;
	private String OldPassword,NewPassword,ConfirmNewPassword;
	private String getRetVal;
	private EncryptionManager encryptionManager;
	private SharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Utility.allowAuthenticationDialog=false;
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_change_login_password_screen);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		Utility.setDeviceTypeAndSecureFlag(ChangeLoginPassword.this);
		initialiseVariable();
		initialiseListener();
		encryptionManager = new EncryptionManager();
		mSharedPreferences = getSharedPreferences("USERID", MODE_WORLD_READABLE);
		AppSinglton.userId = mSharedPreferences.getString("USERID", "");

	}

	@Override
	protected void onResume() {
		super.onResume();
		if(Utility.allowAuthenticationDialog==true){
			Utility.showLock(ChangeLoginPassword.this);
		}
		Utility.UserPauseStatus(ChangeLoginPassword.this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Utility.allowAuthenticationDialog=false;
	}

	@Override
	protected void onStop() {
		super.onStop();
		Utility.taskPromtOnStop(isPromtAllowed, ChangeLoginPassword.this);
	}

	/**
	 * Initialise class variables.
	 */
	private void initialiseVariable() {

		edt_enterOldPwd=(EditText) findViewById(R.id.edt_enterOldPwd);
		edt_enterNewPwd=(EditText) findViewById(R.id.edt_enterNewPwd);
		edt_confirmNewPwd=(EditText) findViewById(R.id.edt_confirmNewPwd);
		createPass_btn_submit=(Button) findViewById(R.id.createPass_btn_submit);
	}

	/**
	 * Initialise click listeners.
	 */
	private void initialiseListener() {
		createPass_btn_submit.setOnClickListener(this);
		edt_confirmNewPwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

			case R.id.createPass_btn_submit:
				submitPassword();
				break;
			default:
				break;
		}
	}

	private void submitPassword() {

		try {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(createPass_btn_submit.getWindowToken(), 0);
			if (NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {
				check_Validation();
			} else {
				Toast.makeText(ChangeLoginPassword.this,getResources().getString(R.string.Network_Availability), Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Check fields are filled and are correct before changing the password.
	 */
	private void check_Validation() {
		try {
			OldPassword = encryptionManager.encryptPayload(edt_enterOldPwd.getText().toString());
			NewPassword = encryptionManager.encryptPayload(edt_enterNewPwd.getText().toString());
			ConfirmNewPassword = encryptionManager.encryptPayload(edt_confirmNewPwd.getText().toString());

			if (edt_enterOldPwd.getText().toString().trim().toCharArray().length == 0){
				Toast.makeText(ChangeLoginPassword.this,"Please Enter Old Password", Toast.LENGTH_SHORT).show();
			}
			else if (edt_enterNewPwd.getText().toString().trim().toCharArray().length == 0){
				Toast.makeText(ChangeLoginPassword.this,"Please Enter New Password", Toast.LENGTH_SHORT).show();
			}
			else if (edt_enterNewPwd.getText().toString().trim().toCharArray().length>15){
				Toast.makeText(ChangeLoginPassword.this,"Maximum Chat Password Length Should be 15 Characters", Toast.LENGTH_SHORT).show();
			}
			else if (edt_confirmNewPwd.getText().toString().trim().toCharArray().length == 0){
				Toast.makeText(ChangeLoginPassword.this,"Please Confirm New Password", Toast.LENGTH_SHORT).show();
			}
			else if (!NewPassword.equals(ConfirmNewPassword)){
				Toast.makeText(ChangeLoginPassword.this,"Passwords Do Not Match", Toast.LENGTH_SHORT).show();
			}
			else if (edt_enterNewPwd.getText().toString().trim().toCharArray().length<6){
				Toast.makeText(ChangeLoginPassword.this,"Minimum User Password Length Should be 6 Characters", Toast.LENGTH_SHORT).show();
			}
			/*else if(NewPassword.equalsIgnoreCase(Utility.getChatPassword())){

			}*/
			else{

				Utility.startDialog(ChangeLoginPassword.this);
				OldPassword = URLEncoder.encode(OldPassword, "UTF-8");
				if(OldPassword.contains("%")){
					OldPassword = OldPassword.replace("%","2");
				}
				NewPassword = URLEncoder.encode(NewPassword,"UTF-8");
				if(NewPassword.contains("%")){
					NewPassword = NewPassword.replace("%","2");
				}
				new GetDataAsync().execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Async Task to update user password.
	 */
	private class GetDataAsync extends AsyncTask<Void, Void, UpdateUserPasswordTemplate> {

		@Override
		protected UpdateUserPasswordTemplate doInBackground(Void... arg0) {

			try {
				UpdateUserPasswordTemplate obj = new WebServiceClient(ChangeLoginPassword.this)
						.updateUserPassword(AppSinglton.userId,OldPassword,NewPassword);
				if(obj!=null)
					if(obj.getmUpdateUserPasswordResult().getmUpdateUserPasswordParams()[0].getRetVal().equals("1")){

						if(XmppManager.getInstance().getXMPPConnection()!=null	&& XmppManager.getInstance().getXMPPConnection().isAuthenticated() ){
							XmppManager.getInstance().getXMPPConnection().getAccountManager().changePassword(NewPassword);
						}

						SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(ChangeLoginPassword.this).edit();
						edit.putString(ThatItApplication.ACCOUNT_PASSWORD_KEY, NewPassword);
						edit.commit();
					}
				return obj;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(UpdateUserPasswordTemplate result) {
			super.onPostExecute(result);
			try {
				edt_enterOldPwd.setText("");
				edt_enterNewPwd.setText("");
				edt_confirmNewPwd.setText("");
				getRetVal=result.getmUpdateUserPasswordResult().getmUpdateUserPasswordParams()[0].getRetVal();
				Utility.stopDialog();
				if(getRetVal.equals("1")){
					Toast.makeText(ChangeLoginPassword.this, "Your Password Changed Successfully", Toast.LENGTH_LONG).show();
				}
				else{
					Toast.makeText(ChangeLoginPassword.this, "Old Password 1 is incorrect", Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
				Utility.stopDialog();
			}
		}
	}
}
