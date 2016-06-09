package com.thatsit.android.activities;

import com.thatsit.android.R;
import com.thatsit.android.Utility;

import android.app.Activity;
import android.os.Bundle;

public class BlurredActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blurred);
		try{
			Utility.setDeviceTypeAndSecureFlag(BlurredActivity.this);
			Utility.taskPromtOnResume(BlurredActivity.this);
		}catch(Exception e){
			e.printStackTrace();
			finish();
		}
	}
}
