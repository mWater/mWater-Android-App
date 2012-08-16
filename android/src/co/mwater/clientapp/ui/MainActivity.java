package co.mwater.clientapp.ui;

import java.io.IOException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.ImageStorage;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.db.RiskCalculations;
import co.mwater.clientapp.db.SourceCodes;
import co.mwater.clientapp.dbsync.CompleteDataSlice;
import co.mwater.clientapp.dbsync.SyncIntentService;
import co.mwater.clientapp.petrifilmanalysis.PetriFilmProcessingIntentService;
import co.mwater.clientapp.ui.map.SourceMapActivity;

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

		// Add listeners
		menu.findItem(R.id.menu_recalculate_source_risks).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				RiskCalculations.updateSourcesRisk(MainActivity.this);
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
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Synchronize");
		builder.setItems(R.array.synchronize_popup, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Start sync
				Intent intent = new Intent(MainActivity.this, SyncIntentService.class);
				intent.putExtra("includeImages", which == 1);
				intent.putExtra("dataSlice", new CompleteDataSlice());

				Log.d(TAG, "Calling sync service");
				startService(intent);
			}
		}).show();
	}
}
