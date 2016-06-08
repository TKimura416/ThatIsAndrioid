package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;

public class GeneateAlphanumericKeyResult {
	@SerializedName("GeneateAlphanumericKeyParams")
private GeneateAlphanumericKeyParams[] generateAlphaKeyParam;

public GeneateAlphanumericKeyParams[] getGenerateAlphaKeyParam() {
	return generateAlphaKeyParam;
}

public void setGenerateAlphaKeyParam(GeneateAlphanumericKeyParams[] generateAlphaKeyParam) {
	this.generateAlphaKeyParam = generateAlphaKeyParam;
}
}
