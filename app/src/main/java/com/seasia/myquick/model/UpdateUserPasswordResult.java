package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;

public class UpdateUserPasswordResult {
	@SerializedName("Status")
    private Status mStatus;
	@SerializedName("UpdateUserPasswordParams")
    private UpdateUserPasswordParams[] mUpdateUserPasswordParams;
	
   public UpdateUserPasswordParams[] getmUpdateUserPasswordParams() {
		return mUpdateUserPasswordParams;
	}
	public void setmUpdateUserPasswordParams(
			UpdateUserPasswordParams[] mUpdateUserPasswordParams) {
		this.mUpdateUserPasswordParams = mUpdateUserPasswordParams;
	}
/**
 * @return the mStatus
 */
public Status getmStatus() {
	return mStatus;
}
/**
 * @param mStatus the mStatus to set
 */
public void setmStatus(Status mStatus) {
	this.mStatus = mStatus;
}

}
