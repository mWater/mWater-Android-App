package co.mwater.clientapp.ui;

import java.text.DateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import co.mwater.clientapp.db.SamplesTable;
import co.mwater.clientapp.db.SourcesTable;
import co.mwater.clientapp.db.TestsTable;
import co.mwater.clientapp.ui.PreferenceWidget.OnChangeListener;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class SampleDetailActivity extends DetailActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String TAG = SampleDetailActivity.class.getSimpleName();
	private static final int LOADER_ID = 0x01;

	private TestListAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sample_detail_activity);

		adapter = new TestListAdapter(this, null);

		ListView listView = (ListView) findViewById(R.id.list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			// @Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				SampleDetailActivity.this.onItemClick(id);
			}
		});
		
		((PreferenceWidget) findViewById(R.id.desc)).setOnChangeListener(new OnChangeListener() {
			public void onChange(Object value) {
				SampleDetailActivity.this.updateRow(SamplesTable.COLUMN_DESC, value.toString());
			}
		});

		getSupportLoaderManager().initLoader(LOADER_ID, null, this);
	}

	void onItemClick(long id) {
		editTest(id);
	}
	
	public void onAddTestClick(View v) {
		new TestCreator(this, uri).create();
	}

	void editTest(long id) {
		TestActivities.editTest(this, id);
	}

	@Override
	protected void displayData() {
		getSupportActionBar().setTitle("Sample " + rowValues.getAsString(SamplesTable.COLUMN_CODE));

		Long sampled_on = rowValues.getAsLong(SamplesTable.COLUMN_SAMPLED_ON);
		if (sampled_on != null) {
			setControlText(R.id.sampled_on, DateFormat.getDateInstance().format(new Date(sampled_on * 1000)));
		}
		else {
			setControlText(R.id.sampled_on, "");
		}

		setPreferenceWidget(R.id.desc, "Description",
				rowValues.getAsString(SamplesTable.COLUMN_DESC), isCreatedByMe());

		// Get source
		String sourceUid = rowValues.getAsString(SamplesTable.COLUMN_SOURCE);
		ContentValues source = null;
		if (sourceUid != null)
			source = MWaterContentProvider.getSingleRow(this, MWaterContentProvider.SOURCES_URI, sourceUid);

		if (source != null) {
			setControlText(R.id.source_name, source.getAsString(SourcesTable.COLUMN_NAME));
			setControlText(R.id.source_code, source.getAsString(SourcesTable.COLUMN_CODE));
		}
		else {
			setControlText(R.id.source_name, "Unspecified source");
			setControlText(R.id.source_code, "");

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.sample_detail_menu, menu);

		menu.findItem(R.id.menu_delete).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				deleteSample();
				return true;
			}
		});
		menu.findItem(R.id.menu_delete).setEnabled(isCreatedByMe());

		return super.onCreateOptionsMenu(menu);
	}

	void deleteSample() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				getContentResolver().delete(uri, null, null);
				finish();
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Permanently delete sample and all its tests?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", null).show();
	}

	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return new CursorLoader(this, MWaterContentProvider.TESTS_URI, null, TestsTable.COLUMN_SAMPLE + "=?", new String[] {
				rowValues.getAsString(SamplesTable.COLUMN_UID) }, null);
	}

	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		adapter.swapCursor(cursor);
	}

	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		adapter.swapCursor(null);
	}
}
