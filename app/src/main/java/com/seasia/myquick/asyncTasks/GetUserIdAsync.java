package com.seasia.myquick.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import com.thatsit.android.interfaces.UserIdInterface;
import com.seasia.myquick.controller.WebServiceClient;
import com.seasia.myquick.model.GenerateKeyIdResponse;

public class GetUserIdAsync extends AsyncTask<Void, Void, GenerateKeyIdResponse>{

	private Context context;
	private UserIdInterface mUserIdInterface;
	
	public GetUserIdAsync(Context context,UserIdInterface mUserIdInterface) {
	this.context = context;
	this.mUserIdInterface = mUserIdInterface;
	}
	
	@Override
	protected GenerateKeyIdResponse doInBackground(Void... params) {
		try {
			GenerateKeyIdResponse objs = new WebServiceClient(
					context).performUserKey(null);
			return objs;
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(GenerateKeyIdResponse result) {
		super.onPostExecute(result);
		mUserIdInterface.getUserId(result);
	}
}
