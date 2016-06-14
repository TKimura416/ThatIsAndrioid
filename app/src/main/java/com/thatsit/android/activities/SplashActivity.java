package com.thatsit.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;

import com.seasia.myquick.model.ValidateUserLoginStatus;
import com.thatsit.android.MainService;
import com.thatsit.android.R;
import com.thatsit.android.Utility;
import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.thatsit.android.interfaces.ValidateUserLoginInterface;

public class SplashActivity extends Activity {
	private final int SPLASH_DISPLAY_LENGHT = 2000;
	private SharedPreferences mSharedPreferences;
	private String text;
	public static boolean userVisited = false;
	private static boolean retryClicked = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash_activity);
		
		Utility.setDeviceTypeAndSecureFlag(SplashActivity.this);
		clearUtilityBooleans();
		executeTask();
	}

	/**
	 * Clear all boolean vlaues used in the application.
	 */
	private void clearUtilityBooleans() {
		Utility.splashActivity = SplashActivity.this;
		userVisited = true;
		Utility.contactActivity =null;
		Utility.fragmentContact = null;
		Utility.enteredFragmentOnce = false;
		Utility.serviceBinded = false;
	}

	/**
	 *  Open alert dialog if no network available
	 *  Start service and proceed to contact activity
	 */
	private void executeTask() {

		if(!NetworkAvailabilityReceiver.isInternetAvailable(SplashActivity.this) && Utility.getUserName().length()>0){
			AlertDialog.Builder builder = new Builder(SplashActivity.this);
			builder.setTitle("That's It");
			builder.setMessage(getResources().getString(R.string.Network_Availability));
			builder.setPositiveButton("Retry", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					retryClicked = true;
					dialog.dismiss();
					executeTask();
				}
			});
			builder.setCancelable(false);
			builder.show();
		}
		else{
			if(Utility.getUserName().length()>0 && MainService.mService==null){
				Utility.startDialog(SplashActivity.this);
				Utility.UserLoginStatus(SplashActivity.this, Utility.getEmail(), "","","", mValidateUserLoginInterface);
			}
			else if(Utility.getUserName().length()>0 && MainService.mService != null){
				Utility.reloginCalled = true;
				startActivity(new Intent(SplashActivity.this, ContactActivity.class));
				finish();
				Utility.allowAuthenticationDialog = true;

			}
			else{
				mSharedPreferences = getSharedPreferences("mypre", 0);
				text = mSharedPreferences.getString("conditionsAccepted", "");
				new MyAsyncTask().execute();
			}
		}
	}

	private class MyAsyncTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(SPLASH_DISPLAY_LENGHT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			if (text.equalsIgnoreCase("")) {
				Intent intent = new Intent(SplashActivity.this, TermsAndConditionsActivity.class);
				startActivity(intent);
				finish();
			}
			else{
				Intent mIntent = new Intent(SplashActivity.this, WelcomeActivity.class);
				startActivity(mIntent);
				finish();
			}
		}
	}


	/**
	 *  Validate User Login Status
	 */
	private final ValidateUserLoginInterface mValidateUserLoginInterface = new ValidateUserLoginInterface() {
		@Override
		public void validateUserLogin(Context context,ValidateUserLoginStatus mValidateUserLoginStatus) {

			try {
				if(mValidateUserLoginStatus != null){

					String UserLoginStatus = mValidateUserLoginStatus.getValidateUserLoginStatusResult().getUserLoginStatus();
					if(UserLoginStatus != null){

						if(UserLoginStatus.equalsIgnoreCase("True")){
							// Check if status id is equal to one present in shared preference
							// If both values are equal, perform login
							// else show already login alert

							String statusID = mValidateUserLoginStatus.getValidateUserLoginStatusResult().getStatusId();

							SharedPreferences mSharedPreferences = getSharedPreferences("statusID", MODE_PRIVATE);
							String sharedStatusID = mSharedPreferences.getString("statusID", "");

							if(statusID.equalsIgnoreCase(sharedStatusID)){
								Utility.LoginStarted = true;
								Utility.startLoginTimer(SplashActivity.this,1);
								startService(new Intent(SplashActivity.this, MainService.class));
								Utility.allowAuthenticationDialog = true;
							}else{
								Utility.stopDialog();
								Utility.openAlertSplash(SplashActivity.this, "AlreadyLoggedIn", "User already logged in some other device. Please login with different account.");
							}
						}
						else{
							// allow user to login
							Utility.LoginStarted = true;
							Utility.startLoginTimer(SplashActivity.this,1);
							startService(new Intent(SplashActivity.this, MainService.class));
							Utility.allowAuthenticationDialog = true;
						}
					}else{
						Utility.stopDialog();
						Utility.showMessage("That's It ID does not exist");
					}
				}else{
					Utility.stopDialog();
				}
			} catch (Exception e) {
				Utility.stopDialog();
				e.printStackTrace();
			}
		}
	};
}
