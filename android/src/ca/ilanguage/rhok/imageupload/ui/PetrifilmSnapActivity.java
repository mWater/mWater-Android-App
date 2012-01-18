package ca.ilanguage.rhok.imageupload.ui;

import ca.ilanguage.rhok.imageupload.R;
import android.app.Activity;
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
		// setContentView(new PetrifilmSnapPreviewView(this));
		setContentView(R.layout.petrifilm_snap_view);
	}

	public void onCaptureClick(View v) {
		Button capture = (Button) findViewById(R.id.capture);
		capture.setEnabled(false);

		// Remove view
		PetrifilmSnapPreviewView previewView = (PetrifilmSnapPreviewView) findViewById(R.id.preview);
		ViewGroup mainView = (ViewGroup) findViewById(R.id.RelativeLayout1);
		mainView.removeView(previewView);

		// Take picture
		camera = Camera.open();
		if (camera == null) {
			Toast.makeText(getApplicationContext(), "Camera locked", 0).show();
			return;
		}

		camera.takePicture(null, null, this);
	}

	public void onPictureTaken(byte[] data, Camera camera) {
		camera.release();

		Intent intent = new Intent(this, SampleListActivity.class);
		startActivity(intent);

		Toast.makeText(getApplicationContext(),
				"Captured image of size " + data.length / 1024, 0).show();
	}

}
