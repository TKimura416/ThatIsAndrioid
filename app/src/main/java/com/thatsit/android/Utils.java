package com.thatsit.android;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import org.apache.http.conn.util.InetAddressUtils;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import com.thatsit.android.application.ThatItApplication;
import com.myquick.socket.ClientChatThread;
import com.myquick.socket.Constants;
import com.myquickapp.receivers.NetworkAvailabilityReceiver;
import com.seasia.myquick.controller.WebServiceClient;
import com.seasia.myquick.model.AppSinglton;
import com.seasia.myquick.model.AuthenticateUserServiceTemplate;

public class Utils{

	/**
	 *  File transferred via socket.
	 * @param is - Input Stream
	 * @param os - Output Stream
	 */
	public static void CopyStream(InputStream is, OutputStream os)
	{
		final int buffer_size=1024;
		try
		{
			byte[] bytes=new byte[buffer_size];
			for(;;)
			{
				int count=is.read(bytes, 0, buffer_size);
				if(count==-1)
					break;
				os.write(bytes, 0, count);
			}
		}
		catch(Exception ex){}
	}

	/**
	 * 
	 * @return IP Address to whom file is to be sent
	 */
	public static String getLocalIpv4Address(){
		try {
			String ipv4;
			List<NetworkInterface>  nilist = Collections.list(NetworkInterface.getNetworkInterfaces());
			if(nilist.size() > 0){                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
				for (NetworkInterface ni : nilist) {
					if (ni.getName().contains("wlan")) {

						List<InetAddress> ialist = Collections.list(ni
								.getInetAddresses());
						if (ialist.size() > 0) {
							for (InetAddress address : ialist) {
								if (!address.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ipv4 = address.getHostAddress())) {
									return ipv4;
								}
							}
						}

					}
				}
			}

		} catch (SocketException ex) {
		}
		return "no wifi connection.";
	}


	/**
	 * Create directory and store file
	 * @param file_name
	 * @return
	 */
	public static String creatNewFile(String file_name)
	{
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Thats It";
		File mfile = new File(path);
		if (!mfile.exists())
			mfile.mkdirs();

		File mWorkingFile = new File(mfile, file_name);
		if (!mWorkingFile.exists())
		{
			try
			{
				mWorkingFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		path = mWorkingFile.getPath();
		return mWorkingFile.getPath();
	}

	public static void resetSocketParams() {
		Constants.isDownloading=false;
		ClientChatThread.connected=false;
	}

	public static boolean isUserExists() {
		if(Utility.getUserName().length()>0 && Utility.getPassword().length()>0)
			return true;
		else return false;
	}

	public static boolean isLoginTaskRunning = false;
	public static void loginUser(){

		if(Utility.getUserName().length()>0 && isLoginTaskRunning==false){
			isLoginTaskRunning = true;

			if(NetworkAvailabilityReceiver.isInternetAvailable(ThatItApplication.getApplication()))
			{
				new Utils().new GetDataAsync().execute();
			}
		}
	}

	public class GetDataAsync extends AsyncTask<Void, Void, AuthenticateUserServiceTemplate> {

		@Override
		protected AuthenticateUserServiceTemplate doInBackground(Void... arg0) {

			try {
				AuthenticateUserServiceTemplate obj = new WebServiceClient(
						ThatItApplication.getApplication()).getUserSignIn(Utility.getEmail(),Utility.getPassword());
				return obj;
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(AuthenticateUserServiceTemplate result) {
			try {
				super.onPostExecute(result);
				AppSinglton.thatsItPincode = result.getmAuthenticateUserServiceResult().getmAuthenticateUserServiceParams()[0].getPinCode();
			} catch (Exception e) {
				Utility.stopDialog();
				e.printStackTrace();
			}

			try {
				if(Utility.serviceBinded == false){
					connectXMPPService();
				}
			} catch (Exception e) {
				Utility.stopDialog();
				e.printStackTrace();
			}
		}
	}

	public synchronized void connectXMPPService() {
		Intent SERVICE_INTENT = new Intent();
		Log.e("Service_Utils", "Service_Utils");
		SERVICE_INTENT.setComponent(new ComponentName(
				com.thatsit.android.xmpputils.Constants.MAINSERVICE_PACKAGE, com.thatsit.android.xmpputils.Constants.MAINSERVICE_PACKAGE
				+ com.thatsit.android.xmpputils.Constants.MAINSERVICE_NAME));
		try {
			ThatItApplication.getApplication().bindService(SERVICE_INTENT, new  MyServiceConnection(/*onLogin*/),
					Context.BIND_AUTO_CREATE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class MyServiceConnection implements ServiceConnection{

		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {

			MainService.mService = ((MainService.MyBinder) binder).getService();

			try {
				MainService.mService.connectAsync();
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				new Thread() {
					public void run() {
						try {
							sleep(2000);
							try {
								isLoginTaskRunning = false;
							} catch (Exception e) {
								e.printStackTrace();
							}
						} catch (Exception e) {
							Log.e("tag", e.getMessage());
						}
					}
				}.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
		}
	} 

	public static boolean isAppNotOpenedYet() {
		return Utility.fragmentContact==null;
	};

}