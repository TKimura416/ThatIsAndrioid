package com.seasia.myquick.model;

public class GetSubscriptionFeeParams {
	 private String Subscriptionfee;

	    private String Renewalfee;

	    public String getSubscriptionfee ()
	    {
	        return Subscriptionfee;
	    }

	    public void setSubscriptionfee (String Subscriptionfee)
	    {
	        this.Subscriptionfee = Subscriptionfee;
	    }

	    public String getRenewalfee ()
	    {
	        return Renewalfee;
	    }

	    public void setRenewalfee (String Renewalfee)
	    {
	        this.Renewalfee = Renewalfee;
	    }

	    @Override
	    public String toString()
	    {
	        return "ClassPojo [Subscriptionfee = "+Subscriptionfee+", Renewalfee = "+Renewalfee+"]";
	    }
}
