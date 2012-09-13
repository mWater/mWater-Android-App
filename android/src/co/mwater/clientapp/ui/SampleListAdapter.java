package co.mwater.clientapp.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import co.mwater.clientapp.R;
import co.mwater.clientapp.db.TestsTable;
import co.mwater.clientapp.db.testresults.Results;
import co.mwater.clientapp.db.testresults.Risk;
import co.mwater.clientapp.db.testresults.TestType;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public abstract class SampleListAdapter extends CustomAdapter {

	public SampleListAdapter(Context context, Cursor c) {
		super(context, c);
	}

	protected List<View> getTagViews(Context context, Cursor tests) {
		List<View> tagViews = new ArrayList<View>();
	
		String[] testTags = context.getResources().getStringArray(R.array.test_tags);
	
		// Create hashmap of risk by tag
		HashMap<String, Risk> tagRisks = new HashMap<String, Risk>();
	
		// For each test, add a tag
		if (tests.moveToFirst()) {
			do {
				String tagText;
				TestType testType = TestType.fromInt(tests.getInt(tests.getColumnIndex(TestsTable.COLUMN_TEST_TYPE)));
	
				if (testType == null)
					continue;
	
				tagText = testTags[testType.getValue()];
				int dilution = tests.getInt(tests.getColumnIndex(TestsTable.COLUMN_DILUTION));
				Risk risk = Results.getResults(testType, tests.getString(tests.getColumnIndex(TestsTable.COLUMN_RESULTS))).getRisk(dilution);
	
				// Compare to existing
				if (tagRisks.containsKey(tagText))
				{
					Risk oldRisk = tagRisks.get(tagText);
					if (risk != Risk.UNSPECIFIED && risk.ordinal() < oldRisk.ordinal())
						tagRisks.put(tagText, risk);
				}
				else
					tagRisks.put(tagText, risk);
			} while (tests.moveToNext());
		}
	
		for (String tagText : tagRisks.keySet()) {
			Risk risk = tagRisks.get(tagText);
			int riskColor = TestActivities.getRiskColor(risk);
			int tagColor = context.getResources().getColor(riskColor);
	
			// Create tag
			TextView tagTextView = new TextView(context, null, R.style.riskTag);
			tagTextView.setText(tagText);
			tagTextView.setBackgroundColor(tagColor);
			
			// Since bug in Android doesn't allow setting styles
		    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		    llp.setMargins(2, 0, 2, 0); // (left, top, right, bottom);
		    tagTextView.setLayoutParams(llp);
		    tagTextView.setPadding(2, 2, 2, 2);
			
			tagViews.add(tagTextView);
		}
		return tagViews;
	}

}