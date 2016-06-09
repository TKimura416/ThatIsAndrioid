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

        //
        // [2016/06/04 05:12 KSH] Received xmpp 2 gcm message via push message.
        // The app must resume xmpp connection now.
        //
        if (message.equals("Offline Xmpp Signal")) {
            sendBroadcast(message);
        }

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
