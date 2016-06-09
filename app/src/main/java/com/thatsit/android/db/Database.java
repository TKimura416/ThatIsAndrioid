
package com.thatsit.android.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

abstract class Database {
	static SQLiteDatabase databaseW;
	private static SQLiteDatabase databaseR;
	static final String TABLE_NAME = DbOpenHelper.TABLE_NAME;
	static final String TABLE_GROUP = DbOpenHelper.TABLE_GROUP;
	static final String TABLE_ROSTER = DbOpenHelper.TABLE_ROSTER;

	Database(Context ctx) {
		DbOpenHelper helper = new DbOpenHelper(ctx);
		databaseW = helper.getWritableDatabase();
		databaseR = helper.getReadableDatabase();
	}
}
