package com.seasia.myquick.model;

/*public class Status {
  private String Description;
  private int Status;
*//**
 * @return the description
 *//*
public String getDescription() {
	return Description;
}
*//**
 * @param description the description to set
 *//*
public void setDescription(String description) {
	Description = description;
}
public int getStatus() {
	return Status;
}
public void setStatus(int status) {
	Status = status;
}

}
*/


public class Status
{
    private String Status;

    private String Description;

    public String getStatus ()
    {
        return Status;
    }

    public void setStatus (String Status)
    {
        this.Status = Status;
    }

    public String getDescription ()
    {
        return Description;
    }

    public void setDescription (String Description)
    {
        this.Description = Description;
    }

   
}