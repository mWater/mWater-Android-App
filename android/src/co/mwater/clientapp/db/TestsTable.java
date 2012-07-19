package co.mwater.clientapp.db;

import co.mwater.clientapp.dbsync.SyncTable;
import co.mwater.clientapp.dbsync.SyncTable.ForeignKey;

public class TestsTable extends SyncTable {
	public static final String TABLE_NAME = "tests";
	public static final String COLUMN_SAMPLE = "sample";
	public static final String COLUMN_TEST_TYPE = "test_type";
	public static final String COLUMN_TEST_VERSION = "test_version";
	public static final String COLUMN_CODE = "code";
	public static final String COLUMN_STARTED_ON = "started_on";
	public static final String COLUMN_READ_ON = "read_on";
	public static final String COLUMN_RESULTS = "results";
	public static final String COLUMN_NOTES = "notes";
	public static final String COLUMN_PHOTO = "photo";

	public TestsTable() {
		super(new String[] { COLUMN_SAMPLE, COLUMN_TEST_TYPE, COLUMN_TEST_VERSION, COLUMN_CODE, COLUMN_STARTED_ON, COLUMN_READ_ON, COLUMN_RESULTS, COLUMN_NOTES, COLUMN_PHOTO, COLUMN_CREATED_BY },
				new ForeignKey[] { new ForeignKey(COLUMN_SAMPLE, SamplesTable.TABLE_NAME, SamplesTable.COLUMN_UID)});
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

				+ COLUMN_SAMPLE + " text, "
				+ COLUMN_TEST_TYPE + " integer not null, "
				+ COLUMN_TEST_VERSION + " integer not null, "
				+ COLUMN_CODE + " text not null, "
				+ COLUMN_STARTED_ON + " integer, "
				+ COLUMN_READ_ON + " integer, "
				+ COLUMN_RESULTS + " text, "
				+ COLUMN_NOTES + " text, "
				+ COLUMN_PHOTO + " text, "
				+ COLUMN_CREATED_BY + " text "
				+ ");";
	}
}
