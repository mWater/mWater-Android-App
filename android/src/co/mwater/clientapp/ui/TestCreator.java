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
import co.mwater.clientapp.db.OtherCodes;
import co.mwater.clientapp.db.SamplesTable;
import co.mwater.clientapp.db.TestsTable;
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
		switch (which) {
		case 0: // Petrifilm
			String sampleUid = null;
			if (sampleUri!=null)
				sampleUid = MWaterContentProvider.getSingleRow(context, sampleUri).getAsString(SamplesTable.COLUMN_UID);
			
			ContentValues values = new ContentValues();
			values.put(TestsTable.COLUMN_SAMPLE, sampleUid);
			values.put(TestsTable.COLUMN_CODE, OtherCodes.getNewCode(context));
			values.put(TestsTable.COLUMN_TEST_TYPE, 0);
			values.put(TestsTable.COLUMN_TEST_VERSION, 1);
			values.put(TestsTable.COLUMN_STARTED_ON, System.currentTimeMillis() / 1000);
			Uri testUri = context.getContentResolver().insert(MWaterContentProvider.TESTS_URI, values);

			Intent intent = new Intent(context, PetrifilmTestDetailActivity.class);
			intent.putExtra("uri", testUri);
			context.startActivity(intent);
			break;
		default:
			Toast.makeText(context, "To do", Toast.LENGTH_SHORT).show();
		}
	}
}
