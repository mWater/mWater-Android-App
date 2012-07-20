package co.mwater.clientapp.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

import co.mwater.clientapp.ui.petrifilm.PetrifilmCameraActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class ImageStorage {
	private static final String TAG = ImageStorage.class.getCanonicalName();

	private final static String FOLDER_DATA_ROOT = "Android/data";

	private final static String IMAGES_FOLDER = "images";
	private final static String IMAGE_EXTENSION = ".jpg";

	private final static String TEMP_FOLDER = "temp";
	private final static String PENDING_FOLDER = "pending";
	private final static String CACHED_FOLDER = "cached";

	private final static String ORIGINAL_FOLDER = "original";
	private final static String THUMBNAIL_FOLDER = "thumbnail";

	public static String createUid() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString().replace("-", "").toLowerCase(Locale.US);
	}

	public static String getTempImagePath(Context context, String uid) throws IOException {
		File tempFile = new File(buildExternalPath(context, IMAGES_FOLDER + File.separator + TEMP_FOLDER + File.separator + uid + IMAGE_EXTENSION));
		tempFile.getParentFile().mkdirs();
		return tempFile.getAbsolutePath();
	}

	public static String getPendingImagePath(Context context, String uid) throws IOException {
		File pendingFile = new File(
				buildExternalPath(context, IMAGES_FOLDER + File.separator + PENDING_FOLDER
						+ File.separator + ORIGINAL_FOLDER + File.separator + uid + IMAGE_EXTENSION));
		pendingFile.getParentFile().mkdirs();
		return pendingFile.getAbsolutePath();
	}

	public static String getPendingThumbnailImagePath(Context context, String uid) throws IOException {
		File pendingFile = new File(
				buildExternalPath(context, IMAGES_FOLDER + File.separator + PENDING_FOLDER
						+ File.separator + THUMBNAIL_FOLDER + File.separator + uid + IMAGE_EXTENSION));
		pendingFile.getParentFile().mkdirs();
		return pendingFile.getAbsolutePath();
	}

	public static void moveTempImageFileToPending(Context context, String uid) throws IOException {
		File tempFile = new File(getTempImagePath(context, uid));
		File pendingFile = new File(getPendingImagePath(context, uid));

		// Delete if already exists
		if (pendingFile.exists())
			pendingFile.delete();

		if (!tempFile.renameTo(pendingFile))
			throw new IOException("Unable to move " + tempFile.getAbsolutePath() + " to " + pendingFile.getAbsolutePath());
		
		createThumbnail(context, pendingFile.getAbsolutePath(), getPendingThumbnailImagePath(context, uid));
	}

	/**
	 * This static function provides a string to the external storage desired
	 * file.
	 * 
	 * @param context
	 *            Android Context.
	 * @param fileName
	 *            File or directory name.
	 * @return
	 * @throws IOException
	 *             Thrown if the external storage is not available or not
	 *             writeable.
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
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
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

	private static void createThumbnail(Context context, String imagePath, String thumbnailPath) throws IOException {
		Bitmap b = createThumbnail(context, imagePath);
		FileOutputStream fos = new FileOutputStream(thumbnailPath);
		b.compress(CompressFormat.JPEG, 90, fos);
		fos.close();
	}

	private static Bitmap createThumbnail(Context context, String imagePath) throws IOException {
		Uri uri = Uri.fromFile(new File(imagePath));
		InputStream in = null;
		final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
		in = context.getContentResolver().openInputStream(uri);

		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(in, null, o);
		in.close();

		int scale = 1;
		while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
			scale++;
		}
		Log.d(TAG, "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight);

		Bitmap b = null;
		in = context.getContentResolver().openInputStream(uri);
		if (scale > 1) {
			scale--;
			// scale to max possible inSampleSize that still yields an image
			// larger than target
			o = new BitmapFactory.Options();
			o.inSampleSize = scale;
			b = BitmapFactory.decodeStream(in, null, o);

			// resize to desired dimensions
			int height = b.getHeight();
			int width = b.getWidth();
			Log.d(TAG, "1th scale operation dimenions - width: " + width + ", height: " + height);

			double y = Math.sqrt(IMAGE_MAX_SIZE
					/ (((double) width) / height));
			double x = (y / height) * width;

			Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x, (int) y, true);
			b.recycle();
			b = scaledBitmap;

			System.gc();
		} else {
			b = BitmapFactory.decodeStream(in);
		}
		in.close();

		Log.d(TAG, "bitmap size - width: " + b.getWidth() + ", height: " + b.getHeight());
		return b;
	}
}
