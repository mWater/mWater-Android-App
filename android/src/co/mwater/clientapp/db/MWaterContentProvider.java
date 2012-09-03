package co.mwater.clientapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;

import co.mwater.clientapp.dbsync.CRUDUriHandler;
import co.mwater.clientapp.dbsync.SyncContentProvider;

public class MWaterContentProvider extends SyncContentProvider {
	public static String AUTHORITY = "co.mwater.clientapp";
	public static final Uri SOURCES_URI = Uri.parse("content://" + AUTHORITY + "/sources");
	public static final Uri SOURCE_NOTES_URI = Uri.parse("content://" + AUTHORITY + "/source_notes");
	public static final Uri SAMPLES_URI = Uri.parse("content://" + AUTHORITY + "/samples");
	public static final Uri TESTS_URI = Uri.parse("content://" + AUTHORITY + "/tests");

	@Override
	public boolean onCreate() {
		this.helper = MWaterDatabase.getDatabase(getContext());

		addUriHandler("sources", new CRUDUriHandler(this, helper, new SourcesTable()));
		addUriHandler("sources/#", new CRUDUriHandler(this, helper, new SourcesTable()));

		addUriHandler("source_notes", new CRUDUriHandler(this, helper, new SourceNotesTable()));
		addUriHandler("source_notes/#", new CRUDUriHandler(this, helper, new SourceNotesTable()));

		addUriHandler("samples", new CRUDUriHandler(this, helper, new SamplesTable()));
		addUriHandler("samples/#", new CRUDUriHandler(this, helper, new SamplesTable()));

		addUriHandler("tests", new CRUDUriHandler(this, helper, new TestsTable()));
		addUriHandler("tests/#", new CRUDUriHandler(this, helper, new TestsTable()));

		return true;
	}

	@Override
	protected String getAuthority() {
		return AUTHORITY;
	}

	/**
	 * Convenience method to lookup a single row's contents
	 * 
	 * @param context
	 * @param uri
	 * @return
	 */
	public static ContentValues getSingleRow(Context context, Uri uri) {
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if (!cursor.moveToFirst())
			return null;

		// Load fields
		ContentValues values = new ContentValues();
		DatabaseUtils.cursorRowToContentValues(cursor, values);
		cursor.close();

		return values;
	}

	/**
	 * Convenience method to lookup a single row's contents by uid
	 * 
	 * @param context
	 * @param uri
	 * @return
	 */
	public static ContentValues getSingleRow(Context context, Uri uri, String uid) {
		Cursor cursor = context.getContentResolver().query(uri, null, "uid=?", new String[] { uid }, null);
		if (!cursor.moveToFirst())
			return null;

		// Load fields
		ContentValues values = new ContentValues();
		DatabaseUtils.cursorRowToContentValues(cursor, values);
		cursor.close();

		return values;
	}

}
