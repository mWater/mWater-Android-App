package co.mwater.clientapp.db;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class OtherCodes {
	private static final String PREF_NAMES = "OtherCodes";
	private static final String LAST_SAMPLE_CODE = "LastSampleCode";
	private static final String LAST_TEST_CODE = "LastSampleCode";

	public static String getNewSampleCode(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAMES, Context.MODE_PRIVATE);
		long code = prefs.getLong(LAST_SAMPLE_CODE, 0) + 1;
		code = code % 1000;
		Editor editor = prefs.edit();
		editor.putLong(LAST_SAMPLE_CODE, code);
		editor.commit();
		return String.format(Locale.US, "%03d", code);
	}

	public static String getNewTestCode(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAMES, Context.MODE_PRIVATE);
		long code = prefs.getLong(LAST_TEST_CODE, 100) + 1;
		code = code % 1000;
		Editor editor = prefs.edit();
		editor.putLong(LAST_TEST_CODE, code);
		editor.commit();
		return String.format(Locale.US, "%03d", code);
	}

}
