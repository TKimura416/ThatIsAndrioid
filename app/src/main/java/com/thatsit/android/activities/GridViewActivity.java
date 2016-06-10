package com.thatsit.android.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.thatsit.android.R;
import com.thatsit.android.Utility;
import com.thatsit.android.adapter.ImageAdapter;
import com.thatsit.android.fragement.FragmentChatScreen;

public class GridViewActivity extends Activity{
	private GridView gridview;
	/**
	 * isPromtAllowed -> Alert dialog to enter chat/login password. 
	 */
	public static boolean isPromtAllowed=true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.emoticons_grid);

		Utility.setDeviceTypeAndSecureFlag(GridViewActivity.this);
		Utility.allowAuthenticationDialog=false;
		
		LayoutParams lp = this.getWindow().getAttributes();
		lp.gravity = Gravity.BOTTOM;
		lp.dimAmount = 0;
		lp.flags = LayoutParams.FLAG_LAYOUT_NO_LIMITS ;

		LayoutInflater inflater = getLayoutInflater();
		RelativeLayout relativeLayout = (RelativeLayout) inflater.inflate(R.layout.emoticons_grid, null);
		setContentView(relativeLayout, lp);
		initialiseVariable();
		initialiseListener();
		gridview.setAdapter(new ImageAdapter(this));     
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Utility.allowAuthenticationDialog=false;
		Utility.smileyScreenOpened = false;
		finish();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Utility.allowAuthenticationDialog=false;
		Utility.smileyScreenOpened = false;
		finish();
		
	}

	/**
	 * Initialise class variables.
	 */
	private void initialiseVariable() {
		gridview = (GridView) findViewById(R.id.emoticons_grid); 
	}

	/**
	 * Initialise click listeners.
	 */
	private void initialiseListener() {
		gridview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int position,long arg3) {

				try {
					if(MUCActivity.chek_Activity){
						MUCActivity.edtChat.append(processSmileyCodes(ImageAdapter.mThumbIdsForDispatch[position]));
					}
					else{
						FragmentChatScreen.mEdtTxtChat.append(processSmileyCodes(ImageAdapter.mThumbIdsForDispatch[position]));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});	
	}

	/**
	 *
	 * @param message - include emoticons with message
	 * @return - Returns message with emoticons
	 */
	private String processSmileyCodes(String message){

		try{
			for(int index=0;index<ImageAdapter.mThumbIdsForDispatch.length;index++){
				message = message.replaceAll(ImageAdapter.mThumbIdsForDispatch[index], ImageAdapter.mThumbIds[index]);
			}
		}catch(Exception e){
			return message;
		}
		return message;
	}
	
}