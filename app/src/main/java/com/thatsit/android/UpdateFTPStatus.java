package com.thatsit.android;

public interface UpdateFTPStatus {
	public void showProgress(int currentProgress);
	public void markStatus(boolean isComplete,String name);
	
}
