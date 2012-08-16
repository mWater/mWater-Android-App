package co.mwater.clientapp.petrifilmanalysis;

import java.io.FileOutputStream;
import java.io.IOException;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.RiskCalculations;
import co.mwater.clientapp.db.testresults.PetrifilmResults;
import co.mwater.clientapp.db.TestsTable;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class PetriFilmProcessingIntentService extends IntentService {
	private static final String TAG = PetriFilmProcessingIntentService.class.getCanonicalName();

	Uri testUri;
	String inImagePath;
	String outImagePath;

	public PetriFilmProcessingIntentService() {
		super("Petrifilm Processor");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		testUri = intent.getParcelableExtra("testUri");

		inImagePath = intent.getStringExtra("inImagePath");
		outImagePath = intent.getStringExtra("outImagePath");

		Log.d(TAG, "Processing " + inImagePath);

		// Process image
		PetrifilmImageProcessor processor = new PetrifilmImageProcessor();
		try {
			PetrifilmAnalysisResults results = processor.process(inImagePath);

			// Save processed image
			if (outImagePath != null)
			{
				FileOutputStream fos;
				fos = new FileOutputStream(outImagePath);
				fos.write(results.jpeg);
				fos.close();
			}
			
			// Record results
			ContentValues values = MWaterContentProvider.getSingleRow(this, testUri);
			if (values == null)
			{
				// Row deleted. Return
				return;
			}

			// TODO race condition?
			String resultsStr = values.getAsString(TestsTable.COLUMN_RESULTS);
			PetrifilmResults testResults = new PetrifilmResults(resultsStr);

			// Record results
			ContentValues update = new ContentValues();
			testResults.autoAlgo = 1;
			testResults.autoEcoli = results.ecoli;
			testResults.autoTC = results.tc;
			testResults.autoOther = results.other;
			update.put(TestsTable.COLUMN_RESULTS, testResults.toJson());

			getContentResolver().update(testUri, update, null, null);

			// Update risk of source
			RiskCalculations.updateSourceRiskForSample(this, values.getAsString(TestsTable.COLUMN_SAMPLE));
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}
}
