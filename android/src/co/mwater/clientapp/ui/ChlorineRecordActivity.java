package co.mwater.clientapp.ui;

import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.RiskCalculations;
import co.mwater.clientapp.db.SamplesTable;
import co.mwater.clientapp.db.TestsTable;
import co.mwater.clientapp.db.testresults.ChlorineResults;
import co.mwater.clientapp.db.testresults.HundredMLEColiResults;
import co.mwater.clientapp.db.testresults.PetrifilmResults;
import co.mwater.clientapp.db.testresults.TenMLColilertResults;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class ChlorineRecordActivity extends DetailActivity {
	private static final String TAG = ChlorineRecordActivity.class.getSimpleName();
	CheckBox presentBox;
	Spinner mgPerLSpinner;
	TextView mgPerLCustom;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chlorine_results_activity);

		presentBox = (CheckBox) findViewById(R.id.present);
		mgPerLSpinner = (Spinner) findViewById(R.id.mgPerLSpinner);
		mgPerLCustom = (TextView) findViewById(R.id.mgPerLCustom);

		presentBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mgPerLSpinner.setVisibility(isChecked ? TextView.VISIBLE : TextView.INVISIBLE);
				mgPerLCustom.setVisibility(isChecked && mgPerLSpinner.getSelectedItemPosition() == 6 ? TextView.VISIBLE : TextView.INVISIBLE);
				findViewById(R.id.concentration_label).setVisibility(isChecked ? TextView.VISIBLE : TextView.INVISIBLE);
			}
		});
		mgPerLSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				updateCustomField();
			}

			public void onNothingSelected(AdapterView<?> parent) {
				updateCustomField();
			}
		});
	}

	void updateCustomField() {
		// Get results
		ChlorineResults results = new ChlorineResults(rowValues.getAsString(TestsTable.COLUMN_RESULTS));

		// Set custom
		if (mgPerLSpinner.getSelectedItemPosition() == 6)
		{
			mgPerLCustom.setVisibility(TextView.VISIBLE);
			mgPerLCustom.setText(results.mgPerL != null ? results.mgPerL.toString() : "");
		}
		else {
			mgPerLCustom.setVisibility(TextView.INVISIBLE);
		}
	}

	@Override
	protected void displayData() {
		getSupportActionBar().setTitle("Test " + rowValues.getAsString(TestsTable.COLUMN_CODE));

		// Get results
		ChlorineResults results = new ChlorineResults(rowValues.getAsString(TestsTable.COLUMN_RESULTS));

		// Set checkbox
		setControlBoolean(R.id.present, results.present == Boolean.TRUE);

		// Set spinner
		if (results.mgPerL == null)
			mgPerLSpinner.setSelection(0);
		else if (results.mgPerL == 0.1)
			mgPerLSpinner.setSelection(1);
		else if (results.mgPerL == 0.2)
			mgPerLSpinner.setSelection(2);
		else if (results.mgPerL == 0.3)
			mgPerLSpinner.setSelection(3);
		else if (results.mgPerL == 0.4)
			mgPerLSpinner.setSelection(4);
		else if (results.mgPerL == 0.5)
			mgPerLSpinner.setSelection(5);
		else
			mgPerLSpinner.setSelection(6);
	}

	@Override
	public void onPause() {
		super.onPause();

		// TODO Check for changes?

		// Get results
		ChlorineResults results = new ChlorineResults(rowValues.getAsString(TestsTable.COLUMN_RESULTS));

		if (presentBox.isChecked()) {
			results.present = true;

			// Save values
			switch (mgPerLSpinner.getSelectedItemPosition()) {
			case 0:
				results.mgPerL = null;
				break;
			case 1:
				results.mgPerL = 0.1;
				break;
			case 2:
				results.mgPerL = 0.2;
				break;
			case 3:
				results.mgPerL = 0.3;
				break;
			case 4:
				results.mgPerL = 0.4;
				break;
			case 5:
				results.mgPerL = 0.5;
				break;
			case 6:
				try {
					results.mgPerL = Double.parseDouble(mgPerLCustom.getText().toString());
				} catch (NumberFormatException ex) {
					Toast.makeText(this, "Invalid value. Value not saved.", Toast.LENGTH_LONG).show();
				}
				break;
			}
		}
		else {
			results.present = false;
			results.mgPerL = null;
		}

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
