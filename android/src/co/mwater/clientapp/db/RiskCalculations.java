package co.mwater.clientapp.db;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.testresults.Results;
import co.mwater.clientapp.db.testresults.Risk;
import co.mwater.clientapp.db.testresults.TestType;

/**
 * Performs calculations of risk of sources, samples and tests
 * 
 * @author Clayton
 * 
 */
public class RiskCalculations {

	public static void updateSourceRiskForSample(Context context, String sampleUid) {
		if (sampleUid == null)
			return;

		// Get sample
		ContentValues sample = MWaterContentProvider.getSingleRow(context, MWaterContentProvider.SAMPLES_URI, sampleUid);
		if (sample == null)
			return;

		updateSourceRisk(context, sample.getAsString(SamplesTable.COLUMN_SOURCE));
	}

	public static void updateSourcesRisk(Context context) {
		Cursor sources = context.getContentResolver().query(MWaterContentProvider.SOURCES_URI, null, null, null, null);

		if (sources.moveToFirst()) {
			do {
				String sourceUid = sources.getString(sources.getColumnIndexOrThrow(SourcesTable.COLUMN_UID));
				updateSourceRisk(context, sourceUid);
			} while (sources.moveToNext());
		}

		sources.close();
	}

	public static void updateSourceRisk(Context context, String sourceUid) {
		// Get source
		ContentValues source = MWaterContentProvider.getSingleRow(context, MWaterContentProvider.SOURCES_URI, sourceUid);
		if (source == null)
			return;

		// Get risk for source
		Risk newRisk = getOverallRiskForSource(context, source.getAsString(SourcesTable.COLUMN_UID));
		Risk curRisk = Risk.fromInt(source.getAsInteger(SourcesTable.COLUMN_RISK));

		if (newRisk != curRisk) {
			ContentValues vals = new ContentValues();
			if (newRisk != null)
				vals.put(SourcesTable.COLUMN_RISK, newRisk.getValue());
			else
				vals.putNull(SourcesTable.COLUMN_RISK);

			context.getContentResolver().update(MWaterContentProvider.SOURCES_URI, vals, SourcesTable.COLUMN_UID + "=?",
					new String[] { source.getAsString(SourcesTable.COLUMN_UID) });
		}
	}

	public static Risk getRiskForTest(TestType testType, Cursor tests) {
		int dilution = tests.getInt(tests.getColumnIndex(TestsTable.COLUMN_DILUTION));
		Risk risk = Results.getResults(testType, tests.getString(tests.getColumnIndex(TestsTable.COLUMN_RESULTS))).getRisk(dilution);
		return risk;
	}

	public static HashMap<String, Risk> getRisksForTests(Context context, Cursor tests) {
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
				Risk risk = getRiskForTest(testType, tests);

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
		return tagRisks;
	}

	/**
	 * Gets the overall risk for a given sample, which is the highest risk when
	 * grouped by tag.
	 */
	public static Risk getOverallRiskForSample(Context context, String sampleUid) {
		// Get tests
		Cursor tests = context.getContentResolver().query(MWaterContentProvider.TESTS_URI, null, TestsTable.COLUMN_SAMPLE + "=?",
				new String[] { sampleUid }, null);

		Risk highestRisk = null;

		for (Risk risk : getRisksForTests(context, tests).values()) {
			if (highestRisk == null || risk.ordinal() > highestRisk.ordinal())
				highestRisk = risk;
		}

		tests.close();

		return highestRisk;
	}

	/**
	 * Gets the overall risk for a given source, which is the risk of the latest
	 * sample with a risk
	 */
	public static Risk getOverallRiskForSource(Context context, String sourceUid) {
		// Get samples
		Cursor samples = context.getContentResolver().query(MWaterContentProvider.SAMPLES_URI, null, SamplesTable.COLUMN_SOURCE + "=?",
				new String[] { sourceUid }, SamplesTable.COLUMN_SAMPLED_ON + " DESC");

		// For each sample, try to get a risk
		try {
			if (samples.moveToFirst()) {
				do {
					// Get uid of sample
					String sampleUid = samples.getString(samples.getColumnIndexOrThrow(SamplesTable.COLUMN_UID));
					Risk sampleRisk = getOverallRiskForSample(context, sampleUid);
					if (sampleRisk != null)
						return sampleRisk;
				} while (samples.moveToNext());
			}
		} finally {
			samples.close();
		}
		return Risk.UNSPECIFIED;
	}
}
