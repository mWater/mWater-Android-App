package com.github.androidimageprocessing.bacteria.ui;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.github.androidimageprocessing.bacteria.R;
import com.github.androidimageprocessing.bacteria.databinding.DataBinder;
import com.github.androidimageprocessing.bacteria.db.MWaterContentProvider;
import com.github.androidimageprocessing.bacteria.db.SourcesTable;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SourceDetailActivity extends SherlockFragmentActivity {
	public static final String TAG = SourceDetailActivity.class.getSimpleName();
	DataBinder dataBinder;
	Handler handler = new Handler();
	Uri uri;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.source_detail);

		// Set up data binder
		dataBinder = new DataBinder(getContentResolver(), handler);
		dataBinder.addTextView((TextView) findViewById(R.id.code), SourcesTable.COLUMN_CODE);
		dataBinder.addTextView((TextView) findViewById(R.id.name), SourcesTable.COLUMN_NAME);
		dataBinder.addTextView((TextView) findViewById(R.id.desc), SourcesTable.COLUMN_DESC);
		dataBinder.addTextView((TextView) findViewById(R.id.latpos), SourcesTable.COLUMN_LAT);
		dataBinder.addTextView((TextView) findViewById(R.id.longpos), SourcesTable.COLUMN_LONG);

		// Bind to id
		String id = getIntent().getStringExtra("id");
		uri = Uri.withAppendedPath(MWaterContentProvider.SOURCES_URI, id);
		dataBinder.bind(uri);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.source_detail_menu, menu);
		
		menu.findItem(R.id.menu_ok).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				finish();
				return true;
			}
		});
		
		menu.findItem(R.id.menu_undo).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				dataBinder.revert();
				return true;
			}
		});

		menu.findItem(R.id.menu_delete).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				deleteSource();
				return true;
			}
		});

		// SearchView searchView = (SearchView)
		// menu.findItem(R.id.search).getActionView();
		// Configure the search info and add any event listeners
		// ...
		return super.onCreateOptionsMenu(menu);
	}

	public void onViewOnMap(View view) {
		Toast.makeText(this, "test", Toast.LENGTH_LONG).show();
	}
	
	void deleteSource() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				getContentResolver().delete(uri, null, null);
				finish();
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Permanently delete source?")
			.setPositiveButton("Yes", dialogClickListener)
			.setNegativeButton("No", null).show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		dataBinder.save();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dataBinder.unbind();
	}
}
