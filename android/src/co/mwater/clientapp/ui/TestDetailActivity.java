package co.mwater.clientapp.ui;

import java.text.DateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.SamplesTable;
import co.mwater.clientapp.db.SourcesTable;
import co.mwater.clientapp.db.TestsTable;
import co.mwater.clientapp.db.testresults.PetrifilmResults;
import co.mwater.clientapp.db.testresults.Results;
import co.mwater.clientapp.db.testresults.TestType;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public abstract class TestDetailActivity extends DetailActivity {
	@Override
	public void onPause() {
		super.onPause();
	
		if (rowValues != null) {
			// Save notes
			String curNotes = getControlText(R.id.notes);
			if (curNotes.length() == 0)
				curNotes = null;
	
			if (curNotes != rowValues.getAsString(TestsTable.COLUMN_NOTES)) {
				ContentValues values = new ContentValues();
				values.put(TestsTable.COLUMN_NOTES, curNotes);
				getContentResolver().update(uri, values, null, null);
			}
		}
	}

	protected void recordResultRead() {
		if (rowValues.getAsString(TestsTable.COLUMN_RESULTS) == null) {
			TestType testType = TestType.fromInt(rowValues.getAsInteger(TestsTable.COLUMN_TEST_TYPE));

			// Record initial results
			ContentValues update = new ContentValues();
			update.put(TestsTable.COLUMN_RESULTS, Results.getResults(testType, null).toJson());
			update.put(TestsTable.COLUMN_READ_ON, System.currentTimeMillis() / 1000);
	
			getContentResolver().update(uri, update, null, null);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.test_detail_menu, menu);
	
		menu.findItem(R.id.menu_delete).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				deleteTest();
				return true;
			}
		});
		menu.findItem(R.id.menu_delete).setEnabled(isCreatedByMe());

		return super.onCreateOptionsMenu(menu);
	}

	public abstract void onRecordResultsClick(View v);
	
	@Override
	protected void displayData() {
		getSupportActionBar().setTitle("Test " + rowValues.getAsString(TestsTable.COLUMN_CODE));

		// Get results
		String results = rowValues.getAsString(TestsTable.COLUMN_RESULTS);
		((Button) findViewById(R.id.record_results)).setText(results != null ? "Edit Results" : "Record Results");

		Long started_on = rowValues.getAsLong(TestsTable.COLUMN_STARTED_ON);
		if (started_on != null) {
			setControlText(R.id.started_on, "Started: " + DateFormat.getDateTimeInstance().format(new Date(started_on * 1000)));
		}
		Long read_on = rowValues.getAsLong(TestsTable.COLUMN_READ_ON);
		if (read_on != null) {
			setControlText(R.id.read_on, "Read: " + DateFormat.getDateTimeInstance().format(new Date(read_on * 1000)));
		}

		setControlText(R.id.notes, rowValues.getAsString(TestsTable.COLUMN_NOTES));
		setControlTextEditable(R.id.notes, isCreatedByMe());
		
		// Get sample
		String sampleUid = rowValues.getAsString(TestsTable.COLUMN_SAMPLE);
		ContentValues sample = null;
		ContentValues source = null;
		if (sampleUid != null)
		{
			sample = MWaterContentProvider.getSingleRow(this, MWaterContentProvider.SAMPLES_URI, sampleUid);
			String sourceUid = sample.getAsString(SamplesTable.COLUMN_SOURCE);
			if (sourceUid != null)
				source = MWaterContentProvider.getSingleRow(this, MWaterContentProvider.SOURCES_URI, sourceUid);
		}
		
		// TODO other options
		if (source != null && sample != null) {
			setControlText(R.id.source,
					String.format("%s sample %s", source.getAsString(SourcesTable.COLUMN_NAME), sample.getAsString(SamplesTable.COLUMN_CODE)));
		}
	}
	
	void deleteTest() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				getContentResolver().delete(uri, null, null);
				finish();
			}
		};
	
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Permanently delete test?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", null).show();
	}

}