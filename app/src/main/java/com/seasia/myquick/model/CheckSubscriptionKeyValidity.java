package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;

public class CheckSubscriptionKeyValidity {
	@SerializedName("CheckSubscriptionResult")
	private CheckSubscriptionResult mCheckSubscriptionResult;
	
	public CheckSubscriptionResult getmCheckSubscriptionResult() {
		return mCheckSubscriptionResult;
	}

	public void setmCheckSubscriptionResult(
			CheckSubscriptionResult mCheckSubscriptionResult) {
		this.mCheckSubscriptionResult = mCheckSubscriptionResult;
	}


}
