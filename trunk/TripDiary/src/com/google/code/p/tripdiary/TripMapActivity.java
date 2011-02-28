package com.google.code.p.tripdiary;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * This activity manages the mapView
 * 
 * @author Arpita Saha
 * @author Ankan Mukherjee
 * 
 */
public class TripMapActivity extends MapActivity {
//	private MapController mapController;
	private MapView mapView;
	private long thisTripId = AppDataDefs.NO_CURRENT_TRIP;
	private TripStorageManager mStorageMgr;
	private Cursor mTripEntries;
	private TripOverlay mItemizedoverlay;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.trip_map);
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
//		mapController = mapView.getController();
		
		// if the activity is resumed
		thisTripId = savedInstanceState != null ? savedInstanceState
				.getLong(AppDataDefs.KEY_TRIP_ID) : 0;

		// if there is a bundle get the trip id
		if (thisTripId == 0) {
			Bundle extras = getIntent().getExtras();
			thisTripId = extras != null ? extras
					.getLong(AppDataDefs.KEY_TRIP_ID) : 0;

			// by now there should be a trip id
			if (thisTripId == 0) {
				TripDiaryLogger.logError("Could not find trip.");
				setResult(RESULT_CANCELED);
				finish();
				return;
			}
		}

		// get the storage manager
		mStorageMgr = TripStorageManagerFactory.getTripStorageManager(getApplicationContext());
		
		// create the cursor
		mTripEntries = mStorageMgr.getEntriesForTrip(thisTripId);
		
		// create and add overlay to map
		mItemizedoverlay = new TripOverlay(getResources().getDrawable(R.drawable.androidmarker), this);		
		mapView.getOverlays().add(mItemizedoverlay);
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
	    refreshMap();
	}
	
	private void refreshMap() {
		mTripEntries.requery();
		mTripEntries.moveToFirst();
		while (!mTripEntries.isAfterLast()) {
			double lat = mTripEntries.getDouble(mTripEntries
					.getColumnIndex(DbDefs.TripDetailCols.LAT));
			double lon = mTripEntries.getDouble(mTripEntries
					.getColumnIndex(DbDefs.TripDetailCols.LON));
			String title = mTripEntries.getString(mTripEntries
					.getColumnIndex(DbDefs.TripDetailCols.NOTE));
			String snippet = mTripEntries.getString(mTripEntries
					.getColumnIndex(DbDefs.TripDetailCols.MEDIA_TYPE));
			GeoPoint point = new GeoPoint((int) (lat * 1e6), (int) (lon * 1e6));
			TripDiaryLogger.logDebug("Point: " + point.getLatitudeE6() + ", "
					+ point.getLongitudeE6());
			OverlayItem o = new OverlayItem(point, title == null ? "" : title,
					snippet == null ? "" : snippet);
			mItemizedoverlay.addOverlay(o);
			mTripEntries.moveToNext();
		}
	}
	
	class TripOverlay extends ItemizedOverlay<OverlayItem> {

		Context mContext = null;
		public TripOverlay(Drawable drawable, Context context) {
			super(boundCenterBottom(drawable));	
			mContext = context;
		}
		
		private List<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
		public void addOverlay(OverlayItem overlay) {
			mOverlays.add(overlay);
			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return mOverlays.get(i);
		}

		@Override
		public int size() {
			return mOverlays.size();
		}
		
		@Override
		protected boolean onTap(int index) {
		  OverlayItem item = mOverlays.get(index);
		  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		  dialog.setTitle(item.getTitle());
		  dialog.setMessage(item.getSnippet());
		  dialog.show();
		  return true;
		}
		
	}
}
