package co.mwater.clientapp.test;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import co.mwater.clientapp.dbsync.ChangeSet;
import co.mwater.clientapp.dbsync.ChangeSet.Table;
import co.mwater.clientapp.dbsync.ChangeSetJsonSerializer;
import co.mwater.clientapp.dbsync.CompleteDataSlice;
import co.mwater.clientapp.dbsync.DataSlice;
import co.mwater.clientapp.dbsync.SyncClientImpl;
import co.mwater.clientapp.dbsync.SyncTable;

public class ChangeSetJsonSerializerTests extends AndroidTestCase {
	TestSyncDatabase testSyncDatabase = new TestSyncDatabase();
	SQLiteDatabase db;
	SyncClientImpl clientImpl;
	DataSlice dataSlice = new CompleteDataSlice();
	ChangeSetJsonSerializer serializer = new ChangeSetJsonSerializer();

	@Override
	public void setUp() throws Exception {
		super.setUp();
		db = testSyncDatabase.setUp(getContext());
		clientImpl = new SyncClientImpl(db, new SyncTable[] { new TestSyncTable() });
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSerializeChangeSet() throws JSONException {
		String uid1 = "123";
		String uid2 = "456";
		testSyncDatabase.insert(uid1, 0);
		testSyncDatabase.insert(uid2, 1);
		testSyncDatabase.delete(uid2);

		JSONObject jcs = serializer.serialize(clientImpl.getChangeSet());
		assertEquals(
				"{\"tables\":[{\"upserts\":{\"cols\":[\"uid\",\"a\",\"b\"],\"rows\":[[\"123\",\"apple\",\"banana\"]]},\"deletes\":{\"cols\":[\"uid\"],\"rows\":[[\"456\"]]},\"name\":\"dbtest\"}],\"until\":2}",
				jcs.toString());
	}

	public void testDeserialize() throws JSONException {
		String json = "{\"tables\":[{\"upserts\":{\"cols\":[\"uid\",\"a\",\"b\"],\"rows\":[[\"123\",\"apple\",\"banana\"]]},\"deletes\":{\"cols\":[\"uid\"],\"rows\":[[\"456\"]]},\"name\":\"dbtest\"}],\"until\":2}";
		ChangeSet changeSet = serializer.deserialize(new JSONObject(json));
		assertEquals(1, changeSet.getTables().length);
		Table table = changeSet.getTables()[0];
		assertEquals(TestSyncTable.TABLE_NAME, table.tableName);
		assertEquals(1, table.upserts.getCount());
		assertEquals(1, table.deletes.getCount());
		assertEquals(3, table.upserts.getColumnCount());
		assertEquals(1, table.deletes.getColumnCount());
	}

}
