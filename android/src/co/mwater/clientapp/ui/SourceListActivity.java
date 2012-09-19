package co.mwater.clientapp.ui;

import java.util.Locale;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import co.mwater.clientapp.LocationFinder;
import co.mwater.clientapp.LocationFinder.LocationFinderListener;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.SourceCodes;
import co.mwater.clientapp.db.SourcesTable;
import co.mwater.clientapp.db.SourceCodes.NoMoreCodesException;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class SourceListActivity extends SherlockFragmentActivity implements LoaderManager.LoaderCallbacks<Cursor>, LocationFinderListener, OnNavigationListener {
	public static final String TAG = SourceListActivity.class.getSimpleName();
	private static final int LOADER_ID = 0x01;
	private SimpleCursorAdapter adapter;
	LocationFinder locationFinder;
	ActionBar actionBar;
	ProgressDialog progressDialog;
	String query = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.source_list);

		// Set up action bar
		actionBar = getSupportActionBar();
		// TODO re-add navigation changes once we store source creation date
		// actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		// SpinnerAdapter spinnerAdapter = new
		// SimpleSpinnerArrayAdapter(getSupportActionBar().getThemedContext(),
		// getResources().getStringArray(R.array.source_list_views));
		// actionBar.setListNavigationCallbacks(spinnerAdapter, this);

		adapter = new SimpleCursorAdapter(this, R.layout.source_row, null, new String[] { "code", "name", "desc" }, new int[] { R.id.code, R.id.name,
				R.id.desc }, Adapter.NO_SELECTION);
		ListView listView = (ListView) findViewById(R.id.list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			// @Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				SourceListActivity.this.onItemClick(id);
			}
		});

		// Get the intent, verify the action and get the query
		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			this.query = query;
		}

		// Set up location service
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationFinder = new LocationFinder(locationManager);

		// If location available, start loader, otherwise wait
		if (locationFinder.getLastLocation() != null)
			getSupportLoaderManager().initLoader(LOADER_ID, null, this);
		else {
			progressDialog = ProgressDialog.show(this, "Sources", "Waiting for location...");
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		locationFinder.addLocationListener(this);
	}

	@Override
	protected void onStop() {
		locationFinder.removeLocationListener(this);
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.source_list_menu, menu);

		menu.findItem(R.id.menu_new).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				createNewSource();
				return true;
			}
		});

		menu.findItem(R.id.menu_search).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				SourceListActivity.this.onSearchRequested();
				return true;
			}
		});

		menu.findItem(R.id.menu_refresh).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				if (getSupportLoaderManager().getLoader(LOADER_ID) != null)
					getSupportLoaderManager().restartLoader(LOADER_ID, null, SourceListActivity.this);
				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	void onItemClick(long id) {
		editSource(id);
	}

	void editSource(long id) {
		Intent intent = new Intent(this, SourceDetailActivity.class);
		intent.putExtra("uri", Uri.withAppendedPath(MWaterContentProvider.SOURCES_URI, id + ""));
		startActivity(intent);
	}

	void createNewSource() {
		if (!SourceCodes.anyCodesAvailable(this))
		{
			// TODO Obtain more source codes if needed
			SourceCreateDialog.showNoSourcesErrorDialog(this);
			return;
		}

		FragmentManager fm = getSupportFragmentManager();
		SourceCreateDialog createDialog = new SourceCreateDialog();
		createDialog.show(fm, "dialog_create");
	}

	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		// Get sort order
		String sort;
		// if (actionBar.getSelectedNavigationIndex() == 0)
		// {
		Location location = locationFinder.getLastLocation();
		sort = String.format(Locale.US, "(%1$s IS NULL) ASC, ((%1$s-(%2$f))*(%1$s-(%2$f))+(%3$s-(%4$f))*(%3$s-(%4$f)))",
				SourcesTable.COLUMN_LAT, location.getLatitude(), SourcesTable.COLUMN_LONG, location.getLongitude());
		// }
		// else {
		// sort = SourcesTable. String.format(Locale.US,
		// "(%1$s IS NULL) ASC, ((%1$s-(%2$f))*(%1$s-(%2$f))+(%3$s-(%4$f))*(%3$s-(%4$f)))",
		// SourcesTable.COLUMN_LAT, location.getLatitude(),
		// SourcesTable.COLUMN_LONG, location.getLongitude());
		// }

		// Add query if present
		if (query != null) {
			return new CursorLoader(this, MWaterContentProvider.SOURCES_URI, null,
					SourcesTable.COLUMN_CODE + " LIKE ? OR "
							+ SourcesTable.COLUMN_NAME + " LIKE ? OR "
							+ SourcesTable.COLUMN_DESC + " LIKE ?",
					new String[] { query + "%", query + "%", query + "%" }, sort);
		}

		return new CursorLoader(this, MWaterContentProvider.SOURCES_URI, null, null, null, sort);
	}

	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		adapter.swapCursor(cursor);
	}

	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		adapter.swapCursor(null);
	}

	public void onLocationChanged(Location location) {
		// Start loader if not already started
		if (getSupportLoaderManager().getLoader(LOADER_ID) == null)
			getSupportLoaderManager().initLoader(LOADER_ID, null, this);

		if (progressDialog != null)
		{
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// Reload list
		getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
		return true;
	}
}

class SimpleSpinnerArrayAdapter extends ArrayAdapter<String> implements SpinnerAdapter {
	public SimpleSpinnerArrayAdapter(Context ctx, String[] items) {
		super(ctx, R.layout.sherlock_spinner_item, items);
		this.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
	}
}