package com.github.androidimageprocessing.bacteria.ui;

import com.github.androidimageprocessing.bacteria.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
	private static final String TAG = "com.github.androidimageprocessing.bacteria";

	@Override
	public void onCreate(Bundle savedInstanceState) {
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

