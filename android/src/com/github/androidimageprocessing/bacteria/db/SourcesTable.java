package com.github.androidimageprocessing.bacteria.db;

import com.github.androidimageprocessing.bacteria.dbsync.SyncTable;

public class SourcesTable extends SyncTable {
	public static final String COLUMN_CODE = "code";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_DESC = "desc";
	public static final String COLUMN_LAT = "lat";
	public static final String COLUMN_LONG = "long";

	public SourcesTable() {
		super(new String[] { COLUMN_CODE, COLUMN_NAME, COLUMN_DESC, COLUMN_LAT, COLUMN_LONG } );
	}
	
	@Override
	public String getTableName() {
		return "sources";
	}

	@Override
	public String getCreateSql() {
		return "create table " 
				+ getTableName()
				+ "(" 
				+ COLUMN_ID + " integer primary key autoincrement, " 
				+ COLUMN_UID + " text unique default (lower(hex(randomblob(16)))), " 
				+ COLUMN_ROWVERSION + " integer default 0, " 
				+ COLUMN_CODE + " text not null, " 
				+ COLUMN_NAME + " text, "
				+ COLUMN_DESC + " text, "
				+ COLUMN_LAT + " numeric (9,6), "
				+ COLUMN_LONG + " numeric (9,6) "
				+ ");";	}
}
