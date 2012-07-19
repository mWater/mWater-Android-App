package co.mwater.clientapp.ui;

import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.dbsync.CompleteDataSlice;
import co.mwater.clientapp.dbsync.RESTClient;
import co.mwater.clientapp.petrifilmanalysis.PetrifilmImages;

import com.actionbarsherlock.app.SherlockActivity;
import co.mwater.clientapp.R;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends SherlockActivity {
	private static final String TAG = MainActivity.class.getCanonicalName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup application
		PetrifilmImages.setup(this);

		// Go to login screen if not logged in
		if (MWaterServer.getClientId(this) == null) {
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		
		setContentView(R.layout.main_activity);
	}

	public void onSourcesClick(View v) {
		Intent intent = new Intent(this, SourceListActivity.class);
		startActivity(intent);
	}

	public void onSamplesClick(View v) {
		Intent intent = new Intent(this, SampleListActivity.class);
		startActivity(intent);
	}

	public void onTestsClick(View v) {
		Intent intent = new Intent(this, TestListActivity.class);
		startActivity(intent);
	}

	public void onSyncClick(View v) {
		SyncTask syncTask = new SyncTask(this);
		syncTask.execute(new CompleteDataSlice());
	}
}
