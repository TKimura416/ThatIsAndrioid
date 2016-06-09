package com.thatsit.android.activities;

import org.jivesoftware.smack.XMPPConnection;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.seasia.myquick.asyncTasks.GetDataAsyncSubscriptionHistory;
import com.seasia.myquick.model.AppSinglton;
import com.seasia.myquick.model.CheckSubscriptionKeyValidity;
import com.seasia.myquick.model.GetSubscriptionHistoryTemplate;
import com.thatsit.android.MainService;
import com.thatsit.android.R;
import com.thatsit.android.Utility;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.interfaces.DaysLeftInterface;
import com.thatsit.android.interfaces.SubscriptionHistoryInterface;
import com.thatsit.android.xmpputils.Constants;
import com.thatsit.android.xmpputils.XmppManager;

public class PaymentConfirmationActivity extends Activity implements OnClickListener{
	private Button mBtn_PaymentConfr_Sign;
	private TextView mtxt_Id, mTxt_ExpiryDate;
	private int DaysLeft;
	private String ExpiryDate;
	private static final Intent SERVICE_INTENT = new Intent();
	private XmppManager mXmppManager;
	private XMPPConnection mConnection;
	private MainService mService;
	private boolean mBinded=false;
	private Boolean checkRegistration=false;

	private SharedPreferences mSharedPreferences;
	static {
		SERVICE_INTENT.setComponent(new ComponentName(Constants.MAINSERVICE_PACKAGE,  Constants.MAINSERVICE_PACKAGE + Constants.MAINSERVICE_NAME ));
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_payment_confirmation_screen);

		try {
			Utility.setDeviceTypeAndSecureFlag(PaymentConfirmationActivity.this);
			mXmppManager = XmppManager.getInstance();
			mConnection = mXmppManager.getXMPPConnection();
			mSharedPreferences = getSharedPreferences("USERID", MODE_WORLD_READABLE);
			AppSinglton.userId = mSharedPreferences.getString("USERID", "");

			initialise_Variables();
			initialise_Listeners();


			callExpiryWebService();

			/*new GetDataAsyncDaysLeft(PaymentConfirmationActivity.this,
					AppSinglton.userId,mDaysLeftInterface).execute();*/
			SharedPreferences mSharedPreferences_Gcm = ThatItApplication.getApplication().getSharedPreferences("GcmPreference", Context.MODE_PRIVATE);
			mSharedPreferences_Gcm.edit().clear().commit();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public PaymentConfirmationActivity(){
	}


	@Override
	protected void onDestroy() {
		try {
			super.onDestroy();
			if (mBinded) {
				unbindService(serviceConnection);
				mBinded=false;
				finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Initialise class variables.
	 */
	private void initialise_Variables() {
		mBtn_PaymentConfr_Sign = (Button) findViewById(R.id.paymentConfirm_btn_signin);
		mtxt_Id = (TextView) findViewById(R.id.paymentConfirm_txtview_idNo);
		mTxt_ExpiryDate = (TextView) findViewById(R.id.paymentConfirm_txtview_date);
	}

	private void initialise_Listeners() {
		mBtn_PaymentConfr_Sign.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.paymentConfirm_btn_signin:
			try {
				if(checkRegistration){
					Toast.makeText(PaymentConfirmationActivity.this, "You Are Not Registered.Please Register To Continue", Toast.LENGTH_LONG).show();
					return;
				}
				if (NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {

					Utility.startDialog(PaymentConfirmationActivity.this);
					Utility.LoginStarted = true;
					Utility.startLoginTimer(PaymentConfirmationActivity.this,2);
					connectXMPPService();	
				} else {
					Toast.makeText(PaymentConfirmationActivity.this,getResources().getString(R.string.Network_Availability), Toast.LENGTH_SHORT).show();
				}
			}  catch (Exception e) {
				Utility.stopDialog();
				e.printStackTrace();
			}
			break;

		default:
			break;
		}
	}

	/**
	 * Method to connect connectiom to xmpp server.
	 */
	private void connectXMPPService(){

		try {
			if (MainService.mService == null) {
				startService(new Intent(PaymentConfirmationActivity.this, MainService.class));
			} else {
				MainService.mService.connectAsync();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Utility.stopDialog();
		}
	}


	/**
	 * background service to maintain connection.
	 */

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			try {
				Log.d("", "   ServiceConnected   ********************");
				mService = ((MainService.MyBinder) binder).getService();
				mBinded = true;
				mService.connectAsync();

			} catch (Exception e) {
				Utility.stopDialog();
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			try {
				mConnection = null;
				mService = null;
			} catch (Exception e) {
				e.printStackTrace();
				Utility.stopDialog();
			}
		}
	};

	/**
	 * Callback to retrive no of days left in Id Expiration .
	 */

	DaysLeftInterface mDaysLeftInterface = new DaysLeftInterface() {

		@Override
		public void daysLeft(CheckSubscriptionKeyValidity daysLeft) {
			try {

				if(daysLeft!=null){

					DaysLeft = daysLeft.getmCheckSubscriptionResult().getCheckSubscriptionParams()[0].getDaysLeft();
					System.out.println("GetUserId" + AppSinglton.userId);
					mtxt_Id.setText(AppSinglton.thatsItPincode);
					mTxt_ExpiryDate.setText("" + DaysLeft);
				}else{
					checkRegistration=true;
					Toast.makeText(PaymentConfirmationActivity.this, "Registration Unsuccessfull", Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	};

	/**
	 * Check for ID Expiration.
	 */
	private void callExpiryWebService() {

		Utility.startDialog(PaymentConfirmationActivity.this);
		new GetDataAsyncSubscriptionHistory(PaymentConfirmationActivity.this,
				AppSinglton.userId,mSubscriptionHistoryInterface).execute();
	}

	/**
	 *  Get expiration date for the jID.
	 */

	SubscriptionHistoryInterface mSubscriptionHistoryInterface = new SubscriptionHistoryInterface() {

		@Override
		public void subscriptionHistory(
				GetSubscriptionHistoryTemplate mGetSubscriptionHistoryTemplate) {

			Utility.stopDialog();
			try {
				if(mGetSubscriptionHistoryTemplate!=null){

					ExpiryDate = mGetSubscriptionHistoryTemplate.getGetSubscriptionHistoryResult().getGetSubscriptionHistoryParams()[0].getSubscriptionExpiryDate();
					mtxt_Id.setText(AppSinglton.thatsItPincode);
					mTxt_ExpiryDate.setText(ExpiryDate);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

}
