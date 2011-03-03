package com.google.code.p.tripdiary;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

/**
 * This activity manages the mapView
 * 
 * @author Arpita Saha
 * @author Ankan Mukherjee
 * 
 */
public class TripMapActivity extends MapActivity {
	private MapController mMapController;
	private MapView mapView;
	private long thisTripId = AppDataDefs.NO_CURRENT_TRIP;
	private TripStorageManager mStorageMgr;
	private Cursor mTripEntries;
	private TripOverlay mItemizedoverlay;
	private MyLocationOverlay mMyLocationOverlay;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.trip_map);
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mMapController = mapView.getController();
		
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
		
		mStartMarker = getResources().getDrawable(R.drawable.marker_start);
		mEndMarker = getResources().getDrawable(R.drawable.marker_end);
		mMarker = getResources().getDrawable(R.drawable.marker);
		
		// create and add overlay to map
		mItemizedoverlay = new TripOverlay(mMarker, this);
		mapView.getOverlays().add(mItemizedoverlay);
		
		// add the my location overlay and add to map
		mMyLocationOverlay = new MyLocationOverlay(getBaseContext(), mapView);
		mapView.getOverlays().add(mMyLocationOverlay);
		
		// buttons
		ImageButton btnZoomTofit = ((ImageButton) findViewById(R.id.btnZoomToFit));
		btnZoomTofit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				zoomToFitAndCenter();
			}
		});
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	public void onPause() {
	    super.onPause();
	    mMyLocationOverlay.disableMyLocation();
	}

	@Override
	public void onResume() {
	    super.onResume();
	    mMyLocationOverlay.enableMyLocation();
	    refreshMap();
	}
	
	private double mMinLat;
	private double mMaxLat;
	private double mMinLon;
	private double mMaxLon;
	
	private void refreshMap() {
		mTripEntries.requery();
		mTripEntries.moveToFirst();
		boolean isInited = false;
		while (!mTripEntries.isAfterLast()) {
			double lat = mTripEntries.getDouble(mTripEntries
					.getColumnIndex(DbDefs.TripDetailCols.LAT));
			double lon = mTripEntries.getDouble(mTripEntries
					.getColumnIndex(DbDefs.TripDetailCols.LON));
			if(!isInited) {
				mMinLat = mMaxLat = lat;
				mMinLon = mMaxLon = lon;
				isInited = true;
			} else {
				mMinLat = mMinLat < lat ? mMinLat : lat;
				mMaxLat = mMaxLat > lat ? mMaxLat : lat;
				mMinLon = mMinLon < lat ? mMinLon : lon;
				mMaxLon = mMaxLon > lat ? mMaxLon : lon;
			}
			String mediaType = mTripEntries.getString(mTripEntries
					.getColumnIndex(DbDefs.TripDetailCols.MEDIA_TYPE));
			String title = mTripEntries.getString(mTripEntries
					.getColumnIndex(DbDefs.TripDetailCols.NOTE));
			String snippet = mTripEntries.getString(mTripEntries
					.getColumnIndex(DbDefs.TripDetailCols.MEDIA_TYPE));
			GeoPoint point = new GeoPoint((int) (lat * 1e6), (int) (lon * 1e6));
			TripDiaryLogger.logDebug("Point: " + point.getLatitudeE6() + ", "
					+ point.getLongitudeE6());
			OverlayItem o = new OverlayItem(point, title == null ? "" : title,
					snippet == null ? "" : snippet);
			
			// don't draw marker for non media points
			boolean drawMarker = mediaType.equals(TripEntry.MediaType.NONE.name())? false : true;
			if(mTripEntries.isFirst()) {
				o.setMarker(mStartMarker);
				drawMarker = true; // draw anyway
			} else if(mTripEntries.isLast()) {
				o.setMarker(mEndMarker);
				drawMarker = true; // draw anyway
			}
			mItemizedoverlay.addOverlay(o, drawMarker);
			mTripEntries.moveToNext();
		}
	}
	
	private Drawable mStartMarker;
	private Drawable mEndMarker;
	private Drawable mMarker;
	static final private int PATH_COLOR = Color.rgb(139, 69, 19);
	
	class TripOverlay extends ItemizedOverlay<OverlayItem> {

		Context mContext = null;
		public TripOverlay(Drawable drawable, Context context) {
			super(boundCenterBottom(drawable));
			boundCenterBottom(mStartMarker);
			boundCenterBottom(mEndMarker);
			mContext = context;
		}
		
		private List<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
		private List<OverlayItem> mOverlaysMarkers = new ArrayList<OverlayItem>();
		
		public void addOverlay(OverlayItem overlay, boolean drawMarker) {
			mOverlays.add(overlay);
			if(drawMarker) {
				mOverlaysMarkers.add(overlay);
			}
			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return mOverlaysMarkers.get(i);
		}

		@Override
		public int size() {
			return mOverlaysMarkers.size();
		}
		
		@Override
		public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
				long when) {
			
			// draw the track first...
			Paint paint;
			paint = new Paint();
			paint.setColor(PATH_COLOR);
			paint.setAntiAlias(true);
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(2);
			GeoPoint lastGp = null;
			GeoPoint currGp = null;
			for (OverlayItem item : mOverlays) {
				if(currGp == null) {
					// this is the first point, so save and skip
					currGp = item.getPoint();
					continue;
				}
				lastGp = currGp;
				currGp = item.getPoint();
				Point pt1 = new Point();
				Point pt2 = new Point();
				Projection projection = mapView.getProjection();
				projection.toPixels(lastGp, pt1);
				projection.toPixels(currGp, pt2);
				canvas.drawLine(pt1.x, pt1.y, pt2.x, pt2.y, paint);
			}
			
			// ... and now draw the markers (default behavior)
			super.draw(canvas, mapView, shadow);
			
			return false;
		}

		@Override
		protected boolean onTap(int index) {
		  OverlayItem item = mOverlays.get(index);
		  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		  dialog.setTitle(item.getTitle());
		  dialog.setMessage(item.getPoint().getLatitudeE6() + ", " + item.getPoint().getLongitudeE6());
		  dialog.show();
		  return true;
		}
	}
	
	private void zoomToFitAndCenter() {
		  mMapController.zoomToSpan((int)((mMaxLat - mMinLat) * 1e6), (int)((mMaxLon - mMinLon) * 1e6));
		  mMapController.animateTo(new GeoPoint((int)((mMaxLat + mMinLat) * 1e6 / 2), (int)((mMaxLon + mMinLon) * 1e6 / 2)));
	}
}
