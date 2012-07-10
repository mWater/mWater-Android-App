package com.github.androidimageprocessing.bacteria.test;

import java.util.UUID;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.github.androidimageprocessing.bacteria.dbsync.ChangeSet;
import com.github.androidimageprocessing.bacteria.dbsync.CompleteDataSlice;
import com.github.androidimageprocessing.bacteria.dbsync.DataSlice;
import com.github.androidimageprocessing.bacteria.dbsync.PendingChangesException;
import com.github.androidimageprocessing.bacteria.dbsync.SyncClientImpl;
import com.github.androidimageprocessing.bacteria.dbsync.SyncTable;

public class SyncClientImplTests extends AndroidTestCase {
	TestSyncDatabase testSyncDatabase = new TestSyncDatabase();
	SQLiteDatabase db;
	SyncClientImpl clientImpl;
	DataSlice dataSlice = new CompleteDataSlice();

	@Override
	public void setUp() throws Exception {
		super.setUp();
		db = testSyncDatabase.setUp(getContext());

		clientImpl = new SyncClientImpl(db, new SyncTable[] { new TestSyncTable() });
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		testSyncDatabase.tearDown();
	}

	public void testGetChangeSetEmpty() {
		assertNull(clientImpl.getChangeSet());
	}

	public void testGetChangeSetUntil() {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 0);
		testSyncDatabase.update(uid, "changed", 0);

