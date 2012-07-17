package co.mwater.clientapp.ui;

import java.text.DateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.SamplesTable;
import co.mwater.clientapp.db.SourcesTable;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.actionbarsherlock.view.Window;

public class SampleDetailActivity extends DetailActivity {
	private static final String TAG = SampleDetailActivity.class.getSimpleName();

	boolean autoAnalysing = false; // TODO this could be done better

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sample_detail_activity);
	}

	@Override
	protected void displayData() {
		getSupportActionBar().setTitle("Sample " + rowValues.getAsString(SamplesTable.COLUMN_CODE));

		Long sampled_on = rowValues.getAsLong(SamplesTable.COLUMN_SAMPLED_ON);
		if (sampled_on != null) {
			setControlText(R.id.started_on, "Started: " + DateFormat.getDateInstance().format(new Date(sampled_on * 1000)));
		}

		setControlText(R.id.desc, rowValues.getAsString(SamplesTable.COLUMN_DESC));

		// Get source
		String sourceUid = rowValues.getAsString(SamplesTable.COLUMN_SOURCE);
		ContentValues source = null;
		if (sourceUid != null)
			source = MWaterContentProvider.getSingleRow(this, MWaterContentProvider.SOURCES_URI, sourceUid);

		// TODO other options
		if (source != null) {
			setControlText(R.id.source_name, source.getAsString(SourcesTable.COLUMN_NAME));
			setControlText(R.id.source_code, source.getAsString(SourcesTable.COLUMN_CODE));
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (rowValues != null) {
			// Save description
			String curDesc = getControlText(R.id.desc);
			if (curDesc.length() == 0)
				curDesc = null;

			if (curDesc != rowValues.getAsString(SamplesTable.COLUMN_DESC)) {
				ContentValues values = new ContentValues();
				values.put(SamplesTable.COLUMN_DESC, curDesc);
				getContentResolver().update(uri, values, null, null);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.sample_detail_menu, menu);

		menu.findItem(R.id.menu_delete).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				deleteSample();
				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	void deleteSample() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				getContentResolver().delete(uri, null, null);
				finish();
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Permanently delete sample and all its tests?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", null).show();
	}
}
