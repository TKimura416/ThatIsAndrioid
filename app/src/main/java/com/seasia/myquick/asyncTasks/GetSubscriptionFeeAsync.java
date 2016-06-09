package com.seasia.myquick.asyncTasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.thatsit.android.interfaces.SubscriptionFeeInterface;
import com.seasia.myquick.controller.WebServiceClient;
import com.seasia.myquick.model.SubcriptionFeeTemplate;

public class GetSubscriptionFeeAsync extends AsyncTask<Void, Void, SubcriptionFeeTemplate>{

	private final Context context;
	private ProgressDialog pdDialog;
	private final SubscriptionFeeInterface mSubscriptionFeeInterface;
	
	public GetSubscriptionFeeAsync(Context context,
			SubscriptionFeeInterface mSubscriptionFeeInterface) {
		this.context = context;
		this.mSubscriptionFeeInterface = mSubscriptionFeeInterface;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		pdDialog = new ProgressDialog(context);
		pdDialog.setMessage("Please Wait..");
		pdDialog.show();
	}
	
	
	@Override
	protected SubcriptionFeeTemplate doInBackground(Void... params) {
		try {
			SubcriptionFeeTemplate obj = new WebServiceClient(
					context).getSubscriptionFee();
			return obj;
		} catch (Exception e) {
			return null;
		}
	}

	
	@Override
	protected void onPostExecute(SubcriptionFeeTemplate result) {
		super.onPostExecute(result);
		try {
			mSubscriptionFeeInterface.subscriptionFee(result);
			pdDialog.dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
