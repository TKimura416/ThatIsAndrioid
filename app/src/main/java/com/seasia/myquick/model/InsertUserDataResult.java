package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;

public class InsertUserDataResult {
	@SerializedName("InsertUserDataParams")
	
    private InsertUserDataParams[] mInsertUserDataParams;
	@SerializedName("Status")
	private Status mStatus;

	public InsertUserDataParams[] getmInsertUserDataParams() {
		return mInsertUserDataParams;
	}

	public void setmInsertUserDataParams(
			InsertUserDataParams[] mInsertUserDataParams) {
		this.mInsertUserDataParams = mInsertUserDataParams;
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
