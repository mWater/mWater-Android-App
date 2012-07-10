package co.mwater.clientapp.dbsync;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.mwater.clientapp.dbsync.ChangeSet.Table;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MatrixCursor.RowBuilder;
import android.database.sqlite.SQLiteCursor;
import android.os.Build;

/**
 * Serializes change sets to and from JSON. 
 * @author Clayton
 *
 */
public class ChangeSetJsonSerializer {

	public JSONObject serialize(ChangeSet changeSet) throws JSONException {
		JSONObject jroot = new JSONObject();

		jroot.put("until", changeSet.getUntil());

		// Create tables array
		JSONArray jtables = new JSONArray();
		for (ChangeSet.Table table : changeSet.getTables()) {
			JSONObject jtable = new JSONObject();

			jtable.put("name", table.tableName);
			jtable.put("upserts", serialize(table.upserts));
			jtable.put("deletes", serialize(table.deletes));
			jtables.put(jtable);
		}
		jroot.put("tables", jtables);
		return jroot;
	}

	public ChangeSet deserialize(JSONObject jroot) throws JSONException {
		long until = jroot.getLong("until");

		ArrayList<Table> tables = new ArrayList<Table>();
		JSONArray jtables = jroot.getJSONArray("tables");
		for (int t = 0; t < jtables.length(); t++) {
			JSONObject jtable = jtables.getJSONObject(t);
			Table table = new Table();
			table.tableName = jtable.getString("name");
			table.upserts = deserializeCursor(jtable.getJSONObject("upserts"));
			table.deletes = deserializeCursor(jtable.getJSONObject("deletes"));
			tables.add(table);
		}
		return new ChangeSet(until, tables.toArray(new Table[tables.size()]));
	}

	Cursor deserializeCursor(JSONObject jcursor) throws JSONException {
		JSONArray jcols = jcursor.getJSONArray("cols");
		String[] cols = new String[jcols.length()];
		for (int i = 0; i < jcols.length(); i++)
			cols[i] = jcols.getString(i);

		// Create cursor
		MatrixCursor cursor = new MatrixCursor(cols);

		// Add rows
		JSONArray jrows = jcursor.getJSONArray("rows");
		for (int i = 0; i < jrows.length(); i++) {
			JSONArray jrow = jrows.getJSONArray(i);
			RowBuilder rowBuilder = cursor.newRow();
			for (int col = 0; col < jrow.length(); col++)
				rowBuilder.add(jrow.get(col));
		}
		return cursor;
	}

	JSONObject serialize(Cursor cursor) throws JSONException {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			return serializeNew(cursor);
		return serializeOld((SQLiteCursor)cursor);
	}

	@SuppressWarnings("deprecation")
	JSONObject serializeOld(SQLiteCursor cursor) throws JSONException {
		JSONArray jcols = new JSONArray(Arrays.asList(cursor.getColumnNames()));
		JSONArray jrows = new JSONArray();

		if (cursor.moveToFirst()) {
			do {
				JSONArray jrow = new JSONArray();
				for (int i = 0; i < cursor.getColumnCount(); i++) {
					if (cursor.isNull(i))
						jrow.put(null);
					else if (cursor.isLong(i))
						jrow.put(cursor.getLong(i));
					else if (cursor.isFloat(i))
						jrow.put(cursor.getDouble(i));
					else if (cursor.isString(i))
						jrow.put(cursor.getString(i));
					else
						throw new IllegalArgumentException("BLOB columns not supported");
				}
				jrows.put(jrow);
			} while (cursor.moveToNext());
		}

		JSONObject jcursor = new JSONObject();
		jcursor.put("cols", jcols);
		jcursor.put("rows", jrows);

		return jcursor;
	}

	@TargetApi(11)
	JSONObject serializeNew(Cursor cursor) throws JSONException {
		JSONArray jcols = new JSONArray(Arrays.asList(cursor.getColumnNames()));
		JSONArray jrows = new JSONArray();

		if (cursor.moveToFirst()) {
			do {
				JSONArray jrow = new JSONArray();
				for (int i = 0; i < cursor.getColumnCount(); i++) {
					switch (cursor.getType(i)) {
					case Cursor.FIELD_TYPE_NULL:
						jrow.put(null);
						break;
					case Cursor.FIELD_TYPE_INTEGER:
						jrow.put(cursor.getLong(i));
						break;
					case Cursor.FIELD_TYPE_FLOAT:
						jrow.put(cursor.getDouble(i));
						break;
					case Cursor.FIELD_TYPE_STRING:
						jrow.put(cursor.getString(i));
						break;
					case Cursor.FIELD_TYPE_BLOB:
						throw new IllegalArgumentException("BLOB columns not supported");
					}
				}
				jrows.put(jrow);
			} while (cursor.moveToNext());
		}

		JSONObject jcursor = new JSONObject();
		jcursor.put("cols", jcols);
		jcursor.put("rows", jrows);

		return jcursor;
	}
}
