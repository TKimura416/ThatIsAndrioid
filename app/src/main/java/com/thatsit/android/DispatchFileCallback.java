package com.thatsit.android;

/*
	Callback for file uploaded on server
 */
public interface DispatchFileCallback {

	public void onError(String message);
	public void onProgress(int progress,String message);
	public void onComplete(String message);
}
