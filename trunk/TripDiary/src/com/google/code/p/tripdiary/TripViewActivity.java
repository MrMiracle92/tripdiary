/**
 * 
 */
package com.google.code.p.tripdiary;

import static com.google.code.p.tripdiary.AppDataDefs.LAT_UNKNOWN;
import static com.google.code.p.tripdiary.AppDataDefs.LON_UNKNOWN;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.code.p.tripdiary.BackgroundGpsService.GPSBinder;
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
	private final int REQUEST_PICTURE = 0;
	private final int REQUEST_VIDEO = 1;
	private final int REQUEST_AUDIO = 2;
	private final int REQUEST_NOTES = 3;
	private final int EDIT_TRIP_SETTINGS = 4;
	private final int SHARE_TRIP = 5;

	private final int DIALOG_CONFIRM_AND_DELETE_TRIP = 1;

	private long thisTripId = AppDataDefs.NO_CURRENT_TRIP;
	
	private TripStorageManager mTripStorageMgr = null;

	/** GPS service */
	private BackgroundGpsService gpsService;

	/** Are we bound to GPS service */
	private boolean gpsServiceIsBound = false;

	/** Connection to GPS service */
	private ServiceConnection gpsServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			TripDiaryLogger
					.logDebug("TripViewActivity.gpsServiceConnection.onServiceConnected");
			GPSBinder binder = (GPSBinder) service;
			gpsService = binder.getService();
			TripDiaryLogger.logDebug("GPS Service connected");
		}

		public void onServiceDisconnected(ComponentName className) {
			TripDiaryLogger
					.logDebug("TripViewActivity.gpsServiceConnection.onServiceDisconnected");
			gpsService = null;
			TripDiaryLogger.logDebug("GPS Service disconnected");
		}
	};

	/** Bind with the background GPS service */
	void doBindService() {
		TripDiaryLogger.logDebug("TripViewActivity - doBindService");

		Intent gpsIntent = new Intent(this, BackgroundGpsService.class);
		gpsIntent.putExtra(BackgroundGpsService.INTENT_TRIP_ID_KEY, thisTripId);
		if (bindService(gpsIntent, gpsServiceConnection,
				Context.BIND_AUTO_CREATE))
			gpsServiceIsBound = true;
		else
			TripDiaryLogger.logError("Service bound failed");
	}

	/** Unbind from the background GPS service */
	void doUnbindService() {
		TripDiaryLogger.logDebug("TripViewActivity - doUnbindService");

		if (gpsServiceIsBound) {
			unbindService(gpsServiceConnection);
			gpsServiceIsBound = false;
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		TripDiaryLogger.logDebug("TripViewActivity - onStart");

		if ((gpsServiceIsBound == false) && (thisTripId == getCurrentTripId())) {
			// Bind to localservice
			TripDiaryLogger.logDebug("Starting again");
			GpsController.startGpsLogging(getBaseContext(), thisTripId);
			doBindService();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		TripDiaryLogger.logDebug("TripViewActivity - onDestroy");
		doUnbindService();
		// GpsController.stopGpsLogging(this); // Do not stop the service
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		TripDiaryLogger.logDebug("TripViewActivity - onCreate");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.trip_view);
		
		mTripStorageMgr = TripStorageManagerFactory.getTripStorageManager(getApplicationContext());

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
			TripDiaryLogger.logError("Could not determine trip id.");
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

			// If this is a current trip, then start the GPS background service
			if (gpsServiceIsBound == false) {
				GpsController.startGpsLogging(getBaseContext(), thisTripId);
				doBindService();
			}
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
			Intent cameraIntent = new Intent(
					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

			startActivityForResult(cameraIntent, REQUEST_PICTURE);

			break;
		}

		case R.id.add_video: {
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
			TripDiaryLogger
					.logDebug("About to start edit trip settings activity for trip id "
							+ thisTripId);
			startActivityForResult(intent, EDIT_TRIP_SETTINGS);
			break;

		case R.id.resume_trip:
			TripViewActivity.this.setCurrentTripId(thisTripId);
			GpsController.startGpsLogging(getBaseContext(), thisTripId);

			doBindService();
			break;

		case R.id.stop_trip:
			TripViewActivity.this.setCurrentTripId(0);
			// GpsController.stopGpsLogging(getBaseContext());

			doUnbindService();
			break;

		case R.id.delete_trip:
			showDialog(DIALOG_CONFIRM_AND_DELETE_TRIP);
			break;

		case R.id.share_trip:
			TripDiaryLogger.logDebug("Start exporting trip");

			Intent shareIntent = new Intent(getApplicationContext(),
					TripExport.class);
			shareIntent.putExtra(AppDataDefs.KEY_TRIP_ID, thisTripId);

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
									storageMgr.deleteTrip(thisTripId);
									Toast.makeText(getBaseContext(),
											"Trip deleted.", Toast.LENGTH_SHORT)
											.show();
									setResult(RESULT_OK);
									finish();
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
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
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

				try {
					String fileName = Environment.getExternalStorageDirectory()
							.getAbsolutePath()
							+ "/DCIM/Camera/"
							+ getMediaFileName() + ".jpg";
					File dir = new File(fileName);
					FileOutputStream fos = new FileOutputStream(dir);
					capturedPic.compress(Bitmap.CompressFormat.JPEG, 90, fos);
					
					TripEntry tripEntry = new TripEntry(LAT_UNKNOWN, LON_UNKNOWN, fileName,
							MediaType.PHOTO);
					addEntryToTrip(tripEntry);

//					if (gpsServiceIsBound && (gpsService != null)) {
//						Location location = gpsService.getLastKnownLocation();
//						if (location == null)
//							TripDiaryLogger
//									.logError("Location not found - no entry created");
//						else {
//							// Save the captured audio path in a tripEntry
//							TripEntry tripEntry = new TripEntry(
//									location.getLatitude(),
//									location.getLongitude(), fileName,
//									MediaType.PHOTO);
//							mTripStorageMgr.addTripEntry(thisTripId, tripEntry);
//							TripDiaryLogger
//									.logDebug("Trip entry created for captured photo : "
//											+ location.getLatitude()
//											+ " "
//											+ location.getLongitude()
//											+ " "
//											+ fileName);
//							Toast.makeText(getBaseContext(),
//									"Photo captured and saved in " + fileName,
//									Toast.LENGTH_SHORT).show();
//						}
//					} else {
//						TripDiaryLogger
//								.logError("gpsService was not bound when photo was captured");
//					}
				} catch (Exception e) {
					TripDiaryLogger
							.logError("Exception while saving a captured photo : "
									+ e.getMessage());
					e.printStackTrace();
				}
			}

			break;
		}

		case REQUEST_VIDEO: {
			if (resultCode == RESULT_CANCELED) {
				Toast.makeText(getBaseContext(), "Video not captured",
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
					
					TripEntry tripEntry = new TripEntry(
							LAT_UNKNOWN, LON_UNKNOWN, videoFileURI.toString(), MediaType.VIDEO);
					addEntryToTrip(tripEntry);

//					TripStorageManager storageMgr = TripStorageManagerFactory
//							.getTripStorageManager(getBaseContext());
//
//					if (gpsServiceIsBound && (gpsService != null)) {
//						Location location = gpsService.getLastKnownLocation();
//						if (location == null)
//							TripDiaryLogger
//									.logError("Location not found - no entry created");
//						else {
//							// Save the captured audio path in a tripEntry
//							TripEntry tripEntry = new TripEntry(
//									location.getLatitude(),
//									location.getLongitude(),
//									videoFileURI.toString(), MediaType.VIDEO);
//
//							mTripStorageMgr.addTripEntry(thisTripId, tripEntry);
//
//							TripDiaryLogger
//									.logDebug("Trip entry created for captured video : "
//											+ location.getLatitude()
//											+ " "
//											+ location.getLongitude()
//											+ " "
//											+ videoFileURI.toString());
//
//							Toast.makeText(
//									getBaseContext(),
//									"Video captured and saved in "
//											+ videoFileURI.toString(),
//									Toast.LENGTH_SHORT).show();
//						}
//					} else {
//						TripDiaryLogger
//								.logError("gpsService was not bound when video was captured");
//					}
				} catch (Exception e) {
					TripDiaryLogger
							.logError("Exception while saving a captured video : "
									+ e.getMessage());
				}
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
					
					TripEntry tripEntry = new TripEntry(LAT_UNKNOWN, LON_UNKNOWN, filePath,
							MediaType.AUDIO);
					addEntryToTrip(tripEntry);

//					if (gpsServiceIsBound && (gpsService != null)) {
//						Location location = gpsService.getLastKnownLocation();
//						if (location == null)
//							TripDiaryLogger
//									.logError("Location not found - no entry created");
//						else {
//							// Save the captured audio path in a tripEntry
//							TripEntry tripEntry = new TripEntry(
//									location.getLatitude(),
//									location.getLongitude(), filePath,
//									MediaType.AUDIO);
//
//							mTripStorageMgr.addTripEntry(thisTripId, tripEntry);
//
//							TripDiaryLogger
//									.logDebug("Trip entry created for Audio : "
//											+ location.getLatitude() + " "
//											+ location.getLongitude() + " "
//											+ filePath);
//
//							Toast.makeText(getBaseContext(),
//									"Audio captured and saved in " + filePath,
//									Toast.LENGTH_SHORT).show();
//						}
//					} else {
//						TripDiaryLogger
//								.logError("gpsService was not bound when photo was captured");
//					}
				} catch (Exception e) {
					if (dataIntent == null)
						TripDiaryLogger.logError("dataIntent is NULL");
					else
						TripDiaryLogger
								.logError("Exception while capturing audio : "
										+ e.getMessage());
				}
			}

			break;
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
					
					TripEntry tripEntry = new TripEntry(
							LAT_UNKNOWN, LON_UNKNOWN, text);
					addEntryToTrip(tripEntry);

//					if (gpsServiceIsBound && (gpsService != null)) {
//						Location location = gpsService.getLastKnownLocation();
//						if (location == null)
//							TripDiaryLogger
//									.logError("Location not found - no entry created");
//						else {
//							// Save the captured text in a tripEntry
//							TripEntry tripEntry = new TripEntry(
//									location.getLatitude(),
//									location.getLongitude(), text);
//
//							mTripStorageMgr.addTripEntry(thisTripId, tripEntry);
//
//							TripDiaryLogger
//									.logDebug("Trip entry created for notes : "
//											+ location.getLatitude() + " "
//											+ location.getLongitude());
//
//							Toast.makeText(getBaseContext(),
//									"Notes captured and saved.",
//									Toast.LENGTH_SHORT).show();
//						}
//					} else {
//						TripDiaryLogger
//								.logError("gpsService was not bound when photo was captured");
//					}
				} catch (Exception e) {
					if (dataIntent == null)
						TripDiaryLogger.logError("dataIntent is NULL");
					else
						TripDiaryLogger
								.logError("Exception while capturing notes : "
										+ e.getMessage());
				}
			}

			break;
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
						TripDiaryLogger.logError("dataIntent is NULL");
					else
						TripDiaryLogger
								.logError("Exception while exporting a trip : "
										+ e.getMessage());
				}
			}
			break;
		}

		default:
			Toast.makeText(getBaseContext(), "Unknown option!",
					Toast.LENGTH_SHORT).show();
			TripDiaryLogger.logError("Unknown option");
		} // switch
	}
	
	private void addEntryToTrip(TripEntry tripEntry) {
		// let's add the trip entry anyway (we don't need to lose this entry if
		// gps fails or is not available etc.)
		long tripEntryId = mTripStorageMgr.addTripEntry(thisTripId, tripEntry);

		// now let's ask the gps service to add the best current location to the entry
		if (gpsServiceIsBound && (gpsService != null)) {
			gpsService.updateEntryWithBestCurrentLocation(tripEntryId);
		}
	}
}
