package ca.ilanguage.rhok.imageupload.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import ca.ilanguage.rhok.imageupload.R;
import ca.ilanguage.rhok.imageupload.db.ImageUploadHistoryDatabase.ImageUploadHistory;
import ca.ilanguage.rhok.imageupload.pref.PreferenceConstants;
import ca.ilanguage.rhok.imageupload.ui.MainPortal;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class TakePicture extends Activity {
	private static final String EXTRA_RESULT_FILENAME = null;
	Uri myPicture = null;
	Uri mImageDBUri= null;
	String mImageFilename = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.take_picture);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		try{
		mImageFilename = getIntent().getExtras().getString(
				PreferenceConstants.EXTRA_IMAGEFILE_FULL_PATH);
		}catch (Exception e) {
			// TODO: handle exception
			
		}
		mImageDBUri = getIntent().getData();
		if(mImageFilename == null){
			mImageFilename="/sdcard/BacteriaCounting/watersamples/error.jpg";
		}
		if(mImageDBUri == null){
			//This activity needs to be called with a URI of its corresponding row in the database.
			finish();
		}
	}

	public void captureImage(View view) {
		ContentValues values = new ContentValues();
		values.put(Media.TITLE, mImageFilename);
		values.put(Media.DESCRIPTION,
				"Image Captured as part of Bacteria Counting Water Sample");

		myPicture = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI,
				values);
		Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		i.putExtra(MediaStore.EXTRA_OUTPUT, myPicture);

		startActivityForResult(i, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
			// Now we know that our myPicture URI refers to the image just taken
			/*
			 * copy image to results folder
			 */
			try {
				File sd = Environment.getExternalStorageDirectory();
				if (sd.canWrite()) {
					String sourceImagePath = getPath(myPicture);
					String destinationImagePath = mImageFilename;
					File source = new File(sourceImagePath);
					File destination = new File(destinationImagePath);
					if (source.exists()) {
						FileChannel src = new FileInputStream(source)
								.getChannel();
						FileChannel dst = new FileOutputStream(destination)
								.getChannel();
						dst.transferFrom(src, 0, src.size());
						src.close();
						dst.close();
					}
				}
				int affectedEntriesCount = updateImageMetadata(mImageDBUri);
				Toast.makeText(getApplicationContext(),
						"Saving as " + mImageFilename + "\nUpdated " + affectedEntriesCount + " water sample.", Toast.LENGTH_LONG)
						.show();
			} catch (Exception e) {
				Toast.makeText(
						getApplicationContext(),
						"Result picture wasn't copied, but it's in the Camera folder: "
								+ getPath(myPicture), Toast.LENGTH_LONG).show();
			}

		}
		finishActivity(MainPortal.WATER_SOURCE);
	}

	/**
	 * TODO detect GPS on device, turn it on and get the Latitude and Longitude when this image is shot.
	 * 
	 * @param uri which matches the row in the database for this image
	 * @return
	 */
	private int updateImageMetadata(Uri uri){
		String metadataInJSON = "{lat: 43, long: 42, timestamp:21312, user: 23425}";
		ContentValues values = new ContentValues();
		values.put(ImageUploadHistory.FILEPATH, mImageFilename);
		values.put(ImageUploadHistory.UPLOADED,"0");//sets deleted flag to true
		values.put(ImageUploadHistory.METADATA, metadataInJSON);
		return getContentResolver().update(uri, values, null, null);
	}
	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		startManagingCursor(cursor);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
}
