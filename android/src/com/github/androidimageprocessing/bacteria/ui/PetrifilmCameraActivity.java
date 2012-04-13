package com.github.androidimageprocessing.bacteria.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.github.androidimageprocessing.bacteria.App;

import com.github.androidimageprocessing.bacteria.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class PetrifilmCameraActivity extends Activity implements
		PictureCallback {
	private static final String TAG = "com.github.androidimageprocessing.bacteria";

	public PetrifilmCameraActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.petrifilm_camera_activity);
	}

	public void onCaptureClick(View v) {
		final Button capture = (Button) findViewById(R.id.capture);
		capture.setEnabled(false);

		// Take picture
		PetrifilmCameraView previewView = (PetrifilmCameraView) findViewById(R.id.CameraView);
		Camera camera = previewView.getCamera();

		// Start auto-focus
		camera.autoFocus(new AutoFocusCallback() {
			public void onAutoFocus(boolean success, Camera camera) {
				Log.i(TAG, "Autofocus success=" + success);
				if (success)
					camera.takePicture(null, null, PetrifilmCameraActivity.this);
				else {
					PetrifilmCameraActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(PetrifilmCameraActivity.this,
									"Unable to focus", Toast.LENGTH_SHORT)
									.show();
							capture.setEnabled(true);
						}
					});
				}
			}
		});
	}

	public void onPictureTaken(byte[] data, Camera camera) {
		// Remove view
		PetrifilmCameraView previewView = (PetrifilmCameraView) findViewById(R.id.CameraView);
		ViewGroup mainView = (ViewGroup) findViewById(R.id.RelativeLayout1);
		mainView.removeView(previewView);

		String filename = getIntent().getStringExtra("filename");
		String filepath = App.getOriginalImageFolder(this) + File.separator
				+ filename;
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(filepath);
			fos.write(data);
			fos.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.toString());
			return;
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			return;
		}

		Log.d(TAG, "Wrote file " + filename);

		Intent result = new Intent();
		result.putExtra("filename", filename);
		setResult(RESULT_OK, result);

		finish();
	}

}
