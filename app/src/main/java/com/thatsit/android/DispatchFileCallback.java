package com.thatsit.android;

/*
	Callback for file uploaded on server
 */
public interface DispatchFileCallback {

	void onError(String message);
	void onProgress(int progress, String message);
	void onComplete(String message);
}
