package com.thatsit.android.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbOpenHelper extends SQLiteOpenHelper {
	final String TAG = "DbOpenHelper";

	public static final  String DATABASE_NAME = "thats_it_database.db";
	private static final int DATABASE_VERSION = 1;
	static final String TABLE_NAME ="one2onechat" ;
	static final String TABLE_GROUP ="groupMaster" ;
	static final String TABLE_ROSTER ="rosterList" ;

	// Columns name
	public static final String COLUMN_ID = "id";
	public static final String SIGNED_IN_ID = "sId";
	public static final String COLUMN_JID = "jid";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_MESSAGE = "message";
	public static final String COLUMN_TIMESTAMP = "date";
	public static final String COLUMN_MESSAGE_SUBJECT = "subject";
	public static final String COLUMN_FILE_DOWNLOAD_STATUS = "filedownloaded";
	public static final String COLUMN_OWNER_OR_PARTICIPANT = "owner_or_participant";
	public static final String COLUMN_MESSAGE_STATUS = "messageStatus";
	public static final String USER_TYPE_OWNER = "owner";
	public static final String USER_IMAGE = "userimage";
	public static final String COLUMN_ROOM= "roomName";
	public static final String USER_TYPE_PARTICIPANT = "participant";
	public static final String FIRSTNAME = "firstname";
	public static final String LASTNAME = "lastname";
	public static final String JABBER_ID = "rosterId";
	public static final String PROFILE_PIC_URL = "rosterimage_url";
	public static final String PROFILE_PIC_BLOB = "rosterimage";
	
	public static final String CREATE_TABLE_ROSTER_ENTRY=
			"CREATE TABLE IF NOT EXISTS " + TABLE_ROSTER + " ( " +
			/*COLUMN_ID + " INTEGER PRIMARY KEY, " +*/
			JABBER_ID+ " TEXT NOT NULL PRIMARY KEY, " +
			FIRSTNAME + " TEXT, " +
			LASTNAME + " TEXT, " +
			PROFILE_PIC_URL+ " TEXT, " +
			PROFILE_PIC_BLOB + " BLOB "  +
			" )";

	public static String IS_DATA_COMPLETE= "is_data_complete";
	
	private final Context context;

	public DbOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
	}

	private void createTables(SQLiteDatabase db) {
		Log.d(TAG, "Creating DB...");
		db.execSQL(createTableCMD());
		db.execSQL(createGroupTable());
		db.execSQL(createTableRosterList());
	}

	/**
	 *
	 * @return table to store chat messages.
	 */
	private String createTableCMD(){

		String CHAT_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " +
				COLUMN_ID + " INTEGER PRIMARY KEY, " +
				SIGNED_IN_ID+ " TEXT NOT NULL, " +
				COLUMN_JID + " TEXT NOT NULL, " +
				COLUMN_NAME + " TEXT, " +
				COLUMN_MESSAGE + " TEXT, " +
				COLUMN_MESSAGE_SUBJECT + " TEXT, " +
				COLUMN_FILE_DOWNLOAD_STATUS + " TEXT, " +
				COLUMN_TIMESTAMP + " TEXT NOT NULL, "  +
				USER_IMAGE + " BLOB, "  +
				COLUMN_OWNER_OR_PARTICIPANT + " TEXT NOT NULL, " +
				COLUMN_MESSAGE_STATUS + " TEXT " +
				" )";
		return CHAT_TABLE_CREATE;
	}


	private String createTableRosterList(){
		return CREATE_TABLE_ROSTER_ENTRY;
	}

	/**
	 *
	 * @return table to store group messages
	 */
	private String createGroupTable(){
		String GROUP_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_GROUP + " ( " +
				COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				SIGNED_IN_ID+ " TEXT NOT NULL, " +
				COLUMN_ROOM + " TEXT, " +
				COLUMN_JID + " TEXT, " +
				COLUMN_MESSAGE + " TEXT, " +
				COLUMN_TIMESTAMP + " TEXT NOT NULL"  +
				")";
		return GROUP_TABLE_CREATE;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUP);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROSTER);
		onCreate(db);
	}

	public boolean deleteDatabase(){
		try {
			return context.deleteDatabase(DATABASE_NAME);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}