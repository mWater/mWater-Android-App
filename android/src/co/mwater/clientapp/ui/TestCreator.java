package co.mwater.clientapp.ui;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.db.OtherCodes;
import co.mwater.clientapp.db.SamplesTable;
import co.mwater.clientapp.db.TestsTable;
import co.mwater.clientapp.db.testresults.TestType;
import co.mwater.clientapp.ui.petrifilm.PetrifilmTestDetailActivity;

/**
 * Holds logic and UI to create a new test
 * 
 * @author Clayton
 * 
 */
public class TestCreator implements OnClickListener {
	Context context;
	Uri sampleUri;

	public TestCreator(Context context, Uri sampleUri) {
		this.context = context;
		this.sampleUri = sampleUri;
	}

	public void create() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Select Test Type");
		builder.setItems(R.array.test_types, this).show();
	}

	public void onClick(DialogInterface dialog, int which) {
		TestType testType;
		switch (which) {
		case 0:
			testType = TestType.PETRIFILM;
			break;
		case 1:
			testType = TestType.TEN_ML_COLILERT;
			break;
		case 2:
			testType = TestType.HUNDRED_ML_ECOLI;
			break;
		case 4:
			testType = TestType.CHLORINE;
			break;
		default:
			Toast.makeText(context, "To do", Toast.LENGTH_SHORT).show();
			return;
		}
		String sampleUid = null;
		if (sampleUri != null)
			sampleUid = MWaterContentProvider.getSingleRow(context, sampleUri).getAsString(SamplesTable.COLUMN_UID);

		ContentValues values = new ContentValues();
		values.put(TestsTable.COLUMN_SAMPLE, sampleUid);
		values.put(TestsTable.COLUMN_CODE, OtherCodes.getNewTestCode(context));
		values.put(TestsTable.COLUMN_TEST_TYPE, testType.getValue());
		values.put(TestsTable.COLUMN_TEST_VERSION, 1);
		values.put(TestsTable.COLUMN_STARTED_ON, System.currentTimeMillis() / 1000);
		values.put(TestsTable.COLUMN_CREATED_BY, MWaterServer.getUsername(context));
		Uri testUri = context.getContentResolver().insert(MWaterContentProvider.TESTS_URI, values);

		Intent intent = new Intent(context, TestActivities.getDetailActivity(testType));
		intent.putExtra("uri", testUri);
		context.startActivity(intent);
	}
}
