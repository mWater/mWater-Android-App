package co.mwater.clientapp.ui;

import co.mwater.clientapp.databinding.DataBinder;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.SourcesTable;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import co.mwater.clientapp.R;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SourceDetailActivity extends SherlockFragmentActivity implements LoaderCallbacks<Cursor> {
	public static final String TAG = SourceDetailActivity.class.getSimpleName();
	private Uri uri;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String id = getIntent().getStringExtra("id");
		uri = Uri.withAppendedPath(MWaterContentProvider.SOURCES_URI, id);

		setContentView(R.layout.source_detail_activity);
		
		// Load row
		getSupportLoaderManager().initLoader(0, null, this);
	}

	public void onAddSampleClick(View v) {
		// TODO
		Toast.makeText(this, "To do", Toast.LENGTH_SHORT).show();
	}

	public void onAddTestClick(View v) {
		// TODO
		Toast.makeText(this, "To do", Toast.LENGTH_SHORT).show();
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

	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return new CursorLoader(this, uri, null, null, null, null);
	}

	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		// Load fields
		if (cursor.moveToFirst())
		{
			ContentValues values = new ContentValues();
			DatabaseUtils.cursorRowToContentValues(cursor, values);
			getSupportActionBar().setTitle("Source #" + values.getAsString(SourcesTable.COLUMN_CODE));
			setText(R.id.name, values.getAsString(SourcesTable.COLUMN_NAME));
			setText(R.id.desc, values.getAsString(SourcesTable.COLUMN_DESC));
			setText(R.id.source_type, "Type: " + SourcesTable.getLocalizedSourceType(values.getAsInteger(SourcesTable.COLUMN_SOURCE_TYPE)));

			// TODO
			setText(R.id.location, "230m NE");
		}
		cursor.close();
	}

	void setText(int id, String text) {
		TextView textView = (TextView) findViewById(id);
		textView.setText(text);
	}

	public void onLoaderReset(Loader<Cursor> cursorLoader) {
	}
}
