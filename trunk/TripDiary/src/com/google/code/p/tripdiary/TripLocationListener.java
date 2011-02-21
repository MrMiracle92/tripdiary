//package com.google.code.p.tripdiary;
//
//import android.location.Location;
//import android.os.Bundle;
//
//import com.google.android.maps.GeoPoint;
//
//public class TripLocationListener {
//
//	public TripLocationListener() {
//		// TODO Auto-generated constructor stub
//	}
//	
//	TripLocationListener(mapController)
//
//	public void onLocationChanged(Location location) {
//		if (location != null) {
//            GeoPoint p = new GeoPoint(
//                    (int) (location.getLatitude() * 1E6), 
//                    (int) (location.getLongitude() * 1E6));
//            mapController.animateTo(p);
//            mapController.setZoom(16);                
//            mapView.invalidate();  
//		}
//	}
//
//	public void onProviderDisabled(String provider) {
//		// TODO Auto-generated method stub
//
//	}
//
//	public void onProviderEnabled(String provider) {
//		// TODO Auto-generated method stub
//
//	}
//
//	public void onStatusChanged(String provider, int status, Bundle extras) {
//		// TODO Auto-generated method stub
//
//	}
//}
