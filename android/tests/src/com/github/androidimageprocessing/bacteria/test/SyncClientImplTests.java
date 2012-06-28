package com.github.androidimageprocessing.bacteria.test;

import java.util.UUID;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.github.androidimageprocessing.bacteria.dbsync.ChangeSet;
import com.github.androidimageprocessing.bacteria.dbsync.CompleteDataSlice;
import com.github.androidimageprocessing.bacteria.dbsync.DataSlice;
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
		assertEquals("2", changeSet.getUntil());
	}

	public void testGetChangeSetInsert() {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 0);
		ChangeSet changeSet = clientImpl.getChangeSet();
		assertNotNull(changeSet);
		
		// Check for one insert
		assertEquals(1, changeSet.getTables()[0].inserts.getCount());
		assertEquals(0, changeSet.getTables()[0].updates.getCount());
		assertEquals(0, changeSet.getTables()[0].deletes.getCount());
	}

	public void testGetChangeSetUpdate() {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 1);
		testSyncDatabase.update(uid, "changed", 1);
		
		ChangeSet changeSet = clientImpl.getChangeSet();
		assertNotNull(changeSet);
		
		// Check for one update
		assertEquals(0, changeSet.getTables()[0].inserts.getCount());
		assertEquals(1, changeSet.getTables()[0].updates.getCount());
		assertEquals(0, changeSet.getTables()[0].deletes.getCount());
	}

	public void testGetChangeSetDelete() {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 1);
		testSyncDatabase.update(uid, "changed", 1);
		testSyncDatabase.delete(uid);
		
		ChangeSet changeSet = clientImpl.getChangeSet();
		assertNotNull(changeSet);
		
		// Check for one delete
		assertEquals(0, changeSet.getTables()[0].inserts.getCount());
		assertEquals(0, changeSet.getTables()[0].updates.getCount());
		assertEquals(1, changeSet.getTables()[0].deletes.getCount());
	}

	public void testGetChangeSetUpdateAbsorbed() {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 0);
		testSyncDatabase.update(uid, "changed", 0);
		ChangeSet changeSet = clientImpl.getChangeSet();
		assertNotNull(changeSet);
		
		// Check for one insert alone (update should be folded in)
		assertEquals(1, changeSet.getTables()[0].inserts.getCount());
		assertEquals(0, changeSet.getTables()[0].updates.getCount());
		assertEquals(0, changeSet.getTables()[0].deletes.getCount());
	}

	public void testGetChangeSetDeleteAbsorbedInsert() {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase.insert(uid, 0);
		testSyncDatabase.delete(uid);
		ChangeSet changeSet = clientImpl.getChangeSet();
		assertNotNull(changeSet);
		
		// Check for zero inserts (delete should be folded in)
		assertEquals(0, changeSet.getTables()[0].inserts.getCount());
		assertEquals(0, changeSet.getTables()[0].updates.getCount());
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
		
		// Check that one update still left
		changeSet = clientImpl.getChangeSet();
		assertEquals(0, changeSet.getTables()[0].inserts.getCount());
		assertEquals(1, changeSet.getTables()[0].updates.getCount());
		assertEquals(0, changeSet.getTables()[0].deletes.getCount());
	}


	public void testApplyChangeSet() {
		fail("Not yet implemented");
	}

	public void testGetSinceEmpty() {
		assertNull(clientImpl.getSince(dataSlice));
	}

//	public void testGetSinceTwo() {
//		String uid = UUID.randomUUID().toString();
//		testSyncDatabase.insert(uid, 0);
//		testSyncDatabase.update(uid, "changed", 0);
//		assertEquals("1", clientImpl.getSince());
//	}

}
