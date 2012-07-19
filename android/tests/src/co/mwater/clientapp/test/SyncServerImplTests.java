package co.mwater.clientapp.test;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.dbsync.ChangeSet;
import co.mwater.clientapp.dbsync.CompleteDataSlice;
import co.mwater.clientapp.dbsync.DataSlice;
import co.mwater.clientapp.dbsync.RESTClient;
import co.mwater.clientapp.dbsync.SyncClientImpl;
import co.mwater.clientapp.dbsync.SyncServerException;
import co.mwater.clientapp.dbsync.SyncServerImpl;
import co.mwater.clientapp.dbsync.SyncTable;
import co.mwater.clientapp.dbsync.Synchronizer;

public class SyncServerImplTests extends AndroidTestCase {
	TestSyncDatabase testSyncDatabase = new TestSyncDatabase();
	SQLiteDatabase db;
	SyncServerImpl serverImpl;
	DataSlice dataSlice = new CompleteDataSlice();

	static String serverAddr = MWaterServer.serverUrl;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		db = testSyncDatabase.setUp(getContext());

		RESTClient restClient = new RESTClient(serverAddr, null);
		restClient.get("resettests");

		String clientId = restClient.get("login", "username", "test", "password", "test");

		serverImpl = new SyncServerImpl(restClient, clientId);
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
