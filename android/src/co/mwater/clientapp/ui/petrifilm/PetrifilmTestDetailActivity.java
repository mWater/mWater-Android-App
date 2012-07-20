package co.mwater.clientapp.ui.petrifilm;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

import com.actionbarsherlock.view.Window;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.SamplesTable;
import co.mwater.clientapp.db.SourcesTable;
import co.mwater.clientapp.db.TestsTable;
import co.mwater.clientapp.db.testresults.PetrifilmResults;
import co.mwater.clientapp.db.testresults.Results;
import co.mwater.clientapp.db.testresults.Risk;
import co.mwater.clientapp.petrifilmanalysis.PetriFilmProcessingIntentService;
import co.mwater.clientapp.petrifilmanalysis.PetrifilmImages;
import co.mwater.clientapp.ui.TestActivities;
import co.mwater.clientapp.ui.TestDetailActivity;


public class PetrifilmTestDetailActivity extends TestDetailActivity implements OnClickListener {
	private static final String TAG = PetrifilmTestDetailActivity.class.getSimpleName();
	static int PETRI_IMAGE_REQUEST = 1;

	boolean autoAnalysing = false; // TODO this could be done better

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setSupportProgressBarIndeterminateVisibility(false);
	
		setContentView(R.layout.petrifilm_detail_activity);
	}

	public void onRecordResultsClick(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Record Results");
		builder.setItems(R.array.petrifilm_record_popup, this).show();
	}

	@Override
	protected void displayData() {
		super.displayData();

		// Get results
		PetrifilmResults results = new PetrifilmResults(rowValues.getAsString(TestsTable.COLUMN_RESULTS));
		setControlInteger(R.id.ecoli_count, results.manualEcoli != null ? results.manualEcoli : results.autoEcoli);
		setControlInteger(R.id.tc_count, results.manualTC != null ? results.manualTC : results.autoTC);
		setControlInteger(R.id.other_count, results.manualOther != null ? results.manualOther : results.autoOther);

		autoAnalysing &= results.autoEcoli == null;

		Risk risk = results.getRisk(rowValues.getAsInteger(TestsTable.COLUMN_DILUTION));
		int riskColor = TestActivities.getRiskColor(risk);
		((TextView) this.findViewById(R.id.ecoli_count)).setBackgroundColor(this.getResources().getColor(riskColor));

		setSupportProgressBarIndeterminateVisibility(autoAnalysing);
	}
	
	public void onClick(DialogInterface dialog, int which) {
		if (which == 0) {
			// Automatic count
			String photoUid = UUID.randomUUID().toString().replace("-", "");
			Intent intent = new Intent(this, PetrifilmCameraActivity.class);
			intent.putExtra("filename", photoUid + ".jpg");
			startActivityForResult(intent, PETRI_IMAGE_REQUEST);
		}
		else if (which == 1) {
			// Record that result was read
			recordResultRead();

			Intent intent = new Intent(this, PetrifilmManualCountActivity.class);
			intent.putExtra("uri", uri);
			startActivity(intent);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PETRI_IMAGE_REQUEST && resultCode == RESULT_OK) {
			String filename = data.getStringExtra("filename");

			// Record that result was read
			recordResultRead();
			autoAnalysing = true;
			setSupportProgressBarIndeterminateVisibility(true);

			// TODO record photo uid

			// Send image to be processed and saved
			Intent intent = new Intent(this,
					PetriFilmProcessingIntentService.class);
			try {
				intent.putExtra("inImagePath", PetrifilmImages.getOriginalImageFolder(this) + File.separator + filename);
				intent.putExtra("outImagePath", PetrifilmImages.getProcessedImageFolder(this) + File.separator + filename);
				intent.putExtra("testUri", uri);
			} catch (IOException e) {
				// TODO Handle this exception
				Log.e(TAG, e.toString());
			}
			Log.d(TAG, "Calling process image");
			startService(intent);
		}
	}
}
