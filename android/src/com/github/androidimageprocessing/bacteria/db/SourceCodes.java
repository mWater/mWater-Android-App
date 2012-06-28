package com.github.androidimageprocessing.bacteria.db;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SourceCodes {
	public static String getNewCode(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("SourceCodes", Context.MODE_PRIVATE);
		long code = prefs.getLong("LastCode", 0) + 1;
		Editor editor = prefs.edit();
		editor.putLong("LastCode", code);
		editor.commit();
		return String.format(Locale.US, "%05d", code);
	}
}
