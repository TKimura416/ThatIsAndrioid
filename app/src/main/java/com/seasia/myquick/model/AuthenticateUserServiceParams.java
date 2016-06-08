package com.seasia.myquick.model;

public class AuthenticateUserServiceParams {
private String RetVal;
private String PinCode;
public String getRetVal() {
	return RetVal;
}

public void setRetVal(String retVal) {
	RetVal = retVal;
}

/**
 * @return the pinCode
 */
public String getPinCode() {
	return PinCode;
}

/**
 * @param pinCode the pinCode to set
 */
public void setPinCode(String pinCode) {
	PinCode = pinCode;
}
}
