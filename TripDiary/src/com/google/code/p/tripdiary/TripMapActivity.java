package com.google.code.p.tripdiary;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

/**
 * This activity manages the mapView
 * 
 * @author Arpita Saha
 * @author anKan
 * 
 */
public class TripMapActivity extends MapActivity {
	private LocationManager locationManager;
	private MapController mapController;
	private MapView mapView;
	
	private LocationListener locationListener;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);

			setContentView(R.layout.trip_map);
			mapView = (MapView) findViewById(R.id.mapview);
			mapView.setBuiltInZoomControls(true);
//			mapController = mapView.getController();

		} catch (Exception e) {
			
		}

		// Using locationManager class to obtain GPS locations
//		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

//		locationListener = new TripLocationListener();
//		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000L, 10.0f, locationListener);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	/**
	 * Turn off the location updates if we're paused
	 * @author arpitas
	 */
	@Override
	public void onPause() {
	    super.onPause();
//	    locationManager.removeUpdates(locationListener);
	}

	/**
	 * Resume location updates if we're paused
	 * @author arpitas
	 *
	 */
	@Override
	public void onResume() {
	    super.onResume();
//	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000L, 10.0f, locationListener);
	}
	
	
	/**
	 * Location listener class
	 * @author arpitas
	 *
	 */
	public class TripLocationListener implements LocationListener {

		public TripLocationListener() {
			// TODO Auto-generated constructor stub
		}

		public void onLocationChanged(Location location) {
			if (location != null) {
                GeoPoint p = new GeoPoint(
                        (int) (location.getLatitude() * 1E6), 
                        (int) (location.getLongitude() * 1E6));
                mapController.animateTo(p);
                mapController.setZoom(16);                
                mapView.invalidate();  
			}
		}

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}
	}
}
