package co.mwater.clientapp.db;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class OtherCodes {
	private static final String LAST_CODE = "LastCode";

	public static String getNewCode(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("OtherCodes", Context.MODE_PRIVATE);
		long code = prefs.getLong(LAST_CODE, 0) + 1;
		code = code % 1000;
		Editor editor = prefs.edit();
		editor.putLong(LAST_CODE, code);
		editor.commit();
		return String.format(Locale.US, "%03d", code);
	}

}
