package com.seasia.myquick.asyncTasks;

import com.thatsit.android.interfaces.ValidatePincodeInterface;
import com.seasia.myquick.controller.WebServiceClient;
import com.seasia.myquick.model.ValidatePincode;

import android.content.Context;
import android.os.AsyncTask;

public class ValidatePincodeAsync extends AsyncTask<Void, Void, ValidatePincode> {

	private final Context context;
	private final ValidatePincodeInterface mValidatePincodeInterface;
	private final String chatUserName;
	
	public ValidatePincodeAsync(Context context,String chatUserName,
			ValidatePincodeInterface mValidatePincodeInterface) {

	this.context = context;
	this.chatUserName = chatUserName;
	this.mValidatePincodeInterface = mValidatePincodeInterface;
	}
	@Override
	protected ValidatePincode doInBackground(Void... params) {

		try {
			ValidatePincode obj = new WebServiceClient(
					context).validatePincode(chatUserName);

			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(ValidatePincode result) {
		super.onPostExecute(result);
		mValidatePincodeInterface.validatePincode(result);
	}

}
