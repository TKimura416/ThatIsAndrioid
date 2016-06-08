package com.thatsit.android.activities;

import java.io.File;

import org.jivesoftware.smack.XMPPConnection;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.thatsit.android.MainService;
import com.thatsit.android.Utility;
import com.thatsit.android.application.ThatItApplication;
import com.thatsit.android.xmpputils.Constants;
import com.thatsit.android.xmpputils.XmppManager;

public class SelectAudioActivity extends ListActivity {

	private SimpleCursorAdapter adapter;
	private SharedPreferences AudioPreference;
	private MainService mService;
	private XmppManager mXmppManager;
	private XMPPConnection mConnection;
	private ThatItApplication myApplication;
	private boolean mBinded;
	private static final Intent SERVICE_INTENT = new Intent();
	public static boolean isPromtAllowed=true;
	static {
		SERVICE_INTENT.setComponent(new ComponentName(Constants.MAINSERVICE_PACKAGE,  Constants.MAINSERVICE_PACKAGE + Constants.MAINSERVICE_NAME ));
	}
	public SelectAudioActivity(MainService mainService){
		mService = mainService;
	}
	public SelectAudioActivity(){
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Utility.setDeviceTypeAndSecureFlag(SelectAudioActivity.this);
		Utility.allowAuthenticationDialog=false;
		myApplication = ThatItApplication.getApplication();
		mXmppManager = XmppManager.getInstance();
		mConnection = mXmppManager.getXMPPConnection();
		AudioPreference = getSharedPreferences("AudioPreference", MODE_WORLD_WRITEABLE);
		String[] from = {
				MediaStore.MediaColumns.TITLE};
		int[] to = {
				android.R.id.text1};

		Cursor cursor = managedQuery(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
				null, 
				null, 
				null, 
				MediaStore.Audio.Media.TITLE);

		adapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1, cursor, from, to);
		setListAdapter(adapter);
	}
	

	@Override
	protected void onResume() {
		super.onResume();
		if(Utility.allowAuthenticationDialog==true){
			Utility.showLock(SelectAudioActivity.this);
		}
		Utility.UserPauseStatus(SelectAudioActivity.this);
	}

	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Cursor cursor = adapter.getCursor();
		cursor.moveToPosition(position);

		String path = cursor.getString(1);

		File source = new File( path);
		if (source.exists()) {
			source.mkdirs();
			AudioPreference.edit().putString("AudioPreference",path).commit();
		}else{
			AudioPreference.edit().putString("AudioPreference","").commit();
		}
		mService.updateNotificationSound();
		Toast.makeText(getApplicationContext(),"Tone Saved", Toast.LENGTH_SHORT).show();
		isPromtAllowed=false;
		finish();
	}
	@Override
	public void onStart() {
		super.onStart();
		try {
			startService(SERVICE_INTENT);
			if(!mBinded){
				mBinded = bindService(SERVICE_INTENT, serviceConnection, Context.BIND_AUTO_CREATE);
			}
		}catch(Exception e){
		}
	} 

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Utility.allowAuthenticationDialog=false;
	}
	
	@Override
	public void onStop() {
		
		Utility.taskPromtOnStop(isPromtAllowed, SelectAudioActivity.this);
		
		super.onStop();
		try {
			if(mBinded){
				try {
					unbindService(serviceConnection);
					mBinded =false;
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Bind service with the activity.
	 */
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			mService = ((MainService.MyBinder) binder).getService();
			mBinded = true;
		}
		@Override
		public void onServiceDisconnected(ComponentName className) {
			mBinded = false;
		}
	};
}