package com.google.code.p.tripdiary;

import android.os.Bundle;

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
	private MapController mapController;
	private MapView mapView;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);

			setContentView(R.layout.trip_map);
			mapView = (MapView) findViewById(R.id.mapview);
			mapView.setBuiltInZoomControls(true);
			mapController = mapView.getController();

		} catch (Exception e) {
			
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	/**
	 * Turn off the location updates if we're paused
	 */
	@Override
	public void onPause() {
	    super.onPause();
	}

	/**
	 * Resume location updates if we're back
	 */
	@Override
	public void onResume() {
	    super.onResume();
	}
}
