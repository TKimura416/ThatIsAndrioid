package com.thatsit.android.activities;

import java.net.URLEncoder;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.myquickapp.receivers.ConnectionBroadcastReceiver;
import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.seasia.myquick.controller.WebServiceClient;
import com.seasia.myquick.model.AppSinglton;
import com.seasia.myquick.model.AuthenticateUserServiceTemplate;
import com.seasia.myquick.model.ValidateUserLoginStatus;
import com.thatsit.android.MainService;
import com.thatsit.android.R;
import com.thatsit.android.Utility;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.beans.LogFile;
import com.thatsit.android.db.DbOpenHelper;
import com.thatsit.android.encryption.helper.EncryptionManager;
import com.thatsit.android.interfaces.OrientationListener;
import com.thatsit.android.interfaces.ValidateUserLoginInterface;
import com.thatsit.android.view.OrientationManager;
import com.thatsit.android.xmpputils.Constants;
import com.thatsit.android.xmpputils.XmppManager;

@SuppressLint("WorldWriteableFiles")
public class WelcomeActivity extends FragmentActivity implements OnClickListener,OrientationListener {
	private boolean isShown = false;
	final String TAG = getClass().getSimpleName();
	public static Button mBtn_SignIn, mBtn_BuyId;
	private String ThatsItId, ThatsItpassword, EmailId;
	boolean mBinded;
	private SharedPreferences settings;
	private String jid, password;
	public static boolean dismissProgressBar = false;
	private static ConnectionBroadcastReceiver connectionBroadcastReceiver = new ConnectionBroadcastReceiver();
	private XmppManager mXmppManager;
	private XMPPConnection mConnection;
	private MainService mService;
	private ThatItApplication myApplication;
	private SharedPreferences mSharedPreferences;
	public static boolean userVisited = false;
	private EncryptionManager encryptionManager;
	private TextView txt_havePincode;
	private Dialog signIndialog;
	private static final Intent SERVICE_INTENT = new Intent();
	private EditText etUsername,etPassword;
	private InputMethodManager imm;

	static {
		SERVICE_INTENT.setComponent(new ComponentName(
				Constants.MAINSERVICE_PACKAGE, Constants.MAINSERVICE_PACKAGE
				+ Constants.MAINSERVICE_NAME));
	}

