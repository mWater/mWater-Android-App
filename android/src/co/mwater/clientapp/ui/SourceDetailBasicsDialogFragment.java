package co.mwater.clientapp.ui;

import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.SourcesTable;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class SourceDetailBasicsDialogFragment extends DialogFragment implements OnClickListener {
	Uri uri;

	public SourceDetailBasicsDialogFragment(String id) {
		// Load existing values if has id
		if (id != null) {
			// Query row
			uri = Uri.withAppendedPath(MWaterContentProvider.SOURCES_URI, id);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.source_detail_basics, container);

		// Load existing values if has id
		if (uri != null) {
			Cursor cursor = this.getActivity().getContentResolver().query(uri, null, null, null, null);
			// Load fields
			if (cursor.moveToFirst())
			{
				ContentValues values = new ContentValues();
				DatabaseUtils.cursorRowToContentValues(cursor, values);
				setText(view, R.id.name, values.getAsString(SourcesTable.COLUMN_NAME));
				setText(view, R.id.desc, values.getAsString(SourcesTable.COLUMN_DESC));
			}
			cursor.close();
		}

		Button okButton = (Button) view.findViewById(R.id.ok);
		okButton.setOnClickListener(this);

		Button cancelButton = (Button) view.findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);

		getDialog().setTitle("Source Details");
		return view;
	}

	void setText(View view, int id, String text) {
		TextView textView = (TextView) view.findViewById(id);
		if (text != null)
			textView.setText(text);
		else
			textView.setText("");
	}

	public void onClick(View v) {
		if (v.getId() == R.id.ok) {
			// Save values
			ContentValues values = new ContentValues();

			this.dismiss();
		}
		if (v.getId() == R.id.cancel) {
			this.dismiss();
		}
	}
}
