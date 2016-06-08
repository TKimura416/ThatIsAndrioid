package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;

public class UpdateSubscriptionResult {
	@SerializedName("Status")
	private  Status mStatus;
	@SerializedName("UpdateSubscriptionParams")
	private UpdateSubscriptionParams[] mUpdateSubscriptionParams;

	public UpdateSubscriptionParams[] getmUpdateSubscriptionParams() {
		return mUpdateSubscriptionParams;
	}

	public void setmUpdateSubscriptionParams(
			UpdateSubscriptionParams[] mUpdateSubscriptionParams) {
		this.mUpdateSubscriptionParams = mUpdateSubscriptionParams;
	}

	public Status getmStatus() {
		return mStatus;
	}

	public void setmStatus(Status mStatus) {
		this.mStatus = mStatus;
	}
}
