package com.google.code.p.tripdiary;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.maps.MapActivity;
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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);

			setContentView(R.layout.trip_map);
			MapView mapView = (MapView) findViewById(R.id.mapview);

			mapView.setBuiltInZoomControls(true);

		} catch (Exception e) {
			System.out.println("Exception caught : " + e.getMessage());// TODO
																		// change
																		// to
																		// right
																		// exceptions
		}

		// Using locationManager class to obtain GPS locations
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		LocationListener locationListener = new tripDiaryLocationListener();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, locationListener);

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public class tripDiaryLocationListener implements LocationListener {

		public tripDiaryLocationListener() {
			// TODO Auto-generated constructor stub
		}

		public void onLocationChanged(Location location) {
			if (location != null) {
//				Toast.makeText(
//						getBaseContext(),
//						"Location changed : Lat: " + location.getLatitude()
//								+ " Lng: " + location.getLongitude(),
//						Toast.LENGTH_SHORT).show(); //TODO to be deleted with actual code

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
