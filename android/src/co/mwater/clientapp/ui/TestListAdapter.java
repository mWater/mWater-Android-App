package co.mwater.clientapp.ui;

import java.text.DateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.SamplesTable;
import co.mwater.clientapp.db.SourcesTable;
import co.mwater.clientapp.db.TestsTable;
import co.mwater.clientapp.db.testresults.Results;
import co.mwater.clientapp.db.testresults.Risk;
import co.mwater.clientapp.db.testresults.TestType;

class TestListAdapter extends CustomAdapter {
	public TestListAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		final View view = inflater.inflate(R.layout.test_row, parent, false);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// Get sample uid
		String sampleUid = cursor.getString(cursor.getColumnIndex(TestsTable.COLUMN_SAMPLE));

		// Get sample
		ContentValues sample = null;
		ContentValues source = null;
		if (sampleUid != null)
		{
			sample = MWaterContentProvider.getSingleRow(context, MWaterContentProvider.SAMPLES_URI, sampleUid);

			// Get source uid
			String sourceUid = sample.getAsString(SamplesTable.COLUMN_SOURCE);

			// Get source
			if (sourceUid != null)
				source = MWaterContentProvider.getSingleRow(context, MWaterContentProvider.SOURCES_URI, sourceUid);
		}

		setControlText(view, R.id.source_name, source != null ? source.getAsString(SourcesTable.COLUMN_NAME) : "Unspecified Source");
		setControlText(view, R.id.code, cursor.getString(cursor.getColumnIndex(TestsTable.COLUMN_CODE)));

		String[] testTags = context.getResources().getStringArray(R.array.test_tags);
		String[] testTypes = context.getResources().getStringArray(R.array.test_types);

		TestType testType = TestType.fromInt(cursor.getInt(cursor.getColumnIndex(TestsTable.COLUMN_TEST_TYPE)));
		if (testType == null) {
			// TODO prettify
			setControlText(view, R.id.tag, "???");
			setControlText(view, R.id.test_type, "???");
			((TextView) view.findViewById(R.id.tag)).setBackgroundColor(context.getResources().getColor(R.color.risk_unspecified));
		}
		else {
			setControlText(view, R.id.tag, testTags[testType.getValue()]);
			setControlText(view, R.id.test_type, testTypes[testType.getValue()]);

			Risk risk = Results.getResults(testType, cursor.getString(cursor.getColumnIndex(TestsTable.COLUMN_RESULTS))).getRisk();
			int riskColor = TestActivities.getRiskColor(risk);
			((TextView) view.findViewById(R.id.tag)).setBackgroundColor(context.getResources().getColor(riskColor));
		}

		int readOnCol = cursor.getColumnIndex(TestsTable.COLUMN_READ_ON);
		if (cursor.isNull(readOnCol)) {
			setControlText(view, R.id.started_on, "Pending");
		}
		else {
			long readOn = cursor.getLong(readOnCol);
			setControlText(view, R.id.started_on, DateFormat.getDateInstance().format(new Date(readOn * 1000)));
		}

		// setControlText(view, R.id.sample, sample != null ?
		// sample.getAsString(SamplesTable.COLUMN_CODE) : null);
	}
}