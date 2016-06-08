package com.thatsit.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import com.thatsit.android.R;
import com.thatsit.android.Utility;

public class TermsAndConditionsActivity extends Activity implements OnClickListener{
	private Button submit,cancel;
	private String submitAccepted;
	private SharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.terms_conditions_activity);

		Utility.setDeviceTypeAndSecureFlag(TermsAndConditionsActivity.this);
		initialiseSharedPreference();
		initialseVariable();
		initialseListener();
	}

	/**
	 * Initialise shared preference.
	 */
	private void initialiseSharedPreference() {
		mSharedPreferences = getSharedPreferences("mypre",0);
	}

	/**
	 * Initialise class variables.
	 */
	private void initialseVariable() {

		submit = (Button)findViewById(R.id.terms_btn_accept);
		cancel = (Button)findViewById(R.id.terms_btn_deny);
	}

	/**
	 * Initialise click listeners.
	 */
	private void initialseListener() {

		submit.setOnClickListener(this);
		cancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.terms_btn_accept:
			submitAccepted = "true";
			mSharedPreferences.edit().putString("conditionsAccepted",submitAccepted).commit();
			Intent mIntent=new Intent(TermsAndConditionsActivity.this,WelcomeActivity.class);
			startActivity(mIntent);
			finish();
			break;
			
		case R.id.terms_btn_deny:
			finish();
			break;

		default:
			break;
		}
	}
}
