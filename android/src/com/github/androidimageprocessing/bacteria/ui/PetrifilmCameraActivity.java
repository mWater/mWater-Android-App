package com.github.androidimageprocessing.bacteria.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.github.androidimageprocessing.bacteria.App;

import com.github.androidimageprocessing.bacteria.R;
import com.github.androidimageprocessing.bacteria.ui.PetrifilmTestListActivity.PopulatePetrifilmTestsListTask;

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
import android.webkit.WebView.PictureListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class PetrifilmCameraActivity extends Activity implements
		PictureCallback {
	private static final String TAG = "com.github.androidimageprocessing.bacteria";

	UITimerTask timerTask = new UITimerTask();
	int autoSnapTimer = 0;			// Timer 0-100 before auto-taking picture. 
	boolean pictureInProgress; 		// True if picture taking is in progress

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.petrifilm_camera_activity);

		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setMinimumWidth(100);
	}

	@Override
	public void onResume() {
		super.onResume();
		timerTask.start(new Runnable() {
			public void run() {
				if (pictureInProgress)
					return;
				
				ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
				PetrifilmCameraView previewView = (PetrifilmCameraView) findViewById(R.id.CameraView);

				if (previewView.results.foundCircle) {
					if (autoSnapTimer < 100) {
						autoSnapTimer += 10;
						progressBar.setProgress(autoSnapTimer);
					} else {
						// Take picture
						Camera camera = previewView.getCamera();
						startAutofocus(camera);
					}
				} else {
					autoSnapTimer = 0;
					progressBar.setProgress(autoSnapTimer);
				}
			}
		}, 150);
	}

	@Override
	public void onPause() {
		super.onPause();
		timerTask.stop();
	}

	public void onCaptureClick(View v) {
		// Take picture
		PetrifilmCameraView previewView = (PetrifilmCameraView) findViewById(R.id.CameraView);
		Camera camera = previewView.getCamera();
		startAutofocus(camera);
	}

	private void startAutofocus(Camera camera) {
		final Button capture = (Button) findViewById(R.id.capture);
		capture.setEnabled(false);
		pictureInProgress = true;

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
							pictureInProgress = false;
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
