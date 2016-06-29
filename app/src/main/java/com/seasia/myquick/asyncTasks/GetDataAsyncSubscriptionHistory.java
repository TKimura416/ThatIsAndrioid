package com.seasia.myquick.asyncTasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.thatsit.android.interfaces.SubscriptionHistoryInterface;
import com.seasia.myquick.controller.WebServiceClient;
import com.seasia.myquick.model.GetSubscriptionHistoryTemplate;

public class GetDataAsyncSubscriptionHistory extends AsyncTask<Void, Void, GetSubscriptionHistoryTemplate>{

	
	private Context context;
	private SubscriptionHistoryInterface mSubscriptionHistoryInterface;
	private String UserID;
	
	public GetDataAsyncSubscriptionHistory(Context context,String UserID,
			SubscriptionHistoryInterface mSubscriptionHistoryInterface) {
		this.context = context;
		this.UserID = UserID;
		this.mSubscriptionHistoryInterface = mSubscriptionHistoryInterface;
	}

	@Override
	protected GetSubscriptionHistoryTemplate doInBackground(Void... params) {

		try {
			GetSubscriptionHistoryTemplate obj = new WebServiceClient(context)
			.getSubscriptionHistory(UserID);
			return obj;
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(GetSubscriptionHistoryTemplate result) {
		super.onPostExecute(result);
		//pdDialog.dismiss();
		mSubscriptionHistoryInterface.subscriptionHistory(result);
	}
}