	private int SCREEN_ORIENTATION=0;
	private OrientationManager orientationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_welcom);

		orientationManager = new OrientationManager(WelcomeActivity.this, SensorManager.SENSOR_DELAY_NORMAL, this);
		orientationManager.enable();

		try {
			Utility.setDeviceTypeAndSecureFlag(WelcomeActivity.this);
			imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			userVisited = true;
			initialiseXmppConnection();
			initialiseSharedPreferences();
			initialise_Variables();
			registerReceivers();
			initialise_Listener();
			checkIfEmailPersistsInSharedPreference();
			checkIfDialogOpened(savedInstanceState);
			clearDatabaseIfExists();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (signIndialog != null) {
			outState.putString("EmailId", etUsername.getText().toString().trim());
			outState.putString("Password", etPassword.getText().toString().trim());
		}
	}

	private void checkIfDialogOpened(Bundle savedInstanceState) {

		if(Utility.isDialogOpened == true){
			showLoginPromtScreen();
			etUsername.setText(savedInstanceState.getString("EmailId"));
			etPassword.setText(savedInstanceState.getString("Password"));
		}
	}

	private void checkIfEmailPersistsInSharedPreference() {
		if(!TextUtils.isEmpty(Utility.getEmail())){
			Utility.UserLoginStatus(WelcomeActivity.this,Utility.getEmail(), "False","","", validateUserLoginInterface);
		}
	}

	/**
	 * Register broadcast receiver for connection error alert
	 */
	private void registerReceivers() {

		IntentFilter filter = new IntentFilter();
		filter.addAction(MainService.CONNECTION_CLOSED);
		filter.addAction(MainService.SIGNIN);
		registerReceiver(connectionBroadcastReceiver, filter);

		ThatItApplication.getApplication().registerReceiver(
				new IncomingReceiver(),
				new IntentFilter("Connection Error While Sign In"));

	}

	/**
	 * Get UserID from shared preference
	 */
	private void initialiseSharedPreferences() {
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		encryptionManager = new EncryptionManager();
		mSharedPreferences = getSharedPreferences("USERID", 0);
	}

	/**
	 * Establish XMPP connection.
	 */
	private void initialiseXmppConnection() {
		mXmppManager = XmppManager.getInstance();
		mConnection = mXmppManager.getXMPPConnection();
	}

	public WelcomeActivity() {
	}

	@Override
	protected void onStop() {
		try {
			super.onStop();
			unregisterReceiver(connectionBroadcastReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		try {
			super.onDestroy();
			Utility.alertDialog = null;
			signIndialog = null;
			if (mBinded) {
				unbindService(serviceConnection);
				mBinded = false;
			}
			orientationManager.disable();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialise class variables.
	 */
	private void initialise_Variables() {
		mBtn_SignIn = (Button) findViewById(R.id.btn_signIn);
		mBtn_BuyId = (Button) findViewById(R.id.btn_buyID);
		txt_havePincode = (TextView) findViewById(R.id.txt_havePincode);
	}

	/**
	 * Initialise click listeners.
	 */
	private void initialise_Listener() {
		mBtn_SignIn.setOnClickListener(this);
		mBtn_BuyId.setOnClickListener(this);
		txt_havePincode.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

			case R.id.btn_signIn:
				try {
					if (NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {

						if (!isShown) {
							showLoginPromtScreen();
						}
					} else {
						Toast.makeText(WelcomeActivity.this,getResources().getString(R.string.Network_Availability),Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case R.id.btn_buyID:

				SharedPreferences clearPseudoData = getSharedPreferences("UpdatePseudoName", Context.MODE_PRIVATE);
				clearPseudoData.edit().clear().commit();

				SharedPreferences clearRegisterData = getSharedPreferences("register_data", Context.MODE_PRIVATE);
				clearRegisterData.edit().clear().commit();

				Utility.clearAllSharedPreferences();

				try {
					if (NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {
						Utility.hasPincode = true;
						startActivity(new Intent(WelcomeActivity.this,RegisterActivity.class));

					} else {
						Toast.makeText(WelcomeActivity.this,getResources().getString(R.string.Network_Availability),
								Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			default:
				break;
		}
	}

	/**
	 * Connect to Openfire Server.
	 */

	private void connectXMPPService() {
		try {
			jid = ThatsItId;
			password = this.ThatsItpassword;
			if ((TextUtils.isEmpty(jid) && TextUtils.isEmpty(password)))
				return;
			saveCredential();

			if (MainService.mService == null) {
				startService(new Intent(WelcomeActivity.this, MainService.class));
			} else {
				MainService.mService.connectAsync();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}
	@Override
	protected void onResume() {
		super.onResume();
		try {
			skipLoginScreenIfAlreadyConnected();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void skipLoginScreenIfAlreadyConnected() {
		try {
			myApplication = ThatItApplication.getApplication();
			if (myApplication instanceof ThatItApplication) {
				if (myApplication.isConnected() && mConnection.isConnected()) {
					Intent intent = new Intent(WelcomeActivity.this,
							ContactActivity.class);
					startActivity(intent);
				} else if (!myApplication.isAccountConfigured()) {
					mBtn_SignIn.setEnabled(true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create connection and bind service.
	 */
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.d(TAG, "   ServiceConnected   ********************");
			mService = ((MainService.MyBinder) binder).getService();
			mBinded = true;
			Utility.serviceBinded = true;
			try {
				mService.connectAsync();
			} catch (Exception e) {
				e.printStackTrace();
			}
			PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
			mConnection.addPacketListener(new PacketListener() {

				public void processPacket(Packet packet) {
					Message message = (Message) packet;
					if (message.getBody() != null) {
						String fromName = StringUtils.parseBareAddress(message
								.getFrom());
						Log.i("XMPPClient", "Got text [" + message.getBody()
								+ "] from [" + fromName + "]");
					}
				}
			}, filter);
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mConnection = null;
			mService = null;
		}
	};

	/**
	 * Store user credentials in shared preference.
	 */
	void saveCredential() {
		try {
			SharedPreferences.Editor edit = settings.edit();
			edit.putString(ThatItApplication.ACCOUNT_USERNAME_KEY, jid);
			edit.putString(ThatItApplication.ACCOUNT_PASSWORD_KEY, password);
			edit.putString(ThatItApplication.ACCOUNT_EMAIL_ID, EmailId);
			edit.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onOrientationChange(OrientationManager.ScreenOrientation screenOrientation) {
		Log.d(TAG, "onOrientationChange-screenOrientation->" + screenOrientation);

		switch (screenOrientation) {

			case PORTRAIT:
				SCREEN_ORIENTATION = 0;
				break;

			/*case REVERSED_PORTRAIT:
				SCREEN_ORIENTATION = 1;
				break;*/

			case LANDSCAPE:
				SCREEN_ORIENTATION = 2;
				break;

			/*case REVERSED_LANDSCAPE:
				SCREEN_ORIENTATION = 3;
				break;*/

		}
	}

	/**
	 * Async to Login on server.
	 */
	private class GetDataAsync extends AsyncTask<Void, Void, AuthenticateUserServiceTemplate> {

		@Override
		protected AuthenticateUserServiceTemplate doInBackground(Void... arg0) {

			try {
				AuthenticateUserServiceTemplate obj = new WebServiceClient(
						WelcomeActivity.this).getUserSignIn(EmailId,
						ThatsItpassword);
				return obj;
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(AuthenticateUserServiceTemplate result) {

			try {
				super.onPostExecute(result);
				if (result != null) {
					AppSinglton.userId = result.getmAuthenticateUserServiceResult().getmAuthenticateUserServiceParams()[0].getRetVal();
					mSharedPreferences.edit().putString("USERID", AppSinglton.userId).commit();
					AppSinglton.thatsItPincode = ThatsItId = result.getmAuthenticateUserServiceResult().getmAuthenticateUserServiceParams()[0].getPinCode();

					Utility.saveThatsItPincode(WelcomeActivity.this,AppSinglton.thatsItPincode);

					if (AppSinglton.userId.equals("3")) {
						Utility.stopDialog();
						Utility.showMessage("Please Check Your Credentials");
						Utility.unlockScreenRotation(WelcomeActivity.this);

					} else if (AppSinglton.userId.equals("2")) {
						Utility.stopDialog();
						Utility.LoginStarted = false;
						if(Utility.mTimer != null){
							Utility.mTimer.cancel();
						}
						openAlert("Your account has been suspended by the Admin");
						Utility.unlockScreenRotation(WelcomeActivity.this);

					} else {
						dismissProgressBar = true;
						try {
							Utility.LoginStarted = true;
							Utility.startLoginTimer(WelcomeActivity.this,1);
							connectXMPPService();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					Utility.unlockScreenRotation(WelcomeActivity.this);
					Utility.stopDialog();
					Utility.showMessage("Sign In Error");
				}
			} catch (Exception e) {
				Utility.unlockScreenRotation(WelcomeActivity.this);
				Utility.stopDialog();
				Utility.showMessage("Sign In Error");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Enter login credentials alert.
	 */
	public void showLoginPromtScreen() {

		if (signIndialog == null) {
			Utility.isDialogOpened = true;
			signIndialog = new Dialog(WelcomeActivity.this);
			signIndialog.setContentView(R.layout.login_promt);
			signIndialog.setTitle("Enter Credentials");
			signIndialog.setCancelable(false);

			try {
				Button btnAccept = (Button) signIndialog.findViewById(R.id.btn_accept);
				Button btnDecline = (Button) signIndialog.findViewById(R.id.btn_decline);
				etUsername = (EditText) signIndialog.findViewById(R.id.etUsername);
				TextView signInDifferentUser = (TextView) signIndialog.findViewById(R.id.signInDifferentUser);
				etPassword = (EditText) signIndialog.findViewById(R.id.etPassword);
				signInDifferentUser.setVisibility(View.GONE);

				btnAccept.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {

						try {
							imm.hideSoftInputFromWindow(etUsername.getWindowToken(), 0);
							SharedPreferences mSharedPreferences_Gcm = ThatItApplication.getApplication().getSharedPreferences("GcmPreference",Context.MODE_PRIVATE);
							mSharedPreferences_Gcm.edit().clear().commit();
							EmailId = etUsername.getText().toString().trim();
							ThatsItpassword = encryptionManager.encryptPayload(etPassword.getText().toString().trim());
							if (EmailId.trim().toCharArray().length == 0) {
								Toast.makeText(WelcomeActivity.this,"Please Enter Your Email Id",Toast.LENGTH_LONG).show();

							} else if (ThatsItpassword.trim().toCharArray().length == 0) {
								Toast.makeText(WelcomeActivity.this,"Please Enter Your Password",Toast.LENGTH_LONG).show();

							} else {
								Utility.email_id = EmailId.trim();
								signIndialog.dismiss();
								Utility.isDialogOpened = false;
								isShown = false;
								ThatsItpassword = URLEncoder.encode(ThatsItpassword, "UTF-8");

								if (ThatsItpassword.contains("%")) {
									ThatsItpassword = ThatsItpassword.replace("%", "2");
								}

								Utility.lockScreenRotation(WelcomeActivity.this,SCREEN_ORIENTATION);
								// Check User Login Status
								Utility.startDialog(WelcomeActivity.this);

								//	new GetDataAsync().execute();
								Utility.UserLoginStatus(WelcomeActivity.this, EmailId, "","","", mValidateUserLoginInterface);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

				btnDecline.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						try {
							etUsername.setText("");
							etPassword.setText("");
							signIndialog.dismiss();
							Utility.isDialogOpened = false;
							isShown = false;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		signIndialog.show();
	}

	/**
	 * Broadcast receiver to encounter connection error.
	 */
	class IncomingReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, final Intent intent) {
			try {
				if (intent.getAction().equalsIgnoreCase(
						"Connection Error While Sign In")) {

					Utility.openAlert(WelcomeActivity.this,"InternetUnstable","Your internet connection seems to be unstable");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 *  Validate User Login Status
	 */
	ValidateUserLoginInterface mValidateUserLoginInterface = new ValidateUserLoginInterface() {
		@Override
		public void validateUserLogin(Context context,ValidateUserLoginStatus mValidateUserLoginStatus) {

			try {
				if(mValidateUserLoginStatus != null){

					String UserLoginStatus = mValidateUserLoginStatus.getValidateUserLoginStatusResult().getUserLoginStatus();
					String statusID = "";
					if(UserLoginStatus != null){
						if(UserLoginStatus.equalsIgnoreCase("True")){
							// Check if status id is equal to one present in log file
							// If both values are equal, perform login
							// else show already login alert

							 statusID = mValidateUserLoginStatus.getValidateUserLoginStatusResult().getStatusId();

							/*SharedPreferences mSharedPreferences = getSharedPreferences("statusID", MODE_PRIVATE);
							String sharedStatusID = mSharedPreferences.getString("statusID", "");*/

							/*if(statusID.equalsIgnoreCase(sharedStatusID)){
								new GetDataAsync().execute();
							}*/
							if(LogFile.logExists(statusID) == true){
								new GetDataAsync().execute();
							}
							else{
								Utility.stopDialog();
								Utility.openAlert(WelcomeActivity.this,"AlreadyLoggedIn", "User already logged in some other device.");
								Utility.unlockScreenRotation(WelcomeActivity.this);
							}
						}
						else{
							// allow user to login
							LogFile.deleteLog(statusID);
							new GetDataAsync().execute();
						}
					}else{
						Utility.stopDialog();
						Utility.unlockScreenRotation(WelcomeActivity.this);
						Utility.showMessage("That's It ID does not exist");
					}
				}else{
					Utility.unlockScreenRotation(WelcomeActivity.this);
					Utility.stopDialog();
				}
			} catch (Exception e) {
				Utility.stopDialog();
				Utility.unlockScreenRotation(WelcomeActivity.this);
				e.printStackTrace();
			}
		}
	};

	/**
	 * Send Login status to server
	 */

	ValidateUserLoginInterface validateUserLoginInterface = new ValidateUserLoginInterface() {
		@Override
		public void validateUserLogin(Context context,ValidateUserLoginStatus mValidateUserLoginStatus) {
			if(mValidateUserLoginStatus != null){
				settings = PreferenceManager.getDefaultSharedPreferences(ThatItApplication.getApplication());
				settings.edit().clear().commit();
			}
		}
	};

	private AlertDialog alertDialog;
	private void openAlert(final String Message) {

		try {
			if (alertDialog == null) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(WelcomeActivity.this);
				alertDialogBuilder.setTitle("That's It");
				alertDialogBuilder.setMessage(Message);

				alertDialogBuilder.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {

								alertDialog.dismiss();
								alertDialog = null;
							}
						});

				alertDialog = alertDialogBuilder.create();
				alertDialog.show();
				alertDialog.setCancelable(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Delete entire app history if exists
	 */
	private void clearDatabaseIfExists(){
		DbOpenHelper dbOpenHelper = new DbOpenHelper(ThatItApplication.getApplication());
		dbOpenHelper.deleteDatabase();
	}
}
