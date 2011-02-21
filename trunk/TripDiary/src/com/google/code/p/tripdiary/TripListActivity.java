package com.google.code.p.tripdiary;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This is where the application starts. This activity lists the trips, and also
 * provides the user an option to start a new trip.
 * 
 * @author Ankan Mukherjee
 */
public class TripListActivity extends ListActivity {
	private static String TAG = "TripListActivity";

	private TripStorageManager mStorageMgr;
	private Cursor mTripCursor;
	private TripAdapter mTripAdapter;

	private final int SETTINGS_CREATE_NEW_TRIP = 1;
	private final int VIEW_TRIP = 2;

	// Note: After much tinkering around, it turns out that we need to create
	// this member for the preferences to get called. For details see
	// http://stackoverflow.com/questions/2542938/sharedpreferences-onsharedpreferencechangelistener-not-being-called-consistently/3104265#3104265
	SharedPreferences.OnSharedPreferenceChangeListener mPrefListener;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mStorageMgr = TripStorageManagerFactory.getTripStorageManager();

		// get list of trips from storage manager
		mTripCursor = mStorageMgr.getAllTrips();

		// create and set the list adapter
		mTripAdapter = new TripAdapter(getApplicationContext(), mTripCursor,
				true);
		setListAdapter(mTripAdapter);

		// set listeners for new trips and list items
		findViewById(R.id.tvStartNewTrip).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(TripListActivity.this,
								TripSettingsActivity.class);
						intent.putExtra(AppDataDefs.KEY_IS_NEW_TRIP, true);
						Log.d(TAG,
								"About to start Trip Settings activity for new trip.");
						startActivityForResult(intent, SETTINGS_CREATE_NEW_TRIP);
					}
				});
		
		// set listener for list item click
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				long tripId = Long.parseLong(((TextView) view
						.findViewById(R.id.tripDetailId)).getText().toString());
				Intent intent = new Intent(getApplicationContext(),
						TripViewActivity.class);
				intent.putExtra(AppDataDefs.KEY_TRIP_ID, tripId);
				Log.d(TAG, "About to start trip view activity for trip id "
						+ tripId);
				startActivityForResult(intent, VIEW_TRIP);
			}
		});

		// listen for changes to current trip
		mPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {
				if (key == AppDataDefs.CURRENT_TRIP_ID_KEY) {
					mTripAdapter.notifyDataSetChanged();
				}
			}
		};
		getApplicationContext().getSharedPreferences(AppDataDefs.APPDATA_FILE,
				MODE_PRIVATE).registerOnSharedPreferenceChangeListener(
				mPrefListener);
	}

	private long getCurrentTripId() {
		return getApplicationContext().getSharedPreferences(
				AppDataDefs.APPDATA_FILE, MODE_PRIVATE).getLong(
				AppDataDefs.CURRENT_TRIP_ID_KEY, AppDataDefs.NO_CURRENT_TRIP);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RESULT_CANCELED) {
			Log.d(TAG, "Sub Activity cancelled.");
		} else {
			switch (requestCode) {
			case SETTINGS_CREATE_NEW_TRIP:
				long tripId = getCurrentTripId();
				Intent intent = new Intent(getApplicationContext(),
						TripViewActivity.class);
				intent.putExtra(AppDataDefs.KEY_TRIP_ID, tripId);
				Log.d(TAG, "About to start trip view activity for trip id "
						+ tripId);
				startActivityForResult(intent, VIEW_TRIP);
			}
		}
	}

	private class TripAdapter extends CursorAdapter {

		private int mTripIdIdx;
		private int mTripNameIdx;
		private int mTripDescriptionIdx;
		private int mTripImageIdx;

		public TripAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);

			mTripIdIdx = c.getColumnIndex(DbDefs.TripCols._ID);
			mTripNameIdx = c.getColumnIndex(DbDefs.TripCols.TRIP_NAME);
			mTripDescriptionIdx = c
					.getColumnIndex(DbDefs.TripCols.TRIP_DESCRIPTION);
			mTripImageIdx = c
					.getColumnIndex(DbDefs.TripCols.THUMBNAIL_LOCATION);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return vi.inflate(R.layout.trip_item, parent, false);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView tvText = (TextView) view.findViewById(R.id.tripDetailText);
			ImageView ivImg = (ImageView) view
					.findViewById(R.id.tripDetailImage);
			TextView tvId = (TextView) view.findViewById(R.id.tripDetailId);
			if (tvText != null) {
				tvText.setText(cursor.getString(mTripNameIdx) + " - "
						+ cursor.getString(mTripDescriptionIdx) + ".");
			}
			if (ivImg != null) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 4;
				options.inTempStorage = new byte[16*1024];
				Bitmap bm = BitmapFactory.decodeFile(cursor
						.getString(mTripImageIdx), options);
				if (bm != null) {
//					ivImg.setImageBitmap(ThumbnailUtils.extractThumbnail(bm, 50, 50));
					ivImg.setImageBitmap(bm);
				} else {
					ivImg.setImageResource(R.drawable.defaultpicicon);
				}
			}
			long tripId = cursor.getLong(mTripIdIdx);
			if (tvId != null) {
				tvId.setText(Long.toString(tripId));
			}
			View currTripIndicator = view
					.findViewById(R.id.tripDetailCurrIndicator);
			long currTrip = getCurrentTripId();
			boolean isCurrentTrip = (currTrip == tripId)
					&& (currTrip != AppDataDefs.NO_CURRENT_TRIP) ? true : false;
			if (isCurrentTrip) {
				currTripIndicator
						.setBackgroundResource(R.drawable.currtripbackground);
			} else {
				currTripIndicator.setBackgroundResource(0);
			}
		}
	}
}