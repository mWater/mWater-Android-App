package co.mwater.clientapp.ui.map;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import co.mwater.clientapp.R;
import android.app.Activity;
import android.os.Bundle;

public class SourceMapActivity extends MapActivity {
	MyLocationOverlay locationOverlay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		//setContentView(R.layout.map_activity);
		MapView mapView = new MapView(this, "0ASvTqLNwKMHoI5MfnfFGA7QeD4HEzaC3oeyUQA");
		mapView.setClickable(true);
		
		locationOverlay = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(locationOverlay);
		
		setContentView(mapView);
	}

	@Override
	protected void onPause() {
		super.onPause();
		locationOverlay.disableCompass();
		locationOverlay.disableMyLocation();
	}

	@Override
	protected void onResume() {
		super.onResume();
		locationOverlay.enableCompass();
		locationOverlay.enableMyLocation();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}
