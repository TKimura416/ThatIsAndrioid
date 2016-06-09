package com.myquick.socket;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONObject;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.widget.Toast;
import com.thatsit.android.R;
import com.thatsit.android.Utils;
import com.thatsit.android.fragement.FragmentChatScreen;

public class ServerThread implements Runnable {

	private ServerSocket serverSocket2;
	private final Activity act;
	private String message  = "";
	private String sender = "";
	private String url = "";
	private FileOutputStream output_file = null;
	private long code_received = 0;
	private long code_total = 0;
	private Builder mBuilder;
	private NotificationManager mNotifyManager;
	private ClientThread send_file;
	private Socket client;
	private BufferedOutputStream bos;
	private String fName;
	public static int currentInt = -1;
	public static int oldInt = -1;
	int currentUniqueIdentifier = -1;
	
	public ServerThread(Activity act){
		this.act =act;
	}
	
	@Override
	public void run() {
        try {
            Looper.prepare();
            String SERVERIP = getLocalIpv4Address();
			if (SERVERIP != null) {
                serverSocket2 = new ServerSocket(8088);
                
                while (true) {
                	
                		try {
                		byte[] receiveData = new byte[4096];
                        
                		Constants.isDownloading = false;
                		client = serverSocket2.accept();
                		Constants.isDownloading = true;
                		
                		InputStream is = client.getInputStream();
        				
                		int bytesRead;
                		boolean check = true;
                		
                		while(true)
         			    {
         				    bytesRead = is.read(receiveData, 0, receiveData.length);
         				    if(bytesRead == -1)
         				    {
								break;
         				    }
         				    
         				   if(output_file != null){
								try {
									if(check){
									check = false;
									code_received = code_received+receiveData.length;
									}
									int div = (int)(((double)code_received/(double)code_total) * 100);
									mBuilder.setProgress(100,div, false);
									mNotifyManager.notify(0, mBuilder.build());
									
									bos.write(receiveData, 0,bytesRead);
									bos.flush();
									
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
         			    }
                		
						sender = client.getInetAddress().getHostAddress();
						message = null;
						message = new String(receiveData);
						
						Log.d("java","response:"+message);
						
						if(message.startsWith(Constants.SENDING_CODE)){
							Handler h = new Handler(act.getMainLooper());
							h.post(new Runnable() {
							    @Override
							    public void run() {
							     String id = message.substring(message.indexOf("$"),message.length());
							     String ip =client.getInetAddress().getHostAddress();
							    }
							});
						}
						else if(message.startsWith(Constants.ERROR)){
							act.runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									Toast.makeText(act,"Download Error.",Toast.LENGTH_SHORT).show();
								}
							});
						}
						else if(message.startsWith(Constants.ACCEPT)){
							
							if(Constants.File_URI !=  null){
								ArrayList<String> ips = new ArrayList<>();
								ips.add( client.getInetAddress().getHostAddress());
								
								String filePath = message.substring(message.indexOf("$")+1,message.lastIndexOf("#"));
								
								send_file = new ClientThread(ips,act,filePath);
								send_file.start();
								com.myquick.socket.Constants.isDownloading = true;
							}
							else{
								ArrayList<String> ips = new ArrayList<>();
								ips.add( client.getInetAddress().getHostAddress());
								
								ClientChatThread thread = new ClientChatThread(client.getInetAddress().getHostAddress(), act,Constants.ERROR);
								new Thread(thread).start();
							}
						}
						else if(message.startsWith(Constants.REJECT)){
						}
						else if(message.startsWith("end")){
							
							Log.d("","file= code_received "+code_received);
							code_received = 0;
							output_file.close();
							output_file = null;
							mNotifyManager.cancel(0);
							code_total = 0;
							com.myquick.socket.Constants.isDownloading = false;
							FragmentChatScreen.update();
							act.runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									Toast.makeText(act,"File saved successfully.",Toast.LENGTH_SHORT).show();
									FragmentChatScreen.adapter.notifyDataSetChanged();
								}
							});
						}
						else{
							if(message.startsWith("FNAME")){
								Constants.isDownloading=true;
								String file_name = message.replace("FNAME","");
								file_name = file_name.substring(0,file_name.lastIndexOf("$"));
								JSONObject jobj = new JSONObject(file_name);
								 fName = jobj.getString("file_name");
								
								code_total = jobj.getLong("file_size");
								url = Utils.creatNewFile(fName);
								output_file = new FileOutputStream(new File(url));
								bos = new BufferedOutputStream(output_file);
								mNotifyManager = (NotificationManager) act.getSystemService(Context.NOTIFICATION_SERVICE);
								mBuilder = new NotificationCompat.Builder(act);
								mBuilder.setContentTitle("Thats it").setContentText("Download in progress").setSmallIcon(R.drawable.download_animation);
								mBuilder.setProgress(100, 0, false);
								mNotifyManager.notify(0, mBuilder.build());
							}
						}
						} catch (Exception e) {
						Log.d("ServerActivity", "Receiving"+e.toString());
	                    e.printStackTrace();
					}
                }
                     
            } else {
            	Toast.makeText(act,"Failed to connect",Toast.LENGTH_SHORT).show();
               }
			
		   } catch (final Exception e) {
        	 Log.d("ServerActivity", "chat 5");
             e.printStackTrace();
        }
	}
	
	private String getLocalIpv4Address(){
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
								if (!address.isLoopbackAddress()	&& InetAddressUtils.isIPv4Address(ipv4 = address.getHostAddress())) {
									return ipv4;
								}
							}
						}

					}
				}
	        }

	    } catch (SocketException ignored) {
	     }
	    return "no wifi connection.";
	}
}
