package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;

public class UpdateUserPasswordTemplate {
	@SerializedName("UpdateUserPasswordResult")
  private UpdateUserPasswordResult mUpdateUserPasswordResult;

/**
 * @return the mUpdateUserPasswordResult
 */
public UpdateUserPasswordResult getmUpdateUserPasswordResult() {
	return mUpdateUserPasswordResult;
}

/**
 * @param mUpdateUserPasswordResult the mUpdateUserPasswordResult to set
 */
public void setmUpdateUserPasswordResult(UpdateUserPasswordResult mUpdateUserPasswordResult) {
	this.mUpdateUserPasswordResult = mUpdateUserPasswordResult;
}
}
