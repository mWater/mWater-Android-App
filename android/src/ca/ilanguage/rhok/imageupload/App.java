package ca.ilanguage.rhok.imageupload;

import java.io.File;

import android.content.Context;

public class App {

	public static String getOriginalImageFolder(Context context) {
		File f = context.getExternalFilesDir(null);
		File f2 = new File(f.getAbsolutePath(), "original");
		f2.mkdirs();
		return f2.getAbsolutePath();
	}

	public static String getProcessedImageFolder(Context context) {
		File f = context.getExternalFilesDir(null);
		File f2 = new File(f.getAbsolutePath(), "processed");
		f2.mkdirs();
		return f2.getAbsolutePath();
	}
}
