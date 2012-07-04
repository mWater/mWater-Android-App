package com.github.androidimageprocessing.bacteria.ui;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.github.androidimageprocessing.bacteria.R;
import com.github.androidimageprocessing.bacteria.databinding.DataBinder;
import com.github.androidimageprocessing.bacteria.db.MWaterContentProvider;
import com.github.androidimageprocessing.bacteria.db.SourcesTable;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SourceDetailInfoFragment extends SherlockFragment {
	DataBinder dataBinder;
	Handler handler = new Handler();
	Uri uri;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dataBinder = new DataBinder(getActivity().getContentResolver(), handler);

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = getLayoutInflater(savedInstanceState).inflate(R.layout.source_detail_general, container, false);

		// Set up data binder
		dataBinder.addTextView((TextView) view.findViewById(R.id.code), SourcesTable.COLUMN_CODE);
		dataBinder.addTextView((TextView) view.findViewById(R.id.name), SourcesTable.COLUMN_NAME);
		dataBinder.addTextView((TextView) view.findViewById(R.id.desc), SourcesTable.COLUMN_DESC);
		dataBinder.addTextView((TextView) view.findViewById(R.id.latpos), SourcesTable.COLUMN_LAT);
		dataBinder.addTextView((TextView) view.findViewById(R.id.longpos), SourcesTable.COLUMN_LONG);

		// Bind to id
		uri = (Uri) getArguments().getParcelable("uri");
		dataBinder.bind(uri);

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add("Revert");
		item.setIcon(R.drawable.content_undo);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				dataBinder.revert();
				return true;
			}
		});
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onDestroy() {
		dataBinder.unbind();
		super.onDestroy();
	}

	@Override
	public void onPause() {
		dataBinder.save();
		super.onPause();
	}

	public void onViewOnMap(View view) {
		Toast.makeText(this.getActivity(), "test", Toast.LENGTH_LONG).show();
	}
}
