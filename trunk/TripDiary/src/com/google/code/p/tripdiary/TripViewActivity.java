/**
 * 
 */
package com.google.code.p.tripdiary;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.code.p.tripdiary.TripEntry.MediaType;

/**
 * This activity takes care of showing the tabbed views of the trips (on the
 * gallery tab and the map tab.)
 * 
 * @author Ankan Mukherjee
 * @author Arpita Saha
 * 
 */
public class TripViewActivity extends TabActivity {

	private static String TAG = "TripViewActivity";

	public final int REQUEST_PICTURE = 0;
	public final int REQUEST_VIDEO = 1;
	public final int REQUEST_AUDIO = 2;
	public final int REQUEST_NOTES = 3;
	private final int EDIT_TRIP_SETTINGS = 4;
	private final int SHARE_TRIP = 5;

	private long thisTripId = AppDataDefs.NO_CURRENT_TRIP;

	private final int DIALOG_CONFIRM_AND_DELETE_TRIP = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trip_view);

		// if the activity is resumed
		thisTripId = savedInstanceState != null ? savedInstanceState
				.getLong(AppDataDefs.KEY_TRIP_ID) : 0;

		// if there is a bundle set tab based on whether trip is current
		if (thisTripId == 0) {
			Bundle extras = getIntent().getExtras();
			thisTripId = extras != null ? extras
					.getLong(AppDataDefs.KEY_TRIP_ID) : 0;
		}

		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec = null; // Resusable TabSpec for each tab
		Intent intent = null; // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, TripGalleryActivity.class);
		intent.putExtra(AppDataDefs.KEY_TRIP_ID, thisTripId);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost
				.newTabSpec("gallery")
				.setIndicator("Gallery",
						res.getDrawable(R.drawable.gallery_res))
				.setContent(intent);
		tabHost.addTab(spec);

		// Do the same for the other tab
		intent = new Intent().setClass(this, TripMapActivity.class);
		intent.putExtra(AppDataDefs.KEY_TRIP_ID, thisTripId);
		spec = tabHost.newTabSpec("map")
				.setIndicator("Map", res.getDrawable(R.drawable.map_res))
				.setContent(intent);
		tabHost.addTab(spec);

		// at this point there needs to be a valid thisTripId
		if (thisTripId == 0) {
			Log.e(TAG, "Could not determine trip id.");
			Toast toast = Toast.makeText(this, "Could not find trip!",
					Toast.LENGTH_SHORT);
			toast.show();
			setResult(RESULT_CANCELED);
			finish();
		}

		// set the right tab to show
		if (thisTripId != getCurrentTripId()) {
			tabHost.setCurrentTab(0);
		} else {
			tabHost.setCurrentTab(1);
		}
	}

	private long getCurrentTripId() {
		return getApplicationContext().getSharedPreferences(
				AppDataDefs.APPDATA_FILE, MODE_PRIVATE).getLong(
				AppDataDefs.CURRENT_TRIP_ID_KEY, AppDataDefs.NO_CURRENT_TRIP);
	}

	private void setCurrentTripId(long tripId) {
		SharedPreferences.Editor editPref = getApplicationContext()
				.getSharedPreferences(AppDataDefs.APPDATA_FILE, MODE_PRIVATE)
				.edit();
		editPref.putLong(AppDataDefs.CURRENT_TRIP_ID_KEY, thisTripId);
		editPref.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.trip_view_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (thisTripId != getCurrentTripId()) {
			menu.findItem(R.id.add_photo).setEnabled(false);
			menu.findItem(R.id.add_photo).setVisible(false);

			menu.findItem(R.id.add_video).setEnabled(false);
			menu.findItem(R.id.add_video).setVisible(false);

			menu.findItem(R.id.add_audio).setEnabled(false);
			menu.findItem(R.id.add_audio).setVisible(false);

			menu.findItem(R.id.add_text).setEnabled(false);
			menu.findItem(R.id.add_text).setVisible(false);

			menu.findItem(R.id.stop_trip).setEnabled(false);
			menu.findItem(R.id.stop_trip).setVisible(false);

			menu.findItem(R.id.resume_trip).setEnabled(true);
			menu.findItem(R.id.resume_trip).setVisible(true);

		} else {
			menu.findItem(R.id.add_photo).setEnabled(true);
			menu.findItem(R.id.add_photo).setVisible(true);

			menu.findItem(R.id.add_video).setEnabled(true);
			menu.findItem(R.id.add_video).setVisible(true);

			menu.findItem(R.id.add_audio).setEnabled(true);
			menu.findItem(R.id.add_audio).setVisible(true);

			menu.findItem(R.id.add_text).setEnabled(true);
			menu.findItem(R.id.add_text).setVisible(true);

			menu.findItem(R.id.stop_trip).setEnabled(true);
			menu.findItem(R.id.stop_trip).setVisible(true);

			menu.findItem(R.id.resume_trip).setEnabled(false);
			menu.findItem(R.id.resume_trip).setVisible(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * This method is called whenever an item from the menu is selected.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
		case R.id.add_photo: {
			// Prints on the screen. //TODO remove later
			Toast.makeText(getBaseContext(), "Capturing photo",
					Toast.LENGTH_SHORT).show();

			Intent cameraIntent = new Intent(
					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

			startActivityForResult(cameraIntent, REQUEST_PICTURE);

			break;
		}

		case R.id.add_video: {
			// Prints on the screen. //TODO remove later
			Toast.makeText(getBaseContext(), "Capturing video",
					Toast.LENGTH_SHORT).show();

			Intent videoIntent = new Intent(
					android.provider.MediaStore.ACTION_VIDEO_CAPTURE);

			// Adding a max duration for video clips
			videoIntent.putExtra("android.intent.extra.durationLimit", 30000);
			videoIntent.putExtra("EXTRA_VIDEO_QUALITY", 0);

			startActivityForResult(videoIntent, REQUEST_VIDEO);

			break;
		}

		case R.id.add_audio: {
			Intent audioIntent = new Intent().setClass(this,
					AudioRecorder.class);
			startActivityForResult(audioIntent, REQUEST_AUDIO);

			break;
		}

		case R.id.add_text: {
			// Prints on the screen. //TODO remove later
			Toast.makeText(getBaseContext(), "Capturing notes",
					Toast.LENGTH_SHORT).show();
			Intent noteIntent = new Intent().setClass(this,
					TripNoteEditor.class);
			startActivityForResult(noteIntent, REQUEST_NOTES);

			break;
		}

		case R.id.edit_trip_settings:
			Intent intent = new Intent(getApplicationContext(),
					TripSettingsActivity.class);
			intent.putExtra(AppDataDefs.KEY_TRIP_ID, thisTripId);
			Log.d(TAG,
					"About to start edit trip settings activity for trip id "
							+ thisTripId);
			startActivityForResult(intent, EDIT_TRIP_SETTINGS);
			break;

		case R.id.resume_trip:
			TripViewActivity.this.setCurrentTripId(thisTripId);
			break;

		case R.id.stop_trip:
			TripViewActivity.this.setCurrentTripId(0);
			break;

		case R.id.delete_trip:
			showDialog(DIALOG_CONFIRM_AND_DELETE_TRIP);
			break;

		case R.id.share_trip:
			Intent shareIntent = new Intent(getApplicationContext(),
					TripShare.class);
			shareIntent.putExtra(AppDataDefs.KEY_TRIP_ID, thisTripId);
			Log.d(TAG, "Start exporting trip");

			startActivityForResult(shareIntent, SHARE_TRIP);
		}
		return false;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_CONFIRM_AND_DELETE_TRIP:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setIcon(R.drawable.delete);
			builder.setMessage(
					"This will remove all trip entries including GPS coordinates and notes.\n"
							+ "However, photos, video clips and audio clips will not be deleted.\n"
							+ "Are you sure you want to remove this trip?")
					.setCancelable(true)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									TripStorageManager storageMgr = TripStorageManagerFactory
											.getTripStorageManager(getApplicationContext());
									storageMgr.removeTrip(thisTripId);
									// TODO: refresh screen
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							});
			dialog = builder.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	public static String getMediaFileName() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
		Date date = new Date();
		String mediaFileName = dateFormat.format(date);

		return mediaFileName;
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent dataIntent) {
		super.onActivityResult(requestCode, resultCode, dataIntent);

		switch (requestCode) {
		case REQUEST_PICTURE: {
			// Activity was canceled
			if (resultCode == RESULT_CANCELED) {
				Toast.makeText(getBaseContext(), "Photo not captured",
						Toast.LENGTH_SHORT).show();
				return;
			}

			if (resultCode == RESULT_OK) {
				Bitmap capturedPic = (Bitmap) dataIntent.getExtras()
						.get("data");

				// save image in /tripDiary
				try {
					String fileName = Environment.getExternalStorageDirectory()
							.getAbsolutePath()
							+ "/DCIM/Camera/"
							+ getMediaFileName() + ".jpg";
					File dir = new File(fileName);
					FileOutputStream fos = new FileOutputStream(dir);
					capturedPic.compress(Bitmap.CompressFormat.JPEG, 90, fos);

				} catch (Exception e) {
					Toast.makeText(getBaseContext(),
							"Exception while saving a captured photo ",
							Toast.LENGTH_SHORT).show();

					Log.e(TAG,
							"Exception while saving a captured photo : "
									+ e.getMessage());
					e.printStackTrace();
				}

				Toast.makeText(getBaseContext(),
						"Photo captured and saved in gallery",
						Toast.LENGTH_SHORT).show();

				// TODO - add the entry in database
				// TODO - with lat, lon
			}

			break;
		}

		case REQUEST_VIDEO: {
			if (resultCode == RESULT_CANCELED) {
				Toast.makeText(getBaseContext(), "Photo not captured",
						Toast.LENGTH_SHORT).show();
			}

			if (resultCode == RESULT_OK) {
				Toast.makeText(getBaseContext(),
						"Video captured and saved in gallery",
						Toast.LENGTH_SHORT).show();

				try {
					Uri videoFileURI = dataIntent.getData();
					Toast.makeText(getBaseContext(),
							"Video saved in " + videoFileURI.toString(),
							Toast.LENGTH_SHORT).show();
					// TODO save video in the same path as photos /tripDiary

				} catch (Exception e) {
					Toast.makeText(getBaseContext(),
							"Exception while saving a captured video ",
							Toast.LENGTH_SHORT).show();
					Log.e(TAG,
							"Exception while saving a captured video : "
									+ e.getMessage());
				}
				// TODO - add the entry in database
			}

			break;
		}

		case REQUEST_AUDIO: {
			if (resultCode == RESULT_CANCELED) {
				Toast.makeText(getBaseContext(), "Audio not captured",
						Toast.LENGTH_SHORT).show();
			}

			if (resultCode == RESULT_OK) {
				try {
					String filePath = (String) dataIntent.getExtras().get(
							"returnKey");
					Toast.makeText(getBaseContext(),
							"Audio captured and saved in " + filePath,
							Toast.LENGTH_SHORT).show();

					double lat = 10.0; // TODO
					double lon = 20.0; // TODO
					String fileName = filePath;

					TripEntry tripEntry = new TripEntry(lat, lon, fileName,
							MediaType.AUDIO);
					TripStorageManager storageMgr = TripStorageManagerFactory
							.getTripStorageManager(getBaseContext());

					assert (storageMgr.addTripEntry(thisTripId, tripEntry));
					Log.d("ARPITA", "Trip entry created for Audio : " + lat
							+ " " + lon + " " + fileName);

					break;

				} catch (Exception e) {
					if (dataIntent == null)
						Log.e(TAG, "dataIntent is NULL");
					else
						Log.e(TAG, "Exception while capturing audio : " + e.getMessage());
				}
			}
		}

		case REQUEST_NOTES: {
			if (resultCode == RESULT_CANCELED) {
				Toast.makeText(getBaseContext(), "Note not captured",
						Toast.LENGTH_SHORT).show();
			}

			if (resultCode == RESULT_OK) {
				try {
					String text = (String) dataIntent.getExtras().get(
							"capturedText");
					Toast.makeText(getBaseContext(), "Text captured " + text,
							Toast.LENGTH_SHORT).show(); // TODO delete

					// TODO
					double lat = 10.0; // TODO
					double lon = 20.0; // TODO

					// Save the captured text in a tripEntry
					TripEntry tripEntry = new TripEntry(lat, lon, text);
					TripStorageManager storageMgr = TripStorageManagerFactory
							.getTripStorageManager(getBaseContext());

					storageMgr.addTripEntry(thisTripId, tripEntry);

					Log.d(TAG, "Trip entry created for notes : " + lat + " "
							+ lon);

				} catch (Exception e) {
					if (dataIntent == null)
						Log.e(TAG, "dataIntent is NULL");
					else
						Log.e(TAG, "Exception while capturing notes : " + e.getMessage());
				}
			}
		}

		case EDIT_TRIP_SETTINGS: {
			// nothing to do here
			break;
		}

		case SHARE_TRIP: {
			if (resultCode == RESULT_CANCELED) {
				Toast.makeText(getBaseContext(), "Trip not exported",
						Toast.LENGTH_SHORT).show();
			}

			if (resultCode == RESULT_OK) {
				try {
					String filePath = (String) dataIntent.getExtras().get(
							"returnKey");
					Toast.makeText(getBaseContext(),
							"Copy the kml file from " + filePath,
							Toast.LENGTH_SHORT).show();

					break;

				} catch (Exception e) {
					if (dataIntent == null)
						Log.e(TAG, "dataIntent is NULL");
					else
						Log.e(TAG, "Exception while exporting a trip : " + e.getMessage());
				}
			}
			break;
		}

		default:
			Toast.makeText(getBaseContext(), "Unknown option!",
					Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Unknown option");
		} // switch
	}
}
