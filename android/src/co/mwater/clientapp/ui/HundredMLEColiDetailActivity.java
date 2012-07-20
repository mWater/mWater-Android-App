package co.mwater.clientapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.TestsTable;
import co.mwater.clientapp.db.testresults.HundredMLEColiResults;
import co.mwater.clientapp.db.testresults.Risk;
import co.mwater.clientapp.ui.petrifilm.PetrifilmManualCountActivity;

public class HundredMLEColiDetailActivity extends TestDetailActivity {
	private static final String TAG = HundredMLEColiDetailActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hundred_ml_ecoli_detail_activity);
	}

	public void onRecordResultsClick(View v) {
		recordResultRead();

		Intent intent = new Intent(this, HundredMLEColiRecordActivity.class);
		intent.putExtra("uri", uri);
		startActivity(intent);
	}

	@Override
	protected void displayData() {
		super.displayData();

		// Get results
		HundredMLEColiResults results = new HundredMLEColiResults(rowValues.getAsString(TestsTable.COLUMN_RESULTS));
		if (results.ecoli == null)
			setControlText(R.id.ecoli_count, "");
		else
			setControlText(R.id.ecoli_count, results.ecoli ? ">= 10 CFU/100mL" : "< 10 CFU/100mL");

		Risk risk = results.getRisk(rowValues.getAsInteger(TestsTable.COLUMN_DILUTION));
		int riskColor = TestActivities.getRiskColor(risk);
		((TextView) this.findViewById(R.id.ecoli_count)).setBackgroundColor(this.getResources().getColor(riskColor));
	}
}