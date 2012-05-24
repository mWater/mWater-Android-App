package com.github.androidimageprocessing.bacteria;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;

public class App {

    public final static String FOLDER_ORIGINAL = "original";
    public final static String FOLDER_PROCESSED = "processed";
    public final static String FOLDER_RESULTS = "results";

    /**
     * Checks if the file (or directory) exists in the external storage.
     * @param context Android Context.
     * @param fileName File name or directory name.
     * @return
     * @throws IOException Thrown if the external storage is not available or not writeable.
     */
    public static boolean isExternalFilePresent(Context context, String fileName) throws IOException {
        String fullPath = buildExternalPath(context, fileName);
        File file = new File(fullPath);
        return file.exists();
    }

    public static String getOriginalImageFolder(Context context) throws IOException {
        //File f = context.getExternalFilesDir(null);
        //File f2 = new File(f.getAbsolutePath(), "original");
        //f2.mkdirs(); // AR not here, but inside the onCreate of the main Activity
        //return f2.getAbsolutePath();
        return buildExternalPath(context, FOLDER_ORIGINAL);
    }

    public static String getProcessedImageFolder(Context context) throws IOException {
        //File f = context.getExternalFilesDir(null);
        //File f2 = new File(f.getAbsolutePath(), "processed");
        //f2.mkdirs(); // AR not here, but inside the onCreate of the main Activity
        //return f2.getAbsolutePath();
        return buildExternalPath(context, FOLDER_PROCESSED);
    }


    public static String getResultsFolder(Context context) throws IOException {
        //File f = context.getExternalFilesDir(null);
        //File f2 = new File(f.getAbsolutePath(), "results");
        //f2.mkdirs(); // AR not here, but inside the onCreate of the main Activity
        //return f2.getAbsolutePath();
        return buildExternalPath(context, FOLDER_RESULTS);
    }

    /**
     * This static function provides a string to the external storage desired file.
     * @param context Android Context.
     * @param fileName File or directory name.
     * @return
     * @throws IOException Thrown if the external storage is not available or not writeable.
     */
    public static String buildExternalPath(Context context, String fileName) throws IOException {

        // The following code has been taken from the Android dev guide:
        // http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        if (mExternalStorageAvailable == false) {
            throw new IOException("The external storage is not available");
        }

        if (mExternalStorageWriteable == false) {
            throw new IOException("The external storage is not writeable");
        }

        File extPath = Environment.getExternalStorageDirectory();
        String fullPath = extPath.getAbsolutePath() + File.separator + context.getPackageName() + File.separator + fileName;
        return fullPath;
    }
}
