package com.myquick.socket;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.widget.Toast;

import com.thatsit.android.R;

public class ClientThread extends Thread {

	public static boolean connected;
	ArrayList<String>  ip;
	Activity act;
	public static boolean isRecording;
	byte[] buffer = null;
	private boolean isNameSent = false;
	private NotificationManager mNotifyManager;
	private Builder mBuilder;
	private String file_name;

	long sent_data = 0;
	Socket socket2;
	String filePath;


	public ClientThread(ArrayList<String> ips,Activity act, String filePath){
		this.ip = ips;
		this.act = act;
		this.filePath = filePath;
	}
	public void run() {
		try {
			try {
				OutputStream os;

				if (true) {
					try {
						for (int i = 0; i < ip.size(); i++) {
							connected = true;

							final File file = new File(Uri.parse(filePath).getPath());
							FileInputStream input = new FileInputStream(file);
							BufferedInputStream bis = new BufferedInputStream(input);

							int log = 0;
							byte[] buf = new byte[4096];
							int bytesRead;

							int count = 0;
							String end = "";

							while ((bytesRead = bis.read(buf)) > 0) {
								Constants.IS_SPEAKING_E = true;

								log = log+bytesRead;
								count++;

								if (!isNameSent ) {
									file_name = file.toString().substring(file.toString().lastIndexOf("/")+1,file.toString().length());

									JSONObject jo = new JSONObject();
									jo.put("file_name",file_name);
									jo.put("file_size",file.length());

									file_name = "FNAME" + jo.toString()+"$";

									byte[] buffer_file = file_name.getBytes();
									socket2 = new  Socket(InetAddress.getByName(ip.get(i)), 8088);
									os = socket2.getOutputStream();
									os.write(buffer_file);
									os.flush();
									socket2.close();

									isNameSent = true;

									mNotifyManager = (NotificationManager) act.getSystemService(Context.NOTIFICATION_SERVICE);
									mBuilder = new NotificationCompat.Builder(act);
									mBuilder.setContentTitle("Thats it").setContentText("sending in progress").setSmallIcon(R.drawable.notification_star);
									mBuilder.setProgress(100, 0, false);
									mNotifyManager.notify(0, mBuilder.build());
								}

								socket2 = new  Socket(InetAddress.getByName(ip.get(i)), 8088);
								os = socket2.getOutputStream();
								os.write(buf,0,bytesRead);
								os.flush();
								socket2.close();

								sent_data = sent_data+buf.length;

								int div = (int)(((double)sent_data/(double)file.length()) * 100);

								mBuilder.setProgress(100,div, false);
								mNotifyManager.notify(0, mBuilder.build());

							}
							Log.d("","file= code_received "+end);
							Log.d("","file sent ::"+log);

							String end1 = "end";
							byte[] buffer_file_end = end1.getBytes();

							socket2 = new  Socket(InetAddress.getByName(ip.get(i)), 8088);
							os = socket2.getOutputStream();
							os.write(buffer_file_end);
							os.flush();
							socket2.close();

							act.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(act,"File sent successfully.",Toast.LENGTH_LONG).show();
								}
							});
							mNotifyManager.cancel(0);
							isNameSent = false;
							input.close();
						}
					} catch (Exception e) {
						Log.d("ClientActivity 1", "C:..."+e);
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("ClientActivity 2", "C:..."+e);

			}
			socket2.close();
		} catch (Exception e) {
			Log.d("ClientActivity 3", "C:jgg..."+e);
			connected = false;
		}
	}
}
