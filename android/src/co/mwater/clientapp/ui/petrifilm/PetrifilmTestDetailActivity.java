package co.mwater.clientapp.ui.petrifilm;

import java.io.IOException;
import java.util.UUID;

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
import android.widget.Toast;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.ImageStorage;
import co.mwater.clientapp.db.TestsTable;
import co.mwater.clientapp.db.testresults.PetrifilmResults;
import co.mwater.clientapp.db.testresults.Risk;
import co.mwater.clientapp.petrifilmanalysis.PetriFilmProcessingIntentService;
import co.mwater.clientapp.ui.TestActivities;
import co.mwater.clientapp.ui.TestDetailActivity;

import com.actionbarsherlock.view.Window;

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

	public void onPhotoClick(View v) {
		displayImage(TestsTable.COLUMN_PHOTO);
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

		// Display photo
		displayImageButton(R.id.photo, TestsTable.COLUMN_PHOTO, R.drawable.bact);
		
		// Enable/disable
		((Button)findViewById(R.id.record_results)).setEnabled(isCreatedByMe());
	}

	public void onClick(DialogInterface dialog, int which) {
		if (which == 0) {
			// Automatic count
			try {
				String photoUid = UUID.randomUUID().toString().replace("-", "");
				String photoPath = ImageStorage.getTempImagePath(this, photoUid);

				Intent intent = new Intent(this, PetrifilmCameraActivity.class);
				intent.putExtra("filepath", photoPath);
				intent.putExtra("uid", photoUid);

				startActivityForResult(intent, PETRI_IMAGE_REQUEST);
			} catch (IOException e) {
				Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
				return;
			}
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
			String photoUid = data.getStringExtra("uid");

			// Move image to pending
			try {
				ImageStorage.moveTempImageFileToPending(this, photoUid);

				// Set photo
				ContentValues update = new ContentValues();
				update.put(TestsTable.COLUMN_PHOTO, photoUid);
				getContentResolver().update(uri, update, null, null);

				// Record that result was read
				recordResultRead();
				autoAnalysing = true;
				setSupportProgressBarIndeterminateVisibility(true);

				// Send image to be processed
				Intent intent = new Intent(this,
						PetriFilmProcessingIntentService.class);
				intent.putExtra("inImagePath", ImageStorage.getPendingImagePath(this, photoUid));
				intent.putExtra("testUri", uri);

				Log.d(TAG, "Calling process image");
				startService(intent);
			} catch (IOException e) {
				Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			}
		}
	}
}
