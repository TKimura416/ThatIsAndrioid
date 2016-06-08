package com.thatsit.android.beans;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.android.gms.gcm.GcmListenerService;
import com.thatsit.android.application.ThatItApplication;

public class PushNotificationService extends GcmListenerService {
    private SharedPreferences mSharedPreferences;

    public PushNotificationService() {
        try {
            mSharedPreferences = ThatItApplication.getApplication().getSharedPreferences("GcmPreference",0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
        String message = data.getString("message");
        //Save message in shared preference
        mSharedPreferences.edit().putString("GcmPreference",message).commit();

        if(message.equalsIgnoreCase("Account Disabled")){
            sendBroadcast(message);
        }
        else if(message.equalsIgnoreCase("Account Paused")){
            sendBroadcast(message);
        }
    }

    private void sendBroadcast(String message) {
        Intent intent = new Intent();
        intent.setAction(message);
        ThatItApplication.getApplication().sendBroadcast(intent);
    }
}
