package com.seasia.myquick.model;

/*public class GetSubscriptionHistoryParams {
private String SubscriptionAction;
public String getSubscriptionAction() {
	return SubscriptionAction;
}
public void setSubscriptionAction(String subscriptionAction) {
	SubscriptionAction = subscriptionAction;
}
public String getSubscriptionDate() {
	return SubscriptionDate;
}
public void setSubscriptionDate(String subscriptionDate) {
	SubscriptionDate = subscriptionDate;
}
public String getSubscriptionExpiryDate() {
	return SubscriptionExpiryDate;
}
public void setSubscriptionExpiryDate(String subscriptionExpiryDate) {
	SubscriptionExpiryDate = subscriptionExpiryDate;
}
public String getSubscriptionMode() {
	return SubscriptionMode;
}
public void setSubscriptionMode(String subscriptionMode) {
	SubscriptionMode = subscriptionMode;
}
public int getSubscriptionFee() {
	return SubscriptionFee;
}
public void setSubscriptionFee(int subscriptionFee) {
	SubscriptionFee = subscriptionFee;
}
private String SubscriptionDate;
private String SubscriptionExpiryDate;
private String SubscriptionMode;
private int SubscriptionFee;
}
*/

public class GetSubscriptionHistoryParams
{
    private String SubscriptionDate;

    private String SubscriptionAction;

    private String SubscriptionFee;

    private String SubscriptionExpiryDate;

    private String SubscriptionMode;

    public String getSubscriptionDate ()
    {
        return SubscriptionDate;
    }

    public void setSubscriptionDate (String SubscriptionDate)
    {
        this.SubscriptionDate = SubscriptionDate;
    }

    public String getSubscriptionAction ()
    {
        return SubscriptionAction;
    }

    public void setSubscriptionAction (String SubscriptionAction)
    {
        this.SubscriptionAction = SubscriptionAction;
    }

    public String getSubscriptionFee ()
    {
        return SubscriptionFee;
    }

    public void setSubscriptionFee (String SubscriptionFee)
    {
        this.SubscriptionFee = SubscriptionFee;
    }

    public String getSubscriptionExpiryDate ()
    {
        return SubscriptionExpiryDate;
    }

    public void setSubscriptionExpiryDate (String SubscriptionExpiryDate)
    {
        this.SubscriptionExpiryDate = SubscriptionExpiryDate;
    }

    public String getSubscriptionMode ()
    {
        return SubscriptionMode;
    }

    public void setSubscriptionMode (String SubscriptionMode)
    {
        this.SubscriptionMode = SubscriptionMode;
    }

}