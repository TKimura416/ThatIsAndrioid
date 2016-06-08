package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;

public class AuthenticateUserServiceResult {
	@SerializedName("AuthenticateUserServiceParams")
private AuthenticateUserServiceParams[] mAuthenticateUserServiceParams;
	@SerializedName("Status")
	private Status mStatus;
	
	

	public AuthenticateUserServiceParams[] getmAuthenticateUserServiceParams() {
		return mAuthenticateUserServiceParams;
	}

	public void setmAuthenticateUserServiceParams(
			AuthenticateUserServiceParams[] mAuthenticateUserServiceParams) {
		this.mAuthenticateUserServiceParams = mAuthenticateUserServiceParams;
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
