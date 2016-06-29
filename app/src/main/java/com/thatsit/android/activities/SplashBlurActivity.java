package com.thatsit.android.activities;

import android.app.Activity;
import android.os.Bundle;

import com.thatsit.android.R;
import com.thatsit.android.Utility;

/**
 * Created by psingh5 on 6/21/2016.
 */
public class SplashBlurActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blurred);
        try{
            Utility.setDeviceTypeAndSecureFlag(SplashBlurActivity.this);
            Utility.taskOnNoInternet(SplashBlurActivity.this);
        }catch(Exception e){
            e.printStackTrace();
            finish();
        }
    }
}
