/*
 * Copyright (C) 2011 Ankan Mukherjee, Arpita Saha
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.code.p.tripdiary;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.google.code.p.tripdiary.TripEntry.MediaType;

/**
 * This is the activity that shows the recorded trip media in a gallery view.
 * 
 * @author Ankan Mukherjee
 * @author Arpita Saha
 * 
 */
public class TripGalleryActivity extends Activity {
	private TripStorageManager mTripStorageMgr;

	private long thisTripId = AppDataDefs.NO_CURRENT_TRIP;
	private Cursor mTripEntryCursor = null;
	private TripEntryAdapter mTripEntryAdapter = null;

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
				TripDiaryLogger.logError("Could not find trip.");
				setResult(RESULT_CANCELED);
				finish();
				return;
			}
		}

		// get the storage manager
		mTripStorageMgr = TripStorageManagerFactory
				.getTripStorageManager(getApplicationContext());

		mTripEntryCursor = mTripStorageMgr.getMediaEntriesForTrip(thisTripId);
		mTripEntryAdapter = new TripEntryAdapter(getApplicationContext(),
				mTripEntryCursor, true);
		GridView gridview = (GridView) findViewById(R.id.gridview);
		gridview.setAdapter(mTripEntryAdapter);

		gridview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				showEntryInDefaultViewer(id);
			}
		});

		registerForContextMenu(gridview);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mTripEntryCursor.requery();
		TripDiaryLogger.logDebug("Entries = " + mTripEntryCursor.getCount());
		mTripEntryAdapter.notifyDataSetInvalidated();
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.trip_entry_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.view_entry:
			showEntryInDefaultViewer(info.id);
			return true;
		case R.id.remove_entry:
			mTripStorageMgr.deleteTripEntry(info.id);
			mTripEntryCursor.requery();
			TripDiaryLogger
					.logDebug("Entries = " + mTripEntryCursor.getCount());
			mTripEntryAdapter.notifyDataSetInvalidated();
			return true;
		case R.id.show_details_entry:
			TripEntry te = mTripStorageMgr.getTripEntry(info.id);
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
			dialogBuilder.setMessage(te.toStringMultiline());
			if (te.mediaType == MediaType.NONE)
				dialogBuilder.setTitle("Location");
			else
				dialogBuilder.setTitle(te.mediaType.name());
			dialogBuilder.show();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void showEntryInDefaultViewer(long tripEntryId) {
		TripEntry te = mTripStorageMgr.getTripEntry(tripEntryId);
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
				break;
			case TEXT:
				AlertDialog.Builder noteDialog = new AlertDialog.Builder(
						TripGalleryActivity.this);
				noteDialog.setMessage(te.noteText);
				noteDialog.setTitle("Text");
				noteDialog.show();
				break;
			case NONE:
			default:
				break;
			}
		}
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
				ivImg.setImageResource(R.drawable.gallery_audio);
				break;
			case TEXT:
				ivImg.setImageResource(R.drawable.gallery_text);
				break;
			}
		}
	}
}
