package co.mwater.clientapp.dbsync;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public abstract class SyncContentProvider extends ContentProvider {
	protected SyncDatabaseHelper helper;
	UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	List<UriHandler> uriHandlers;

	public SyncContentProvider() {
		uriHandlers = new ArrayList<UriHandler>();
	}

	@Override
	public abstract boolean onCreate();

	protected abstract String getAuthority();

	// protected abstract SyncDatabaseHelper createHelper(Context context);

	protected void addUriHandler(String path, UriHandler uriHandler) {
		uriMatcher.addURI(getAuthority(), path, uriHandlers.size());
		uriHandlers.add(uriHandler);
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		int match = uriMatcher.match(uri);
		if (match >= 0 && match < uriHandlers.size())
			return uriHandlers.get(match).query(uri, projection, selection, selectionArgs, sortOrder);

		throw new IllegalArgumentException("Unknown URI: " + uri);
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int match = uriMatcher.match(uri);
		if (match >= 0 && match < uriHandlers.size())
			return uriHandlers.get(match).insert(uri, values);

		throw new IllegalArgumentException("Unknown URI: " + uri);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int match = uriMatcher.match(uri);
		if (match >= 0 && match < uriHandlers.size())
			return uriHandlers.get(match).update(uri, values, selection, selectionArgs);

		throw new IllegalArgumentException("Unknown URI: " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int match = uriMatcher.match(uri);
		if (match >= 0 && match < uriHandlers.size())
			return uriHandlers.get(match).delete(uri, selection, selectionArgs);

		throw new IllegalArgumentException("Unknown URI: " + uri);
	}

}
