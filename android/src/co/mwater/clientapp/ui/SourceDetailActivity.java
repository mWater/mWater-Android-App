package co.mwater.clientapp.ui;

import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.OtherCodes;
import co.mwater.clientapp.db.SamplesTable;
import co.mwater.clientapp.db.SourcesTable;
import co.mwater.clientapp.db.TestsTable;
import co.mwater.clientapp.ui.petrifilm.PetrifilmTestDetailsActivity;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import co.mwater.clientapp.R;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class SourceDetailActivity extends DetailActivity {
	public static final String TAG = SourceDetailActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.source_detail_activity);
	}

	@Override
	protected void displayData() {
		getSupportActionBar().setTitle("Source " + rowValues.getAsString(SourcesTable.COLUMN_CODE));
		setControlText(R.id.name, rowValues.getAsString(SourcesTable.COLUMN_NAME));
		setControlText(R.id.desc, rowValues.getAsString(SourcesTable.COLUMN_DESC));

		// Look up type
		String[] sourceTypes = getResources().getStringArray(R.array.source_types);
		Integer sourceType = rowValues.getAsInteger(SourcesTable.COLUMN_SOURCE_TYPE);
		String sourceTypeText;
		if (sourceType == null || sourceType >= sourceTypes.length)
			sourceTypeText = "?";
		else
			sourceTypeText = sourceTypes[sourceType];
		setControlText(R.id.source_type, "Type: " + sourceTypeText);

		// TODO
		setControlText(R.id.location, "230m NE");
	}

	public void onBasicsClick(View v) {
		// TODO
		Toast.makeText(this, "To do", Toast.LENGTH_SHORT).show();
	}

	public void onAddSampleClick(View v) {
		// TODO
		Toast.makeText(this, "To do", Toast.LENGTH_SHORT).show();
	}

	public void onAddTestClick(View v) {
		// Create sample linked to source
		ContentValues values = new ContentValues();
		values.put(SamplesTable.COLUMN_SOURCE, rowValues.getAsString(SourcesTable.COLUMN_UID));
		values.put(SamplesTable.COLUMN_CODE, OtherCodes.getNewCode(this));
		Uri sampleUri = getContentResolver().insert(MWaterContentProvider.SAMPLES_URI, values);

		// Create test
		// TODO choose type
		String sampleUid = MWaterContentProvider.getSingleRow(this, sampleUri).getAsString(SamplesTable.COLUMN_UID);
		values = new ContentValues();
		values.put(TestsTable.COLUMN_SAMPLE, sampleUid);
		values.put(TestsTable.COLUMN_CODE, OtherCodes.getNewCode(this));
		values.put(TestsTable.COLUMN_TEST_TYPE, 0);
		values.put(TestsTable.COLUMN_TEST_VERSION, 1);
		values.put(TestsTable.COLUMN_STARTED_ON, System.currentTimeMillis() / 1000);
		Uri testUri = getContentResolver().insert(MWaterContentProvider.TESTS_URI, values);

		Intent intent = new Intent(this, PetrifilmTestDetailsActivity.class);
		intent.putExtra("uri", testUri);
		startActivity(intent);
	}

	public void onAddNoteClick(View v) {
		// TODO
		Toast.makeText(this, "To do", Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.source_detail_menu, menu);

		// Add listeners
		menu.findItem(R.id.menu_star).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				// TODO
				Toast.makeText(SourceDetailActivity.this, "To do", Toast.LENGTH_SHORT).show();
				return true;
			}
		});

		menu.findItem(R.id.menu_delete).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				deleteSource();
				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	void deleteSource() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				getContentResolver().delete(uri, null, null);
				finish();
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Permanently delete source?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", null).show();
	}
}
