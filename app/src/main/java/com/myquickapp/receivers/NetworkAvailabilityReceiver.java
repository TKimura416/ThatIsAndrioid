package com.myquickapp.receivers;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import com.thatsit.android.xmpputils.Constants;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkAvailabilityReceiver {

	private static URL url;
	private static boolean isInternetWorking = false;

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
			} catch (Exception e) {
			}
		}
		return haveConnectedWifi || haveConnectedMobile;
	}

	/**
	 *  TO CHECK IF INTERNET IS ACTUALLY WORKING AT BACKEND.............................................................
	 * @return
	 */
	public static boolean isInternetWorking() {
			try {
				url = new URL("https://www.google.co.in" );
				//url = new URL("http://190.97.163.145/" );
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						HttpURLConnection conn;
						conn = (HttpURLConnection) url.openConnection();
						conn.setReadTimeout(15000);//milliseconds
						conn.setConnectTimeout(15000);//milliseconds
						conn.setRequestMethod("GET");
						conn.setDoInput(true);

						// Start connect
						conn.connect();
						InputStream response =conn.getInputStream();
						Log.d("Response:", response.toString());
						if(response != null){
							isInternetWorking = true;
						}
					} catch (ProtocolException e) {
						e.printStackTrace();
						isInternetWorking = false;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
		
		return isInternetWorking;
	}
}
