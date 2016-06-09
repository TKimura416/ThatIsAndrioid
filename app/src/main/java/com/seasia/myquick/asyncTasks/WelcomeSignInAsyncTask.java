package com.seasia.myquick.asyncTasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.thatsit.android.interfaces.WelcomeSignInInterface;
import com.seasia.myquick.controller.WebServiceClient;
import com.seasia.myquick.model.AuthenticateUserServiceTemplate;

public class WelcomeSignInAsyncTask extends AsyncTask<Context, Void, AuthenticateUserServiceTemplate> {
	
	private ProgressDialog pdDialog;
	private final Context mContext;
	private final String EmailId;
	private final String ThatsItpassword;
	private final WelcomeSignInInterface mWelcomeGetDataInterface;
	
	public WelcomeSignInAsyncTask(Context mContext,String EmailId,String ThatsItpassword,
			WelcomeSignInInterface mWelcomeGetDataInterface) {
		this.mContext = mContext;
		this.EmailId = EmailId;
		this.ThatsItpassword = ThatsItpassword;
		this.mWelcomeGetDataInterface = mWelcomeGetDataInterface;
	}
	
	@Override
	protected void onPreExecute() {
		try {
			pdDialog = new ProgressDialog(mContext);
			pdDialog.setMessage("Loading Please Wait...");
			pdDialog.show();
			pdDialog.setCancelable(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected AuthenticateUserServiceTemplate doInBackground(Context... params) {
		try {
			AuthenticateUserServiceTemplate obj = new WebServiceClient(
					mContext).getUserSignIn(EmailId,ThatsItpassword);
			return obj;
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(AuthenticateUserServiceTemplate result) {
		mWelcomeGetDataInterface.getWelcomeData(result);
		pdDialog.dismiss();
		super.onPostExecute(result);
	}
}
