package co.mwater.clientapp.dbsync;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import co.mwater.clientapp.dbsync.ChangeSet.Table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

public class SyncClientImpl implements SyncClient {
	SQLiteDatabase db;
	SyncTable[] syncTables;

	public SyncClientImpl(SQLiteDatabase db, SyncTable[] syncTables) {
		this.db = db;
		this.syncTables = syncTables;
	}

	public ChangeSet getChangeSet() {
		// Perform in transaction
		try {
			db.beginTransaction();

			// Get until value
			long until;
			Cursor untilCursor = db.rawQuery("SELECT MAX(" + SyncChangesTable.COLUMN_ID + ") FROM " + SyncChangesTable.TABLE_NAME, null);
			try {
				if (!untilCursor.moveToFirst() || untilCursor.isNull(0))
					return null;

				until = untilCursor.getLong(0);
			} finally {
				untilCursor.close();
			}

			// For each table
			ChangeSet.Table[] tableChangeSets = new ChangeSet.Table[syncTables.length];
			for (int i = 0; i < syncTables.length; i++) {
				// Get changes for table
				tableChangeSets[i] = getTableChangeSet(syncTables[i]);
			}

			db.setTransactionSuccessful();
			return new ChangeSet(until, tableChangeSets);
		} finally {
			db.endTransaction();
		}
	}

	private Table getTableChangeSet(SyncTable syncTable) {
		Table table = new Table();

		table.tableName = syncTable.getTableName();
		table.upserts = getUpserts(syncTable, table);
		table.deletes = getDeletes(syncTable, table);

		return table;
	}

	private Cursor getUpserts(SyncTable syncTable, Table table) {
		// Get updates
		// e.g.
		// SELECT uid, x, y FROM sometable
		// WHERE uid in (SELECT rowuid FROM syncchanges AS sc1
		// WHERE tablename=? AND action='U'
		// AND NOT EXISTS (SELECT NULL FROM syncchanges AS sc2 WHERE tablename=?
		// AND sc2.rowuid=sc1.rowuid AND action='D'))
		// TODO substite row names
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ").append(SyncTable.COLUMN_UID);

		// For each column
		for (String col : syncTable.getSyncColumns())
			sql.append(", ").append(col);

		sql.append(" FROM ").append(syncTable.getTableName());
		sql.append(" WHERE ").append(SyncTable.COLUMN_UID).append(" in (SELECT ");
		sql.append(SyncChangesTable.COLUMN_ROWUID).append(" FROM ").append(SyncChangesTable.TABLE_NAME);
		sql.append(" AS sc1 WHERE tablename=? AND (action='I' OR action='U'))");

		return db.rawQuery(sql.toString(), new String[] { syncTable.getTableName() });
	}

	private Cursor getDeletes(SyncTable syncTable, Table table) {
		// Get deletes
		// e.g.
		// SELECT DISTINCT rowuid FROM syncchanges
		// WHERE tablename=? AND action='D'
		// AND NOT EXISTS (SELECT NULL FROM FROM syncchanges AS sc1
		// WHERE tablename=? AND sc2.rowuid=sc1.rowuid AND action='I')
		// TODO substite row names
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT ").append(SyncChangesTable.COLUMN_ROWUID);
		sql.append(" AS ").append(SyncTable.COLUMN_UID).append(" FROM ").append(SyncChangesTable.TABLE_NAME);
		sql.append(" AS sc1 WHERE tablename=? AND action='D'");
		sql.append(" AND NOT EXISTS (SELECT NULL FROM ");
		sql.append(SyncChangesTable.TABLE_NAME).append(" AS sc2 WHERE tablename=? AND sc2.rowuid=sc1.rowuid AND action='I')");

		return db.rawQuery(sql.toString(), new String[] { syncTable.getTableName(), syncTable.getTableName() });
	}

