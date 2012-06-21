package com.github.androidimageprocessing.bacteria.test;

import java.util.UUID;

import com.github.androidimageprocessing.bacteria.db.SyncChangesTable;
import com.github.androidimageprocessing.bacteria.db.SyncTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import junit.framework.TestCase;

public class SyncTableTests extends AndroidTestCase {
	SQLiteDatabase db;
	SyncChangesTable syncChangesTable = new SyncChangesTable();
	TestSyncTable testSyncTable = new TestSyncTable();

	@Override
	public void setUp() throws Exception {
		super.setUp();

		// Create database
		db = getContext().openOrCreateDatabase("test", Context.MODE_PRIVATE,
				null);

		// Create changes table
		syncChangesTable.onCreate(db);

		// Create test table
		testSyncTable.onCreate(db);
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();

		getContext().deleteDatabase("test");
	}

	public void testInsertClient() {
		String uid = UUID.randomUUID().toString();
		insert(uid, 0);

		// Check that changes table contains single entry
		Cursor cursor = db.query(syncChangesTable.getTableName(), null, null,
				null, null, null, null);
		assertEquals(1, cursor.getCount());

		cursor.moveToFirst();
		assertEquals(testSyncTable.getTableName(), cursor.getString(cursor
				.getColumnIndex(SyncChangesTable.COLUMN_TABLENAME)));
		assertEquals(uid, cursor.getString(cursor
				.getColumnIndex(SyncChangesTable.COLUMN_ROWUID)));
		assertEquals("I", cursor.getString(cursor
				.getColumnIndex(SyncChangesTable.COLUMN_ACTION)));
		cursor.deactivate();
	}


	public void testInsertServer() {
		String uid = UUID.randomUUID().toString();
		insert(uid, 1);

		// Check that changes table contains no entry
		Cursor cursor = db.query(syncChangesTable.getTableName(), null, null,
				null, null, null, null);
		assertEquals(0, cursor.getCount());
		cursor.deactivate();
	}

	public void testDeleteClient() {
		String uid = UUID.randomUUID().toString();
		insert(uid, 0);
		delete(uid);

		// Check that changes table contains single entry
		Cursor cursor = db.query(syncChangesTable.getTableName(), null, 
				SyncChangesTable.COLUMN_ACTION + "=?",
				new String[] { "D" }, null, null, null);
		assertEquals(1, cursor.getCount());

		cursor.moveToFirst();
		assertEquals(testSyncTable.getTableName(), cursor.getString(cursor
				.getColumnIndex(SyncChangesTable.COLUMN_TABLENAME)));
		assertEquals(uid, cursor.getString(cursor
				.getColumnIndex(SyncChangesTable.COLUMN_ROWUID)));
		assertEquals("D", cursor.getString(cursor
				.getColumnIndex(SyncChangesTable.COLUMN_ACTION)));
		cursor.deactivate();
	}

	public void testDeleteServer() {
		String uid = UUID.randomUUID().toString();
		insert(uid, 0);
		update(uid, "apple", -1);
		delete(uid);

		// Check that changes table contains no entry
		Cursor cursor = db.query(syncChangesTable.getTableName(), null, 
				SyncChangesTable.COLUMN_ACTION + "=?",
				new String[] { "D" }, null, null, null);
		assertEquals(0, cursor.getCount());
		cursor.deactivate();
	}

	public void testUpdateClient() {
		String uid = UUID.randomUUID().toString();
		insert(uid, 1);
		update(uid, "change", 1);

		// Check that changes table contains single entry
		Cursor cursor = db.query(syncChangesTable.getTableName(), null, 
				SyncChangesTable.COLUMN_ACTION + "=?",
				new String[] { "U" }, null, null, null);
		assertEquals(1, cursor.getCount());

		cursor.moveToFirst();
		assertEquals(testSyncTable.getTableName(), cursor.getString(cursor
				.getColumnIndex(SyncChangesTable.COLUMN_TABLENAME)));
		assertEquals(uid, cursor.getString(cursor
				.getColumnIndex(SyncChangesTable.COLUMN_ROWUID)));
		assertEquals("U", cursor.getString(cursor
				.getColumnIndex(SyncChangesTable.COLUMN_ACTION)));
		cursor.deactivate();
	}

	public void testUpdateServer() {
		String uid = UUID.randomUUID().toString();
		insert(uid, 1);
		update(uid, "change", 2);

		// Check that changes table contains single entry
		Cursor cursor = db.query(syncChangesTable.getTableName(), null, 
				SyncChangesTable.COLUMN_ACTION + "=?",
				new String[] { "U" }, null, null, null);
		assertEquals(0, cursor.getCount());
		cursor.deactivate();
	}

	
	private void insert(String uid, int rowversion) {
		ContentValues values = new ContentValues();
		values.put(TestSyncTable.COLUMN_UID, uid);
		values.put(TestSyncTable.COLUMN_ROWVERSION, rowversion);
		values.put(TestSyncTable.COLUMN_A, "apple");
		values.put(TestSyncTable.COLUMN_B, "banana");

		try {
			db.beginTransaction();
			db.insert(testSyncTable.getTableName(), null, values);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	private void update(String uid, String a, int rowversion) {
		ContentValues values = new ContentValues();
		values.put(TestSyncTable.COLUMN_ROWVERSION, rowversion);
		values.put(TestSyncTable.COLUMN_A, a);

		try {
			db.beginTransaction();
			db.update(testSyncTable.getTableName(), values, "uid=?", new String[] { uid });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	private void delete(String uid) {
		try {
			db.beginTransaction();
			db.delete(testSyncTable.getTableName(), "uid=?", new String[] { uid });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

}

class TestSyncTable extends SyncTable {
	public static final String COLUMN_A = "a";
	public static final String COLUMN_B = "b";

	@Override
	public String getTableName() {
		return "test";
	}

	@Override
	public String getCreateSql() {
		return "create table " + getTableName() + "(" + COLUMN_ID
				+ " integer primary key autoincrement, " + COLUMN_UID
				+ " text not null, " + COLUMN_A + " text, " + COLUMN_B
				+ " text, " + COLUMN_ROWVERSION + " integer not null default 0"
				+ ");";
	}

}
