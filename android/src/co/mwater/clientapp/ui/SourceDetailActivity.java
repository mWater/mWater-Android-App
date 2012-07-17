package co.mwater.clientapp.ui;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.db.OtherCodes;
import co.mwater.clientapp.db.SamplesTable;
import co.mwater.clientapp.db.SourcesTable;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class SourceDetailActivity extends DetailActivity implements LocationListener {
	public static final String TAG = SourceDetailActivity.class.getSimpleName();
	LocationManager locationManager;
	String locationProvider;
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
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
		locationProvider = locationManager.getBestProvider(criteria, true);
	}

	@Override
	protected void onStart() {
		super.onStart();

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
	}

	public void onBasicsClick(View v) {
		// TODO
		Toast.makeText(this, "To do", Toast.LENGTH_SHORT).show();
	}

	public void onAddSampleClick(View v) {
		// TODO
		Toast.makeText(this, "To do", Toast.LENGTH_SHORT).show();
	}

	public void onAddTestClick(View v) {
		// Create sample linked to source
		ContentValues values = new ContentValues();
		values.put(SamplesTable.COLUMN_SOURCE, rowValues.getAsString(SourcesTable.COLUMN_UID));
		values.put(SamplesTable.COLUMN_CODE, OtherCodes.getNewCode(this));
		Uri sampleUri = getContentResolver().insert(MWaterContentProvider.SAMPLES_URI, values);

		new TestCreator(this, sampleUri).create();
	}

	public void onAddNoteClick(View v) {
		// TODO
		Toast.makeText(this, "To do", Toast.LENGTH_SHORT).show();
	}

	public void onLocationSetClick(View v) {
		setLocationFlag = true;
		displayLocation();
	}

	public void onLocationMapClick(View v) {
		// TODO
		Toast.makeText(this, "To do", Toast.LENGTH_SHORT).show();
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

		lastLocation = loc;

		// If waiting to set location
		if (setLocationFlag)
		{
			ContentValues values = new ContentValues();
			values.put(SourcesTable.COLUMN_LAT, loc.getLatitude());
			values.put(SourcesTable.COLUMN_LONG, loc.getLongitude());
			getContentResolver().update(uri, values, null, null);
			setLocationFlag = false;
			return;
		}

		displayLocation();
	}

	private void displayLocation() {
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
				int compassDir = (int) ((angle + 22.5) / 45);
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
}
