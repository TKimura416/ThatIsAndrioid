package com.seasia.myquick.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import com.thatsit.android.interfaces.RegisterInterface;
import com.seasia.myquick.controller.WebServiceClient;
import com.seasia.myquick.model.InsertUserResponseTemplate;

public class RegistrationAsyncTask extends AsyncTask<Context, Void, InsertUserResponseTemplate>{

	private String pincode, password, subsciptionMode,
	subscriptionAction, gender, country, DeviceType, city, image, age,
	chatPassword, email;
	private String subscriptionFee;
	private Context mContext;
	private RegisterInterface mRegisterInterface;

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
	};

}
