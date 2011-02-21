/**
 * 
 */
package com.google.code.p.tripdiary;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * This is the activity that shows the recorded trip media in a gallery view.
 * 
 * @author Ankan Mukherjee
 * 
 */
public class TripGalleryActivity extends Activity {
	private final static String TAG = "TripGalleryActivity";

	private TripStorageManager storageMgr;

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

			// get the storage manager and the trip entry cursor for the trip id
			storageMgr = TripStorageManagerFactory.getTripStorageManager();
			Cursor tripEntryCursor = storageMgr.getEntriesForTrip(thisTripId);
			
			GridView gridview = (GridView) findViewById(R.id.gridview);
			gridview.setAdapter(new TripEntryAdapter(getApplicationContext(),
					tripEntryCursor, true));

			gridview.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {
					Toast.makeText(TripGalleryActivity.this, "" + position + ", " + id,
							Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	private class TripEntryAdapter extends CursorAdapter {

//		private int mEntryIdx;
		private int mEntryMediaLocIdx;
		private int mEntryTypeIdx;

		public TripEntryAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);

//			mEntryIdx = c.getColumnIndex(DbDefs.TripDetailCols._ID);
			mEntryMediaLocIdx = c
					.getColumnIndex(DbDefs.TripDetailCols.MEDIA_LOCATION);
			mEntryTypeIdx = c.getColumnIndex(DbDefs.TripDetailCols.MEDIA_TYPE);

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(95, 95));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return imageView;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ImageView ivImg = (ImageView) view;
			Bitmap bm = null;
			int defRes = R.drawable.picture;
			switch (Enum.valueOf(TripEntry.MediaType.class,
					cursor.getString(mEntryTypeIdx))) {
			case PHOTO:
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 4;
				options.inTempStorage = new byte[16*1024];
				bm = BitmapFactory.decodeFile(cursor
						.getString(mEntryMediaLocIdx), options);
				defRes = R.drawable.picture;
				break;
			case VIDEO:
				bm = ThumbnailUtils.createVideoThumbnail(
						cursor.getString(mEntryMediaLocIdx),
						MediaStore.Video.Thumbnails.MINI_KIND);
				defRes = R.drawable.video;
				break;
			case AUDIO:
				defRes = R.drawable.audio;
				break;
			case TEXT:
				defRes = R.drawable.text;
				break;
			}

			if (bm != null) {
				ivImg.setImageBitmap(bm);
			} else {
				ivImg.setImageResource(defRes);
			}
		}
	}

}
