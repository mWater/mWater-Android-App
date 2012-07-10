package com.github.androidimageprocessing.bacteria.ui;

import com.actionbarsherlock.app.SherlockActivity;
import com.github.androidimageprocessing.bacteria.App;
import com.github.androidimageprocessing.bacteria.R;
import com.github.androidimageprocessing.bacteria.db.MWaterServer;
import com.github.androidimageprocessing.bacteria.dbsync.CompleteDataSlice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends SherlockActivity {
	private static final String TAG = MainActivity.class.getCanonicalName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup application
		App.setup(this);

		// Go to login screen if not logged in
		if (MWaterServer.getClientId(this) == null) {
			Intent intent = new Intent(this, LoginActivity.class);
			startActivityForResult(intent, 0);
		}

		setContentView(R.layout.main_activity2);
	}

	public void onSourcesClick(View v) {
		Intent intent = new Intent(this, SourcesActivity.class);
		startActivity(intent);
	}

	public void onSamplesClick(View v) {
	}

	public void onTestsClick(View v) {
		Intent intent = new Intent(this, PetrifilmTestListActivity.class);
		startActivity(intent);
	}

	public void onSyncClick(View v) {
		SyncTask syncTask = new SyncTask(this);
		syncTask.execute(new CompleteDataSlice());
	}
}
