package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;

public class UpdateUserSettingTemplate {
	@SerializedName("UpdateUserSettingsResult")
private UpdateUserSettingsResult mUpdateUserSettingsResult;

public UpdateUserSettingsResult getmUpdateUserSettingsResult() {
	return mUpdateUserSettingsResult;
}

public void setmUpdateUserSettingsResult(
		UpdateUserSettingsResult mUpdateUserSettingsResult) {
	this.mUpdateUserSettingsResult = mUpdateUserSettingsResult;
}
}
