package com.seasia.myquick.model;

import com.google.gson.annotations.SerializedName;

/*public class GetSubscriptionHistoryResult {
	@SerializedName("GetSubscriptionHistoryParams")
private GetSubscriptionHistoryParams[] mGetSubscriptionHistoryParams;

public GetSubscriptionHistoryParams[] getmGetSubscriptionHistoryParams() {
	return mGetSubscriptionHistoryParams;
}

public void setmGetSubscriptionHistoryParams(
		GetSubscriptionHistoryParams[] mGetSubscriptionHistoryParams) {
	this.mGetSubscriptionHistoryParams = mGetSubscriptionHistoryParams;
}
}*/

public class GetSubscriptionHistoryResult
{
	@SerializedName("GetSubscriptionHistoryParams")
    private GetSubscriptionHistoryParams[] GetSubscriptionHistoryParams;

    private Status Status;

    public GetSubscriptionHistoryParams[] getGetSubscriptionHistoryParams ()
    {
        return GetSubscriptionHistoryParams;
    }

    public void setGetSubscriptionHistoryParams (GetSubscriptionHistoryParams[] GetSubscriptionHistoryParams)
    {
        this.GetSubscriptionHistoryParams = GetSubscriptionHistoryParams;
    }

    public Status getStatus ()
    {
        return Status;
    }

    public void setStatus (Status Status)
    {
        this.Status = Status;
    }
}
