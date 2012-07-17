package co.mwater.clientapp.dbsync;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database helper that should be subclassed for specific database. If this will
 * be used by multiple classes, for example a content provider and a
 * synchronizer, it should contain a static getter.
 * 
 * @author Clayton
 * 
 */
public abstract class SyncDatabaseHelper extends SQLiteOpenHelper {
	SyncTable[] syncTables;

	public SyncDatabaseHelper(Context context, String name, int version, SyncTable[] syncTables) {
		super(context, name, null, version);
		this.syncTables = syncTables;
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (!db.isReadOnly()) {
			// Enable foreign key constraints
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}

	public SyncTable[] getSyncTables() {
		return syncTables;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		new DataSlicesTable().onCreate(db);
		new SyncChangesTable().onCreate(db);

		// Create sync tables
		for (SyncTable syncTable : syncTables)
			syncTable.onCreate(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		new DataSlicesTable().onUpgrade(db, oldVersion, newVersion);
		new SyncChangesTable().onUpgrade(db, oldVersion, newVersion);

		// Upgrade sync tables
		for (SyncTable syncTable : syncTables)
			syncTable.onUpgrade(db, oldVersion, newVersion);
	}
}
