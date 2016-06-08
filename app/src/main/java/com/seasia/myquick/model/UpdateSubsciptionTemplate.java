package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;

public class UpdateSubsciptionTemplate {
	@SerializedName("UpdateSubscriptionResult")
	private UpdateSubscriptionResult mUpdateSubscriptionResult;

	public UpdateSubscriptionResult getmUpdateSubscriptionResult() {
		return mUpdateSubscriptionResult;
	}

	public void setmUpdateSubscriptionResult(
			UpdateSubscriptionResult mUpdateSubscriptionResult) {
		this.mUpdateSubscriptionResult = mUpdateSubscriptionResult;
	}
}
