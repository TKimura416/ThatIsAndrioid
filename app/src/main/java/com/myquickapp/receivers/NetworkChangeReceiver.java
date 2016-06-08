package com.myquickapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.thatsit.android.MainService;
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
            // Toast.makeText(context, "Network Available", Toast.LENGTH_SHORT).show();
        }
        else if (!NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication())
                 && MainService.mService != null){
             try {
                 MainService.mService.stopTimer();
             } catch (Exception e) {
                 e.printStackTrace();
             }
              //Toast.makeText(context, "Network Not Available",Toast.LENGTH_SHORT).show();
        }
    }
}
