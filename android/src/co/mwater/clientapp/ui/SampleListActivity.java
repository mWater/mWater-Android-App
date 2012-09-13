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
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.db.OtherCodes;
import co.mwater.clientapp.db.SamplesTable;
import co.mwater.clientapp.db.SourcesTable;
import co.mwater.clientapp.db.TestsTable;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class SampleListActivity extends SherlockFragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final String TAG = SampleListActivity.class.getSimpleName();
	private static final int LOADER_ID = 0x01;
	private CursorAdapter adapter;
	String sourceUid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sample_list);

		sourceUid = getIntent().getStringExtra("sourceUid");
		if (sourceUid != null)
			adapter = new SampleListNoSourceAdapter(this, null);
		else
			adapter = new SampleListWithSourceAdapter(this, null);

		ListView listView = (ListView) findViewById(R.id.list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			// @Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				SampleListActivity.this.onItemClick(id);
			}
		});

		getSupportLoaderManager().initLoader(LOADER_ID, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.sample_list_menu, menu);

		menu.findItem(R.id.menu_new).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				createNewSample();
				return true;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

	void onItemClick(long id) {
		editSample(id);
	}

	void editSample(long id) {
		Intent intent = new Intent(this, SampleDetailActivity.class);
		intent.putExtra("uri", Uri.withAppendedPath(MWaterContentProvider.SAMPLES_URI, id + ""));
		startActivity(intent);
	}

	void createNewSample() {
		// Create sample linked to source
		ContentValues values = new ContentValues();
		values.put(SamplesTable.COLUMN_SOURCE, sourceUid);
		values.put(SamplesTable.COLUMN_CODE, OtherCodes.getNewSampleCode(this));
		values.put(SamplesTable.COLUMN_SAMPLED_ON, System.currentTimeMillis() / 1000);
		values.put(SamplesTable.COLUMN_CREATED_BY, MWaterServer.getUsername(this));
		Uri sampleUri = getContentResolver().insert(MWaterContentProvider.SAMPLES_URI, values);

		// View sample
		Intent intent = new Intent(this, SampleDetailActivity.class);
		intent.putExtra("uri", sampleUri);
		startActivity(intent);
	}

	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		if (sourceUid != null)
			return new CursorLoader(this, MWaterContentProvider.SAMPLES_URI, null, 
					SamplesTable.COLUMN_SOURCE + "=? AND " + SamplesTable.COLUMN_CREATED_BY + "=?", 
					new String[] { sourceUid, MWaterServer.getUsername(this) }, 
					null);
		else
			return new CursorLoader(this, MWaterContentProvider.SAMPLES_URI, null, 
					SamplesTable.COLUMN_CREATED_BY + "=?", 
					new String[] { MWaterServer.getUsername(this) }, 
					null);
	}

	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		adapter.swapCursor(cursor);
	}

	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		adapter.swapCursor(null);
	}
}
