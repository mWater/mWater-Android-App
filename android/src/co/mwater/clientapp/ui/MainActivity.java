package co.mwater.clientapp.ui;

import java.io.IOException;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.ImageStorage;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.dbsync.CompleteDataSlice;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main_activity_menu, menu);

		// Add listeners
		menu.findItem(R.id.menu_recreate_thumbnails).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				try {
					ImageStorage.recreateThumbnails(MainActivity.this);
				} catch (IOException ex) {
					Toast.makeText(MainActivity.this, ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
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
