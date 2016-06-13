package com.thatsit.android;

/**
 *  This class includes receiving file via SFTP.
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.db.One2OneChatDb;
import com.thatsit.android.encryption.helper.EncryptionManager;
import com.thatsit.android.fragement.FragmentChatScreen;
import com.thatsit.android.xmpputils.Constants;

public class FileReceiveraAsync_  {
	private ThatItApplication myApplication;
	private Context parentContext;
	private NotificationManager mNotifyManager ;
	private NotificationCompat.Builder	mBuilder;
	public static int notificaion_id = 11;
	private String FILE_PATH=""; // initialized in onCreated
	private String incomingFileName;
	private Handler handler = new Handler();
	private EncryptionManager encryptionManager;
	private Session session = null;
	private Channel channel = null;
	private ChannelSftp channelSftp = null;
	private long count = 0;

	/**
	 * Download file from server
	 * @param fileName - downloading file
	 * @param context
	 */
	public FileReceiveraAsync_(String fileName,Context context) {

		notificaion_id = (int) System.currentTimeMillis();
		notificaion_id = (notificaion_id < 0) ? -notificaion_id : notificaion_id;
		parentContext=context;
		encryptionManager = new EncryptionManager();

		FILE_PATH=Environment.getExternalStorageDirectory().getAbsolutePath() +"/Thats It";

		File wallpaperDirectory = new File(FILE_PATH);
		wallpaperDirectory.mkdirs();

		myApplication = ThatItApplication.getApplication();

		mNotifyManager = (NotificationManager) myApplication.getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(context);
		mBuilder.setContentTitle("File Download " +fileName)
				.setContentText("Establishing Connection")
				.setSmallIcon(R.drawable.download_animation);
		incomingFileName = fileName;

		PreExecute(notificaion_id);
	}

	/**
	 *  Building progress in notification.
	 */
	protected void PreExecute(int notificaion_id){
		// configure the notification
		mNotifyManager = (NotificationManager) parentContext.getSystemService(Context.NOTIFICATION_SERVICE);

		mBuilder.setProgress(100, 0, true).setOngoing(true);
		// Displays the progress bar for the first time.
		mNotifyManager.notify(notificaion_id, mBuilder.build());
	}

	public void doInBackground(final String fileName) {

		new Thread() {
			public void run() {
				try {
					String ftpFilePath = "/ThatsItDirectory/"+fileName;
					downloadSFTpSign(ftpFilePath, notificaion_id);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}}.start();
	}

	private void stopIndeterminantProgressBar( final int notificaion_id){
		try {
			new One2OneChatDb(parentContext).updateFileDownloadstatus(incomingFileName);
			mBuilder.setContentText("Download Complete");
			handler.post(new Runnable() {

				@Override
				public void run() {
					try {
						mNotifyManager.notify(notificaion_id, mBuilder.build());
						mNotifyManager.cancel(notificaion_id);
						Toast.makeText(parentContext, "File Successfully Downloaded", Toast.LENGTH_LONG).show();
						FragmentChatScreen.adapter.notifyDataSetChanged();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 *
	 * @param ftp_path - SFTP path -> File location.
	 */
	private void downloadSFTpSign(final String ftp_path,int notificaion_id) {

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
			channelSftp.cd(path);
			InputStream bis = new BufferedInputStream(channelSftp.get(ftp_path));

			OutputStream output;
			output = new FileOutputStream(FILE_PATH + "/" + incomingFileName);

			SftpATTRS attrs = channelSftp.lstat(ftp_path);
			long size = attrs.getSize();

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = bis.read(bytes)) != -1) {
				output.write(bytes, 0, read);
				count += read;
				long percentNow = count*100/size;
				mBuilder.setContentTitle("File Download " +incomingFileName)
						.setContentText("Downloading File  "+ percentNow + " %")
						.setSmallIcon(R.drawable.download_animation);
				mBuilder.setProgress(100, (int) percentNow, false)
						.setOngoing(true);
				// Issue the notification
				mNotifyManager.notify(notificaion_id, mBuilder.build());
			}
			bis.close();
			output.close();
			channelSftp.rm(ftp_path);
			stopIndeterminantProgressBar(notificaion_id);

		} catch (Exception ex) {
			ex.printStackTrace();
			mNotifyManager.cancel(notificaion_id);
		}
		session.disconnect();
		channel.disconnect();
	}
}
