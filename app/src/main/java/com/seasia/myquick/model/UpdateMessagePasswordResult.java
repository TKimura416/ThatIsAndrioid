package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;

public class UpdateMessagePasswordResult {
	@SerializedName("UpdateMessagePasswordParams")
private UpdateMessagePasswordParams[] mUpdateMessagePasswordParams;

	public UpdateMessagePasswordParams[] getmUpdateMessagePasswordParams() {
		return mUpdateMessagePasswordParams;
	}

	public void setmUpdateMessagePasswordParams(
			UpdateMessagePasswordParams[] mUpdateMessagePasswordParams) {
		this.mUpdateMessagePasswordParams = mUpdateMessagePasswordParams;
	}


}
