package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;

public class UpdateUserSettingsResult {
	@SerializedName("UpdateUserSettingsParams")
private UpdateUserSettingsParams[] UpdateUserSettingsParams;

public UpdateUserSettingsParams[] getUpdateUserSettingsParams() {
	return UpdateUserSettingsParams;
}

public void setUpdateUserSettingsParams(
		UpdateUserSettingsParams[] updateUserSettingsParams) {
	UpdateUserSettingsParams = updateUserSettingsParams;
}
}
