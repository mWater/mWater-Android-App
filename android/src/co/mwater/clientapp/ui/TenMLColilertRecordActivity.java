package co.mwater.clientapp.ui;

import android.content.ContentValues;
import android.os.Bundle;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.TestsTable;
import co.mwater.clientapp.db.testresults.PetrifilmResults;
import co.mwater.clientapp.db.testresults.TenMLColilertResults;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class TenMLColilertRecordActivity extends DetailActivity {
	private static final String TAG = TenMLColilertRecordActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ten_ml_colilert_results_activity);
	}

	@Override
	protected void displayData() {
		getSupportActionBar().setTitle("Test " + rowValues.getAsString(TestsTable.COLUMN_CODE));

		// Get results
		TenMLColilertResults results = new TenMLColilertResults(rowValues.getAsString(TestsTable.COLUMN_RESULTS));
		setControlBoolean(R.id.ecoli, results.ecoli);
		setControlBoolean(R.id.tc, results.tc);
	}

	@Override
	public void onPause() {
		super.onPause();

		// TODO Check for changes?

		// Get results
		TenMLColilertResults results = new TenMLColilertResults(rowValues.getAsString(TestsTable.COLUMN_RESULTS));

		// Save values
		results.ecoli = getControlBoolean(R.id.ecoli);
		results.tc = getControlBoolean(R.id.tc);
		
		ContentValues values = new ContentValues();
		values.put(TestsTable.COLUMN_RESULTS, results.toJson());
		getContentResolver().update(uri, values, null, null);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.ok_menu, menu);

		// Add listeners
		menu.findItem(R.id.menu_done).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				finish();
				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

}
