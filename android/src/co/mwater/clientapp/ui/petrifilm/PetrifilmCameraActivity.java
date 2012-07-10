package co.mwater.clientapp.ui.petrifilm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import co.mwater.clientapp.App;
import co.mwater.clientapp.ui.petrifilm.PetrifilmTestListActivity.PopulatePetrifilmTestsListTask;

import co.mwater.clientapp.R;

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
    private static final String TAG = "co.mwater.clientapp";

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
                                    "Unable to focus. Please try again and/or move camera farther away", Toast.LENGTH_SHORT)
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
        boolean fileError = false;
        
        // Remove view
        PetrifilmCameraView previewView = (PetrifilmCameraView) findViewById(R.id.CameraView);
        ViewGroup mainView = (ViewGroup) findViewById(R.id.RelativeLayout1);
        mainView.removeView(previewView);

        String filename = getIntent().getStringExtra("filename");
        //String filepath = App.getOriginalImageFolder(this) + File.separator // AR - inside try-catch
        //		+ filename;
        FileOutputStream fos = null;
        try {
            String filepath = App.getOriginalImageFolder(getApplicationContext()) + File.separator + filename;
            fos = new FileOutputStream(filepath);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.toString());
            fileError = true;
            //return;
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            fileError = true;
            //return;
        }
        // AR - Trying to close the handle (if the error is on the fos.write
        // you will have an dangling handle to a file)
        if (fileError == true && fos != null) {
            try {
                 fos.close();
            } catch (IOException e) {
                // TODO: handle exception
            }
        }
        
        if (!fileError) {
            Log.d(TAG, "Wrote file " + filename);

            Intent result = new Intent();
            result.putExtra("filename", filename);
            setResult(RESULT_OK, result);
        } else {
            Log.d(TAG, "Error writing the file " + filename);
            // TODO AR handle this in a better way
        }
        finish();
    }

}
