package co.mwater.clientapp.ui;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import co.mwater.clientapp.R;
import co.mwater.clientapp.databinding.DataBinder;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.SourceCodes;
import co.mwater.clientapp.db.SourcesTable;

public class SourceDetailBasicsDialogFragment extends DialogFragment {
	DataBinder dataBinder;
	Uri uri;

	public interface DialogListener {
        void sourceCreated(String id);
    }
	
	public SourceDetailBasicsDialogFragment(String id) {
		// Load existing values if has id
		if (id != null) {
			// Query row
			uri = Uri.withAppendedPath(MWaterContentProvider.SOURCES_URI, id);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.source_detail_basics, container);

		dataBinder = new DataBinder(getActivity().getContentResolver(), new Handler());
		dataBinder.addTextView((TextView) view.findViewById(R.id.name), SourcesTable.COLUMN_NAME);
		dataBinder.addTextView((TextView) view.findViewById(R.id.desc), SourcesTable.COLUMN_DESC);

		// Load existing values if has id
		if (uri != null)
			dataBinder.loadFrom(uri);

		// Wire up buttons
		((Button) view.findViewById(R.id.ok)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) { 
				onOKClick();
			}
		});
		((Button) view.findViewById(R.id.cancel)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) { 
				dismiss();
			}
		});

		getDialog().setTitle("Source Details");
		return view;
	}

	private void onOKClick() {
		boolean newlyCreated = false;
		// Create row if non-existant
		if (uri == null) {
			ContentValues cv = new ContentValues();
			cv.put("code", SourceCodes.getNewCode(this.getActivity()));
			uri = getActivity().getContentResolver().insert(MWaterContentProvider.SOURCES_URI, cv);
			newlyCreated = true;
		}
		
		// Save values
		dataBinder.saveTo(uri);

		// Edit source if newly created
		if (newlyCreated)
			((DialogListener)getActivity()).sourceCreated(uri.getLastPathSegment());

		this.dismiss();
	}
}
