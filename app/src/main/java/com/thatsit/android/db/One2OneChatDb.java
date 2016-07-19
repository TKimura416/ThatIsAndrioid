package com.thatsit.android.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.thatsit.android.Utility;
import com.thatsit.android.application.ThatItApplication;

import org.jxmpp.jid.Jid;

public class One2OneChatDb extends Database {

	public One2OneChatDb(Context ctx) {
		super(ctx);
	}

	/**
	 *
	 * @param jid - message from
	 * @param name - firstname
	 * @param msg - message
	 * @param messageSubject - isFile
	 * @param image - profile pic
	 * @param owner_or_participant - sender or receiver
	 * @param messageStatus - delivered or received
	 * @return - ret value
	 */
	public static boolean addMessage(String jid, String name,String msg, String messageSubject,byte[] image,String owner_or_participant,String messageStatus) {

		synchronized (ThatItApplication.getApplication()){
			ContentValues values = composeValues(jid, name, msg,messageSubject,image,owner_or_participant,messageStatus);
			long ret = databaseW.insert(TABLE_NAME, null, values);
			return ret != -1;
		}
	}

	/**
	 *
	 * @param groupName - room name
	 * @param jid - from
	 * @param msg - message text
	 * @return - ret value
	 */
	public static boolean addGroupMessage(String groupName,String jid,String msg) {
		ContentValues values = composeGroupValues(groupName,jid,msg);
		long ret = databaseW.insert(TABLE_GROUP, null, values);
		Log.e("room name ", groupName);
		return ret != -1;
	}

	/**
	 *
	 * @param jid
	 * @param messagestatus - delivered or received
	 * @return
	 */
	public boolean updateMessagestatus(String jid,String messagestatus) {

		synchronized (ThatItApplication.getApplication()){
			ContentValues values = new ContentValues();
			values.put(DbOpenHelper.COLUMN_MESSAGE_STATUS, messagestatus);
			return databaseW.update(TABLE_NAME, values, "jid='" + jid + "'" +andUserName() ,null)>0;
		}
	}

	private static String andUserName(){
		return " AND "+DbOpenHelper.SIGNED_IN_ID+" = '" + Utility.getUserName() + "'";
	}

	/**
	 *
	 * @param fileName - file sent via FTP
	 * @return
	 */
	public boolean updateFileDownloadstatus(String fileName) {

		synchronized (ThatItApplication.getApplication()){
			ContentValues values = new ContentValues();
			values.put(DbOpenHelper.COLUMN_FILE_DOWNLOAD_STATUS, "1"); // 1 == file has been downloaded successfully
			long result = databaseW.update(TABLE_NAME, values, "subject='" + fileName + "'" + andUserName(), null);
			Log.e("UPDATE_STATUS" , result+"");
			return result > 0;
		}
	}

	/**
	 *
	 * @param fileName - file sent via FTP
	 * @return downloaded or not
	 */
	public boolean isFileDownloaded(String fileName){
		try{
			String row = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ DbOpenHelper.COLUMN_MESSAGE_SUBJECT + " = '" + fileName + "'" +andUserName();
			Cursor cursor= databaseW.rawQuery(row, null);
			if(cursor!=null)
				cursor.moveToFirst();
			String downloadStatus=cursor.getString(cursor.getColumnIndex(DbOpenHelper.COLUMN_FILE_DOWNLOAD_STATUS));
			return !downloadStatus.equalsIgnoreCase("0");
		}catch(Exception e){
			return false;
		}
	}


	/**
	 *
	 * @param jid - New entry
	 * @param firstName - first name from vcard
	 * @param lastName - last name from vcard
	 * @param profilePicUrl - pic from vcard
	 * @return
	 */
	
