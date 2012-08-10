package co.mwater.clientapp.ui;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.SourceNotesTable;

class SourceNoteListAdapter extends CustomAdapter {
	public SourceNoteListAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		final View view = inflater.inflate(R.layout.source_note_row, parent, false);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		int createdOnCol = cursor.getColumnIndex(SourceNotesTable.COLUMN_CREATED_ON);
		long createdOn = cursor.getLong(createdOnCol);
		setControlText(view, R.id.created_on, DateFormat.getDateInstance().format(new Date(createdOn * 1000)));

		int operationalCol = cursor.getColumnIndex(SourceNotesTable.COLUMN_OPERATIONAL);
		TextView operationalView = ((TextView) view.findViewById(R.id.operational));
		if (cursor.isNull(operationalCol)) {
			// No data on operational
			setControlText(view, R.id.operational, "");
			operationalView.setVisibility(TextView.GONE);
		}
		else if (cursor.getInt(operationalCol) == 1) {
			setControlText(view, R.id.operational, "Operational");
			operationalView.setTextColor(context.getResources().getColor(R.color.operational));
			operationalView.setVisibility(TextView.VISIBLE);
		}
		else if (cursor.getInt(operationalCol) == 0) {
			setControlText(view, R.id.operational, "Broken");
			operationalView.setTextColor(context.getResources().getColor(R.color.broken));
			operationalView.setVisibility(TextView.VISIBLE);
		}

		setControlText(view, R.id.note, cursor.getString(cursor.getColumnIndex(SourceNotesTable.COLUMN_NOTE)));
	}
}