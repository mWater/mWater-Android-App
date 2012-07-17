package co.mwater.clientapp.ui;

import java.text.DateFormat;
import java.util.Date;

import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.SamplesTable;
import co.mwater.clientapp.db.SourceCodes;
import co.mwater.clientapp.db.SourcesTable;
import co.mwater.clientapp.db.TestsTable;
import co.mwater.clientapp.db.testresults.Results;
import co.mwater.clientapp.db.testresults.Risk;
import co.mwater.clientapp.ui.petrifilm.PetrifilmTestDetailsActivity;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import co.mwater.clientapp.R;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class TestListActivity extends SherlockFragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final String TAG = TestListActivity.class.getSimpleName();
	private static final int LOADER_ID = 0x01;
	private TestListAdapter adapter;

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
		return super.onCreateOptionsMenu(menu);
	}

	void onItemClick(long id) {
		editTest(id);
	}

	void editTest(long id) {
		// Get test
		Uri testUri = Uri.withAppendedPath(MWaterContentProvider.TESTS_URI, id + "");
		ContentValues testValues = MWaterContentProvider.getSingleRow(this, testUri);
		@SuppressWarnings("rawtypes")
		Class detailClass = TestActivities.getDetailActivity(testValues.getAsInteger(TestsTable.COLUMN_TEST_TYPE));
		if (detailClass != null) {
			Intent intent = new Intent(this, detailClass);
			intent.putExtra("uri", testUri);
			startActivity(intent);
		}
	}

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

class TestListAdapter extends CursorAdapter {
	public TestListAdapter(Context context, Cursor c) {
		super(context, c, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// Get sample uid
		String sampleUid = cursor.getString(cursor.getColumnIndex(TestsTable.COLUMN_SAMPLE));

		// Get sample
		ContentValues sample = null;
		ContentValues source = null;
		if (sampleUid != null)
		{
			sample = MWaterContentProvider.getSingleRow(context, MWaterContentProvider.SAMPLES_URI, sampleUid);

			// Get source uid
			String sourceUid = sample.getAsString(SamplesTable.COLUMN_SOURCE);

			// Get source
			if (sourceUid != null)
				source = MWaterContentProvider.getSingleRow(context, MWaterContentProvider.SOURCES_URI, sourceUid);
		}

		setControlText(view, R.id.source_name, source != null ? source.getAsString(SourcesTable.COLUMN_NAME) : "Unspecified Source");
		setControlText(view, R.id.code, cursor.getString(cursor.getColumnIndex(TestsTable.COLUMN_CODE)));

		String[] testTags = context.getResources().getStringArray(R.array.test_tags);
		String[] testTypes = context.getResources().getStringArray(R.array.test_types);

		int testType = cursor.getInt(cursor.getColumnIndex(TestsTable.COLUMN_TEST_TYPE));
		if (testType >= testTags.length) {
			// TODO prettify
			setControlText(view, R.id.tag, "???");
			setControlText(view, R.id.test_type, "???");
			((TextView) view.findViewById(R.id.tag)).setBackgroundColor(context.getResources().getColor(R.color.risk_unspecified));
		}
		else {
			setControlText(view, R.id.tag, testTags[testType]);
			setControlText(view, R.id.test_type, testTypes[testType]);

			Risk risk = Results.getResults(testType, cursor.getString(cursor.getColumnIndex(TestsTable.COLUMN_RESULTS))).getRisk();
			int riskColor = TestActivities.getRiskColor(risk);
			((TextView) view.findViewById(R.id.tag)).setBackgroundColor(context.getResources().getColor(riskColor));
		}

		int readOnCol = cursor.getColumnIndex(TestsTable.COLUMN_READ_ON);
		if (cursor.isNull(readOnCol)) {
			setControlText(view, R.id.started_on, "Pending");
		}
		else {
			long readOn = cursor.getLong(readOnCol);
			setControlText(view, R.id.started_on, DateFormat.getDateInstance().format(new Date(readOn * 1000)));
		}

		// setControlText(view, R.id.sample, sample != null ?
		// sample.getAsString(SamplesTable.COLUMN_CODE) : null);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		final View view = inflater.inflate(R.layout.test_row, parent, false);
		return view;
	}

	/**
	 * Convenience method to set the text of a text view
	 * 
	 * @param id
	 * @param text
	 */
	protected void setControlText(View view, int id, String text) {
		TextView textView = (TextView) view.findViewById(id);
		textView.setText(text != null ? text : "");
	}
}
