package com.google.code.p.tripdiary;

import java.text.DateFormat;
import java.util.Date;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.code.p.tripdiary.TripEntry.MediaType;

/**
 * This is where the application starts. This activity lists the trips, and also
 * provides the user an option to start a new trip.
 * 
 * @author Ankan Mukherjee
 */
public class TripListActivity extends ListActivity {

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

		mStorageMgr = TripStorageManagerFactory
				.getTripStorageManager(getApplicationContext());

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
						tripDiaryLogger.logDebug(
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
				tripDiaryLogger.logDebug( "About to start trip view activity for trip id "
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getApplicationContext().getSharedPreferences(AppDataDefs.APPDATA_FILE,
				MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(
				mPrefListener);
	}

	private long getCurrentTripId() {
		return getApplicationContext().getSharedPreferences(
				AppDataDefs.APPDATA_FILE, MODE_PRIVATE).getLong(
				AppDataDefs.CURRENT_TRIP_ID_KEY, AppDataDefs.NO_CURRENT_TRIP);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mTripCursor.requery();
		mTripAdapter.notifyDataSetInvalidated();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CANCELED) {
			tripDiaryLogger.logDebug( "Sub Activity cancelled.");
		} else {
			mTripAdapter.notifyDataSetChanged();
			switch (requestCode) {
			case SETTINGS_CREATE_NEW_TRIP:
				long tripId = getCurrentTripId();
				Intent intent = new Intent(getApplicationContext(),
						TripViewActivity.class);
				intent.putExtra(AppDataDefs.KEY_TRIP_ID, tripId);
				tripDiaryLogger.logDebug( "About to start trip view activity for trip id "
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
		private int mTripCreateTimeIdx;

		public TripAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);

			mTripIdIdx = c.getColumnIndex(DbDefs.TripCols._ID);
			mTripNameIdx = c.getColumnIndex(DbDefs.TripCols.TRIP_NAME);
			mTripDescriptionIdx = c
					.getColumnIndex(DbDefs.TripCols.TRIP_DESCRIPTION);
			mTripImageIdx = c
					.getColumnIndex(DbDefs.TripCols.THUMBNAIL_LOCATION);
			mTripCreateTimeIdx = c.getColumnIndex(DbDefs.TripCols.CREATE_TIME);
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
			TextView tvItemDate = (TextView) view
					.findViewById(R.id.tripItemDate);
			long tripId = cursor.getLong(mTripIdIdx);
			if (tvId != null) {
				tvId.setText(Long.toString(tripId));
			}
			if (tvText != null) {
				tvText.setText(cursor.getString(mTripNameIdx) + " - "
						+ cursor.getString(mTripDescriptionIdx));
			}
			if (tvItemDate != null) {
				long createTime = cursor.getLong(mTripCreateTimeIdx);
				long updateTime = mStorageMgr.getLastUpdatedTime(tripId);
				DateFormat df = DateFormat.getDateInstance();
				StringBuffer dateString = new StringBuffer();
				dateString
						.append("Created: ")
						.append(df.format(new Date(createTime)))
						.append(" | Updated: ")
						.append(updateTime < 0 ? "Never" : df.format(new Date(
								updateTime)));
				tvItemDate.setText(dateString);
			}
			String imagePath = cursor.getString(mTripImageIdx);
			if (imagePath != null && ivImg != null) {
				ImageCache.getInstance().setBitmapThreaded(imagePath,
						MediaType.PHOTO, ivImg);
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