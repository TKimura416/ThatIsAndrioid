package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;

public class FetchUserSettingsResult {
	@SerializedName("FetchUserSettingsParams")
private FetchUserSettingsParams[] mFetchUserSettingsParams;

public FetchUserSettingsParams[] getmFetchUserSettingsParams() {
	return mFetchUserSettingsParams;
}

public void setmFetchUserSettingsParams(FetchUserSettingsParams[] mFetchUserSettingsParams) {
	this.mFetchUserSettingsParams = mFetchUserSettingsParams;
}
}
