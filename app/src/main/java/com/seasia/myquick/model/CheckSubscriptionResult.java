package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;

public class CheckSubscriptionResult {
	@SerializedName("CheckSubscriptionParams")
	private CheckSubscriptionParams[] mCheckSubscriptionParams;

	public CheckSubscriptionParams[] getCheckSubscriptionParams() {
		return mCheckSubscriptionParams;
	}

	public void setCheckSubscriptionParams(
			CheckSubscriptionParams[] checkSubscriptionParams) {
		mCheckSubscriptionParams = checkSubscriptionParams;
	}

}
