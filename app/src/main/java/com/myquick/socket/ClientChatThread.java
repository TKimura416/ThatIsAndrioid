package com.myquick.socket;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;


import android.app.Activity;
import android.util.Log;

public class ClientChatThread implements Runnable {

	public static boolean connected;
	final String  ip;
	final Activity act;
	int buffersize = 0;
	final String msg;
	Socket socket2;

	public ClientChatThread(String ips,Activity act,String msg){
		this.ip = ips;
		this.act = act;
		this.msg = msg;
	}
	public void run() {
		try {
			try {
				try {
					
					Log.d("ClientActivity", "C:..."+msg);
					byte[] buffer = msg.getBytes();
					socket2 = new  Socket(InetAddress.getByName(ip), 8088);
					connected = true;
					OutputStream os = socket2.getOutputStream();
					os.write(buffer);
					Log.d("ClientActivity", "C:..."+msg);

				} catch (Exception e) {
					
					act.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
						//	Toast.makeText(act, "Download Error", Toast.LENGTH_SHORT).show();
						}
					});
					e.printStackTrace();
				}


			} catch (Exception e) {
				Log.e(" Catch ClientActivity", "C:..."+e.toString());
			}

			socket2.close();
		} catch (Exception e) {
			Log.e("Catch2 ClientActivity", "C:jgg..."+e.toString());
			connected = false;
		}
	}
}
