package co.mwater.clientapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.TestsTable;
import co.mwater.clientapp.db.testresults.ChlorineResults;

public class ChlorineDetailActivity extends TestDetailActivity {
	private static final String TAG = ChlorineDetailActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chlorine_detail_activity);
	}

	public void onRecordResultsClick(View v) {
		recordResultRead();

		Intent intent = new Intent(this, ChlorineRecordActivity.class);
		intent.putExtra("uri", uri);
		startActivity(intent);
	}

	@Override
	protected void displayData() {
		super.displayData();

		// Get results
		ChlorineResults results = new ChlorineResults(rowValues.getAsString(TestsTable.COLUMN_RESULTS));
		if (results.present == null)
			setControlText(R.id.chlorine, "");
		else if (results.present == false)
			setControlText(R.id.chlorine, "Not Present");
		else
		{
			if (results.mgPerL == null)
				setControlText(R.id.chlorine, "Present");
			else
				setControlText(R.id.chlorine, results.mgPerL.toString() + " mg/L");
		}

		// Enable/disable
		((Button)findViewById(R.id.record_results)).setEnabled(isCreatedByMe());
	}
}