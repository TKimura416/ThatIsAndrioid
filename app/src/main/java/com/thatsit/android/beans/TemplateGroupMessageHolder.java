package com.thatsit.android.beans;

public class TemplateGroupMessageHolder {
	
	private String jid;
	private String message;
	private String timeStamp;
	private String roomName;
	
	
	
	/**
	 * @return
	 */
	public String getRoomName() {
		return roomName;
	}
	/**
	 * @param roomName
	 */
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	/**
	 * @return
	 */
	public String getJid() {
		return jid;
	}
	/**
	 * @param jid
	 */
	public void setJid(String jid) {
		this.jid = jid;
	}
	/**
	 * @return
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @return
	 */
	public String getTimeStamp() {
		return timeStamp;
	}
	/**
	 * @param timeStamp
	 */
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	
	
}
