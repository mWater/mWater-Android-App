package co.mwater.clientapp.test;

import co.mwater.clientapp.dbsync.DataSlicesTable;
import co.mwater.clientapp.dbsync.SyncChangesTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TestSyncDatabase {
	SQLiteDatabase db;
	Context context;

	SyncChangesTable syncChangesTable = new SyncChangesTable();
	DataSlicesTable syncStatusTable = new DataSlicesTable();

	TestSyncTable testSyncTable = new TestSyncTable();

	public SQLiteDatabase setUp(Context context) {
		this.context = context;
		// Create database
		context.deleteDatabase("test");
		db = context.openOrCreateDatabase("test", Context.MODE_PRIVATE, null);

		// Create changes table
		syncChangesTable.onCreate(db);
		syncStatusTable.onCreate(db);

		// Create test table
		testSyncTable.onCreate(db);
		
		return db;
	}

	public Cursor query() {
		return db.query(TestSyncTable.TABLE_NAME, null, null, null, null, null, null);
	}

	public void insert(String uid, int rowVersion) {
		ContentValues values = new ContentValues();
		values.put(TestSyncTable.COLUMN_UID, uid);
		values.put(TestSyncTable.COLUMN_ROW_VERSION, rowVersion);
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

	public void update(String uid, String a, int rowVersion) {
		ContentValues values = new ContentValues();
		values.put(TestSyncTable.COLUMN_ROW_VERSION, rowVersion);
		values.put(TestSyncTable.COLUMN_A, a);

		try {
			db.beginTransaction();
			db.update(testSyncTable.getTableName(), values, "uid=?", new String[] { uid });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public void delete(String uid) {
		try {
			db.beginTransaction();
			db.delete(testSyncTable.getTableName(), "uid=?", new String[] { uid });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public void tearDown() {
		context.deleteDatabase("test");
	}
}
