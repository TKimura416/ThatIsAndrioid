package com.seasia.myquick.model;

public class SubcriptionFeeTemplate {

	 private GetSubscriptionFeeResult GetSubscriptionFeeResult;

	    public GetSubscriptionFeeResult getGetSubscriptionFeeResult ()
	    {
	        return GetSubscriptionFeeResult;
	    }

	    public void setGetSubscriptionFeeResult (GetSubscriptionFeeResult GetSubscriptionFeeResult)
	    {
	        this.GetSubscriptionFeeResult = GetSubscriptionFeeResult;
	    }

	    @Override
	    public String toString()
	    {
	        return "ClassPojo [GetSubscriptionFeeResult = "+GetSubscriptionFeeResult+"]";
	    }
	
}
