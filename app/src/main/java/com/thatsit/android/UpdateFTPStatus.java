package com.thatsit.android;

public interface UpdateFTPStatus {
	void showProgress(int currentProgress);
	void markStatus(boolean isComplete, String name);
	
}
