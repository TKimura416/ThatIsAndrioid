package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;

public class FetchUserSettingTemplates {
	@SerializedName("FetchUserSettingsResult")
private FetchUserSettingsResult mFetchUserSettingsResult;

public FetchUserSettingsResult getmFetchUserSettingsResult() {
	return mFetchUserSettingsResult;
}

public void setmFetchUserSettingsResult(FetchUserSettingsResult mFetchUserSettingsResult) {
	this.mFetchUserSettingsResult = mFetchUserSettingsResult;
}
}
