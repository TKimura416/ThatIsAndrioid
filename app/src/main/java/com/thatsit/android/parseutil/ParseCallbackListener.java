package com.thatsit.android.parseutil;

import java.util.List;

import com.parse.ParseException;
import com.parse.ParseObject;

public interface ParseCallbackListener {

	/**
	 * FRAGMENT CONTACTS
	 * */
	final int OPERATION_ON_START = 1001;
	final int OPERATION_FRIEND_REQUEST_RECEIVED=2002;
	final int OPERATION_FRIEND_REQUEST_ACCEPTED=2003;
	final int OPERATION_FRIEND_REQUEST_DELETED=2007;
	final int OPERATION_JOIN_GROUP=3001;
	final int OPERATION_LEAVE_GROUP=3002;


	void done(ParseException parseException, int requestId);

	public void done(List<ParseObject> receipients, ParseException e,
					 int requestId);
}
