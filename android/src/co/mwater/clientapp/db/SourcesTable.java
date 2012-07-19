package co.mwater.clientapp.db;

import co.mwater.clientapp.dbsync.SyncTable;

public class SourcesTable extends SyncTable {
	public static final String TABLE_NAME = "sources";
	public static final String COLUMN_CODE = "code";
	public static final String COLUMN_SOURCE_TYPE = "source_type";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_DESC = "desc";
	public static final String COLUMN_LAT = "latitude";
	public static final String COLUMN_LONG = "longitude";
	public static final String COLUMN_RISK = "risk";
	public static final String COLUMN_PHOTO = "photo";

	public SourcesTable() {
		super(new String[] { COLUMN_CODE, COLUMN_SOURCE_TYPE, COLUMN_NAME, COLUMN_DESC, COLUMN_LAT, COLUMN_LONG, COLUMN_RISK, COLUMN_PHOTO, COLUMN_CREATED_BY },
			new ForeignKey[] { });
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}

	@Override
	public String getCreateSql() {
		return "create table "
				+ getTableName()
				+ "("
				+ COLUMN_ID + " integer primary key autoincrement, "
				+ COLUMN_UID + " text unique default (lower(hex(randomblob(16)))), "
				+ COLUMN_ROW_VERSION + " integer default 0, "

				+ COLUMN_CODE + " text not null, "
				+ COLUMN_SOURCE_TYPE + " integer, "
				+ COLUMN_NAME + " text, "
				+ COLUMN_DESC + " text, "
				+ COLUMN_LAT + " numeric (9,6), "
				+ COLUMN_LONG + " numeric (9,6), "
				+ COLUMN_RISK + " integer, "
				+ COLUMN_PHOTO + " text, "
				+ COLUMN_CREATED_BY + " text"
				+ ");";
	}
}
