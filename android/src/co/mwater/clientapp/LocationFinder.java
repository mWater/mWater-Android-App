package co.mwater.clientapp;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Takes various location services and combines to give best guess as to location
 * @author Clayton
 *
 */
public class LocationFinder implements LocationListener {
	ArrayList<LocationFinderListener> listeners = new ArrayList<LocationFinderListener>();
	LocationManager locationManager;
	List<String> locationProviders;
	Location lastLocation = null;
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	public LocationFinder(LocationManager locationManager) {
		this.locationManager = locationManager;

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

	public Location getLastLocation() {
		return lastLocation;
	}
	
	public void addLocationListener(LocationFinderListener l) {
		if (listeners.size() == 0) {
			for (String locationProvider : locationProviders)
				locationManager.requestLocationUpdates(locationProvider, 1000, 0, this);
		}
		listeners.add(l);
	}

	public void removeLocationListener(LocationFinderListener l) {
		listeners.remove(l);

		if (listeners.size() == 0) {
			locationManager.removeUpdates(this);
		}
	}

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

	public void onLocationChanged(Location location) {
		if (isBetterLocation(location, lastLocation))
			lastLocation = location;
		
		// Call listeners TODO only call if better?
		for (LocationFinderListener l : listeners)
			l.onLocationChanged(lastLocation);
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	public interface LocationFinderListener {
		public void onLocationChanged(Location location);
	}

}
