package com.github.androidimageprocessing.bacteria.test;

import java.util.UUID;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.github.androidimageprocessing.bacteria.dbsync.ChangeSet;
import com.github.androidimageprocessing.bacteria.dbsync.CompleteDataSlice;
import com.github.androidimageprocessing.bacteria.dbsync.DataSlice;
import com.github.androidimageprocessing.bacteria.dbsync.RESTClient;
import com.github.androidimageprocessing.bacteria.dbsync.SyncClientImpl;
import com.github.androidimageprocessing.bacteria.dbsync.SyncServerException;
import com.github.androidimageprocessing.bacteria.dbsync.SyncServerImpl;
import com.github.androidimageprocessing.bacteria.dbsync.SyncTable;
import com.github.androidimageprocessing.bacteria.dbsync.Synchronizer;

public class SynchronizerTests extends AndroidTestCase {
	TestSyncDatabase testSyncDatabase1 = new TestSyncDatabase();
	TestSyncDatabase testSyncDatabase2 = new TestSyncDatabase();
	SQLiteDatabase db1, db2;
	SyncClientImpl clientImpl1, clientImpl2;
	SyncServerImpl serverImpl1, serverImpl2;
	Synchronizer sync1, sync2;
	DataSlice dataSlice = new CompleteDataSlice();

	@Override
	public void setUp() throws Exception {
		super.setUp();
		db1 = testSyncDatabase1.setUp(getContext());
		db2 = testSyncDatabase2.setUp(getContext());

		clientImpl1 = new SyncClientImpl(db1, new SyncTable[] { new TestSyncTable() });
		clientImpl2 = new SyncClientImpl(db2, new SyncTable[] { new TestSyncTable() });
		serverImpl1 = new SyncServerImpl("http://192.168.0.2:8000/mwater/sync/", "uidtest1");
		serverImpl2 = new SyncServerImpl("http://192.168.0.2:8000/mwater/sync/", "uidtest2");
		
		sync1 = new Synchronizer(clientImpl1, serverImpl1);
		sync2 = new Synchronizer(clientImpl2, serverImpl2);

		// Reset tests
		RESTClient restClient=new RESTClient("http://192.168.0.2:8000/mwater/sync/resettests");
		restClient.get();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		testSyncDatabase1.tearDown();
		testSyncDatabase2.tearDown();
	}

	public void testDontGetMyChanges() throws SyncServerException {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase1.insert(uid, 0);

		ChangeSet upcs = clientImpl1.getChangeSet();
		serverImpl1.uploadChangeSet(upcs);
		
		ChangeSet downcs = serverImpl1.downloadChangeSet(dataSlice, 0);
		assertEquals(0, downcs.getTable(TestSyncTable.TABLE_NAME).upserts.getCount());
	}

	public void testOtherGetsMyChanges() throws SyncServerException {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase1.insert(uid, 0);

		sync1.synchronize(dataSlice);
		sync2.synchronize(dataSlice);

		assertEquals(1, testSyncDatabase2.query().getCount());
	}

	public void testOtherGetsMyDeletes() throws SyncServerException {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase1.insert(uid, 0);

		sync1.synchronize(dataSlice);
		sync2.synchronize(dataSlice);

		testSyncDatabase1.delete(uid);

		sync1.synchronize(dataSlice);
		sync2.synchronize(dataSlice);
		
		assertEquals(0, testSyncDatabase2.query().getCount());
	}
}
