package com.github.androidimageprocessing.bacteria.dbsync;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public abstract class SyncTable {
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_UID = "uid";
	public static final String COLUMN_ROWVERSION = "rowversion";

	private String[] syncColumns; // Columns, excluding any declared above
	
	public abstract String getTableName();
	public abstract String getCreateSql();
	
	public SyncTable(String[] syncColumns) {
		this.syncColumns = syncColumns;
	}
	
	public String[] getSyncColumns() {
		return syncColumns;
	}
	
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(getCreateSql());
		
		// Create trigger to synchronize
		database.execSQL(getInsertTriggerSql());
		database.execSQL(getUpdateTriggerSql());
		database.execSQL(getDeleteTriggerSql());
	}

	String getInsertTriggerSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TRIGGER IF NOT EXISTS ");
		sql.append("inserttrigger").append(getTableName());
		sql.append(" AFTER INSERT ON ").append(getTableName());
		sql.append(" WHEN new.").append(COLUMN_ROWVERSION).append("=0");
		sql.append(" BEGIN INSERT INTO ").append(SyncChangesTable.TABLE_NAME);
		sql.append(" (").append(SyncChangesTable.COLUMN_TABLENAME);
		sql.append(", ").append(SyncChangesTable.COLUMN_ROWUID);
		sql.append(", ").append(SyncChangesTable.COLUMN_ACTION).append(")");
		sql.append(" VALUES ('").append(getTableName()).append("',");
		sql.append(" new.").append(COLUMN_UID).append(",");
		sql.append(" 'I'); END");
		
		return sql.toString();
	}

	String getUpdateTriggerSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TRIGGER IF NOT EXISTS ");
		sql.append("updatetrigger").append(getTableName());
		sql.append(" AFTER UPDATE ON ").append(getTableName());
		sql.append(" WHEN new.").append(COLUMN_ROWVERSION).append("=old.").append(COLUMN_ROWVERSION);
		sql.append(" BEGIN INSERT INTO ").append(SyncChangesTable.TABLE_NAME);
		sql.append(" (").append(SyncChangesTable.COLUMN_TABLENAME);
		sql.append(", ").append(SyncChangesTable.COLUMN_ROWUID);
		sql.append(", ").append(SyncChangesTable.COLUMN_ACTION).append(")");
		sql.append(" VALUES ('").append(getTableName()).append("',");
		sql.append(" new.").append(COLUMN_UID).append(",");
		sql.append(" 'U'); END");
		
		return sql.toString();
	}
	
	String getDeleteTriggerSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TRIGGER IF NOT EXISTS ");
		sql.append("deletetrigger").append(getTableName());
		sql.append(" AFTER DELETE ON ").append(getTableName());
		sql.append(" WHEN old.").append(COLUMN_ROWVERSION).append(">=0");
		sql.append(" BEGIN INSERT INTO ").append(SyncChangesTable.TABLE_NAME);
		sql.append(" (").append(SyncChangesTable.COLUMN_TABLENAME);
		sql.append(", ").append(SyncChangesTable.COLUMN_ROWUID);
		sql.append(", ").append(SyncChangesTable.COLUMN_ACTION).append(")");
		sql.append(" VALUES ('").append(getTableName()).append("',");
		sql.append(" old.").append(COLUMN_UID).append(",");
		sql.append(" 'D'); END");
		
		return sql.toString();
	}

	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(getTableName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + getTableName());
		database.execSQL("DROP TRIGGER IF EXISTS inserttrigger" + getTableName());
		database.execSQL("DROP TRIGGER IF EXISTS updatetrigger" + getTableName());
		database.execSQL("DROP TRIGGER IF EXISTS deletetrigger" + getTableName());
		onCreate(database);
	}
}
