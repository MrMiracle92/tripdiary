package com.google.code.p.tripdiary;

import java.io.File;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
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
		
		fillDataUsingList();
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

	private void fillDataUsingList() {
		// get list of trips from storage manager
		ArrayList<TripDetail> tripList = new ArrayList<TripDetail>(
				storageMgr.getAllTrips());

		// set listeners for new trips and list items
		findViewById(R.id.tvStartNewTrip).setOnClickListener(
				new StartNewTripListener());
		getListView().setOnItemClickListener(new TripOnItemClickListener());

		// set the list adapter
		setListAdapter(new TripDetailAdapter(this, R.layout.trip_item, tripList));

	}

	private class TripDetailAdapter extends ArrayAdapter<TripDetail> {

		private ArrayList<TripDetail> items;

		public TripDetailAdapter(Context context, int textViewResourceId,
				ArrayList<TripDetail> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.trip_item, parent, false);
			}
			TripDetail t = items.get(position);
			if (t != null) {
				TextView tt = (TextView) v.findViewById(R.id.tripDetailText);
				ImageView it = (ImageView) v.findViewById(R.id.tripDetailImage);
				TextView tid = (TextView) v.findViewById(R.id.tripDetailId);
				if(tid != null) {
					tid.setText(Long.toString(t.getTripId()));
				}
				if (tt != null) {
					tt.setText(t.getName() + " - " + t.getTripDescription() + ".");
				}
				if (it != null) {
					if (t.getImageLocation() != null) {
						it.setImageURI(Uri.fromFile(new File(t
								.getImageLocation())));
					} else {
						it.setImageResource(R.drawable.defaultpicicon);
					}
				}
			}
			return v;
		}
	}

	private class StartNewTripListener implements OnClickListener {
		public void onClick(View v) {
			Intent intent = new Intent(TripListActivity.this, TripSettingsActivity.class);
			Log.d(TAG, "About to start activity for result");
			startActivityForResult(intent, SETTINGS_CREATE_NEW_TRIP);
		}

	}

	private class TripOnItemClickListener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			long tripId = Long.parseLong(((TextView)view.findViewById(R.id.tripDetailId)).getText().toString());
			Intent intent = new Intent(getApplicationContext(), TripViewActivity.class);
			intent.putExtra("tripId", tripId);
			Log.d(TAG, "About to start trip view activity for trip id " + tripId);
			startActivityForResult(intent, VIEW_TRIP);
		}
	}
}