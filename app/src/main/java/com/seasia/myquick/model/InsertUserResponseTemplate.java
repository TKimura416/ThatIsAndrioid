package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;

public class InsertUserResponseTemplate {

	@SerializedName("InsertUserDataResult")
	private InsertUserDataResult InsertUserDataResult;

	public InsertUserDataResult getInsertUserDataResult() {
		return InsertUserDataResult;
	}

	public void setInsertUserDataResult(InsertUserDataResult insertUserDataResult) {
		InsertUserDataResult = insertUserDataResult;
	}
	
	
}
