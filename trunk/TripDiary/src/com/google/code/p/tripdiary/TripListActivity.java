package com.google.code.p.tripdiary;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
 * This is where the application starts. This activity lists the trips, and
 * also provides the user an option to start a new trip.
 * 
 * @author Ankan Mukherjee
 */
public class TripListActivity extends ListActivity {
	private static String TAG="TripListActivity";

	private TripStorageManager storageMgr;
	private final int SETTINGS_CREATE_NEW_TRIP = 1;
	private final int VIEW_TRIP = 2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		storageMgr = TripStorageManagerFactory.getTripStorageManager();

		// get list of trips from storage manager
		Cursor tripCursor = storageMgr.getAllTrips();

		// set listeners for new trips and list items
		findViewById(R.id.tvStartNewTrip).setOnClickListener(
				new StartNewTripListener());
		getListView().setOnItemClickListener(new TripOnItemClickListener());

		// set the list adapter
		setListAdapter(new TripDetailAdapter(getApplicationContext(), 
				tripCursor, true));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == RESULT_CANCELED) {
			Log.d(TAG, "Sub Activity cancelled.");
		} else {
			switch(requestCode) {
			case SETTINGS_CREATE_NEW_TRIP:
				//TODO: new trip settings created.. check and proceed to current trip
			}
		}
	}

	private class TripDetailAdapter extends CursorAdapter {

		private int mTripIdIdx;
		private int mTripNameIdx;
		private int mTripDescriptionIdx;
		private int mTripImageIdx;

		public TripDetailAdapter(Context context, Cursor c, boolean autoRequery) {
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
			if (tvId != null) {
				tvId.setText(Long.toString(cursor.getLong(mTripIdIdx)));
			}
			if (tvText != null) {
				tvText.setText(cursor.getString(mTripNameIdx) + " - "
						+ cursor.getString(mTripDescriptionIdx) + ".");
			}
			if (ivImg != null) {
				Bitmap bm = BitmapFactory.decodeFile(cursor
						.getString(mTripImageIdx));
				if (bm != null) {
					ivImg.setImageBitmap(bm);
				} else {
					ivImg.setImageResource(R.drawable.defaultpicicon);
				}
			}
		}
	}

	private class StartNewTripListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(TripListActivity.this, TripSettingsActivity.class);
			intent.putExtra(TripSettingsActivity.KEY_IS_NEW_TRIP, true);
			Log.d(TAG, "About to start activity for result");
			startActivityForResult(intent, SETTINGS_CREATE_NEW_TRIP);
		}

	}

	private class TripOnItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			long tripId = Long.parseLong(((TextView)view.findViewById(R.id.tripDetailId)).getText().toString());
			Intent intent = new Intent(getApplicationContext(), TripViewActivity.class);
			intent.putExtra(TripViewActivity.KEY_TRIP_ID, tripId);
			Log.d(TAG, "About to start trip view activity for trip id " + tripId);
			startActivityForResult(intent, VIEW_TRIP);
		}
	}
}