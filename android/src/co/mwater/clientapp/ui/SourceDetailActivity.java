package co.mwater.clientapp.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.MWaterServer;
import co.mwater.clientapp.db.OtherCodes;
import co.mwater.clientapp.db.SamplesTable;
import co.mwater.clientapp.db.SourcesTable;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class SourceDetailActivity extends DetailActivity implements LocationListener {
	private static final String TAG = SourceDetailActivity.class.getSimpleName();
	LocationManager locationManager;
	List<String> locationProviders;
	boolean setLocationFlag;

	Location lastLocation = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.source_detail_activity);

		setLocationFlag = getIntent().getBooleanExtra("setLocation", false);

		// Set up fragment
		SampleListSummaryFragment sampleFragment = new SampleListSummaryFragment();
		Bundle args = new Bundle();
		args.putString("sourceUid", rowValues.getAsString(SourcesTable.COLUMN_UID));
		sampleFragment.setArguments(args);
		getSupportFragmentManager().beginTransaction()
				.add(R.id.sample_list, sampleFragment).commit();

		// Set up location service
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationProviders = new ArrayList<String>();
		locationProviders.add(LocationManager.GPS_PROVIDER);
		locationProviders.add(LocationManager.NETWORK_PROVIDER);

		// Pick best old one first
		for (String locationProvider : locationProviders)
		{
			Location loc = locationManager.getLastKnownLocation(locationProvider);
			if (isBetterLocation(loc, lastLocation))
				lastLocation = loc;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		for (String locationProvider : locationProviders)
			locationManager.requestLocationUpdates(locationProvider, 1000, 0, this);
	}

	@Override
	protected void onStop() {
		super.onStop();

		locationManager.removeUpdates(this);
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
	}

	public void onPhotoClick(View v) {
		String photoUid =rowValues.getAsString(SourcesTable.COLUMN_PHOTO); 
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
		// TODO
		Toast.makeText(this, "To do", Toast.LENGTH_SHORT).show();
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

		if (isBetterLocation(loc, lastLocation))
			lastLocation = loc;

		// If waiting to set location and sufficient accuracy and time
		if (setLocationFlag)
			attemptSetLocation();

		displayLocation();
	}

	private void attemptSetLocation() {
		long age = System.currentTimeMillis() - lastLocation.getTime();

		if (lastLocation.getAccuracy() < 100 && age < TWO_MINUTES)
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
		return (rowValues.get(SourcesTable.COLUMN_LAT)) != null && (rowValues.get(SourcesTable.COLUMN_LONG) != null);
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onProviderDisabled");
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onProviderEnabled");
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onStatusChanged = " + status);
	}

	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 */
	boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
}
