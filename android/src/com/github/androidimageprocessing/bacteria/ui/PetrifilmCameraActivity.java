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
	private static final String TAG = "ca.ilanguage.rhok";
	Camera camera;

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
		Button capture = (Button) findViewById(R.id.capture);
		capture.setEnabled(false);

		// Take picture
		PetrifilmCameraView previewView = (PetrifilmCameraView) findViewById(R.id.CameraView);
		Camera camera = previewView.getCamera();
		camera.takePicture(null, null, this);
		// // TODO focus, flash, resolution
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
