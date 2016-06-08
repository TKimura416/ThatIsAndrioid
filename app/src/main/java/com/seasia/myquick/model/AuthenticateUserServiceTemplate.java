package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;

public class AuthenticateUserServiceTemplate {
	@SerializedName("AuthenticateUserServiceResult")
private AuthenticateUserServiceResult mAuthenticateUserServiceResult;

public AuthenticateUserServiceResult getmAuthenticateUserServiceResult() {
	return mAuthenticateUserServiceResult;
}

public void setmAuthenticateUserServiceResult(
		AuthenticateUserServiceResult mAuthenticateUserServiceResult) {
	this.mAuthenticateUserServiceResult = mAuthenticateUserServiceResult;
}
}
