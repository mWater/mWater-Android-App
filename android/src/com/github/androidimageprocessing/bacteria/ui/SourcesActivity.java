package com.github.androidimageprocessing.bacteria.ui;

import com.github.androidimageprocessing.bacteria.R;
import com.github.androidimageprocessing.bacteria.db.MWaterContentProvider;
import com.github.androidimageprocessing.bacteria.db.SourceCodes;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class SourcesActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final String TAG = SourcesActivity.class.getSimpleName();
	private static final int LOADER_ID = 0x01;
	private SimpleCursorAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sources);
		getSupportLoaderManager().initLoader(LOADER_ID, null, this);

		adapter = new SimpleCursorAdapter(this, R.layout.sources_row, null, 
				new String[] { "code", "name", "desc" }, 
				new int[] { R.id.code, R.id.name, R.id.desc },
				Adapter.NO_SELECTION);
		ListView listView = (ListView) findViewById(R.id.list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			// @Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				SourcesActivity.this.onItemClick(id);
			}
		});
		registerForContextMenu(listView);

//		// Add sample rows
//		Cursor c = getContentResolver().query(MWaterContentProvider.SOURCES_URI, null, null, null, null);
//		if (c.getCount() < 2) {
//			ContentValues cv = new ContentValues();
//			cv.put("code", "12345");
//			cv.put("name", "Tap #1");
//			cv.put("desc", "City center");
//			getContentResolver().insert(MWaterContentProvider.SOURCES_URI, cv);
//
//			cv.put("code", "12346");
//			cv.put("name", "River");
//			cv.put("desc", "Before intake");
//			getContentResolver().insert(MWaterContentProvider.SOURCES_URI, cv);
//		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.list) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			// TODO Add header
			//menu.setHeaderTitle(adapter.getItemId(info.position) + "");
			// TODO Add id
			menu.add(Menu.NONE, 0, 0, "Delete");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		int menuItemIndex = item.getItemId();
		if (menuItemIndex == 0) {
			Uri uri = Uri.withAppendedPath(MWaterContentProvider.SOURCES_URI, info.id + "");
			getContentResolver().delete(uri, null, null);
		}
		Toast.makeText(this, "Source deleted", Toast.LENGTH_SHORT).show();
		return true;
	}

	void onItemClick(long id) {
		editSource(id + "");
	}

	private void editSource(String id) {
		Intent intent = new Intent(this, SourceDetailActivity.class);
		intent.putExtra("id", id);
		startActivity(intent);
	}

	public void onNewSourceClick(View v) {
		// Create new row
		ContentValues cv = new ContentValues();
		cv.put("code", SourceCodes.getNewCode(this));
		Uri uri = getContentResolver().insert(MWaterContentProvider.SOURCES_URI, cv);
		editSource(uri.getLastPathSegment());
	}

	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return new CursorLoader(this, MWaterContentProvider.SOURCES_URI, null, null, null, null);
	}

	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		adapter.swapCursor(cursor);
	}

	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		adapter.swapCursor(null);
	}
}
