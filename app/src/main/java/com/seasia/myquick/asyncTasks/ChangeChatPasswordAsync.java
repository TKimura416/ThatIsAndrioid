package com.seasia.myquick.asyncTasks;

import com.thatsit.android.interfaces.ChangeChatPasswordInterface;
import com.seasia.myquick.controller.WebServiceClient;
import com.seasia.myquick.model.AppSinglton;
import com.seasia.myquick.model.UpdateMessagePaswordTemplate;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class ChangeChatPasswordAsync extends AsyncTask<Void, Void, UpdateMessagePaswordTemplate>{
	
	private ChangeChatPasswordInterface mChangeChatPasswordInterface;
	private Context context;
	private String OldPassword,NewPassword;
	
	public ChangeChatPasswordAsync(Context context,String userid,String	OldPassword, 
			String NewPassword,ChangeChatPasswordInterface mChangeChatPasswordInterface) {
		
		this.context = context;
		this.OldPassword = OldPassword;
		this.NewPassword = NewPassword;
		this.mChangeChatPasswordInterface = mChangeChatPasswordInterface;
	}

	@Override
	protected UpdateMessagePaswordTemplate doInBackground(Void... params) {

		try {
			UpdateMessagePaswordTemplate obj = new WebServiceClient(
					context).updateMessagePassword(
							AppSinglton.userId,
							OldPassword, 
							NewPassword
							);
			return obj;
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(UpdateMessagePaswordTemplate result) {
		super.onPostExecute(result);
		mChangeChatPasswordInterface.changeChatPassword(result);
	}
}
