package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;


public class GenerateKeyIdResponse {
	@SerializedName("GeneateAlphanumericKeyResult")
 private GeneateAlphanumericKeyResult generalAlphakeyResult;

public GeneateAlphanumericKeyResult getGeneralAlphakeyResult() {
	return generalAlphakeyResult;
}

public void setGeneralAlphakeyResult(GeneateAlphanumericKeyResult generalAlphakeyResult) {
	this.generalAlphakeyResult = generalAlphakeyResult;
}
}
