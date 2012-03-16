package ca.ilanguage.rhok.imageupload;

import java.io.File;

import android.content.Context;

public class App {

	public static String getOriginalImageFolder(Context context) {
		return context.getExternalFilesDir(null).getAbsolutePath();
	}
}
