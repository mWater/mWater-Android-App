package co.mwater.clientapp.ui;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.SamplesTable;
import co.mwater.clientapp.db.TestsTable;
import co.mwater.clientapp.db.testresults.Results;
import co.mwater.clientapp.db.testresults.Risk;
import co.mwater.clientapp.db.testresults.TestType;
import co.mwater.clientapp.ui.petrifilm.PetrifilmTestDetailActivity;

public class TestActivities {
	@SuppressWarnings("rawtypes")
	public static Class getDetailActivity(TestType testType) {
		switch (testType) {
		case PETRIFILM:
			return PetrifilmTestDetailActivity.class;
		case TEN_ML_COLILERT:
			return TenMLColilertDetailActivity.class;
		case HUNDRED_ML_ECOLI:
			return HundredMLEColiDetailActivity.class;
		default:
			return null;
		}
	}

	public static void editTest(Context context, long id) {
		// Get test
		Uri testUri = Uri.withAppendedPath(MWaterContentProvider.TESTS_URI, id + "");
		ContentValues testValues = MWaterContentProvider.getSingleRow(context, testUri);
		TestType testType = TestType.fromInt(testValues.getAsInteger(TestsTable.COLUMN_TEST_TYPE));
		if (testType != null)
		{
			@SuppressWarnings("rawtypes")
			Class detailClass = TestActivities.getDetailActivity(testType);
			if (detailClass != null) {
				Intent intent = new Intent(context, detailClass);
				intent.putExtra("uri", testUri);
				context.startActivity(intent);
			}
		}
		// TODO if not?
	}

	public static int getRiskColor(Risk risk) {
		switch (risk) {
		case UNSPECIFIED:
			return R.color.risk_unspecified;
		case BLUE:
			return R.color.risk_blue;
		case GREEN:
			return R.color.risk_green;
		case YELLOW:
			return R.color.risk_yellow;
		case ORANGE:
			return R.color.risk_orange;
		case RED:
			return R.color.risk_red;
		default:
			return R.color.risk_unspecified;
		}
	}
}
