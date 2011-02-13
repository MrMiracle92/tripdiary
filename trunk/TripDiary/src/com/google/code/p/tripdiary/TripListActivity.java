package com.google.code.p.tripdiary;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This is where the application starts. This activity lists out the trip, and
 * also provides the user an option to start a new trip.
 * 
 * @author Ankan Mukherjee
 */
public class TripListActivity extends ListActivity {
	// private static String TAG="TripListActivity";

	TripStorageManager storageMgr;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// TODO:need to use actual TripStorageManager impl
		storageMgr = new TripStorageManagerFake();

		fillDataUsingList();

	}

	private void fillDataUsingList() {
		// get list of trips from storage manager
		ArrayList<TripDetail> tripList = new ArrayList<TripDetail>(
				storageMgr.getAllTrips());
		
		// set the on click listener for new trips
		findViewById(R.id.tvStartNewTrip).setOnClickListener(new StartNewTripListener());

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
				if (tt != null) {
					tt.setText(t.getName() + " [" + t.getImageLocation() + "]");
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
			// TODO need to implement
			
			
			AlertDialog alertDialog = new AlertDialog.Builder(v.getContext()).create();
			alertDialog.setTitle("New Trip...");
			alertDialog.setMessage("Have to start a new trip now.. (not implemented yet!)");
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			   public void onClick(DialogInterface dialog, int which) {
			      // here you can add functions
			   }
			});
			alertDialog.setIcon(R.drawable.icon);
			alertDialog.show();
		}
		
	}

	/*
	 * TODO:Fake classes.. to be removed later
	 */
	private class TripStorageManagerFake implements TripStorageManager {
		private final String TAG = "TripStorageManagerFake";

		// list of photos
		private File[] photos = null;

		TripStorageManagerFake() {
			File dcimDir = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
			File photosDir = new File(dcimDir.getAbsolutePath()
					+ "/.thumbnails");
			if (photosDir.exists() && photosDir.isDirectory()) {
				photos = photosDir.listFiles(new FilenameFilter() {

					public boolean accept(File dir, String filename) {
						Log.d(TAG, filename);
						if (filename.endsWith("jpg")) {
							return true;
						}
						return false;
					}
				});
			}
		}

		public void updateTrip(long tripId, String name,
				String tripDescription, boolean traceRouteEnabled)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub

		}

		public TripDetail getTripDetail(long tripId)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		public List<TripEntry> getEntriesForTrip(long tripId)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return null;
		}

		// just a dummy method to create trips, pointing to a few camera
		// picture thumbnails (if any)
		public List<TripDetail> getAllTrips() {
			List<TripDetail> trips = new ArrayList<TripDetail>();

			for (int i = 0; i < 20; i++) {
				TripDetail detail = new TripDetail();
				detail.name = "Fake Trip " + i;
				if (photos != null && photos.length > i) {
					detail.imageLocation = photos[i].getAbsolutePath();
					Log.d(TAG, detail.imageLocation);
				}

				trips.add(detail);
			}

			return trips;
		}

		public long createNewTrip(String name, String tripDescription,
				boolean traceRouteEnabled) {
			// TODO Auto-generated method stub
			return 0;
		}

		public boolean addTripEntry(long tripId, TripEntry tripEntry)
				throws IllegalArgumentException {
			// TODO Auto-generated method stub
			return false;
		}
	}
}