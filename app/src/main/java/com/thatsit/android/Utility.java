package com.thatsit.android;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smackx.packet.VCard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.seasia.myquick.asyncTasks.UserPauseStateAsync;
import com.seasia.myquick.asyncTasks.ValidateUserLoginStatusAsync;
import com.seasia.myquick.model.AuthenticateUserServiceTemplate;
import com.seasia.myquick.model.ValidateUserLoginStatus;
import com.thatsit.android.activities.BlurredActivity;
import com.thatsit.android.activities.ContactActivity;
import com.thatsit.android.activities.InviteContactsToRoster;
import com.thatsit.android.activities.SplashActivity;
import com.thatsit.android.activities.WelcomeActivity;
import com.thatsit.android.adapter.ImageAdapter;
import com.thatsit.android.adapter.PresenceAdapter;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.db.DbOpenHelper;
import com.thatsit.android.db.One2OneChatDb;
import com.thatsit.android.encryption.helper.EncryptionManager;
import com.thatsit.android.fragement.FragmentChatHistoryScreen;
import com.thatsit.android.fragement.FragmentChatScreen;
import com.thatsit.android.fragement.FragmentContact;
import com.thatsit.android.fragement.FragmentInvitationReceive;
import com.thatsit.android.fragement.FragmentInvitationSent;
import com.thatsit.android.interfaces.ValidateUserLoginInterface;
import com.thatsit.android.interfaces.ValidateUserPauseStateInterface;
import com.thatsit.android.invites.PresenceType;
import com.thatsit.android.xmpputils.XmppManager;
import com.myquick.socket.ClientChatThread;
import com.myquick.socket.Constants;
import com.myquick.socket.ServerThread;
import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.seasia.myquick.model.AppSinglton;

/**
 * @author psingh5
 * This class includes static methods which are declared here once and used all
 * over the app to avoid redundancy such as show toast, show dialogs, show connection
 * error alerts etc. 
 *
 */
public class Utility {

	public static FragmentInvitationReceive fragmentInvitationReceive = null;
	public static FragmentInvitationSent fragmentInvitationsent = null;
	public static FragmentChatHistoryScreen fragmentChatHistoryScreen = null;
	public static FragmentChatScreen fragmentChatScreen = null;
	public static FragmentContact fragmentContact = null;
	public static RefreshApplicationListener refreshApplicationListener = null;
	public static boolean allowAuthenticationDialog;
	private static boolean isShowing = false;
	private static DbOpenHelper dbOpenHelper;
	private static SharedPreferences settings,prefSaveThatsItId;
	public static boolean VcardLoadedOnce = false;
	public static boolean RegsiterDataFetchedOnce = false;
	public static boolean FragmentContactDataFetchedOnce = false;
	public static boolean FragmentHistoryDataFetchedOnce = false;
	public static Bitmap catchedBitmap = null;
	private static final Handler handler = new Handler();
	public static InviteContactsToRoster inviteContactsToRoster=null;
	public static String email_id;
	private static Dialog dialog;
	static Dialog dialog_expiry;
	static Dialog dialogConnectionErrorAlert;
	static Dialog dialogConnectionErrorSplash = null;
	public static ContactActivity contactActivity = null;
	private static boolean isAuthenticationWindowOpened;
	public static boolean enteredFragmentOnce = false;
	private static ProgressDialog progressDialog ;
	public static boolean isAppStarted = false;
	public static SplashActivity splashActivity;
	public static WelcomeActivity welcomeActivity;
	public static boolean disconnected = false;
	public static boolean connectionClosedCalled = false;
	public static boolean reloginCalled = false;
	public static boolean loginCalledOnce = false;
	public static boolean smileyScreenOpened = false;
	public static boolean serviceBinded = false;
	private static final EncryptionManager encryptionManager = new EncryptionManager();
	public static boolean groupNotificationClicked = false;
	public static boolean fragPaymentSettingsOpen = false;
	public static boolean hasPincode = false;
	public static boolean fragChatIsOpen = false;
	private static final VCard card =new VCard();
	public static String action;
	public static boolean fragChatHistoryOpened = false;
	public static boolean disAllowSubscription = false;
	public static Timer mTimer;
	public static int radioBtnValue;
	public static boolean isBusy = false,isDialogOpened = false;
	public static boolean mBinded = false;
	public static boolean googleServicesUnavailable;