	public boolean saveRoster(String jid , String firstName , String lastName , String profilePicUrl ){

		ContentValues values = new ContentValues();
		
		if(jid.contains("@"))
		jid = jid.substring(0, jid.indexOf("@"));
		
		values.put(DbOpenHelper.JABBER_ID, jid);
		values.put(DbOpenHelper.FIRSTNAME, firstName);
		values.put(DbOpenHelper.LASTNAME, lastName);
		values.put(DbOpenHelper.PROFILE_PIC_URL, profilePicUrl);
		
			try {
				databaseW.delete(TABLE_ROSTER, ""+DbOpenHelper.JABBER_ID+"='"+jid+"'", null );
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		long ret = databaseW.insert(TABLE_ROSTER, null, values);
		
		return ret != -1;
	}


	public void clearRosters1(){
		 try {
			 databaseW.delete(TABLE_ROSTER, null , null);
		} catch (Exception e) {
			databaseW.execSQL(DbOpenHelper.CREATE_TABLE_ROSTER_ENTRY);
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param cid - jID message to be deleted
	 * @return
	 */
	public boolean deleteMessage(int cid) {
		return databaseW.delete(TABLE_NAME, "id='" + cid + "'" +andUserName()  , null) >0;
	}

	/**
	 * Delete all chat from database.
	 * @return value for message deletion
	 */
	public static boolean deleteAllMessages() {
		int ret = databaseW.delete(TABLE_NAME, username() , null);
		return ret == 1;
	}

	/**
	 * Access all chat message from database.
	 */
	public static Cursor getAllMessages() {
		return databaseW.rawQuery("select * from "+TABLE_NAME+" where "+username(), null);
	}

	/**
	 *
	 * @return roster list
	 */
	public static Cursor getAllRoster() {
		return databaseW.rawQuery("select * from "+TABLE_ROSTER, null);
	}
	
	public static Cursor getRosterEntry(String jid) {
		
		if(jid.contains("@")){
			jid = jid.split("@")[0];
		}
		return databaseW.rawQuery("select * from "+TABLE_ROSTER+" where "+ DbOpenHelper.JABBER_ID+" = '"+jid+"'", null);
	}
	
	/**
	 * Access all distinct jid's from database.
	 */
	public static Cursor getAllChatRoster() {
		// order by timestamp
		String orderBy = DbOpenHelper.COLUMN_TIMESTAMP + " DESC";
		return databaseW.query(true,TABLE_NAME, null, username(), null,DbOpenHelper.COLUMN_JID,null, orderBy, null);
	}
	
	private static String username(){
		return DbOpenHelper.SIGNED_IN_ID+" = '"+Utility.getUserName()+"'" ;
	}

	/**
	 * Access all chat message from database.
	 */

	//SELECT * FROM table ORDER BY column DESC LIMIT 1;
	public static Cursor getAllMessagesOfParticipant(Jid jid) {
		//String orderBy = DbOpenHelper.COLUMN_TIMESTAMP + " DESC";
		String row = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ DbOpenHelper.COLUMN_JID + " = '" + jid + "'"+ andUserName();
		
		return databaseW.rawQuery(row, null);
	}

	public static Cursor getAllMessagesOfRoom(String room) {
		String row = "SELECT * FROM " + TABLE_GROUP + " WHERE "
				+ DbOpenHelper.COLUMN_ROOM + " = '" + room + "'"+andUserName();
		return databaseW.rawQuery(row, null);
	}
	
	
	/**
	 * Access all chat message from database.
	 */
	public static Cursor deleteUserChatHistory(String jid) {
		String row = "DELETE * FROM " + TABLE_NAME + " WHERE "
				+ DbOpenHelper.COLUMN_JID + " = '" + jid + "'"+andUserName();
		return databaseW.rawQuery(row, null);
	}

	/**
	 * Close the database.
	 */
	public static void closeDb() {
		try {
			if(databaseW.isOpen())
				databaseW.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static ContentValues composeValues(String jid, String name, String msg,String subject, byte[] image,String owner_or_participant,String messageStatus) {
		ContentValues values = new ContentValues();
		values.put(DbOpenHelper.SIGNED_IN_ID, Utility.getUserName());
		values.put(DbOpenHelper.COLUMN_JID, jid);
		values.put(DbOpenHelper.COLUMN_NAME, name);
		values.put(DbOpenHelper.COLUMN_MESSAGE, msg);
		values.put(DbOpenHelper.COLUMN_MESSAGE_SUBJECT, subject);
		values.put(DbOpenHelper.COLUMN_FILE_DOWNLOAD_STATUS, "0");
		values.put(DbOpenHelper.COLUMN_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
		values.put(DbOpenHelper.USER_IMAGE, image);
		values.put(DbOpenHelper.COLUMN_OWNER_OR_PARTICIPANT, owner_or_participant);
		values.put(DbOpenHelper.COLUMN_MESSAGE_STATUS, messageStatus);
		return values;
	}
	
	private static ContentValues composeGroupValues(String groupName,String jid,String msg) {
		ContentValues values = new ContentValues();
		values.put(DbOpenHelper.SIGNED_IN_ID, Utility.getUserName());
		values.put(DbOpenHelper.COLUMN_ROOM, groupName);
		values.put(DbOpenHelper.COLUMN_JID, jid);
		values.put(DbOpenHelper.COLUMN_MESSAGE, msg);
		values.put(DbOpenHelper.COLUMN_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
		return values;
	}

	/**
	 * Access all chat message from database.
	 */
	public static void leaveGroup(String jid) {
		SQLiteDatabase db = ThatItApplication.getApplication().openOrCreateDatabase(DbOpenHelper.DATABASE_NAME, SQLiteDatabase.CREATE_IF_NECESSARY, null);		
		try {
			db.delete(TABLE_GROUP, DbOpenHelper.COLUMN_ROOM+" = '"+jid+"'" +andUserName(), null);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
	}

	public void removeFriendDataIfExists(String friend_jid) {
		SQLiteDatabase db = ThatItApplication.getApplication().openOrCreateDatabase(DbOpenHelper.DATABASE_NAME, SQLiteDatabase.CREATE_IF_NECESSARY, null);		
		try {
			db.delete(TABLE_ROSTER, DbOpenHelper.JABBER_ID+" = '"+friend_jid+"'"  , null);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

	public boolean isFriendExists(String sender) {
		
		if(sender.contains("@"))
		sender = sender.substring(0, sender.indexOf("@"));
		
		try {
			Cursor friend_jid = databaseW.query(TABLE_ROSTER, null, DbOpenHelper.JABBER_ID+" = "+sender, null,null, null, null);
			friend_jid.moveToFirst();
			return friend_jid.getColumnCount()>0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// NOT USED FOR NOW
	public static Cursor getSelectedMessagesOfParticipant(String jid,int limit) {
		String row = "SELECT * FROM " + TABLE_NAME + " WHERE "
				+ DbOpenHelper.COLUMN_JID + " = '" + jid + "'"+ andUserName()+" LIMIT "+limit;
		return databaseW.rawQuery(row, null);
	}
}
