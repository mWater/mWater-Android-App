package co.mwater.clientapp.ui;

import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.SourceCodes;
import co.mwater.clientapp.ui.petrifilm.PetrifilmTestDetailsActivity;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import co.mwater.clientapp.R;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class TestListActivity extends SherlockFragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final String TAG = TestListActivity.class.getSimpleName();
	private static final int LOADER_ID = 0x01;
	private SimpleCursorAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_list);

		adapter = new SimpleCursorAdapter(this, R.layout.test_row, null, new String[] { "code", "notes" }, new int[] { R.id.code, R.id.notes }, Adapter.NO_SELECTION);
		ListView listView = (ListView) findViewById(R.id.list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			// @Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				TestListActivity.this.onItemClick(id);
			}
		});

		getSupportLoaderManager().initLoader(LOADER_ID, null, this);
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getSupportMenuInflater().inflate(R.menu.sources_menu, menu);
//
//		menu.findItem(R.id.menu_new).setOnMenuItemClickListener(new OnMenuItemClickListener() {
//			public boolean onMenuItemClick(MenuItem item) {
//				createNewSource();
//				return true;
//			}
//		});
//		return super.onCreateOptionsMenu(menu);
//	}

	void onItemClick(long id) {
		editTest(id);
	}

	void editTest(long id) {
		// TODO different test types
		Intent intent = new Intent(this, PetrifilmTestDetailsActivity.class);
		intent.putExtra("uri", Uri.withAppendedPath(MWaterContentProvider.TESTS_URI, id + ""));
		startActivity(intent);
	}

//	void createNewSource() {
//		FragmentManager fm = getSupportFragmentManager();
//		SourceCreateDialog createDialog = new SourceCreateDialog();
//		createDialog.show(fm, "dialog_create");
//	}

	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return new CursorLoader(this, MWaterContentProvider.TESTS_URI, null, null, null, null);
	}

	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		adapter.swapCursor(cursor);
	}

	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		adapter.swapCursor(null);
	}
}