		ChangeSet changeSet = clientImpl.getChangeSet();
		assertEquals(2, changeSet.getUntil());
	}

	public void testGetChangeSetInsert() {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 0);
		ChangeSet changeSet = clientImpl.getChangeSet();
		assertNotNull(changeSet);

		// Check for one upsert
		assertEquals(1, changeSet.getTables()[0].upserts.getCount());
		assertEquals(0, changeSet.getTables()[0].deletes.getCount());
	}

	public void testGetChangeSetUpdate() {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 1);
		testSyncDatabase.update(uid, "changed", 1);

		ChangeSet changeSet = clientImpl.getChangeSet();
		assertNotNull(changeSet);

		// Check for one upsert
		assertEquals(1, changeSet.getTables()[0].upserts.getCount());
		assertEquals(0, changeSet.getTables()[0].deletes.getCount());
	}

	public void testGetChangeSetDelete() {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 1);
		testSyncDatabase.update(uid, "changed", 1);
		testSyncDatabase.delete(uid);

		ChangeSet changeSet = clientImpl.getChangeSet();
		assertNotNull(changeSet);

		// Check for one delete (since row inserted was not new row)
		assertEquals(0, changeSet.getTables()[0].upserts.getCount());
		assertEquals(1, changeSet.getTables()[0].deletes.getCount());
	}

	public void testGetChangeSetUpdateAbsorbed() {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 0);
		testSyncDatabase.update(uid, "changed", 0);
		ChangeSet changeSet = clientImpl.getChangeSet();
		assertNotNull(changeSet);

		// Check for one upsert alone (update should be folded in)
		assertEquals(1, changeSet.getTables()[0].upserts.getCount());
		assertEquals(0, changeSet.getTables()[0].deletes.getCount());
	}

	public void testGetChangeSetDeleteAbsorbedInsert() {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 0);
		testSyncDatabase.delete(uid);
		ChangeSet changeSet = clientImpl.getChangeSet();
		assertNotNull(changeSet);

		// Check for zero inserts (delete should be folded in)
		assertEquals(0, changeSet.getTables()[0].upserts.getCount());
		assertEquals(0, changeSet.getTables()[0].deletes.getCount());
	}

	public void testMarkChangeSetSent() {
		// Do two updates
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 0);
		testSyncDatabase.update(uid, "changed1", 0);
		testSyncDatabase.update(uid, "changed2", 0);
		ChangeSet changeSet = clientImpl.getChangeSet();

		// Mark as sent
		clientImpl.markChangeSetSent(changeSet.getUntil());

		// Check that no changes still left
		changeSet = clientImpl.getChangeSet();
		assertNull(changeSet);
	}

	public void testMarkChangeSetPartiallySent() {
		// Do two updates
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 0);
		testSyncDatabase.update(uid, "changed1", 0);
		ChangeSet changeSet = clientImpl.getChangeSet();

		testSyncDatabase.update(uid, "changed2", 0);

		// Mark as sent
		clientImpl.markChangeSetSent(changeSet.getUntil());

		// Check that one update still left
		changeSet = clientImpl.getChangeSet();
		assertEquals(1, changeSet.getTables()[0].upserts.getCount());
		assertEquals(0, changeSet.getTables()[0].deletes.getCount());
	}

	public void testApplyChangeSetInsert() {
		// Create simple insert
		ChangeSet changeSet = createTestChangeSet(10);
		MatrixCursor upserts = (MatrixCursor) changeSet.getTable(TestSyncTable.TABLE_NAME).upserts;
		upserts.addRow(new Object[] { "001", 1, "cola", "colb" });

		// Apply
		try {
			clientImpl.applyChangeSet(changeSet, dataSlice);
		} catch (PendingChangesException e) {
			fail();
		}

		// Check that row was added
		Cursor query = testSyncDatabase.query();
		assertEquals(1, query.getCount());
		query.moveToFirst();
		assertEquals("cola", query.getString(query.getColumnIndex("a")));

		// Check that no changes
		assertNull(clientImpl.getChangeSet());

		// Check until
		assertEquals(10, clientImpl.getUntil(dataSlice));
	}

	public void testApplyChangeSetUpdate() {
		// Make change and pretend sent to server
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 0);
		clientImpl.markChangeSetSent(clientImpl.getChangeSet().getUntil());

		// Create delete
		ChangeSet changeSet = createTestChangeSet(10);
		MatrixCursor upserts = (MatrixCursor) changeSet.getTable(TestSyncTable.TABLE_NAME).upserts;
		upserts.addRow(new Object[] { uid, 2, "changed", "colb" });

		// Apply
		try {
			clientImpl.applyChangeSet(changeSet, dataSlice);
		} catch (PendingChangesException e) {
			fail();
		}

		// Check that row was updated
		Cursor query = testSyncDatabase.query();
		assertEquals(1, query.getCount());
		query.moveToFirst();
		assertEquals("changed", query.getString(query.getColumnIndex("a")));

		// Check that no changes
		assertNull(clientImpl.getChangeSet());
	}

	public void testApplyChangeSetDelete() {
		// Make change and pretend sent to server
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 0);
		clientImpl.markChangeSetSent(clientImpl.getChangeSet().getUntil());

		// Create delete
		ChangeSet changeSet = createTestChangeSet(10);
		MatrixCursor deletes = (MatrixCursor) changeSet.getTable(TestSyncTable.TABLE_NAME).deletes;
		deletes.addRow(new Object[] { uid });

		// Apply
		try {
			clientImpl.applyChangeSet(changeSet, dataSlice);
		} catch (PendingChangesException e) {
			fail();
		}

		// Check that row was deleted
		Cursor query = testSyncDatabase.query();
		assertEquals(0, query.getCount());

		// Check that no changes
		assertNull(clientImpl.getChangeSet());
	}

	public void testApplyChangeSetWithPending() {
		ChangeSet changeSet = createTestChangeSet(10);

		// Make change
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 0);

		// Apply
		try {
			clientImpl.applyChangeSet(changeSet, dataSlice);
			fail("Pending not thrown");
		} catch (PendingChangesException e) {
		}
	}

	public static ChangeSet createTestChangeSet(long until) {
		ChangeSet.Table table = new ChangeSet.Table();
		table.tableName = TestSyncTable.TABLE_NAME;
		table.upserts = new MatrixCursor(new String[] { "uid", "row_version", "a", "b" });
		table.deletes = new MatrixCursor(new String[] { "uid" });
		ChangeSet changeSet = new ChangeSet(until, new ChangeSet.Table[] { table });
		return changeSet;
	}

	public void testGetSinceEmpty() {
		assertEquals(0, clientImpl.getUntil(dataSlice));
	}

	public void testGetSinceAfterChanges() {
		// Create empty change set
		ChangeSet changeSet = createTestChangeSet(10);

		// Apply
		try {
			clientImpl.applyChangeSet(changeSet, dataSlice);
		} catch (PendingChangesException e) {
			fail();
		}
		assertEquals(10, clientImpl.getUntil(dataSlice));
	}

}
