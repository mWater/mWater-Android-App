package co.mwater.clientapp.ui.petrifilm;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.SamplesTable;
import co.mwater.clientapp.db.SourcesTable;
import co.mwater.clientapp.db.TestResults;
import co.mwater.clientapp.db.TestsTable;
import co.mwater.clientapp.petrifilmanalysis.PetriFilmProcessingIntentService;
import co.mwater.clientapp.petrifilmanalysis.PetrifilmImages;
import co.mwater.clientapp.ui.DetailActivity;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.actionbarsherlock.view.Window;

import co.mwater.clientapp.R;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class PetrifilmTestDetailsActivity extends DetailActivity implements OnClickListener {
	private static final String TAG = PetrifilmTestDetailsActivity.class.getSimpleName();
	static int PETRI_IMAGE_REQUEST = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setSupportProgressBarIndeterminateVisibility(false);

		setContentView(R.layout.petrifilm_detail_activity);
	}

	@Override
	protected void displayData() {
		getSupportActionBar().setTitle("Test " + rowValues.getAsString(TestsTable.COLUMN_CODE));

		// Get results
		String results = rowValues.getAsString(TestsTable.COLUMN_RESULTS);
		// TODO handle null to put blank
		if (results != null)
		{
			TestResults.Petrifilm pfr = TestResults.Petrifilm.fromJson(results);
			setControlInteger(R.id.ecoli_count, pfr.manualEcoli != null ? pfr.manualEcoli : pfr.autoEcoli);
			setControlInteger(R.id.tc_count, pfr.manualTC != null ? pfr.manualTC : pfr.autoTC);
			setControlInteger(R.id.other_count, pfr.manualOther != null ? pfr.manualOther : pfr.autoOther);

			setSupportProgressBarIndeterminateVisibility(pfr.autoEcoli == null);
		}
		else
			setSupportProgressBarIndeterminateVisibility(false);
		
		((Button)findViewById(R.id.record_results)).setText(results != null ? "Edit Results" : "Record Results");

		Long started_on = rowValues.getAsLong(TestsTable.COLUMN_STARTED_ON);
		if (started_on != null) {
			setControlText(R.id.started_on, "Started: " + DateFormat.getDateTimeInstance().format(new Date(started_on * 1000)));
		}
		Long read_on = rowValues.getAsLong(TestsTable.COLUMN_READ_ON);
		if (read_on != null) {
			setControlText(R.id.read_on, "Read: " + DateFormat.getDateTimeInstance().format(new Date(read_on * 1000)));
		}

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

	@Override
	public void onPause() {
		super.onPause();

		// Save notes
		// TODO
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

		return super.onCreateOptionsMenu(menu);
	}

	public void onRecordResultsClick(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Record Results");
		builder.setItems(R.array.petrifilm_record_popup, this).show();
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

	void recordResultRead() {
		if (rowValues.getAsString(TestsTable.COLUMN_RESULTS) == null) {
			// Record initial results
			ContentValues update = new ContentValues();
			update.put(TestsTable.COLUMN_RESULTS, TestResults.Petrifilm.toJson(new TestResults.Petrifilm()));
			update.put(TestsTable.COLUMN_READ_ON, System.currentTimeMillis() / 1000);

			getContentResolver().update(uri, update, null, null);
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
