package co.mwater.clientapp.ui.map;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import co.mwater.clientapp.R;
import co.mwater.clientapp.db.MWaterContentProvider;
import co.mwater.clientapp.ui.SourceDetailActivity;
import co.mwater.clientapp.ui.map.SourceItemizedOverlay.SourceTapped;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class SourceMapActivity extends SherlockMapActivity implements SourceTapped {
	MyLocationOverlay locationOverlay;
	MapController mapController;
	Cursor sourceCursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MapView mapView = new MapView(this, getMapAPIKey());
		mapView.setClickable(true);
		mapView.setSatellite(true);

		mapController = mapView.getController();

		locationOverlay = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(locationOverlay);

		// TODO move to loader
		sourceCursor = getContentResolver().query(MWaterContentProvider.SOURCES_URI, null, null, null, null);
		SourceItemizedOverlay sourceItemizedOverlay = new SourceItemizedOverlay(
				getApplicationContext(), getResources().getDrawable(R.drawable.marker), sourceCursor, this);
		mapView.getOverlays().add(sourceItemizedOverlay);

		// If provided with location, go there
		if (getIntent().hasExtra("latitude")) {
			double latitude = getIntent().getDoubleExtra("latitude", 0);
			double longitude = getIntent().getDoubleExtra("longitude", 0);
			mapController.setZoom(19);
			mapController.animateTo(new GeoPoint((int) (latitude * 1000000), (int) (longitude * 1000000)));
		}

		setContentView(mapView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.source_map_activity_menu, menu);

		// Add listeners
		menu.findItem(R.id.menu_goto_my_location).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				locationOverlay.runOnFirstFix(new Runnable() {
					public void run() {
						mapController.setZoom(16);
						mapController.animateTo(locationOverlay.getMyLocation());
					}
				});

				return true;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onDestroy() {
		sourceCursor.close();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		locationOverlay.disableMyLocation();
	}

	@Override
	protected void onResume() {
		super.onResume();
		locationOverlay.enableMyLocation();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public void onSourceTapped(long id) {
		// Launch source details
		Intent intent = new Intent(this, SourceDetailActivity.class);
		intent.putExtra("uri", Uri.withAppendedPath(MWaterContentProvider.SOURCES_URI, id + ""));
		startActivity(intent);
	}

	/**
	 * Gets map key
	 */
	private String getMapAPIKey() {
		if (isDebuggable())
			return "0ASvTqLNwKMHoI5MfnfFGA7QeD4HEzaC3oeyUQA";
		else
			return "0ASvTqLNwKMGTpR-dqEdmunhSHL8-fxDZQRkw7w";
	}

	/**
	 * Check if in debug mode for correct map key
	 * 
	 * @return
	 */
	private boolean isDebuggable()
	{
		boolean debuggable = false;

		PackageManager pm = getPackageManager();
		try
		{
			ApplicationInfo appinfo = pm.getApplicationInfo(getPackageName(), 0);
			debuggable = (0 != (appinfo.flags &= ApplicationInfo.FLAG_DEBUGGABLE));
		} catch (NameNotFoundException e)
		{
			/* debuggable variable will remain false */
		}

		return debuggable;
	}
}
