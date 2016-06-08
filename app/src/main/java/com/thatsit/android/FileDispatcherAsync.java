package com.thatsit.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.jivesoftware.smackx.filetransfer.FileTransferManager;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.encryption.helper.EncryptionManager;
import com.thatsit.android.xmpputils.Constants;
import com.thatsit.android.R;

/**
 * This class includes sending files via SFTP.
 */
public class FileDispatcherAsync implements SftpProgressMonitor{

	private File mFile;
	private NotificationManager mNotifyManager ;
	private android.support.v4.app.NotificationCompat.Builder mBuilder;
	private int id = 12;
	private long timeMillis;
	private UpdateFTPStatus statusCallback;
	private Handler handle;
	Session session = null;
	Channel channel = null;
	ChannelSftp channelSftp = null;
	private EncryptionManager encryptionManager;
	private long max = 0;
	private long count = 0;
	private long percent = 0;
	private long fileSizeBytes;

	public FileDispatcherAsync(File fileTosend,TextView feildToUpdate,
							   ProgressBar progressBar, FileTransferManager Fmanager,
							   String recipient,Context context,
							   UpdateFTPStatus statusCallbackReference) {

		timeMillis = System.currentTimeMillis();
		id = (int) System.currentTimeMillis();
		id = (id < 0) ? -id : id;
		mFile = fileTosend;
		handle = new Handler();
		encryptionManager = new EncryptionManager();
		this.statusCallback = statusCallbackReference;

		PreExecution();
	}

	public FileDispatcherAsync(long fileSizeBytes,int id) {
		// File transfer initiated
		this.id = id;
		this.fileSizeBytes = fileSizeBytes;
	}

	public void init(int op, java.lang.String src, java.lang.String dest, long max) {
		// File transfer started
		initialiseNotificationManager("Sending File","Transfer in progress");
		this.max = fileSizeBytes;
	}

	public boolean count(long bytes){
		// File transfer in progress
		count += bytes;
		long percentNow = count*100/max;

		mBuilder.setContentTitle("Sending File")
				.setContentText("Transfer in progress  " + percentNow + "%")
				.setSmallIcon(R.drawable.notification_star);
		mBuilder.setProgress(100, (int) percentNow, false)
				.setOngoing(true);
		// Issue the notification
		mNotifyManager.notify(id, mBuilder.build());

		return(true);
	}

	public void end(){
		// File Transfer Completed
		initialiseNotificationManager("Sending File", "Transfer Complete");
		mNotifyManager.notify(id, mBuilder.build());
		mNotifyManager.cancel(id);
		Utility.showMessage("File Successfully Sent");

	}

	/**
	 *  Building progress notification.
	 */
	protected void PreExecution(){

		//mBuilder.setContentTitle("File Transfer " +mFile.getName())
		initialiseNotificationManager("Sending File","Establishing Connection");
		mBuilder.setProgress(100, 0, true).setOngoing(true);
		mNotifyManager.notify(id, mBuilder.build());
	}

	/**
	 * Upload file to server in separate thread
	 */
	public void processFileDispatch(){

		new Thread() {
			public void run() {

				connectToSFTP();
			}
		}.start();
	}

	/**
	 * Setup Notification Manager and builder
	 * @param title - Notification title
	 * @param message - Message shown in the notification
	 */
	private void initialiseNotificationManager(String title,String message){

		mNotifyManager = (NotificationManager) ThatItApplication.getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(ThatItApplication.getApplication());
		mBuilder.setContentTitle(title)
				.setContentText(message)
				.setSmallIcon(R.drawable.notification_star);
	}

	/**
	 *  Establish connection with SFTP to upload file.
	 */
	private void connectToSFTP() {
		try {
			JSch jsch = new JSch();

			String username = encryptionManager.decryptPayload(Constants.SFTP_USERNAME);
			String address = encryptionManager.decryptPayload(Constants.SFTP_SERVER_ADDRESS);
			String port = encryptionManager.decryptPayload(Constants.SFTP_PORT_NO);
			String password = encryptionManager.decryptPayload(Constants.SFTP_PASSWORD);
			String path = encryptionManager.decryptPayload(Constants.SFTP_PATH);

			session = jsch.getSession(username,address,Integer.parseInt(port));
			session.setPassword(password);

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp) channel;

			try {
				channelSftp.cd(path);
			} catch (SftpException e) {
				e.printStackTrace();
			}

			// Get toatl file size in bytes
			fileSizeBytes = mFile.length();

			// Upload file to SFTP server
			channelSftp.put(new FileInputStream(mFile), timeMillis + mFile.getName(),
					new FileDispatcherAsync(fileSizeBytes, id));

			handle.post(new Runnable() {
				@Override
				public void run() {
					statusCallback.markStatus(true, timeMillis + mFile.getName());
				}
			});

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Utility.showMessage("File Transfer Error");
			mNotifyManager.cancel(id);
			Log.e("Exception", "FileNotFoundException");
		} catch (JSchException e) {
			Log.e("Exception", "JSchException");
			Utility.showMessage("File Transfer Error");
			e.printStackTrace();
			mNotifyManager.cancel(id);
		} catch (SftpException e) {
			Log.e("Exception", "SftpException");
			e.printStackTrace();
			Utility.showMessage("File Transfer Error");
			mNotifyManager.cancel(id);
		}
		catch (Exception e) {
			e.printStackTrace();
			Utility.showMessage("File Transfer Error");
			mNotifyManager.cancel(id);
		}
		try {
			session.disconnect();
			channel.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

