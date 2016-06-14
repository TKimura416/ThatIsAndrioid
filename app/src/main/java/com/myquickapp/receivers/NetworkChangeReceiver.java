package com.myquickapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.thatsit.android.MainService;
import com.thatsit.android.Utility;
import com.thatsit.android.activities.BlurredActivity;
import com.thatsit.android.application.ThatItApplication;

public class NetworkChangeReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {

        if (NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())
                && MainService.mService != null) {
            try {
                MainService.mService.setTimer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (!NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())
                && MainService.mService != null){
            try {
                MainService.mService.stopTimer();
                // Display black screen on no network
                Utility.allowAuthenticationDialog = true;
                Utility.noNetwork = true;
                Intent mIntent = new Intent(context.getApplicationContext(), BlurredActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(mIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
