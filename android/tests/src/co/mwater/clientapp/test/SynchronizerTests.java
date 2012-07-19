package co.mwater.clientapp.test;

import java.util.UUID;

import android.database.Cursor;
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

public class SynchronizerTests extends AndroidTestCase {
	TestSyncDatabase testSyncDatabase1 = new TestSyncDatabase();
	TestSyncDatabase testSyncDatabase2 = new TestSyncDatabase();
	SQLiteDatabase db1, db2;
	SyncClientImpl clientImpl1, clientImpl2;
	SyncServerImpl serverImpl1, serverImpl2;
	Synchronizer sync1, sync2;
	DataSlice dataSlice = new CompleteDataSlice();

	static String serverAddr = MWaterServer.serverUrl;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		db1 = testSyncDatabase1.setUp(getContext());
		db2 = testSyncDatabase2.setUp(getContext());

		RESTClient restClient = new RESTClient(serverAddr, null);
		restClient.get("resettests");

		String clientId1 = restClient.get("login", "username", "test", "password", "test");
		String clientId2 = restClient.get("login", "username", "test", "password", "test");

		clientImpl1 = new SyncClientImpl(db1, new SyncTable[] { new TestSyncTable() });
		clientImpl2 = new SyncClientImpl(db2, new SyncTable[] { new TestSyncTable() });
		serverImpl1 = new SyncServerImpl(restClient, clientId1);
		serverImpl2 = new SyncServerImpl(restClient, clientId2);

		sync1 = new Synchronizer(clientImpl1, serverImpl1);
		sync2 = new Synchronizer(clientImpl2, serverImpl2);
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

	public void testNullPreserved() throws SyncServerException {
		String uid = UUID.randomUUID().toString();
		testSyncDatabase1.insert(uid, 0);
		testSyncDatabase1.update(uid, null, 0);

		sync1.synchronize(dataSlice);
		sync2.synchronize(dataSlice);

		assertEquals(1, testSyncDatabase2.query().getCount());
		Cursor c = testSyncDatabase2.query();
		c.moveToFirst();
		assertTrue(c.isNull(c.getColumnIndex("a")));
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
