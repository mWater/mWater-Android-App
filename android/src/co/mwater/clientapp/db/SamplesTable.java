package co.mwater.clientapp.db;

import co.mwater.clientapp.dbsync.SyncTable;

public class SamplesTable extends SyncTable {
	public static final String TABLE_NAME = "samples";
	public static final String COLUMN_SOURCE = "source";
	public static final String COLUMN_CODE = "code";
	public static final String COLUMN_DESC = "desc";
	public static final String COLUMN_SAMPLED_ON = "sampled_on";

	public SamplesTable() {
		super(new String[] { COLUMN_SOURCE, COLUMN_CODE, COLUMN_DESC, COLUMN_SAMPLED_ON, COLUMN_CREATED_BY },
				new ForeignKey[] { new ForeignKey(COLUMN_SOURCE, SourcesTable.TABLE_NAME, SourcesTable.COLUMN_UID)});
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

				+ COLUMN_SOURCE + " text, "
				+ COLUMN_CODE + " text not null, "
				+ COLUMN_DESC + " text, "
				+ COLUMN_SAMPLED_ON + " integer, "
				+ COLUMN_CREATED_BY + " text "
				+ ");";
	}
}
