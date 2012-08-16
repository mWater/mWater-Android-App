package co.mwater.clientapp.ui.petrifilm;

import android.content.ContentValues;
import android.os.Bundle;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.RiskCalculations;
import co.mwater.clientapp.db.TestsTable;
import co.mwater.clientapp.db.testresults.PetrifilmResults;
import co.mwater.clientapp.ui.DetailActivity;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class PetrifilmManualCountActivity extends DetailActivity {
	private static final String TAG = PetrifilmManualCountActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.petrifilm_manual_count_activity);
	}

	@Override
	protected void displayData() {
		getSupportActionBar().setTitle("Test " + rowValues.getAsString(TestsTable.COLUMN_CODE));

		// Get results
		String results = rowValues.getAsString(TestsTable.COLUMN_RESULTS);
		PetrifilmResults pfr = new PetrifilmResults(results);

		setControlInteger(R.id.ecoli_count, pfr.manualEcoli != null ? pfr.manualEcoli : pfr.autoEcoli);
		setControlInteger(R.id.tc_count, pfr.manualTC != null ? pfr.manualTC: pfr.autoTC);
		setControlInteger(R.id.other_count, pfr.manualOther != null ? pfr.manualOther : pfr.autoOther);
	}

	@Override
	public void onPause() {
		super.onPause();

		// TODO Check for changes?

		// Get results
		String results = rowValues.getAsString(TestsTable.COLUMN_RESULTS);
		PetrifilmResults pfr = new PetrifilmResults(results);

		// Save values
		pfr.manualEcoli = getControlInteger(R.id.ecoli_count);
		pfr.manualTC = getControlInteger(R.id.tc_count);
		pfr.manualOther = getControlInteger(R.id.other_count);
		
		ContentValues values = new ContentValues();
		values.put(TestsTable.COLUMN_RESULTS, pfr.toJson());
		getContentResolver().update(uri, values, null, null);

		// Update risk of source
		RiskCalculations.updateSourceRiskForSample(this, rowValues.getAsString(TestsTable.COLUMN_SAMPLE));
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
