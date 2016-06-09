package com.thatsit.android.beans;

public class OneBubble {
	public final boolean isOwner;
	public final String id;
	public final String message;
	public final long timestamp;
	public final boolean isVisibleTime;
	public final String name;
	public final String messageStatus;
	public final String messageSubject;

	/**
	 * 
	 * @param isOwner
	 *            : its a boolean to check whether to add message on left(TRUE)
	 *            or right(FALSE)
	 * @param comment
	 *            : Text to be shown in bubble
	 * @param isVisibleTime
	 *            : whether to show date or not (now showing only one date per
	 *            day)
	 * @param timestamp
	 *            : message time
	 * @param name
	 *            : user name to show
	 */
	public OneBubble(boolean isOwner, String id, String message,String messageSubject,
			boolean isVisibleTime, long timestamp, String name,String messageStatus) {
		super();
		this.isOwner = isOwner;
		this.id = id;
		this.message = message;
		this.isVisibleTime = isVisibleTime;
		this.timestamp = timestamp;
		this.name = name;
		this.messageStatus = messageStatus;
		this.messageSubject=messageSubject;
	}
}