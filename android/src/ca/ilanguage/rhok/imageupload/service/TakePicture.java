package ca.ilanguage.rhok.imageupload.service;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

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
	 Uri myPicture = null;
	 String mImageFilename = "";

	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.take_picture);

	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	        mImageFilename = getIntent().getExtras().getString(OPrime.EXTRA_RESULT_FILENAME);
	    }

	    public void captureImage(View view)
	    {
	        ContentValues values = new ContentValues();
	        values.put(Media.TITLE, mImageFilename);
	        values.put(Media.DESCRIPTION, "Image Captured as part of Bilingual Aphasia Test");

	        myPicture = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
	        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	        i.putExtra(MediaStore.EXTRA_OUTPUT, myPicture);

	        startActivityForResult(i, 0);
	    }

	    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
			// Now we know that our myPicture URI refers to the image just taken
			/*
			 *  copy image to results folder
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
				Toast.makeText(getApplicationContext(),
						"Saving as " + mImageFilename, Toast.LENGTH_LONG)
						.show();
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(),
						"Result picture wasn't copied, its in the Camera folder: " + getPath(myPicture), Toast.LENGTH_LONG)
						.show();
			}

		}
	}
	    public String getPath(Uri uri) {
	        String[] projection = { MediaStore.Images.Media.DATA };
	        Cursor cursor = managedQuery(uri, projection, null, null, null);
	        startManagingCursor(cursor);
	        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	        cursor.moveToFirst();
	        return cursor.getString(column_index);
	    }
	}
}
