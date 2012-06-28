package com.github.androidimageprocessing.bacteria.ui;

import com.github.androidimageprocessing.bacteria.R;
import com.github.androidimageprocessing.bacteria.databinding.DataBinder;
import com.github.androidimageprocessing.bacteria.db.MWaterContentProvider;
import com.github.androidimageprocessing.bacteria.db.SourcesTable;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SourceDetailActivity extends FragmentActivity {
	public static final String TAG = SourceDetailActivity.class.getSimpleName();
	DataBinder dataBinder;
	Handler handler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.source_detail);
		
		// Set up data binder
		dataBinder = new DataBinder(getContentResolver(), handler);
		dataBinder.addTextView((TextView)findViewById(R.id.code), SourcesTable.COLUMN_CODE);
		dataBinder.addTextView((TextView)findViewById(R.id.name), SourcesTable.COLUMN_NAME);
		dataBinder.addTextView((TextView)findViewById(R.id.desc), SourcesTable.COLUMN_DESC);
		dataBinder.addTextView((TextView)findViewById(R.id.latpos), SourcesTable.COLUMN_LAT);
		dataBinder.addTextView((TextView)findViewById(R.id.longpos), SourcesTable.COLUMN_LONG);
		
		// Bind to id
		String id = getIntent().getStringExtra("id");
		Uri uri = Uri.withAppendedPath(MWaterContentProvider.SOURCES_URI, id);
		dataBinder.bind(uri);
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
