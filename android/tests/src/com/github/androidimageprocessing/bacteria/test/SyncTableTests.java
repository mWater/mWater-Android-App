package com.github.androidimageprocessing.bacteria.test;

import java.util.UUID;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.github.androidimageprocessing.bacteria.dbsync.SyncChangesTable;

public class SyncTableTests extends AndroidTestCase {
	TestSyncDatabase testSyncDatabase = new TestSyncDatabase();
	SQLiteDatabase db;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		db = testSyncDatabase.setUp(getContext());
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		testSyncDatabase.tearDown();
	}

	public void testInsertClient() {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 0);

		// Check that changes table contains single entry
		Cursor cursor = db.query(SyncChangesTable.TABLE_NAME, null, null,
				null, null, null, null);
		assertEquals(1, cursor.getCount());

		cursor.moveToFirst();
		assertEquals(TestSyncTable.TABLE_NAME, cursor.getString(cursor
				.getColumnIndex(SyncChangesTable.COLUMN_TABLENAME)));
		assertEquals(uid, cursor.getString(cursor
				.getColumnIndex(SyncChangesTable.COLUMN_ROWUID)));
		assertEquals("I", cursor.getString(cursor
				.getColumnIndex(SyncChangesTable.COLUMN_ACTION)));
		cursor.deactivate();
	}


	public void testInsertServer() {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 1);

		// Check that changes table contains no entry
		Cursor cursor = db.query(SyncChangesTable.TABLE_NAME, null, null,
				null, null, null, null);
		assertEquals(0, cursor.getCount());
		cursor.deactivate();
	}

	public void testDeleteClient() {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 0);
		testSyncDatabase.delete(uid);

		// Check that changes table contains single entry
		Cursor cursor = db.query(SyncChangesTable.TABLE_NAME, null, 
				SyncChangesTable.COLUMN_ACTION + "=?",
				new String[] { "D" }, null, null, null);
		assertEquals(1, cursor.getCount());

		cursor.moveToFirst();
		assertEquals(TestSyncTable.TABLE_NAME, cursor.getString(cursor
				.getColumnIndex(SyncChangesTable.COLUMN_TABLENAME)));
		assertEquals(uid, cursor.getString(cursor
				.getColumnIndex(SyncChangesTable.COLUMN_ROWUID)));
		assertEquals("D", cursor.getString(cursor
				.getColumnIndex(SyncChangesTable.COLUMN_ACTION)));
		cursor.deactivate();
	}

	public void testDeleteServer() {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 0);
		testSyncDatabase.update(uid, "apple", -1);
		testSyncDatabase.delete(uid);

		// Check that changes table contains no entry
		Cursor cursor = db.query(SyncChangesTable.TABLE_NAME, null, 
				SyncChangesTable.COLUMN_ACTION + "=?",
				new String[] { "D" }, null, null, null);
		assertEquals(0, cursor.getCount());
		cursor.deactivate();
	}

	public void testUpdateClient() {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 1);
		testSyncDatabase.update(uid, "change", 1);

		// Check that changes table contains single entry
		Cursor cursor = db.query(SyncChangesTable.TABLE_NAME, null, 
				SyncChangesTable.COLUMN_ACTION + "=?",
				new String[] { "U" }, null, null, null);
		assertEquals(1, cursor.getCount());

		cursor.moveToFirst();
		assertEquals(TestSyncTable.TABLE_NAME, cursor.getString(cursor
				.getColumnIndex(SyncChangesTable.COLUMN_TABLENAME)));
		assertEquals(uid, cursor.getString(cursor
				.getColumnIndex(SyncChangesTable.COLUMN_ROWUID)));
		assertEquals("U", cursor.getString(cursor
				.getColumnIndex(SyncChangesTable.COLUMN_ACTION)));
		cursor.deactivate();
	}

	public void testUpdateServer() {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 1);
		testSyncDatabase.update(uid, "change", 2);

		// Check that changes table contains single entry
		Cursor cursor = db.query(SyncChangesTable.TABLE_NAME, null, 
				SyncChangesTable.COLUMN_ACTION + "=?",
				new String[] { "U" }, null, null, null);
		assertEquals(0, cursor.getCount());
		cursor.deactivate();
	}
}
