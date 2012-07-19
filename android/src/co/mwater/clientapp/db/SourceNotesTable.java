package co.mwater.clientapp.db;

import co.mwater.clientapp.dbsync.SyncTable;
import co.mwater.clientapp.dbsync.SyncTable.ForeignKey;

public class SourceNotesTable extends SyncTable {
	public static final String TABLE_NAME = "source_notes";
	public static final String COLUMN_SOURCE = "source";
	public static final String COLUMN_CREATED_ON = "created_on";
	public static final String COLUMN_OPERATIONAL = "operational";
	public static final String COLUMN_NOTE = "note";


	public SourceNotesTable() {
		super(new String[] { COLUMN_SOURCE, COLUMN_CREATED_ON, COLUMN_OPERATIONAL, COLUMN_NOTE, COLUMN_CREATED_BY },
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

				+ COLUMN_SOURCE + " text not null, "
				+ COLUMN_CREATED_ON + " integer, "
				+ COLUMN_OPERATIONAL + " integer, "
				+ COLUMN_NOTE + " text, "
				+ COLUMN_CREATED_BY + " text "
				+ ");";
	}
}
