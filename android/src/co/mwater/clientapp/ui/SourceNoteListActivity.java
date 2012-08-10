package co.mwater.clientapp.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.db.SourceNotesTable;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class SourceNoteListActivity extends SherlockFragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final String TAG = SourceNoteListActivity.class.getSimpleName();
	private static final int LOADER_ID = 0x01;
	private CursorAdapter adapter;
	String sourceUid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.source_note_list);

		sourceUid = getIntent().getStringExtra("sourceUid");
		adapter = new SourceNoteListAdapter(this, null);

		ListView listView = (ListView) findViewById(R.id.list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			// @Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				SourceNoteListActivity.this.onItemClick(id);
			}
		});

		getSupportLoaderManager().initLoader(LOADER_ID, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.source_note_list_menu, menu);

		menu.findItem(R.id.menu_new).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				createNewSourceNote();
				return true;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

	void onItemClick(long id) {
		editSourceNote(id);
	}

	void editSourceNote(long id) {
		Intent intent = new Intent(this, SourceNoteDetailActivity.class);
		intent.putExtra("uri", Uri.withAppendedPath(MWaterContentProvider.SOURCE_NOTES_URI, id + ""));
		startActivity(intent);
	}

	void createNewSourceNote() {
		// Create source note linked to source
		ContentValues values = new ContentValues();
		values.put(SourceNotesTable.COLUMN_SOURCE, sourceUid);
		values.put(SourceNotesTable.COLUMN_CREATED_ON, System.currentTimeMillis() / 1000);
		values.put(SourceNotesTable.COLUMN_CREATED_BY, MWaterServer.getUsername(this));
		Uri sourceNoteUri = getContentResolver().insert(MWaterContentProvider.SOURCE_NOTES_URI, values);

		// View source note
		Intent intent = new Intent(this, SourceNoteDetailActivity.class);
		intent.putExtra("uri", sourceNoteUri);
		startActivity(intent);
	}

	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return new CursorLoader(this, MWaterContentProvider.SOURCE_NOTES_URI, null, SourceNotesTable.COLUMN_SOURCE + "=?", new String[] { sourceUid }, null);
	}

	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		adapter.swapCursor(cursor);
	}

	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		adapter.swapCursor(null);
	}
}
