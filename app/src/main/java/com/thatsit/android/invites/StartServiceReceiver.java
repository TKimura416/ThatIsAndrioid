
package com.thatsit.android.invites;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.thatsit.android.MainService;

/**
 * vinay Apr 10, 2014, 10:40:05 AM XMPPChat
 */

public class StartServiceReceiver extends BroadcastReceiver{
    private static final String TAG = "StartServiceReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
			Log.e(TAG, "StartServiceReceiver");
			context.startService(new Intent(context, MainService.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
