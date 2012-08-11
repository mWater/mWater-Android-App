package co.mwater.clientapp.ui;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import co.mwater.clientapp.LocationFinder;
import co.mwater.clientapp.LocationFinder.LocationFinderListener;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.db.OtherCodes;
import co.mwater.clientapp.db.SamplesTable;
import co.mwater.clientapp.db.SourceNotesTable;
import co.mwater.clientapp.db.SourcesTable;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class SourceDetailActivity extends DetailActivity implements LocationFinderListener {
	private static final String TAG = SourceDetailActivity.class.getSimpleName();
	LocationFinder locationFinder;
	boolean setLocationFlag;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.source_detail_activity);

		setLocationFlag = getIntent().getBooleanExtra("setLocation", false);

		// Set up fragments
		SampleListSummaryFragment sampleFragment = new SampleListSummaryFragment();
		Bundle args = new Bundle();
		args.putString("sourceUid", rowValues.getAsString(SourcesTable.COLUMN_UID));
		sampleFragment.setArguments(args);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.sample_list, sampleFragment).commit();

		SourceNoteListSummaryFragment sourceNoteFragment = new SourceNoteListSummaryFragment();
		args = new Bundle();
		args.putString("sourceUid", rowValues.getAsString(SourcesTable.COLUMN_UID));
		sourceNoteFragment.setArguments(args);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.note_list, sourceNoteFragment).commit();

		// Set up location service
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationFinder = new LocationFinder(locationManager);
	}

	@Override
	protected void onStart() {
		super.onStart();
		locationFinder.addLocationListener(this);
	}

	@Override
	protected void onStop() {
		locationFinder.removeLocationListener(this);
		super.onStop();
	}

	@Override
	protected void displayData() {
		getSupportActionBar().setTitle("Source " + rowValues.getAsString(SourcesTable.COLUMN_CODE));
		setControlText(R.id.name, rowValues.getAsString(SourcesTable.COLUMN_NAME));
		setControlText(R.id.desc, rowValues.getAsString(SourcesTable.COLUMN_DESC));

		// Look up type
		String[] sourceTypes = getResources().getStringArray(R.array.source_types);
		Integer sourceType = rowValues.getAsInteger(SourcesTable.COLUMN_SOURCE_TYPE);
		String sourceTypeText;
		if (sourceType == null || sourceType >= sourceTypes.length)
			sourceTypeText = "?";
		else
			sourceTypeText = sourceTypes[sourceType];
		setControlText(R.id.source_type, "Type: " + sourceTypeText);

		displayLocation();

		// Display photo
		displayImageButton(R.id.photo, SourcesTable.COLUMN_PHOTO, R.drawable.camera);
		
		// Enable/disable
		((Button)findViewById(R.id.locationSet)).setEnabled(isCreatedByMe());
	}

	public void onPhotoClick(View v) {
		String photoUid = rowValues.getAsString(SourcesTable.COLUMN_PHOTO);
		if (photoUid == null) {
			// Take photo
			takePhoto(SourcesTable.COLUMN_PHOTO);
		}
		else {
			// Display photo
			displayImage(SourcesTable.COLUMN_PHOTO);
		}
	}

	public void onBasicsClick(View v) {
		// TODO
		Toast.makeText(this, "To do", Toast.LENGTH_SHORT).show();
	}

	public void onAddSampleClick(View v) {
		// Create sample linked to source
		ContentValues values = new ContentValues();
		values.put(SamplesTable.COLUMN_SOURCE, rowValues.getAsString(SourcesTable.COLUMN_UID));
		values.put(SamplesTable.COLUMN_CODE, OtherCodes.getNewSampleCode(this));
		values.put(SamplesTable.COLUMN_SAMPLED_ON, System.currentTimeMillis() / 1000);
		values.put(SamplesTable.COLUMN_CREATED_BY, MWaterServer.getUsername(this));
		Uri sampleUri = getContentResolver().insert(MWaterContentProvider.SAMPLES_URI, values);

		// View sample
		Intent intent = new Intent(this, SampleDetailActivity.class);
		intent.putExtra("uri", sampleUri);
		startActivity(intent);
	}

	public void onAddTestClick(View v) {
		// Create sample linked to source
		ContentValues values = new ContentValues();
		values.put(SamplesTable.COLUMN_SOURCE, rowValues.getAsString(SourcesTable.COLUMN_UID));
		values.put(SamplesTable.COLUMN_CODE, OtherCodes.getNewSampleCode(this));
		values.put(SamplesTable.COLUMN_SAMPLED_ON, System.currentTimeMillis() / 1000);
		values.put(SamplesTable.COLUMN_CREATED_BY, MWaterServer.getUsername(this));
		Uri sampleUri = getContentResolver().insert(MWaterContentProvider.SAMPLES_URI, values);

		new TestCreator(this, sampleUri).create();
	}

	public void onAddNoteClick(View v) {
		// Create sample linked to source
		ContentValues values = new ContentValues();
		values.put(SourceNotesTable.COLUMN_SOURCE, rowValues.getAsString(SourcesTable.COLUMN_UID));
		values.put(SourceNotesTable.COLUMN_CREATED_ON, System.currentTimeMillis() / 1000);
		values.put(SourceNotesTable.COLUMN_CREATED_BY, MWaterServer.getUsername(this));
		Uri sourceNoteUri = getContentResolver().insert(MWaterContentProvider.SOURCE_NOTES_URI, values);

		// View sample
		Intent intent = new Intent(this, SourceNoteDetailActivity.class);
		intent.putExtra("uri", sourceNoteUri);
		startActivity(intent);
	}

	public void onLocationSetClick(View v) {
		setLocationFlag = true;
		attemptSetLocation();
		displayLocation();
	}

	public void onLocationMapClick(View v) {
		String mapUri = String.format("geo:%1$f,%2$f?q=%1$f,%2$f(%3$s)",
				rowValues.getAsDouble(SourcesTable.COLUMN_LAT),
				rowValues.getAsDouble(SourcesTable.COLUMN_LONG),
				Uri.encode(rowValues.getAsString(SourcesTable.COLUMN_CODE)));
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUri));
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.source_detail_menu, menu);

		// Add listeners
		menu.findItem(R.id.menu_star).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				// TODO
				Toast.makeText(SourceDetailActivity.this, "To do", Toast.LENGTH_SHORT).show();
				return true;
			}
		});

		menu.findItem(R.id.menu_delete).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				deleteSource();
				return true;
			}
		});
		menu.findItem(R.id.menu_delete).setEnabled(isCreatedByMe());

		return super.onCreateOptionsMenu(menu);
	}

	void deleteSource() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				getContentResolver().delete(uri, null, null);
				finish();
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Permanently delete source?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", null).show();
	}

	public void onLocationChanged(Location loc) {
		Log.d(TAG, String.format("onLocationChanged: acc=%f", loc.getAccuracy()));

		// If waiting to set location and sufficient accuracy and time
		if (setLocationFlag)
			attemptSetLocation();

		displayLocation();
	}

	private void attemptSetLocation() {
		Location lastLocation = locationFinder.getLastLocation();
		long age = System.currentTimeMillis() - lastLocation.getTime();

		// If recent (<2 min) and close 
		if (lastLocation.getAccuracy() < 100 && age < 1000 * 60 * 2)
		{
			ContentValues values = new ContentValues();
			values.put(SourcesTable.COLUMN_LAT, lastLocation.getLatitude());
			values.put(SourcesTable.COLUMN_LONG, lastLocation.getLongitude());
			getContentResolver().update(uri, values, null, null);
			setLocationFlag = false;
			return;
		}
	}

	private void displayLocation() {
		((Button) findViewById(R.id.locationMap)).setEnabled(hasLocation());

		if (setLocationFlag) {
			setControlText(R.id.locationText, "Setting location");
			((ProgressBar) findViewById(R.id.locationProgress)).setVisibility(View.VISIBLE);
			setControlText(R.id.accuracy, "");
			return;
		}

		if (hasLocation())
		{
			Location lastLocation = locationFinder.getLastLocation();

			if (lastLocation != null) {
				double lat = rowValues.getAsDouble(SourcesTable.COLUMN_LAT);
				double dlat = lat - lastLocation.getLatitude();
				double dlong = rowValues.getAsDouble(SourcesTable.COLUMN_LONG) - lastLocation.getLongitude();

				// Convert to relative position (approximate)
				double dy = dlat / 57.3 * 6371000;
				double dx = Math.cos(lat / 57.3) * dlong / 57.3 * 6371000;

				// Determine direction and angle
				double dist = Math.sqrt(dx * dx + dy * dy);
				double angle = 90 - (Math.atan2(dy, dx) * 57.3);
				if (angle < 0)
					angle += 360;
				if (angle > 360)
					angle -= 360;

				// Get approximate direction
				int compassDir = ((int) ((angle + 22.5) / 45)) % 8;
				String[] compassStrs = new String[] { "N", "NE", "E", "SE", "S", "SW", "W", "NW" };

				setControlText(R.id.locationText, String.format("%.0fm %s from here", dist, compassStrs[compassDir]));
				((ProgressBar) findViewById(R.id.locationProgress)).setVisibility(View.GONE);
				setControlText(R.id.accuracy, String.format("+/- %.1f", lastLocation.getAccuracy()));
			}
			else {
				setControlText(R.id.locationText, "Waiting for GPS");
				((ProgressBar) findViewById(R.id.locationProgress)).setVisibility(View.VISIBLE);
				setControlText(R.id.accuracy, "");
			}
		}
		else {
			setControlText(R.id.locationText, "Unspecified");
			((ProgressBar) findViewById(R.id.locationProgress)).setVisibility(View.GONE);
			setControlText(R.id.accuracy, "");
		}
	}

	boolean hasLocation() {
		return rowValues != null && (rowValues.get(SourcesTable.COLUMN_LAT)) != null && (rowValues.get(SourcesTable.COLUMN_LONG) != null);
	}
}
