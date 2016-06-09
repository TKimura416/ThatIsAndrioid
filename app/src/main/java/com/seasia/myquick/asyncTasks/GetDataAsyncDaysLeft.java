package com.seasia.myquick.asyncTasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.thatsit.android.interfaces.DaysLeftInterface;
import com.seasia.myquick.controller.WebServiceClient;
import com.seasia.myquick.model.CheckSubscriptionKeyValidity;

public class GetDataAsyncDaysLeft extends AsyncTask<Void, Void, CheckSubscriptionKeyValidity>{

	private final Context context;
	private ProgressDialog pdDialog;
	private final DaysLeftInterface mDaysLeftInterface;
	private final String UserID;
	
	public GetDataAsyncDaysLeft(Context context,String UserID,
			DaysLeftInterface mDaysLeftInterface) {
		this.context = context;
		this.UserID = UserID;
		this.mDaysLeftInterface = mDaysLeftInterface;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		pdDialog = new ProgressDialog(context);
		pdDialog.setMessage("Please Wait..");
		//pdDialog.setCancelable(false);
		pdDialog.show();
	}
	
	@Override
	protected CheckSubscriptionKeyValidity doInBackground(Void... params) {

		try {
			CheckSubscriptionKeyValidity obj = new WebServiceClient(context)
					.getUserValidity(UserID);
			return obj;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(CheckSubscriptionKeyValidity result) {
		super.onPostExecute(result);
		pdDialog.dismiss();
		mDaysLeftInterface.daysLeft(result);
	}
}
