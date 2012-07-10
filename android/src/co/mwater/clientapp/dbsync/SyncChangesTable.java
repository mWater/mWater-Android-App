package co.mwater.clientapp.dbsync;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Table which stores a list of changes that have been made since the last upload
 * @author Clayton
 *
 */
public class SyncChangesTable {
	public static final String TABLE_NAME = "syncchanges";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_TABLENAME = "tablename";
	public static final String COLUMN_ROWUID = "rowuid";
	public static final String COLUMN_ACTION = "action";

	public String getTableName() { return TABLE_NAME; }
	
	public String getCreateSql() {
		return "create table " 
				+ TABLE_NAME
				+ "(" 
				+ COLUMN_ID + " integer primary key autoincrement, " 
				+ COLUMN_TABLENAME + " text not null, " 
				+ COLUMN_ROWUID + " text not null, "
				+ COLUMN_ACTION + " text not null"
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
