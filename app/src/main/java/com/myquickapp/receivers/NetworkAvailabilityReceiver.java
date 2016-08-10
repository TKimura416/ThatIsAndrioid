package com.myquickapp.receivers;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkAvailabilityReceiver {

	/**
	 * Check if internet connection is available
	 */
	public static boolean isInternetAvailable(Context context) {
		boolean haveConnectedWifi = false;
		boolean haveConnectedMobile = false;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();

		for (NetworkInfo ni : netInfo) {
			try {
				if(ni.isConnected()){
					haveConnectedWifi = true;
				}
			} catch (Exception ignored) {
			}
		}
		return haveConnectedWifi || haveConnectedMobile;
	}
}
