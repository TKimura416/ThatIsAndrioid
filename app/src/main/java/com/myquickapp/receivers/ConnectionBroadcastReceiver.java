package com.myquickapp.receivers;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.util.Log;
import com.seasia.myquick.model.ValidateUserLoginStatus;
import com.thatsit.android.LogFile;
import com.thatsit.android.MainService;
import com.thatsit.android.Utility;
import com.thatsit.android.activities.ContactActivity;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.db.DbOpenHelper;
import com.thatsit.android.interfaces.ValidateUserLoginInterface;
import java.util.Timer;
import java.util.TimerTask;

public class ConnectionBroadcastReceiver extends BroadcastReceiver {
	final String TAG = "ConnectionBroadcastReceiver";
	private Timer mTimer;

	public ConnectionBroadcastReceiver() {
	}

	public ConnectionBroadcastReceiver(MainService mainServiceInstance) {
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {
		try {
			String intentAction = intent.getAction();

			if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())
					|| Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			}
			else if (intentAction.equals(MainService.CONNECTION_CLOSED)) {
				return;
			}
			else if (intentAction.equals(MainService.SIGNIN)) {
				Intent ints = new Intent(context, ContactActivity.class);

				ints.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TASK);

				context.startActivity(ints);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Utility.resetSocket();
	}
}
