package com.github.androidimageprocessing.bacteria.test;

import com.github.androidimageprocessing.bacteria.dbsync.SyncTable;

public class TestSyncTable extends SyncTable {
	public static final String TABLE_NAME = "dbtest";
	public static final String COLUMN_A = "a";
	public static final String COLUMN_B = "b";

	public TestSyncTable() {
		super(new String[] { COLUMN_A, COLUMN_B });
	}
	
	@Override
	public String getTableName() {
		return TABLE_NAME;
	}

	@Override
	public String getCreateSql() {
		return "create table " + getTableName() 
				+ "(" + COLUMN_ID + " integer primary key autoincrement, " 
				+ COLUMN_UID + " text not null, " 
				+ COLUMN_ROW_VERSION + " integer not null default 0, "
				+ COLUMN_A + " text, " 
				+ COLUMN_B  + " text "
				+ ");";
	}
}