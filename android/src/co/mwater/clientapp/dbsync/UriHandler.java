package co.mwater.clientapp.dbsync;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public abstract class UriHandler {
	abstract Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder);

	Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}

	int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}
}