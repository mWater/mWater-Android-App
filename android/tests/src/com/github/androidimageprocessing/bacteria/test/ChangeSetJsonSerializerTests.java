package com.github.androidimageprocessing.bacteria.test;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.github.androidimageprocessing.bacteria.dbsync.ChangeSet;
import com.github.androidimageprocessing.bacteria.dbsync.ChangeSet.Table;
import com.github.androidimageprocessing.bacteria.dbsync.ChangeSetJsonSerializer;
import com.github.androidimageprocessing.bacteria.dbsync.CompleteDataSlice;
import com.github.androidimageprocessing.bacteria.dbsync.DataSlice;
import com.github.androidimageprocessing.bacteria.dbsync.SyncClientImpl;
import com.github.androidimageprocessing.bacteria.dbsync.SyncTable;

import junit.framework.TestCase;

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
				"{\"tables\":[{\"upserts\":{\"cols\":[\"uid\",\"a\",\"b\"],\"rows\":[[\"123\",\"apple\",\"banana\"]]},\"deletes\":{\"cols\":[\"uid\"],\"rows\":[[\"456\"]]},\"name\":\"test\"}],\"until\":2}",
				jcs.toString());
	}

	public void testDeserialize() throws JSONException {
		String json = "{\"tables\":[{\"upserts\":{\"cols\":[\"uid\",\"a\",\"b\"],\"rows\":[[\"123\",\"apple\",\"banana\"]]},\"deletes\":{\"cols\":[\"uid\"],\"rows\":[[\"456\"]]},\"name\":\"test\"}],\"until\":2}";
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
