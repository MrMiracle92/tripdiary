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
import android.location.Location;
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
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.google.code.p.tripdiary.TripEntry.MediaType;

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
	private TripEntriesOverlay mMediaItemizedOverlay;
	private TripTrackOverlay mTrackOverlay;
	private MyLocationOverlay mMyLocationOverlay;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.trip_map);
		mapView = (MapView) findViewById(R.id.mapview);
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
		mStorageMgr = TripStorageManagerFactory
				.getTripStorageManager(getApplicationContext());

		// create the cursor
		mTripEntries = mStorageMgr.getEntriesForTrip(thisTripId);

		mStartMarker = getResources().getDrawable(R.drawable.marker_start);
		mEndMarker = getResources().getDrawable(R.drawable.marker_end);
		mMarker = getResources().getDrawable(R.drawable.marker);

		// create and add overlays to map
		mTrackOverlay = new TripTrackOverlay();
		mapView.getOverlays().add(mTrackOverlay);
		mMediaItemizedOverlay = new TripEntriesOverlay(mMarker, this);
		mapView.getOverlays().add(mMediaItemizedOverlay);

		// add the my location overlay and add to map
		mMyLocationOverlay = new MyLocationOverlay(getBaseContext(), mapView);
		mapView.getOverlays().add(mMyLocationOverlay);

		// buttons
		ImageButton btnToggleTrack = ((ImageButton) findViewById(R.id.btnToggleTrack));
		btnToggleTrack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showOrHideTrack();
			}
		});

		ImageButton btnCurrLoc = ((ImageButton) findViewById(R.id.btnCurrLoc));
		btnCurrLoc.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleOrShowCurrentLocation();
			}
		});

		final ImageButton btnZoomToFit = ((ImageButton) findViewById(R.id.btnZoomToFit));
		final ImageButton btnZoomOut = ((ImageButton) findViewById(R.id.btnZoomOut));
		final ImageButton btnZoomIn = ((ImageButton) findViewById(R.id.btnZoomIn));
		btnZoomOut.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean isLimit = mMapController.zoomOut();
				btnZoomOut.setEnabled(isLimit);
				btnZoomIn.setEnabled(true);
			}
		});

		btnZoomIn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean isLimit = mMapController.zoomIn();
				btnZoomIn.setEnabled(isLimit);
				btnZoomOut.setEnabled(true);
			}
		});

		btnZoomToFit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				zoomToFitAndCenter();
				btnZoomIn.setEnabled(true);
				btnZoomOut.setEnabled(true);
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
		if (mMyLocationOverlay.isMyLocationEnabled()) {
			mMyLocationOverlay.disableMyLocation();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshMap();
	}

	private double mMinLat;
	private double mMaxLat;
	private double mMinLon;
	private double mMaxLon;
	private boolean mIsInited = false;

	private void refreshMap() {
		mTripEntries.requery();
		mTripEntries.moveToFirst();
		mTrackOverlay.clear();
		mMediaItemizedOverlay.clear();
		mIsInited = false;
		while (mTripEntries.getCount() > 0 && !mTripEntries.isAfterLast()) {
			double lat = mTripEntries.getDouble(mTripEntries
					.getColumnIndex(DbDefs.TripDetailCols.LAT));
			double lon = mTripEntries.getDouble(mTripEntries
					.getColumnIndex(DbDefs.TripDetailCols.LON));
			if (lat == AppDataDefs.LAT_UNKNOWN
					|| lon == AppDataDefs.LON_UNKNOWN) {
				// let's skip and log
				mTripEntries.moveToNext();
				TripDiaryLogger
						.logWarning("Skipped on map for unknown location: ["
								+ lat + ", " + lon + "]");
				continue;
			}
			if (!mIsInited) {
				mMinLat = mMaxLat = lat;
				mMinLon = mMaxLon = lon;
				mIsInited = true;
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
					.getColumnIndex(DbDefs.TripDetailCols._ID));
			GeoPoint point = new GeoPoint((int) (lat * 1e6), (int) (lon * 1e6));

			// all points go to the track
			mTrackOverlay.addTrackItem(point);

			// now for the overlay items
			OverlayItem o = new OverlayItem(point, title == null ? "" : title,
					snippet == null ? "" : snippet);

			// don't draw marker for non media points except for the start and
			// end
			boolean drawMarker = mediaType.equals(TripEntry.MediaType.NONE
					.name()) ? false : true;
			if (mTripEntries.isFirst()) {
				o.setMarker(mStartMarker);
				drawMarker = true; // draw anyway
			} else if (mTripEntries.isLast()) {
				o.setMarker(mEndMarker);
				drawMarker = true; // draw anyway
			}

			if (drawMarker) {
				mMediaItemizedOverlay.addOverlayItem(o, drawMarker);
			}
			mTripEntries.moveToNext();
		}
	}

	private Drawable mStartMarker;
	private Drawable mEndMarker;
	private Drawable mMarker;
	static final private int PATH_COLOR = Color.rgb(139, 69, 19);

	class TripEntriesOverlay extends ItemizedOverlay<OverlayItem> {

		Context mContext = null;

		public TripEntriesOverlay(Drawable drawable, Context context) {
			super(boundCenterBottom(drawable));
			boundCenterBottom(mStartMarker);
			boundCenterBottom(mEndMarker);
			mContext = context;
		}

		private List<OverlayItem> mOverlaysMarkers = new ArrayList<OverlayItem>();

		public void addOverlayItem(OverlayItem overlay, boolean drawMarker) {
			mOverlaysMarkers.add(overlay);
			populate();
		}

		public void clear() {
			mOverlaysMarkers.clear();
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
		protected boolean onTap(int index) {
			OverlayItem item = mOverlaysMarkers.get(index);
			TripEntry te = mStorageMgr.getTripEntry(Long.parseLong(item
					.getSnippet()));
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
					TripMapActivity.this);
			dialogBuilder.setMessage(te.toStringMultiline());
			if (te.mediaType == MediaType.NONE) {
				dialogBuilder.setTitle("Location");
			} else {
				dialogBuilder.setTitle(te.mediaType.name());
			}
			dialogBuilder.show();
			return true;
		}
	}

	class TripTrackOverlay extends Overlay {

		private List<GeoPoint> mOverlayItems = new ArrayList<GeoPoint>();

		public void addTrackItem(GeoPoint point) {
			mOverlayItems.add(point);
		}

		public void clear() {
			mOverlayItems.clear();
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {

			// skip shadow
			if (shadow) {
				return;
			}

			// draw the track...
			Paint paint;
			paint = new Paint();
			paint.setColor(PATH_COLOR);
			paint.setAntiAlias(true);
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(4);
			GeoPoint lastGp = null;
			GeoPoint currGp = null;
			for (GeoPoint point : mOverlayItems) {
				if (currGp == null) {
					// this is the first point, so save and skip
					currGp = point;
					continue;
				}
				lastGp = currGp;
				currGp = point;
				Point pt1 = new Point();
				Point pt2 = new Point();
				Projection projection = mapView.getProjection();
				projection.toPixels(lastGp, pt1);
				projection.toPixels(currGp, pt2);
				canvas.drawLine(pt1.x, pt1.y, pt2.x, pt2.y, paint);
			}
		}
	}

	private void zoomToFitAndCenter() {
		if (mIsInited) {
			mMapController.zoomToSpan((int) ((mMaxLat - mMinLat) * 1e6),
					(int) ((mMaxLon - mMinLon) * 1e6));
			mMapController.animateTo(new GeoPoint(
					(int) ((mMaxLat + mMinLat) * 1e6 / 2),
					(int) ((mMaxLon + mMinLon) * 1e6 / 2)));
		}
	}

	private void toggleOrShowCurrentLocation() {
		if (mMyLocationOverlay.isMyLocationEnabled()) {
			GeoPoint point = mMyLocationOverlay.getMyLocation();
			int minLat = mapView.getMapCenter().getLatitudeE6()
					- mapView.getLatitudeSpan() / 2;
			int maxLat = mapView.getMapCenter().getLatitudeE6()
					+ mapView.getLatitudeSpan() / 2;
			int minLon = mapView.getMapCenter().getLongitudeE6()
					- mapView.getLongitudeSpan() / 2;
			int maxLon = mapView.getMapCenter().getLongitudeE6()
					+ mapView.getLongitudeSpan() / 2;
			if (point.getLatitudeE6() < minLat
					|| point.getLatitudeE6() > maxLat
					|| point.getLatitudeE6() < minLon
					|| point.getLongitudeE6() > maxLon) {
				// current location is not visible, so animate to it
				mMapController.animateTo(point);
			} else {
				// current location is in visible area, so disable it
				mMyLocationOverlay.disableMyLocation();
			}
		} else {
			mMyLocationOverlay.enableMyLocation();
			mMyLocationOverlay.runOnFirstFix(new Runnable() {

				@Override
				public void run() {
					Location lastFix = mMyLocationOverlay.getLastFix();
					GeoPoint point = new GeoPoint(
							(int) (lastFix.getLatitude() * 1e6), (int) (lastFix
									.getLongitude() * 1e6));
					TripMapActivity.this.mMapController.animateTo(point);

				}
			});
		}
	}

	private void showOrHideTrack() {
		List<Overlay> overlays = mapView.getOverlays();
		if (overlays.contains(mTrackOverlay)) {
			overlays.remove(mTrackOverlay);
		} else {
			overlays.remove(mMediaItemizedOverlay); // remove and add to ensure
													// it is drawn over track
			overlays.add(mTrackOverlay);
			overlays.add(mMediaItemizedOverlay);
		}
		mapView.invalidate();
	}
}
