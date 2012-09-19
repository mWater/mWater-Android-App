package co.mwater.clientapp.ui;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.db.TestsTable;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class TestListActivity extends SherlockFragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final String TAG = TestListActivity.class.getSimpleName();
	private static final int LOADER_ID = 0x01;
	private CustomAdapter adapter;
	String query = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_list);

		adapter = new TestListAdapter(this, null);
		ListView listView = (ListView) findViewById(R.id.list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			// @Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				TestListActivity.this.onItemClick(id);
			}
		});

		// Get the intent, verify the action and get the query
		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			this.query = query;
		}

		getSupportLoaderManager().initLoader(LOADER_ID, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.test_list_menu, menu);

		menu.findItem(R.id.menu_new).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				new TestCreator(TestListActivity.this, null).create();
				return true;
			}
		});

		menu.findItem(R.id.menu_search).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				TestListActivity.this.onSearchRequested();
				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	void onItemClick(long id) {
		editTest(id);
	}

	void editTest(long id) {
		TestActivities.editTest(this, id);
	}

	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		if (query != null)
			return new CursorLoader(this, MWaterContentProvider.TESTS_URI, null,
					TestsTable.COLUMN_CREATED_BY + "=? AND "
					+ TestsTable.COLUMN_CODE + " LIKE ?",
					new String[] { 
						MWaterServer.getUsername(this),
						query + "%"
					},
					TestsTable.COLUMN_STARTED_ON + " DESC");

		return new CursorLoader(this, MWaterContentProvider.TESTS_URI, null,
				TestsTable.COLUMN_CREATED_BY + "=?",
				new String[] { MWaterServer.getUsername(this) },
				TestsTable.COLUMN_STARTED_ON + " DESC");
	}

	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		adapter.swapCursor(cursor);
	}

	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		adapter.swapCursor(null);
	}
}
