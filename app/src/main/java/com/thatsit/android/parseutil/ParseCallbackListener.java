package com.thatsit.android.parseutil;

import java.util.List;

import com.parse.ParseException;
import com.parse.ParseObject;

public interface ParseCallbackListener {

	/**
	 * FRAGMENT CONTACTS
	 * */
	int OPERATION_ON_START = 1001;
	int OPERATION_ADAPTER_RELOAD = 1002;
	int OPERATION_ADAPTER_RELOAD_RESET = 1003;
	
	
	/**
	 * MAIN SERVICE 
	 * **/
	int OPERATION_FRIEND_REQUEST_SENT=2001;
	int OPERATION_FRIEND_REQUEST_RECEIVED=2002;
	int OPERATION_FRIEND_REQUEST_ACCEPTED=2003;
	int OPERATION_FRIEND_REQUEST_DECLINED=2004;
	int OPERATION_FRIEND_REQUEST_ACCEPTED_RESPONSE_OFFLINE=2005;
	int OPERATION_FRIEND_REQUEST_DECLINED_RESPPONSE_OFFLINE=2006;
	int OPERATION_FRIEND_REQUEST_DELETED=2007;
	
	int OPERATION_JOIN_GROUP=3001;
	int OPERATION_LEAVE_GROUP=3002;
	
	
	void done(ParseException parseException, int requestId);

	void done(List<ParseObject> receipients, ParseException e,
			  int requestId);
}