	public void markChangeSetSent(long until) {
		// Clear all change rows before and including until
		db.delete(SyncChangesTable.TABLE_NAME, SyncChangesTable.COLUMN_ID + "<=?", new String[] { Long.toString(until) });
	}

	public void applyChangeSet(ChangeSet changeSet, DataSlice dataSlice) throws PendingChangesException {
		// Perform in transaction
		try {
			db.beginTransaction();

			// Check for changes
			Cursor changes = db.rawQuery("SELECT COUNT(*) FROM " + SyncChangesTable.TABLE_NAME, null);
			changes.moveToFirst();
			if (changes.getLong(0) > 0)
				throw new PendingChangesException();
			changes.close();

			// Apply upserts in topological order
			for (SyncTable syncTable : syncTables) {
				Table table = changeSet.getTable(syncTable.getTableName());
				if (table != null)
					applyUpserts(syncTable, table.upserts);
			}

			// Apply deletes in reverse topological order
			for (int i = syncTables.length - 1; i >= 0; i--) {
				SyncTable syncTable = syncTables[i];
				Table table = changeSet.getTable(syncTable.getTableName());
				if (table != null)
					applyDeletes(syncTable, table.deletes);
			}

			// Mark slice status
			ContentValues values = new ContentValues();
			values.put(DataSlicesTable.COLUMN_SERVERUNTIL, changeSet.getUntil());
			if (db.update(DataSlicesTable.TABLE_NAME, values, DataSlicesTable.COLUMN_ID + "=?", new String[] { dataSlice.getSliceId() }) == 0) {
				// Not found. Insert new row
				values.put(DataSlicesTable.COLUMN_ID, dataSlice.getSliceId());
				db.insert(DataSlicesTable.TABLE_NAME, null, values);
			}

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	void applyUpserts(SyncTable syncTable, Cursor upserts) {
		// Determine columns to ignore
		Set<String> colsIgnore = new HashSet<String>();
		for (String colName : upserts.getColumnNames())
			colsIgnore.add(colName);

		// Remove known columns
		colsIgnore.remove(SyncTable.COLUMN_UID);
		colsIgnore.remove(SyncTable.COLUMN_ROW_VERSION);
		for (String colName : syncTable.getSyncColumns())
			colsIgnore.remove(colName);

		if (upserts.moveToFirst()) {
			do {
				ContentValues values = new ContentValues();
				DatabaseUtils.cursorRowToContentValues(upserts, values);

				// Remove columns to ignore
				for (String colName : colsIgnore)
					values.remove(colName);

				// Attempt update
				// TODO updates uid to same value
				if (db.update(syncTable.getTableName(), values, SyncTable.COLUMN_UID + "=?", new String[] { values.getAsString(SyncTable.COLUMN_UID) }) == 0) {
					// Insert since not present
					db.insert(syncTable.getTableName(), null, values);
				}
			} while (upserts.moveToNext());
		}
	}

	void applyDeletes(SyncTable syncTable, Cursor deletes) {
		if (deletes.moveToFirst()) {
			do {
				String uid = deletes.getString(deletes.getColumnIndex(SyncTable.COLUMN_UID));

				// Set row version to -1 first
				ContentValues values = new ContentValues();
				values.put(SyncTable.COLUMN_ROW_VERSION, -1);

				db.update(syncTable.getTableName(), values, SyncTable.COLUMN_UID + "=?", new String[] { uid });
				db.delete(syncTable.getTableName(), SyncTable.COLUMN_UID + "=?", new String[] { uid });
			} while (deletes.moveToNext());
		}
	}

	public long getUntil(DataSlice dataSlice) {
		Cursor cursor = db.query(DataSlicesTable.TABLE_NAME, null, DataSlicesTable.COLUMN_ID + "=?", new String[] { dataSlice.getSliceId() }, null, null, null);
		if (!cursor.moveToFirst())
			return 0;
		return cursor.getLong(cursor.getColumnIndexOrThrow(DataSlicesTable.COLUMN_SERVERUNTIL));
	}
}
