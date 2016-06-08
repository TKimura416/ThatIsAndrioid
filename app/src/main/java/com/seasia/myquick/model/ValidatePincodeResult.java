package com.seasia.myquick.model;

public class ValidatePincodeResult {
	    private Status Status;

	    public Status getStatus ()
	    {
	        return Status;
	    }

	    public void setStatus (Status Status)
	    {
	        this.Status = Status;
	    }

	    @Override
	    public String toString()
	    {
	        return "ClassPojo [Status = "+Status+"]";
	    }
}