	/**
	 * @param getRosterHistoryList
	 * Delete chat history of the unsubscribed person
	 */
	public void deleteUnsubscribedUserChatHistory(

			ArrayList<String> getRosterHistoryList,
			ArrayList<Integer> rosterHistoryToBeDeleted,
			String rosterWhoseHistoryDeleted, MainService mService) {
		try {
			getRosterHistoryList = new ArrayList<>();
			rosterHistoryToBeDeleted = new ArrayList<>();
			ThatItApplication myApplication = ThatItApplication.getApplication();
			myApplication.openDatabase();

			Cursor cursor = One2OneChatDb.getAllMessagesOfParticipant(rosterWhoseHistoryDeleted);
			DatabaseUtils.dumpCursor(cursor);

			if (cursor.moveToFirst()) {
				int position = 0;
				do {
					getRosterHistoryList.add(cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_ID)));
					rosterHistoryToBeDeleted.add(Integer.parseInt(getRosterHistoryList.get(position)));
					position = position + 1;
				} while (cursor.moveToNext());
			}
			cursor.close();

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		try {
			One2OneChatDb chatDb = new One2OneChatDb(mService);
			for (int i = 0; i < rosterHistoryToBeDeleted.size(); i++) {
				chatDb.deleteMessage(rosterHistoryToBeDeleted.get(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * @param email
	 * Check whether the email entered is valid.
	 */
	public boolean isEmailValid(String email) {
		boolean isValid = false;
		String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		CharSequence inputStr = email;
		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		if (matcher.matches()) {
			isValid = true;
		}
		return isValid;
	}


	/**
	 * @param IdToBeRemoved
	 * @param sendPresence
	 * Unsubscribe Roster and set presence unavailable
	 */
	public static void removeFriendXMPP(final Context activity  , final String IdToBeRemoved , final XMPPConnection connection , boolean sendPresence){

		try{

			ThatItApplication.getApplication().getIncomingRequestHash().remove(IdToBeRemoved);
			Presence p = new Presence(Type.unsubscribed);
			p.setTo(IdToBeRemoved);

			if(sendPresence)
				sendPresence(p,IdToBeRemoved,connection);

			RosterPacket packet = new RosterPacket();
			packet.setType(IQ.Type.SET);
			RosterPacket.Item item  = new RosterPacket.Item(IdToBeRemoved, null);
			item.setItemType(RosterPacket.ItemType.remove);
			packet.addRosterItem(item);
			connection.sendPacket(packet);


		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static void sendPresence(Presence p, String deniedID, XMPPConnection  connection ) {
		try {
			PresenceAdapter preAdapt = new PresenceAdapter(p);
			Presence presence2 = new Presence(PresenceType.getPresenceTypeFrom(preAdapt.getType()));
			presence2.setTo(p.getTo());
			deniedID = p.getTo();
			if (connection != null) {
				presence2.setFrom(connection.getUser());
				connection.sendPacket(presence2);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Insert Emojicons.
	 * @param message
	 * @return
	 */
	public static String processSmileyCodes(String message){

		try{
			for(int index=0;index<ImageAdapter.mThumbIdsForDispatch.length;index++){
				message = message.replaceAll(ImageAdapter.mThumbIdsForDispatch[index], ImageAdapter.mThumbIds[index]);
			}
		}catch(Exception e){
			return message;
		}
		return message;
	}

	public static void showMessage(final String message) {

		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(ThatItApplication.getApplication(), message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 *  Set default values for sending files via socket.
	 */
	public static void resetSocket() {
		Constants.isDownloading=false;
		ClientChatThread.connected=false;
		ServerThread.currentInt=-1;
		ServerThread.currentInt=-1;
	}

	/**
	 * That's It 11-digit pincode.
	 * @return
	 */
	public static String getUserName() {
		SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(ThatItApplication.getApplication());
		return mSettings.getString(ThatItApplication.ACCOUNT_USERNAME_KEY, "");
	}

	public static String getPassword() {
		SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(ThatItApplication.getApplication());
		return mSettings.getString(ThatItApplication.ACCOUNT_PASSWORD_KEY, "");
	}

	public static String getChatPassword() {
		SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(ThatItApplication.getApplication());
		return mSettings.getString(ThatItApplication.ACCOUNT_CHAT_PASSWORD_KEY, "");
	}

	public static String getEmail() {
		SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(ThatItApplication.getApplication());
		return mSettings.getString(ThatItApplication.ACCOUNT_EMAIL_ID, "");
	}


	/**
	 * Login Dialog
	 * @param activity
	 */
	private static void showLoginPromtScreen(final Activity activity) {

		try {
			if(dialog!=null && dialog.isShowing()){
				dialog.dismiss();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		dialog=null;

		dialog = new Dialog(activity);

		dialog.setContentView(R.layout.login_promt);
		dialog.setTitle("Enter Login Password 1");

		isAuthenticationWindowOpened = true;
		Utility.isShowing = true;

		try{
            Button btnAccept  = (Button)dialog.findViewById(R.id.btn_accept);
            Button btnDecline  = (Button)dialog.findViewById(R.id.btn_decline);
            EditText etUsername = (EditText)dialog.findViewById(R.id.etUsername);
            TextView signInDifferentUser = (TextView)dialog.findViewById(R.id.signInDifferentUser);

            etUsername.setEnabled(false);
            etUsername.setVisibility(View.GONE);
            final EditText etPassword = (EditText)dialog.findViewById(R.id.etPassword);

            InputMethodManager imm = (InputMethodManager)activity .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);

            etUsername.setText(Utility.email_id);

            signInDifferentUser.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {


                    openExitDialog(activity);
                }
            });

            btnAccept.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        try {
                            InputMethodManager imm = (InputMethodManager)activity .getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(etPassword.getText().toString().trim().toCharArray().length == 0){
                            Utility.showMessage("Enter login password");
                        }
                        else{
                            //startDialog(activity);
                            String login_password = encryptionManager.encryptPayload(etPassword.getText().toString());
                            login_password = URLEncoder.encode(login_password, "UTF-8");
                            if(login_password.contains("%")){
                                login_password = login_password.replace("%","2");
                            }
                            if(login_password.equals(Utility.getPassword())){
                                //stopDialog();
                                Utility.allowAuthenticationDialog = false;
                                isAuthenticationWindowOpened = false;
                                dialog.dismiss();
                                dialog=null;
                                activity.finish();
                            }else{
                                stopDialog();
                                Utility.showMessage("Incorrect Password");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            btnDecline.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    dialog=null;
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity .startActivity(intent);
                    activity.finish();
                }
            });

        }catch(Exception e){
            e.printStackTrace();
        }
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	/**
	 * Open dialog after the app is resumed after stop.
	 * @param
	 * 		- activity
	 */
	public static void taskPromtOnResume(Activity  activity){
		if(Utility.allowAuthenticationDialog){
			Utility.showLoginPromtScreen(activity);
		}
	}

	/**
	 * Set Prompt dialog true i.e. allowed to be opened on resume.
	 * @param isAllowed
	 * @param activity
	 */
	public static void taskPromtOnStop(boolean isAllowed, Activity activity){

		try {
			allowAuthenticationDialog = true;
			/*if(allowAuthenticationDialog){
				View v = activity.getWindow().getDecorView();
				v.setDrawingCacheEnabled(true);
				v.buildDrawingCache();
				catchedBitmap = Bitmap.createBitmap(v.getDrawingCache());
			}*/
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set activity blurred in the background when lock dialog is opened.
	 */

	public static void showLock(final Activity activity){

		try {
			if(Utility.allowAuthenticationDialog){
				Intent intent = new Intent(activity, BlurredActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				activity.startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void clearAppSingletonData() {

		AppSinglton.userId = "";
		AppSinglton.thatsItPincode = "";
	}

	/**
	 *  Clear all shared preference data of the app
	 */

	public static void clearAllSharedPreferences() {

		settings = PreferenceManager.getDefaultSharedPreferences(ThatItApplication.getApplication());
		settings.edit().clear().commit();

		SharedPreferences clearVibrateData = ThatItApplication.getApplication().getSharedPreferences("mSharedPreferencesVibrateValue",Context.MODE_PRIVATE);
		clearVibrateData.edit().clear().commit();

		SharedPreferences clearUserID = ThatItApplication.getApplication().getSharedPreferences("USERID",Context.MODE_PRIVATE);
		clearUserID.edit().clear().commit();

		SharedPreferences THATSITID = ThatItApplication.getApplication().getSharedPreferences("THATSITID",Context.MODE_PRIVATE);
		THATSITID.edit().clear().commit();

		SharedPreferences clearAvlData = ThatItApplication.getApplication().getSharedPreferences("mSharedPreferencesAvlValue",Context.MODE_PRIVATE);
		clearAvlData.edit().clear().commit();

		SharedPreferences clearSetNotificationTone = ThatItApplication.getApplication().getSharedPreferences("AudioPreference",Context.MODE_PRIVATE);
		clearSetNotificationTone.edit().clear().commit();

		SharedPreferences clearRegisterData = ThatItApplication.getApplication().getSharedPreferences("register_data",Context.MODE_PRIVATE);
		clearRegisterData.edit().clear().commit();
	}

	/**
	 *  Destroy database tables and clear all data
	 */

	private static void clearDatabase() {
		dbOpenHelper = new DbOpenHelper(ThatItApplication.getApplication());
		boolean isDatabaseDeleted = dbOpenHelper.deleteDatabase();
		System.out.println("Database deletion status :" + isDatabaseDeleted);
	}


	/**
	 * Dialog prompt befor exiting the application.
	 * @param activity
	 */
	private static void openExitDialog(final Activity activity) {
		try {
			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
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
			dialog.setCancelable(false);
			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					try {
						dialog.dismiss();
						if (!NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())) {

							performSignOutTask(activity, "InternetUnavailable");

						} else {
							progressDialog = new ProgressDialog(activity);
							progressDialog.setMessage("Please Wait");
							progressDialog.setCancelable(false);
							progressDialog.show();
							UserLoginStatus(activity, Utility.getEmail(), "False", "", "", mValidateUserLoginInterface);
						}


					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						dialog.dismiss();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void startDialog(final Activity context){

		try {
			context.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (progressDialog != null && progressDialog.isShowing()) {
						stopDialog();
					}
				}
			});

			if(progressDialog!=null)
				progressDialog=null;

			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("Please Wait");
			progressDialog.setCancelable(false);

			try {
				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						//	if(!context.isFinishing() &&  !context.isDestroyed() )
						progressDialog.show();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void startDialog(Activity context, String message){

		try {
			if(progressDialog !=null && progressDialog.isShowing()){
				return;
			}

			if(progressDialog!=null)
				progressDialog=null;

			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage(message);
			progressDialog.setCancelable(false);
			context.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					progressDialog.show();
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void stopDialog(Activity activity){
		try {
			if(progressDialog!=null && progressDialog.isShowing()){
				activity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						try {
							progressDialog.dismiss();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}

			if(progressDialog!=null){
				progressDialog=null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void stopDialog(){
		try {
			if(progressDialog!=null && progressDialog.isShowing()){
				progressDialog.dismiss();
			}

			if(progressDialog!=null){
				progressDialog=null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Load details of the subscribed friend using Vcard.  
	 * @param friend_jid
	 */
	public static void onFriendAdded(String friend_jid){

		//ProviderManager.getInstance().addIQProvider("vCard", "vcard-temp:x:update", new VCardProvider());
		try {
			card.load(XmppManager.getInstance().getXMPPConnection(), friend_jid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		(new One2OneChatDb(ThatItApplication.getApplication())).saveRoster(friend_jid, card.getFirstName(), card.getLastName(), card.getField("profile_picture_url"));
	}

	public static  void removeFriendIfExists(String friend_jid){
		if(friend_jid.contains("@")){
			friend_jid = friend_jid.substring(0, friend_jid.indexOf("@"));
		}

		new One2OneChatDb(ThatItApplication.getApplication()).removeFriendDataIfExists(friend_jid);

	}

	public static boolean isFriendExists(String sender) {
		return new One2OneChatDb(ThatItApplication.getApplication()).isFriendExists(sender);
	}


	/**
	 * CONNECTION ERROR WHILE USING APPLICATION
	 */

	/*public static void showConnectionErrorAlert(final String action,final Activity activity){
		try {
			if(dialogConnectionErrorAlert!=null && dialogConnectionErrorAlert.isShowing()){
				dialogConnectionErrorAlert.dismiss();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		dialogConnectionErrorAlert=null;

		if(dialogConnectionErrorAlert==null ){

			dialogConnectionErrorAlert = new Dialog(activity);

			dialogConnectionErrorAlert.setContentView(R.layout.connection_error_alert_dialog);
			dialogConnectionErrorAlert.setTitle("That's It");

			Utility.isShowing = true;

			try{
				Button btnAccept  = (Button)dialogConnectionErrorAlert.findViewById(R.id.btn_accept_error_dialog);
				Button btnDecline  = (Button)dialogConnectionErrorAlert.findViewById(R.id.btn_decline_error_dialog);

				btnAccept.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {

						if(NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())){
							dialogConnectionErrorAlert.dismiss();

							if(action.equalsIgnoreCase("WelcomeActivityError")){

								if(!MainService.mService.connection.isConnected() || !MainService.mService.connection.isAuthenticated()){
									try {
										Utility.startDialog(activity);
										MainService.mService.connectAsync();
									}
									catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
							else if(action.equalsIgnoreCase("ContactActivityError")){

								if(!MainService.mService.connection.isConnected() || !MainService.mService.connection.isAuthenticated()){
									try {
										MainService.mService.connection.connect();
									}
									catch(XMPPException e){
										e.printStackTrace();
									}
									catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						}else{
							Toast.makeText(activity,"No Network Available",Toast.LENGTH_SHORT).show();
						}
					}
				});

				btnDecline.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						try {
							dialogConnectionErrorAlert.dismiss();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}catch(Exception e){
				e.printStackTrace();
			}
			dialogConnectionErrorAlert.setCancelable(false);
			dialogConnectionErrorAlert.setCanceledOnTouchOutside(false);
		}
		dialogConnectionErrorAlert.show();
	}*/

	public static void saveThatsItPincode(Activity activity,String ThatsItId){
		prefSaveThatsItId = activity.getSharedPreferences("THATSITID", 0);
		prefSaveThatsItId.edit().putString("THATSITID",ThatsItId).commit();
	}

	public static void openAlertDialogIfInappFail(Context context) {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle("That's It");
		alertDialogBuilder
				.setMessage("Please change your email registered with the play store to purchase That's It ID.");

		alertDialogBuilder.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {

					}
				});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
	private static AlertDialog alertDialogSplash;
	public static void openAlertSplash(final Context context,final String string, final String Message) {

		handler.post(new Runnable() {

			@Override
			public void run() {
				try {
					if (alertDialogSplash == null) {
						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
						alertDialogBuilder.setTitle("That's It");
						alertDialogBuilder.setMessage(Message);

						alertDialogBuilder.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0, int arg1) {

										alertDialogSplash.dismiss();
										alertDialogSplash = null;

										performSignOutTask(context,"");
									}
								});

						alertDialogSplash = alertDialogBuilder.create();
						alertDialogSplash.show();
						alertDialogSplash.setCancelable(false);
					}
				} catch (Exception e) {
					alertDialogSplash = null;
					e.printStackTrace();
				}
			}
		});
	}







	public static AlertDialog alertDialog;
	public static void openAlert(final Context context,final String string, final String Message) {

		handler.post(new Runnable() {

			@Override
			public void run() {
				try {
					if (alertDialog == null) {
						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
						alertDialogBuilder.setTitle("That's It");
						alertDialogBuilder.setMessage(Message);

						alertDialogBuilder.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0, int arg1) {

										alertDialog.dismiss();
										alertDialog = null;

										// Clear Gcm Shared Preference if exists
										if(string.equalsIgnoreCase("AccountDisabled")
												|| string.equalsIgnoreCase("AccountPaused")){

											SharedPreferences mSharedPreferences_Gcm = ThatItApplication.getApplication().getSharedPreferences("GcmPreference",Context.MODE_PRIVATE);
											mSharedPreferences_Gcm.edit().clear().commit();
										}

										if (string.equalsIgnoreCase("RenewalError")
												|| string.equalsIgnoreCase("AccountDisabled")
												|| string.equalsIgnoreCase("AccountPaused")) {

											if(!NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())){

												performSignOutTask(context,"InternetUnavailable");

											}else{
												progressDialog = new ProgressDialog(context);
												progressDialog.setMessage("Please Wait");
												progressDialog.setCancelable(false);
												progressDialog.show();

												if(string.equalsIgnoreCase("AccountDisabled")){
												// Remove user from XMPP Server
													try {
														if(MainService.mService.connection.isConnected() && MainService.mService.connection.isAuthenticated()){
															AccountManager accountManager = MainService.mService.connection.getAccountManager();
															accountManager.deleteAccount();
															performSignOutTask(context,"InternetAvailable");
														}
													} catch (XMPPException e) {
														e.printStackTrace();
													}
													//performSignOutTask(context,"InternetAvailable");
												}
												else{
													UserLoginStatus(context, Utility.getEmail(), "False","","", mValidateUserLoginInterface);
												}
											}
										}
										else if (string.equalsIgnoreCase("InternetUnstable")) {
											//settings = PreferenceManager.getDefaultSharedPreferences(ThatItApplication.getApplication());
											//settings.edit().clear().commit();
										}
									}
								});

						alertDialog = alertDialogBuilder.create();
						alertDialog.show();
						alertDialog.setCancelable(false);
					}
				} catch (Exception e) {
					alertDialog = null;
					e.printStackTrace();
				}
			}
		});
	}

	private static void performSignOutTask(Context context,String value) {

		if(value.equalsIgnoreCase("InternetAvailable")){
			LogFile.deleteLog(Utility.getEmail());
		}
		clearAppSingletonData();
		clearAllSharedPreferences();
		clearDatabase();
		Utility.isAppStarted = false;
		Utility.disconnected = true;
		if(MainService.mService != null) {
			MainService.mService.connection.disconnect();
			MainService.mService.disconnect();
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
			context.stopService(new Intent(context, MainService.class));
		}
		Intent startMain = new Intent(context, WelcomeActivity.class);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		context.startActivity(startMain);
		android.os.Process.killProcess(android.os.Process.myPid());

	}

	public static boolean LoginStarted = false;
	/**
	 * Timer to update user presence
	 */
	public static void startLoginTimer(final Context context,final int value) {

		try {
			Log.d("LOGIN_TIMER_STARTED", "LOGIN_TIMER_STARTED");
			if(mTimer != null){
				mTimer.cancel();
			}
			mTimer= new Timer();
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					Log.e("","TIMER STOPPED");
					if(LoginStarted){
						Utility.stopDialog();
						mTimer.cancel();
						mTimer = null;

						if(value == 1){
							//settings = PreferenceManager.getDefaultSharedPreferences(ThatItApplication.getApplication());
							//settings.edit().clear().commit();
						}
						openAlert(context,"InternetUnstable","Your internet connection seems to be unstable");
					}
				}
			},50000,50000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setDeviceTypeAndSecureFlag(Activity activity){
		activity.getWindow().setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE);
		checkTabletSize(activity);
	}

	private static void checkTabletSize(Activity activity) {
		boolean tabletSize = activity.getResources().getBoolean(R.bool.isTablet);
		if (!tabletSize) {
			// is smartphone
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

	/**
	 * Send User Login Status
	 * @param context
	 * @param email
	 * @param status
	 */
	public static void UserLoginStatus(final Context context,final String email,
									   final String status,
									   final String registrationId,
									   final String statusID,
									   final ValidateUserLoginInterface mValidateUserLoginInterface) {
		new ValidateUserLoginStatusAsync(context,email,status,registrationId,statusID,mValidateUserLoginInterface).execute();
	}

	/**
	 * Send Login status to server
	 */

	private static final ValidateUserLoginInterface mValidateUserLoginInterface = new ValidateUserLoginInterface() {
		@Override
		public void validateUserLogin(Context context,ValidateUserLoginStatus mValidateUserLoginStatus) {

			if (mValidateUserLoginStatus != null) {
				performSignOutTask(context,"InternetAvailable");
			}
		}
	};


	public static void UserPauseStatus(final Context context) {

		if(NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())
				&& Utility.googleServicesUnavailable) {
			new UserPauseStateAsync(context, Utility.getEmail(), Utility.getPassword(),
					mValidateUserPauseStateInterface).execute();
		}
	}

	/**
	 *  Validate User Pause State
	 */

	private static final ValidateUserPauseStateInterface mValidateUserPauseStateInterface = new ValidateUserPauseStateInterface() {
		@Override
		public void validateUserPauseState(Context context,AuthenticateUserServiceTemplate mAuthenticateUserServiceTemplate) {

			Utility.stopDialog();
			if(mAuthenticateUserServiceTemplate != null){
				String userId = mAuthenticateUserServiceTemplate.getmAuthenticateUserServiceResult().getmAuthenticateUserServiceParams()[0].getRetVal();
				if (userId.equals("2")) {
					Utility.openAlert(context,"AccountDisabled", "Your account has been suspended by Admin. Kindly Sign in with different account.");
				}
			}
		}
	};

	public static void lockScreenRotation(Activity activity,int SCREEN_ORIENTATION) {

			if (activity.getResources().getBoolean(R.bool.isTablet)) {
				if (SCREEN_ORIENTATION == 0) {
					// PORTRAIT
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				} /*else if (SCREEN_ORIENTATION == 1) {
					// REVERSE PORTRAIT
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
				}*/ else if (SCREEN_ORIENTATION == 2) {
					// LANDSCAPE
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}/* else if (SCREEN_ORIENTATION == 3) {
					// REVERSE LANDSCAPE
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
				}*/
			}
	}

	public static void unlockScreenRotation(Activity activity) {
		if (activity.getResources().getBoolean(R.bool.isTablet)) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
		}
	}
}
