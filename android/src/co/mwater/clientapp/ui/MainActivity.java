package co.mwater.clientapp.ui;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.ImageManager;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.dbsync.CompleteDataSlice;
import co.mwater.clientapp.dbsync.SyncIntentService;
import co.mwater.clientapp.dbsync.WelcomeIntentService;
import co.mwater.clientapp.ui.map.SourceMapActivity;
import co.mwater.clientapp.util.ProgressTask;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class MainActivity extends SherlockFragmentActivity {
	private static final String TAG = MainActivity.class.getSimpleName();

	static boolean firstRun = true;
	static long lastSync = 0; // TODO move to preference file

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Go to login screen if not logged in
		if (MWaterServer.getClientUid(this) == null) {
			Intent intent = new Intent(this, SignupActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
			startActivity(intent);
			finish();
			return;
		}

		// Show welcome message if needed
		if (firstRun) {
			firstRun = false;

			Intent intent = new Intent(MainActivity.this, WelcomeIntentService.class);
			Log.d(TAG, "Calling welcome service");
			startService(intent);
		}

		// If autosync, perform sync
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("autosync", true))
		{
			if (System.currentTimeMillis() - lastSync > 5 * 60 * 1000) {
				// Start sync if starting up
				Intent intent = new Intent(MainActivity.this, SyncIntentService.class);
				intent.putExtra("includeImages", false);
				intent.putExtra("dataSlice", new CompleteDataSlice());
				Log.d(TAG, "Calling sync service");
				startService(intent);
			}
			lastSync = System.currentTimeMillis();
		}

		setContentView(R.layout.main_activity);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main_activity_menu, menu);

		// Add listeners
		menu.findItem(R.id.menu_logout).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				MWaterServer.login(MainActivity.this, null, null, new ArrayList<String>());
				Intent intent = new Intent(MainActivity.this, SignupActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				startActivity(intent);
				finish();
				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	public void onSourcesClick(View v) {
		Intent intent = new Intent(this, SourceListActivity.class);
		startActivity(intent);
	}

	public void onMapClick(View v) {
		Intent intent = new Intent(this, SourceMapActivity.class);
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
		// Start sync
		Intent intent = new Intent(MainActivity.this, SyncIntentService.class);
		intent.putExtra("includeImages", true);
		intent.putExtra("dataSlice", new CompleteDataSlice());

		Log.d(TAG, "Calling sync service");
		startService(intent);
	}

	public void onSettingsClick(View v) {
		Intent intent = new Intent(this, PrefActivity.class);
		startActivity(intent);
	}
}
