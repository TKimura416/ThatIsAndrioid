package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;

/*public class GetSubscriptionHistoryTemplate {
	@SerializedName("GetSubscriptionHistoryResult")
private GetSubscriptionHistoryResult mGetSubscriptionHistoryResult;

public GetSubscriptionHistoryResult getmGetSubscriptionHistoryResult() {
	return mGetSubscriptionHistoryResult;
}

public void setmGetSubscriptionHistoryResult(
		GetSubscriptionHistoryResult mGetSubscriptionHistoryResult) {
	this.mGetSubscriptionHistoryResult = mGetSubscriptionHistoryResult;
}
}*/

public class GetSubscriptionHistoryTemplate
{
	@SerializedName("GetSubscriptionHistoryResult")
    private GetSubscriptionHistoryResult GetSubscriptionHistoryResult;

    public GetSubscriptionHistoryResult getGetSubscriptionHistoryResult ()
    {
        return GetSubscriptionHistoryResult;
    }

    public void setGetSubscriptionHistoryResult (GetSubscriptionHistoryResult GetSubscriptionHistoryResult)
    {
        this.GetSubscriptionHistoryResult = GetSubscriptionHistoryResult;
    }

}
