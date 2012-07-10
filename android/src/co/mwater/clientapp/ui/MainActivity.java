package co.mwater.clientapp.ui;

import co.mwater.clientapp.App;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.dbsync.CompleteDataSlice;
import co.mwater.clientapp.ui.petrifilm.PetrifilmTestListActivity;

import com.actionbarsherlock.app.SherlockActivity;
import co.mwater.clientapp.R;

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
