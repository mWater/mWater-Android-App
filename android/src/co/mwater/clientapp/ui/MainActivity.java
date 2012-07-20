package co.mwater.clientapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.dbsync.CompleteDataSlice;

import com.actionbarsherlock.app.SherlockActivity;

public class MainActivity extends SherlockActivity {
	private static final String TAG = MainActivity.class.getCanonicalName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Go to login screen if not logged in
		if (MWaterServer.getClientUid(this) == null) {
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
