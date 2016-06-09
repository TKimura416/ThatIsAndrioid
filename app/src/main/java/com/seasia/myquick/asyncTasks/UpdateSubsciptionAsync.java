package com.seasia.myquick.asyncTasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.thatsit.android.interfaces.UpdateSubscriptionInterface;
import com.seasia.myquick.controller.WebServiceClient;
import com.seasia.myquick.model.UpdateSubsciptionTemplate;

public class UpdateSubsciptionAsync extends AsyncTask<Void, Void, UpdateSubsciptionTemplate> {
	private final Context context;
	private ProgressDialog pdDialog;
	private final UpdateSubscriptionInterface mUpdateSubscriptionInterface;
	private final String UserID;
	private final String renewalFee;
	private final String SubscribeDate;
	
	public UpdateSubsciptionAsync(Context context,String UserID,String renewalFee,
			String formattedDate,UpdateSubscriptionInterface mUpdateSubscriptionInterface) {
		this.context = context;
		this.UserID = UserID;
		this.renewalFee = renewalFee;
		this.mUpdateSubscriptionInterface = mUpdateSubscriptionInterface;
		this.SubscribeDate = formattedDate;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		pdDialog = new ProgressDialog(context);
		pdDialog.setMessage("Please Wait..");
		pdDialog.show();
	}
	
	@Override
	protected UpdateSubsciptionTemplate doInBackground(Void... params) {

		try {
			UpdateSubsciptionTemplate obj = new WebServiceClient(
					context).updateSubsciption(UserID,/*"PayPal"*/"In-App Purchase",renewalFee,"R",SubscribeDate);

			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(UpdateSubsciptionTemplate result) {
		super.onPostExecute(result);
		pdDialog.dismiss();
		mUpdateSubscriptionInterface.updateSubscription(result);
	}

}
