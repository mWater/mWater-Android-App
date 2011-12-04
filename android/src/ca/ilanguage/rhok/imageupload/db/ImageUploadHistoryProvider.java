package ca.ilanguage.rhok.imageupload.db;

import java.util.HashMap;

import ca.ilanguage.rhok.imageupload.db.ImageUploadHistoryDatabase.ImageUploadHistory;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import android.text.TextUtils;
import android.util.Log;

public class ImageUploadHistoryProvider extends ContentProvider {
	private static final String TAG = "ImageUploadHistoryProvider";

	private static final String DATABASE_NAME = ImageUploadHistoryDatabase.IMAGE_UPLOAD_HISTORY_TABLE_NAME
			+ ".db";
	private static final int DATABASE_VERSION = 1;

	private static HashMap<String, String> sImageUploadHistoryProjectionMap;

	// retrieve options?
	private static final int IMAGEUPLOADHISTORIES = 1;
	private static final int IMAGEUPLOADHISTORY_ID = 2;

	private static final UriMatcher sUriMatcher;


	private DatabaseHelper mOpenHelper;

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case IMAGEUPLOADHISTORIES:
			count = db.delete(
					ImageUploadHistoryDatabase.IMAGE_UPLOAD_HISTORY_TABLE_NAME,
					where, whereArgs);
			break;

		case IMAGEUPLOADHISTORY_ID:
			String itemId = uri.getPathSegments().get(1);
			count = db.delete(
					ImageUploadHistoryDatabase.IMAGE_UPLOAD_HISTORY_TABLE_NAME,
					ImageUploadHistory._ID
							+ "="
							+ itemId
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case IMAGEUPLOADHISTORIES:
		case IMAGEUPLOADHISTORY_ID:
			return ImageUploadHistory.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		// initial values are null

		// Validate the requested uri
		if (sUriMatcher.match(uri) != IMAGEUPLOADHISTORIES) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		// Make sure that the fields are all set
		if (values.containsKey(ImageUploadHistory.FILEPATH) == false) {
			values.put(ImageUploadHistory.FILEPATH, "none");
		}
		if (values.containsKey(ImageUploadHistory.METADATA) == false) {
			values.put(ImageUploadHistory.METADATA, "{}");
		}
		if (values.containsKey(ImageUploadHistory.UPLOADED) == false) {
			values.put(ImageUploadHistory.UPLOADED, "0");
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		// it seems suspicious to only be the content of PARENT_ENTRY, ah its
		// the nullcolumnhack
		long rowId = db.insert(
				ImageUploadHistoryDatabase.IMAGE_UPLOAD_HISTORY_TABLE_NAME,
				ImageUploadHistory.UPLOADED, values);
		if (rowId > 0) {
			Uri resultUri = ContentUris.withAppendedId(
					ImageUploadHistory.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(resultUri, null);
			return resultUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(ImageUploadHistoryDatabase.IMAGE_UPLOAD_HISTORY_TABLE_NAME);

		switch (sUriMatcher.match(uri)) {
		case IMAGEUPLOADHISTORIES:
			// gets a cursor of all rows, with all columns (all should be
			// entered into the projectionmap)
			qb.setProjectionMap(sImageUploadHistoryProjectionMap);
			break;

		case IMAGEUPLOADHISTORY_ID:
			qb.setProjectionMap(sImageUploadHistoryProjectionMap);
			// get the row (of selected columns in projetion, it should be all
			// of them) for that ID
			// gets a cursor of the row which matches the id from the uri
			qb.appendWhere(ImageUploadHistory._ID + "="
					+ uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = ImageUploadHistory.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		// Get the database and run the query
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, orderBy);

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO add modified set to current time in the values ?
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case IMAGEUPLOADHISTORIES:
			count = db.update(
					ImageUploadHistoryDatabase.IMAGE_UPLOAD_HISTORY_TABLE_NAME,
					values, selection, selectionArgs);
			break;

		case IMAGEUPLOADHISTORY_ID:
			String audiobookId = uri.getPathSegments().get(1);
			// set last modified to now.
			// Long now = Long.valueOf(System.currentTimeMillis());
			// values.put(ImageUploadHistory.LAST_MODIFIED, now);

			count = db.update(
					ImageUploadHistoryDatabase.IMAGE_UPLOAD_HISTORY_TABLE_NAME,
					values, ImageUploadHistory._ID
							+ "="
							+ audiobookId
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ')' : ""), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(ImageUploadHistoryDatabase.AUTHORITY,
				ImageUploadHistoryDatabase.IMAGE_UPLOAD_HISTORY_TABLE_NAME,
				IMAGEUPLOADHISTORIES);
		sUriMatcher.addURI(ImageUploadHistoryDatabase.AUTHORITY,
				ImageUploadHistoryDatabase.IMAGE_UPLOAD_HISTORY_TABLE_NAME
						+ "/#", IMAGEUPLOADHISTORY_ID);

		sImageUploadHistoryProjectionMap = new HashMap<String, String>();
		sImageUploadHistoryProjectionMap.put(ImageUploadHistory._ID,
				ImageUploadHistory._ID);

		sImageUploadHistoryProjectionMap.put(ImageUploadHistory.FILEPATH,
				ImageUploadHistory.FILEPATH);
		sImageUploadHistoryProjectionMap.put(ImageUploadHistory.METADATA,
				ImageUploadHistory.METADATA);
		sImageUploadHistoryProjectionMap.put(ImageUploadHistory.UPLOADED,
				ImageUploadHistory.UPLOADED);

	}

	/**
	 * From Google IO 2010 app best practices
	 */

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE "
					+ ImageUploadHistoryDatabase.IMAGE_UPLOAD_HISTORY_TABLE_NAME
					+ " (" + ImageUploadHistory._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ ImageUploadHistory.UPLOADED + " INTEGER,"
					+ ImageUploadHistory.FILEPATH + " TEXT,"
					+ ImageUploadHistory.METADATA + " TEXT" + ");");

			// TODO insert sample images and metadata here

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}

		
	}// end databasehelper

}
