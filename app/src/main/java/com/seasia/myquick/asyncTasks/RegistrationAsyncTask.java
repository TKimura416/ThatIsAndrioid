package com.seasia.myquick.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import com.thatsit.android.interfaces.RegisterInterface;
import com.seasia.myquick.controller.WebServiceClient;
import com.seasia.myquick.model.InsertUserResponseTemplate;

public class RegistrationAsyncTask extends AsyncTask<Context, Void, InsertUserResponseTemplate>{

	private final String pincode;
	private final String password;
	private final String subsciptionMode;
	private final String subscriptionAction;
	private final String gender;
	private final String country;
	private final String DeviceType;
	private final String city;
	private final String image;
	private final String age;
	private final String chatPassword;
	private final String email;
	private final String subscriptionFee;
	private final Context mContext;
	private final RegisterInterface mRegisterInterface;

	public RegistrationAsyncTask(String pincode, String password,
			String subsciptionMode, String subscriptionFee,
			String subscriptionAction, String gender, String country,
			String DeviceType, String city, String image, String age,
			String chatPassword, String email,Context mContext,
			RegisterInterface mRegisterInterface) {

		this.pincode = pincode;
		this.password = password;
		this.subsciptionMode = subsciptionMode;
		this.subscriptionFee = subscriptionFee;
		this.subscriptionAction = subscriptionAction;
		this.gender = gender;
		this.country = country;
		this.DeviceType = DeviceType;
		this.city = city;
		this.image = image;
		this.age = age;
		this.chatPassword = chatPassword;
		this.email = email;
		this.mContext = mContext;
		this.mRegisterInterface = mRegisterInterface;

	}

	@Override
	protected InsertUserResponseTemplate doInBackground(Context... params) {
		InsertUserResponseTemplate obj = new WebServiceClient(mContext).performUserResgisteration(pincode, password,
				subsciptionMode, subscriptionFee, subscriptionAction, gender,
				country, DeviceType, city, image, age, chatPassword, email);
		return obj;
	}

	protected void onPostExecute(InsertUserResponseTemplate result) {
		mRegisterInterface.registerInterfaceMethod(result);
		super.onPostExecute(result);
	}

}
