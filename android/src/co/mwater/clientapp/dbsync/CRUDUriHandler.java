package co.mwater.clientapp.dbsync;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class CRUDUriHandler extends UriHandler {
	private static final String TAG = CRUDUriHandler.class.getCanonicalName();

	SyncContentProvider syncContentProvider;
	SyncDatabaseHelper helper;
	SyncTable syncTable;

	public CRUDUriHandler(SyncContentProvider syncContentProvider, SyncDatabaseHelper helper, SyncTable syncTable) {
		this.syncContentProvider = syncContentProvider;
		this.helper = helper;
		this.syncTable = syncTable;
	}

	@Override
	Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// Using SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// Set the table
		queryBuilder.setTables(syncTable.getTableName());

		// Check for appended id
		try {
			long id = Long.parseLong(uri.getLastPathSegment());

			// Add the ID to the original query
			queryBuilder.appendWhere(SyncTable.COLUMN_ID + "=" + id);
		} catch (NumberFormatException ex) {
		}

		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

		Log.d(TAG, String.format("query: %s", queryBuilder.toString()));
		
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(syncContentProvider.getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = helper.getWritableDatabase();

		long id = db.insert(syncTable.getTableName(), null, values);
		syncContentProvider.getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(uri + "/" + id);
	}

	@Override
	int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = helper.getWritableDatabase();

		// Check for appended id
		int rowsUpdated;
		try {
			long id = Long.parseLong(uri.getLastPathSegment());

			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = db.update(syncTable.getTableName(), values, SyncTable.COLUMN_ID + "=" + id, null);
			} else {
				rowsUpdated = db.update(syncTable.getTableName(), values, SyncTable.COLUMN_ID + "=" + id + " AND " + selection, selectionArgs);
			}
		} catch (NumberFormatException ex) {
			rowsUpdated = db.update(syncTable.getTableName(), values, selection, selectionArgs);
		}

		syncContentProvider.getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	@Override
	int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = helper.getWritableDatabase();

		// Check for appended id
		int rowsDeleted;
		try {
			long id = Long.parseLong(uri.getLastPathSegment());

			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = db.delete(syncTable.getTableName(), SyncTable.COLUMN_ID + "=" + id, null);
			} else {
				rowsDeleted = db.delete(syncTable.getTableName(), SyncTable.COLUMN_ID + "=" + id + " AND " + selection, selectionArgs);
			}
		} catch (NumberFormatException ex) {
			rowsDeleted = db.delete(syncTable.getTableName(), selection, selectionArgs);
		}

		syncContentProvider.getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}
}