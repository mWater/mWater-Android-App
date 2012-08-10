package co.mwater.clientapp.ui;

import java.text.DateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Spinner;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.SourceNotesTable;
import co.mwater.clientapp.db.SourcesTable;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class SourceNoteDetailActivity extends DetailActivity {
	private static final String TAG = SourceNoteDetailActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.source_note_detail_activity);
	}
	
	@Override
	protected void displayData() {
		getSupportActionBar().setTitle("Source Note");

		Long created_on = rowValues.getAsLong(SourceNotesTable.COLUMN_CREATED_ON);
		if (created_on != null) {
			setControlText(R.id.created_on, DateFormat.getDateInstance().format(new Date(created_on * 1000)));
		}
		else {
			setControlText(R.id.created_on, "");
		}

		setControlText(R.id.note, rowValues.getAsString(SourceNotesTable.COLUMN_NOTE));
		setControlTextEditable(R.id.note, isCreatedByMe());

		// Get source
		String sourceUid = rowValues.getAsString(SourceNotesTable.COLUMN_SOURCE);
		ContentValues source = null;
		if (sourceUid != null)
			source = MWaterContentProvider.getSingleRow(this, MWaterContentProvider.SOURCES_URI, sourceUid);

		setControlText(R.id.source_name, source.getAsString(SourcesTable.COLUMN_NAME));
		setControlText(R.id.source_code, source.getAsString(SourcesTable.COLUMN_CODE));
	}

	@Override
	public void onPause() {
		super.onPause();

		if (rowValues != null) {
			ContentValues values = new ContentValues();

			// Save note
			String curNote = getControlText(R.id.note);
			if (curNote.length() == 0)
				curNote = null;

			if (curNote != rowValues.getAsString(SourceNotesTable.COLUMN_NOTE)) {
				values.put(SourceNotesTable.COLUMN_NOTE, curNote);
			}
			
			// Save operational
			Boolean curOperational;
			Spinner operational = (Spinner)findViewById(R.id.operational);
			if (operational.getSelectedItemPosition() == 0 || operational.getSelectedItemPosition() == Spinner.INVALID_POSITION) {
				curOperational = null;
			}
			else if (operational.getSelectedItemPosition() == 1)
				curOperational = true;
			else
				curOperational = false;
			
			if (curOperational != rowValues.getAsBoolean(SourceNotesTable.COLUMN_OPERATIONAL)) {
				values.put(SourceNotesTable.COLUMN_OPERATIONAL, curOperational);
			}
			if (values.size()>0)
				getContentResolver().update(uri, values, null, null);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.source_note_detail_menu, menu);

		// Add listeners
		menu.findItem(R.id.menu_done).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				finish();
				return true;
			}
		});

		menu.findItem(R.id.menu_delete).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				deleteSourceNote();
				return true;
			}
		});
		menu.findItem(R.id.menu_delete).setEnabled(isCreatedByMe());

		return super.onCreateOptionsMenu(menu);
	}

	void deleteSourceNote() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				getContentResolver().delete(uri, null, null);
				finish();
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Permanently delete note?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", null).show();
	}
}
