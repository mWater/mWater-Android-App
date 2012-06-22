package com.github.androidimageprocessing.bacteria.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Table which stores information about data slices that 
 * have been downloaded
 * @author Clayton
 *
 */
public class DataSlicesTable {
	public static final String TABLE_NAME = "dataslices";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_SERVERUNTIL = "serveruntil";

	public String getTableName() { return TABLE_NAME; }
	
	public String getCreateSql() {
		return "create table " 
				+ TABLE_NAME
				+ " (" 
				+ COLUMN_ID + " text not null PRIMARY KEY, "
				+ COLUMN_SERVERUNTIL + " text not null"
				+ ");";
	}
	
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(getCreateSql());
	}

	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(getTableName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + getTableName());
		onCreate(database);
	}
}
