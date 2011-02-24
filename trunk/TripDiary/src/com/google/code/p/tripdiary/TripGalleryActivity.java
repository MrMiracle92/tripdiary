/**
 * 
 */
package com.google.code.p.tripdiary;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.google.code.p.tripdiary.TripEntry.MediaType;

/**
 * This is the activity that shows the recorded trip media in a gallery view.
 * 
 * @author Ankan Mukherjee
 * 
 */
public class TripGalleryActivity extends Activity {
	private final static String TAG = "TripGalleryActivity";

	private TripStorageManager mStorageMgr;

	private long thisTripId = AppDataDefs.NO_CURRENT_TRIP;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trip_view_grid);

		// if the activity is resumed
		thisTripId = savedInstanceState != null ? savedInstanceState
				.getLong(AppDataDefs.KEY_TRIP_ID) : 0;

		// if there is a bundle set tab based on whether trip is current
		if (thisTripId == 0) {
			Bundle extras = getIntent().getExtras();
			thisTripId = extras != null ? extras
					.getLong(AppDataDefs.KEY_TRIP_ID) : 0;

			// by now there should be a trip id
			if (thisTripId == 0) {
				Log.e(TAG, "Could not find trip.");
				setResult(RESULT_CANCELED);
				finish();
				return;
			}
		}

		// get the storage manager
		mStorageMgr = TripStorageManagerFactory.getTripStorageManager();

		Cursor tripEntryCursor = mStorageMgr.getEntriesForTrip(thisTripId);
		GridView gridview = (GridView) findViewById(R.id.gridview);
		gridview.setAdapter(new TripEntryAdapter(getApplicationContext(),
				tripEntryCursor, true));

		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				TripEntry te = mStorageMgr.getTripEntry(id);
				if (te.mediaLocation != null) {
					File file = new File(te.mediaLocation);
					Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
					switch (te.mediaType) {
					case PHOTO:
						intent.setDataAndType(Uri.fromFile(file), "image/*");
						startActivity(intent);
						break;
					case VIDEO:
						intent.setDataAndType(Uri.fromFile(file), "video/*");
						startActivity(intent);
						break;
					case AUDIO:
						intent.setDataAndType(Uri.fromFile(file), "audio/*");
						startActivity(intent);
					case TEXT:
						intent.setDataAndType(Uri.fromFile(file), "text/*");
						startActivity(intent);
					case NONE:
					default:
						break;
					}
				}
			}
		});
	}

	private class TripEntryAdapter extends CursorAdapter {

		private int mEntryMediaLocIdx;
		private int mEntryTypeIdx;

		public TripEntryAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);

			mEntryMediaLocIdx = c
					.getColumnIndex(DbDefs.TripDetailCols.MEDIA_LOCATION);
			mEntryTypeIdx = c.getColumnIndex(DbDefs.TripDetailCols.MEDIA_TYPE);

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return vi.inflate(R.layout.trip_view_grid_item, parent, false);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ImageView ivImg = (ImageView) view.findViewById(R.id.tripGridImage);

			MediaType mediaType = Enum.valueOf(TripEntry.MediaType.class,
					cursor.getString(mEntryTypeIdx));
			switch (mediaType) {
			case PHOTO:
				ImageCache.getInstance().setBitmapThreaded(
						cursor.getString(mEntryMediaLocIdx), mediaType, ivImg);
				break;
			case VIDEO:
				ImageCache.getInstance().setBitmapThreaded(
						cursor.getString(mEntryMediaLocIdx), mediaType, ivImg);
				break;
			case AUDIO:
				ivImg.setImageResource(R.drawable.audio);
				break;
			case TEXT:
				ivImg.setImageResource(R.drawable.text);
				break;
			}
		}
	}
}
