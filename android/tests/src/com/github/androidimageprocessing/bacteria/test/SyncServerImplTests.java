package com.github.androidimageprocessing.bacteria.test;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.github.androidimageprocessing.bacteria.dbsync.ChangeSet;
import com.github.androidimageprocessing.bacteria.dbsync.CompleteDataSlice;
import com.github.androidimageprocessing.bacteria.dbsync.DataSlice;
import com.github.androidimageprocessing.bacteria.dbsync.RESTClient;
import com.github.androidimageprocessing.bacteria.dbsync.SyncServerException;
import com.github.androidimageprocessing.bacteria.dbsync.SyncServerImpl;

public class SyncServerImplTests extends AndroidTestCase {
	TestSyncDatabase testSyncDatabase = new TestSyncDatabase();
	SQLiteDatabase db;
	SyncServerImpl serverImpl;
	DataSlice dataSlice = new CompleteDataSlice();

	@Override
	public void setUp() throws Exception {
		super.setUp();
		db = testSyncDatabase.setUp(getContext());

		RESTClient restClient=new RESTClient("http://192.168.0.2:8000/mwater/sync/resettests");
		restClient.get();

		restClient=new RESTClient("http://192.168.0.2:8000/mwater/sync/login");
		restClient.addParam("username", "test");
		restClient.addParam("password", "test");
		String clientId = restClient.get();

		serverImpl = new SyncServerImpl("http://192.168.0.2:8000/mwater/sync/", clientId);
}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		testSyncDatabase.tearDown();
	}

	public void testGetChangeSetEmpty() throws SyncServerException {
		ChangeSet cs = serverImpl.downloadChangeSet(dataSlice, 0);
		assertEquals(0, cs.getTable(TestSyncTable.TABLE_NAME).upserts.getCount());
		assertEquals(0, cs.getTable(TestSyncTable.TABLE_NAME).deletes.getCount());
	}
}
