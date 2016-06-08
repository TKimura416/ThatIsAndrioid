package com.seasia.myquick.model;

public class ValidatePincode {
		
	private ValidatePincodeResult ValidatePincodeResult;

		public ValidatePincodeResult getValidatePincodeResult ()
		{
			return ValidatePincodeResult;
		}

		public void setValidatePincodeResult (ValidatePincodeResult ValidatePincodeResult)
		{
			this.ValidatePincodeResult = ValidatePincodeResult;
		}

		@Override
		public String toString()
		{
			return "ClassPojo [ValidatePincodeResult = "+ValidatePincodeResult+"]";
		}
	}
