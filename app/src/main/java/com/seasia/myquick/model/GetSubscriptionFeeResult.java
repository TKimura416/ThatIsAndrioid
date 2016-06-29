package com.seasia.myquick.model;

public class GetSubscriptionFeeResult {
	 private Status Status;

	    private GetSubscriptionFeeParams[] GetSubscriptionFeeParams;

	    public Status getStatus ()
	    {
	        return Status;
	    }

	    public void setStatus (Status Status)
	    {
	        this.Status = Status;
	    }

	    public GetSubscriptionFeeParams[] getGetSubscriptionFeeParams ()
	    {
	        return GetSubscriptionFeeParams;
	    }

	    public void setGetSubscriptionFeeParams (GetSubscriptionFeeParams[] GetSubscriptionFeeParams)
	    {
	        this.GetSubscriptionFeeParams = GetSubscriptionFeeParams;
	    }

	    @Override
	    public String toString()
	    {
	        return "ClassPojo [Status = "+Status+", GetSubscriptionFeeParams = "+GetSubscriptionFeeParams+"]";
	    }
}
