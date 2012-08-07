package co.mwater.clientapp.ui;

import java.io.IOException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
			Intent intent = new Intent(this, SignupActivity.class);
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

		// Add listeners
		menu.findItem(R.id.menu_logout).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				MWaterServer.login(MainActivity.this, null, null, new ArrayList<String>());
				Intent intent = new Intent(MainActivity.this, SignupActivity.class);
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

	public void onSamplesClick(View v) {
		Intent intent = new Intent(this, SampleListActivity.class);
		startActivity(intent);
	}

	public void onTestsClick(View v) {
		Intent intent = new Intent(this, TestListActivity.class);
		startActivity(intent);
	}

	public void onSyncClick(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Synchronize");
		builder.setItems(R.array.synchronize_popup, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					SyncTask syncTask = new SyncTask(MainActivity.this);
					syncTask.execute(new CompleteDataSlice());
				}
				if (which == 1) {
					try {
						ImageUploadTask uploadTask = new ImageUploadTask(
								MWaterServer.createClient(MainActivity.this),
								MainActivity.this,
								ImageStorage.getPendingUids(MainActivity.this)
								);
						uploadTask.execute();
					} catch (IOException ex) {
						Toast.makeText(MainActivity.this, ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
					}
				}
			}
		}).show();
	}
}
