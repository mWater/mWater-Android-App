package ca.ilanguage.rhok.imageupload.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import ca.ilanguage.rhok.imageupload.R;
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

public class PetrifilmSnapActivity extends Activity implements PictureCallback {
	private static final String TAG = "Sample::Activity";
	Camera camera;

	public PetrifilmSnapActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.petrifilm_snap_view);
	}

	public void onCaptureClick(View v) {
		Button capture = (Button) findViewById(R.id.capture);
		capture.setEnabled(false);

		// Take picture
		PetrifilmSnapPreviewView previewView = (PetrifilmSnapPreviewView) findViewById(R.id.preview);
		Camera camera = previewView.getCamera();
		camera.takePicture(null, null, this);

		// // Remove view
		// PetrifilmSnapPreviewView previewView = (PetrifilmSnapPreviewView)
		// findViewById(R.id.preview);
		// ViewGroup mainView = (ViewGroup) findViewById(R.id.RelativeLayout1);
		// mainView.removeView(previewView);
		//
		// // Take picture
		// camera = Camera.open();
		// if (camera == null) {
		// Toast.makeText(getApplicationContext(), "Camera locked", 0).show();
		// return;
		// }
		//
		// // TODO focus, flash, resolution
		//
		//
		// camera.takePicture(null, null, this);
	}

	public void onPictureTaken(byte[] data, Camera camera) {
		// camera.release();

		// Remove view
		PetrifilmSnapPreviewView previewView = (PetrifilmSnapPreviewView) findViewById(R.id.preview);
		ViewGroup mainView = (ViewGroup) findViewById(R.id.RelativeLayout1);
		mainView.removeView(previewView);

		String filepath = getIntent().getStringExtra("filepath");
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(filepath);
			fos.write(data);
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		Intent result = new Intent();
		result.putExtra("guid", getIntent().getStringExtra("guid"));
		result.putExtra("filepath", filepath);
		setResult(RESULT_OK, result);

		finish();
	}

}
