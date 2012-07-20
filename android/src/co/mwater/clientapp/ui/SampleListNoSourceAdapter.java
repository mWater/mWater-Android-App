package co.mwater.clientapp.ui;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.SamplesTable;
import co.mwater.clientapp.db.TestsTable;

class SampleListNoSourceAdapter extends SampleListAdapter {
	public SampleListNoSourceAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		final View view = inflater.inflate(R.layout.sample_row_no_source, parent, false);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		setControlText(view, R.id.code, cursor.getString(cursor.getColumnIndex(SamplesTable.COLUMN_CODE)));

		int sampledOnCol = cursor.getColumnIndex(SamplesTable.COLUMN_SAMPLED_ON);
		long sampledOn = cursor.getLong(sampledOnCol);
		setControlText(view, R.id.sampled_on, DateFormat.getDateInstance().format(new Date(sampledOn * 1000)));

		String sampleUid = cursor.getString(cursor.getColumnIndex(SamplesTable.COLUMN_UID));

		// Get tests
		// TODO sort order
		Cursor tests = context.getContentResolver().query(MWaterContentProvider.TESTS_URI, null,
				TestsTable.COLUMN_SAMPLE + "=?", new String[] { sampleUid }, null);

		// Clear tags
		LinearLayout tagsLayout = (LinearLayout) view.findViewById(R.id.tags);
		tagsLayout.removeAllViews();

		for (View tagView : getTagViews(context, tests))
			tagsLayout.addView(tagView);

		tests.close();
	}
}