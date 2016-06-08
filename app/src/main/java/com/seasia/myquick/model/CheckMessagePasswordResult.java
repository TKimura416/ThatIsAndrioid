package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;

public class CheckMessagePasswordResult {
	@SerializedName("CheckMessagePasswordParams")
private CheckMessagePasswordParams[] mCheckMessagePasswordParams;

public CheckMessagePasswordParams[] getmCheckMessagePasswordParams() {
	return mCheckMessagePasswordParams;
}

public void setmCheckMessagePasswordParams(
		CheckMessagePasswordParams[] mCheckMessagePasswordParams) {
	this.mCheckMessagePasswordParams = mCheckMessagePasswordParams;
}
}
