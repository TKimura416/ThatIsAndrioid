package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;

public class CheckMessage_ChatPasswrd {
	@SerializedName("CheckMessagePasswordResult")
private CheckMessagePasswordResult mCheckMessagePasswordResult;

public CheckMessagePasswordResult getmCheckMessagePasswordResult() {
	return mCheckMessagePasswordResult;
}

public void setmCheckMessagePasswordResult(
		CheckMessagePasswordResult mCheckMessagePasswordResult) {
	this.mCheckMessagePasswordResult = mCheckMessagePasswordResult;
}
}
