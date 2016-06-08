
package com.thatsit.android.invites;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.thatsit.android.MainService;

/**
 * vinay Apr 10, 2014, 10:36:26 AM XMPPChat
 */

public class ScheduleReceiver extends BroadcastReceiver {

    final String TAG = "ScheduleReceiver";
    // restart service every 30 seconds
    private static final long REPEAT_TIME = 1000 * 30;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
			AlarmManager service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(context, MainService.class);
			PendingIntent pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
			Calendar cal = Calendar.getInstance();
			// start 30 seconds after boot completed
			cal.add(Calendar.SECOND, 30);
      
			service.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pending);
			Log.i(TAG, "ScheduleReceiver");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
