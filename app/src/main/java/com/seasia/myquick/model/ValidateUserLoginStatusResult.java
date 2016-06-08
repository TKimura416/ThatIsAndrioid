package com.seasia.myquick.model;

public class ValidateUserLoginStatusResult
{
    private Status Status;

    private String StatusId;

    private String UserLoginStatus;

    public Status getStatus ()
    {
        return Status;
    }

    public void setStatus (Status Status)
    {
        this.Status = Status;
    }

    public String getStatusId ()
    {
        return StatusId;
    }

    public void setStatusId (String StatusId)
    {
        this.StatusId = StatusId;
    }

    public String getUserLoginStatus ()
    {
        return UserLoginStatus;
    }

    public void setUserLoginStatus (String UserLoginStatus)
    {
        this.UserLoginStatus = UserLoginStatus;
    }
}

