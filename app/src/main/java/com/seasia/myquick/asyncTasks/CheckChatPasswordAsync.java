package com.seasia.myquick.asyncTasks;

import android.app.*;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.thatsit.android.interfaces.CheckChatPasswordInterface;
import com.seasia.myquick.controller.WebServiceClient;
import com.seasia.myquick.model.AppSinglton;
import com.seasia.myquick.model.CheckMessage_ChatPasswrd;

public class CheckChatPasswordAsync extends AsyncTask<Void, Void, CheckMessage_ChatPasswrd> {

	private final Context context;
	private final String Chatpassword;
	private final CheckChatPasswordInterface mCheckChatPasswordInterface;
	private final SharedPreferences mSharedPreferences;
	private ProgressDialog pdDialog;

	public CheckChatPasswordAsync(Context context,String Chatpassword,
			CheckChatPasswordInterface mCheckChatPasswordInterface) {
	
	this.context = context;
	this.Chatpassword = Chatpassword;
	this.mCheckChatPasswordInterface = mCheckChatPasswordInterface;
	mSharedPreferences = context.getSharedPreferences("USERID", Context.MODE_PRIVATE);
	AppSinglton.userId = mSharedPreferences.getString("USERID", "");
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		pdDialog = new ProgressDialog(context);
		pdDialog.setMessage("Verifying Password");
		pdDialog.show();
		pdDialog.setCancelable(false);
	}
	
	@Override
	protected CheckMessage_ChatPasswrd doInBackground(Void... params) {
		try {
			CheckMessage_ChatPasswrd obj = new WebServiceClient(
					context).authenticateChatPasswd(
							AppSinglton.userId, Chatpassword);
			return obj;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(CheckMessage_ChatPasswrd result) {
		super.onPostExecute(result);
		pdDialog.dismiss();
		mCheckChatPasswordInterface.checkChatPassword(result);
	}
}
