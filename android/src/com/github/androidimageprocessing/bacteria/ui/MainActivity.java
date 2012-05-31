package com.github.androidimageprocessing.bacteria.ui;

import java.io.File;
import java.io.IOException;

import com.github.androidimageprocessing.bacteria.App;
import com.github.androidimageprocessing.bacteria.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {
    private static final String TAG = "com.github.androidimageprocessing.bacteria";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        try {
            // AR - it's better to check them here than inside the getters
            if (App.isExternalFilePresent(getApplicationContext(), App.FOLDER_ORIGINAL) == false) {
                File f = new File(App.getOriginalImageFolder(getApplicationContext()));
                f.mkdirs();
            }
            if (App.isExternalFilePresent(getApplicationContext(), App.FOLDER_RESULTS) == false) {
                File f = new File(App.getResultsFolder(getApplicationContext()));
                f.mkdirs();
            }
            if (App.isExternalFilePresent(getApplicationContext(), App.FOLDER_PROCESSED) == false) {
                File f = new File(App.getProcessedImageFolder(getApplicationContext()));
                f.mkdirs();
            }
        } catch (IOException e) {
            // TODO Handle this exception (popup a dialog with the error)
            Log.e(TAG, e.toString());
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
    }

    public void onTestsClick(View v) {
        Intent intent = new Intent(this,
                PetrifilmTestListActivity.class);
        startActivity(intent);
    }

    public void onResultsClick(View v) {
        Intent intent = new Intent(this,
                PetrifilmTestListActivity.class);
        startActivity(intent);
    }
}

