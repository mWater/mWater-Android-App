package co.mwater.clientapp.petrifilmanalysis;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class PetrifilmImages {

    public final static String FOLDER_DATA_ROOT = "Android/data";
    public final static String FOLDER_ORIGINAL = "original";
    public final static String FOLDER_PROCESSED = "processed";
	private static final String TAG = PetrifilmImages.class.getCanonicalName();

    public static void setup(Context context)   {
        try {
            if (PetrifilmImages.isExternalFilePresent(context, PetrifilmImages.FOLDER_ORIGINAL) == false) {
                File f = new File(PetrifilmImages.getOriginalImageFolder(context));
                f.mkdirs();
            }
            if (PetrifilmImages.isExternalFilePresent(context, PetrifilmImages.FOLDER_PROCESSED) == false) {
                File f = new File(PetrifilmImages.getProcessedImageFolder(context));
                f.mkdirs();
            }
        } catch (IOException e) {
            // TODO Handle this exception (popup a dialog with the error)
            Log.e(TAG, e.toString());
        }
    }
    
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
        return buildExternalPath(context, FOLDER_ORIGINAL);
    }

    public static String getProcessedImageFolder(Context context) throws IOException {
        return buildExternalPath(context, FOLDER_PROCESSED);
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
        String fullPath = extPath.getAbsolutePath() + File.separator + FOLDER_DATA_ROOT + File.separator + context.getPackageName() + File.separator + fileName;
        return fullPath;
    }
}
