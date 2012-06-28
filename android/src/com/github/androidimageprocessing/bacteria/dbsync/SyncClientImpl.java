package com.github.androidimageprocessing.bacteria.dbsync;

import com.github.androidimageprocessing.bacteria.dbsync.ChangeSet.Table;

import android.database.Cursor;
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
			Cursor untilCursor = db.rawQuery("SELECT MAX(" + SyncChangesTable.COLUMN_ID + ") FROM " + SyncChangesTable.TABLE_NAME, null);
			if (!untilCursor.moveToFirst())
				return null;

			String until = untilCursor.getString(0);
			if (until == null)
				return null;

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

		table.inserts = getInserts(syncTable, table);
		table.updates = getUpdates(syncTable, table);
		table.deletes = getDeletes(syncTable, table);
		
		return table;
	}

	private Cursor getInserts(SyncTable syncTable, Table table) {
		// Get inserts
		// e.g.
		// SELECT uid, x, y FROM sometable
		// WHERE uid in (SELECT rowuid FROM syncchanges AS sc1
		// WHERE tablename=? AND action='I'
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
		sql.append(" AS sc1 WHERE tablename=? AND action='I'");
		sql.append(" AND NOT EXISTS (SELECT NULL FROM ");
		sql.append(SyncChangesTable.TABLE_NAME).append(" AS sc2 WHERE tablename=? AND sc2.");
		sql.append(SyncChangesTable.COLUMN_ROWUID).append("=sc1.").append(SyncChangesTable.COLUMN_ROWUID);
		sql.append(" AND action='D'))");

		return db.rawQuery(sql.toString(), new String[] { syncTable.getTableName(), syncTable.getTableName() });
	}

	private Cursor getUpdates(SyncTable syncTable, Table table) {
		// Get updates
		// e.g.
		// SELECT uid, x, y FROM sometable
		// WHERE uid in (SELECT rowuid FROM syncchanges AS sc1
		// WHERE tablename=? AND action='U'
		// AND NOT EXISTS (SELECT NULL FROM syncchanges AS sc2 WHERE tablename=?
		// AND sc2.rowuid=sc1.rowuid AND (action='D' OR action='I'))
		// TODO substite row names
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ").append(SyncTable.COLUMN_UID);

		// For each column
		for (String col : syncTable.getSyncColumns())
			sql.append(", ").append(col);

		sql.append(" FROM ").append(syncTable.getTableName());
		sql.append(" WHERE ").append(SyncTable.COLUMN_UID).append(" in (SELECT ");
		sql.append(SyncChangesTable.COLUMN_ROWUID).append(" FROM ").append(SyncChangesTable.TABLE_NAME);
		sql.append(" AS sc1 WHERE tablename=? AND action='U'");
		sql.append(" AND NOT EXISTS (SELECT NULL FROM ");
		sql.append(SyncChangesTable.TABLE_NAME).append(" AS sc2 WHERE tablename=? AND sc2.");
		sql.append(SyncChangesTable.COLUMN_ROWUID).append("=sc1.").append(SyncChangesTable.COLUMN_ROWUID);
		sql.append(" AND (action='D' OR action='I')))");

		return db.rawQuery(sql.toString(), new String[] { syncTable.getTableName(), syncTable.getTableName() });
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
		sql.append(" FROM ").append(SyncChangesTable.TABLE_NAME);
		sql.append(" AS sc1 WHERE tablename=? AND action='D'");
		sql.append(" AND NOT EXISTS (SELECT NULL FROM ");
		sql.append(SyncChangesTable.TABLE_NAME).append(" AS sc2 WHERE tablename=? AND sc2.rowuid=sc1.rowuid AND action='I')");

		return db.rawQuery(sql.toString(), new String[] { syncTable.getTableName(), syncTable.getTableName() });
	}

	public void markChangeSetSent(String until) {
		// TODO Auto-generated method stub
	}

	public void applyChangeSet(ChangeSet changeSet, DataSlice dataSlice) throws PendingChangesException {
		// TODO Auto-generated method stub

	}

	public String getSince(DataSlice dataSlice) {
		// TODO Auto-generated method stub
		return null;
	}

}
