package com.seasia.myquick.asyncTasks;

import java.util.HashMap;
import java.util.Map;
//import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
//import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.iqregister.packet.Registration;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.thatsit.android.Utility;
import com.thatsit.android.activities.PaymentConfirmationActivity;
import com.thatsit.android.application.ThatItApplication;

public class RegisterUserOnChatServerAsyncTask extends AsyncTask<Void, Void, Void>{

	private XMPPConnection mConnection;
	private String chatUserName,chatPassword;
	private Activity activity;
	private boolean userAlreadyExists,registerFailed;
	private SharedPreferences settings;

	public RegisterUserOnChatServerAsyncTask(Activity activity,XMPPConnection mConnection, String chatUserName, String chatPassword) {

		this.mConnection = mConnection;
		this.chatUserName = chatUserName;
		this.chatPassword = chatPassword;
		this.activity = activity;
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			mConnection.connect();
			AccountManager accountManager = mConnection.getAccountManager();
			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("username", chatUserName);
			attributes.put("password", chatPassword);
			attributes.put("name", "");

			if (accountManager.supportsAccountCreation()) {
				accountManager.createAccount(chatUserName, chatPassword,attributes);
				}
			Registration reg = new Registration();
			reg.setType(IQ.Type.SET);
			reg.setTo(mConnection.getServiceName());
			reg.addAttribute("username", chatUserName);
			reg.addAttribute("password", chatPassword);
			reg.addAttribute("email", "");
			reg.addAttribute("name", "");
			PacketFilter filter = new AndFilter(new PacketIDFilter(reg.getPacketID()), new PacketTypeFilter(IQ.class));
			PacketCollector collector = mConnection.createPacketCollector(filter);
			mConnection.sendPacket(reg);

		} catch (XMPPException e) {
			e.printStackTrace();
			registerFailed = true;
			try {
				if(e.getX MPPError().getCode() == 409){
					userAlreadyExists = true;
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (SmackException.NoResponseException e) {
			e.printStackTrace();
		} catch (SmackException.NotConnectedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);

		Utility.stopDialog();
		if(userAlreadyExists){
			
			// OPEN ALERT DIALOG FOR USER ALREADY EXISTS ON OPENFIRE
			openAlertDialog();
			clearCredentials();
			userAlreadyExists = false;
		}
		else if(registerFailed == true){
			clearCredentials();
			Utility.showMessage("Registration Failed");
		}
		else{
			Intent ints = new Intent(activity,PaymentConfirmationActivity.class);
			activity.startActivity(ints);
			activity.finish();
		}
	}

	private void clearCredentials() {
		settings = PreferenceManager.getDefaultSharedPreferences(ThatItApplication.getApplication());
		settings.edit().clear().commit();

		SharedPreferences clearUserID = ThatItApplication.getApplication().getSharedPreferences("USERID", Context.MODE_PRIVATE);
		clearUserID.edit().clear().commit();
	}

	private void openAlertDialog() {
		
		AlertDialog.Builder builder = new Builder(activity);
		builder.setTitle("That's It");
		builder.setMessage("User with same pincode already exists.");

		builder.setPositiveButton("OK", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Utility.unlockScreenRotation(activity);
			}
		});
		builder.setCancelable(false);
		builder.show();
	}
}
