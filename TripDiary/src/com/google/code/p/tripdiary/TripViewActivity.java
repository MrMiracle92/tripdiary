package com.google.code.p.tripdiary;

import static com.google.code.p.tripdiary.AppDataDefs.LAT_UNKNOWN;
import static com.google.code.p.tripdiary.AppDataDefs.LON_UNKNOWN;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.code.p.tripdiary.BackgroundLocationService.LocationBinder;
import com.google.code.p.tripdiary.TripEntry.MediaType;
import com.google.code.p.tripdiary.utils.Util;

/**
 * This activity takes care of showing the tab views of the trips (on the
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
	private SharedPreferences.OnSharedPreferenceChangeListener mPrefListener;
	private ImageView mCurrTripIndicator;

	private String mediaFileName = "";

	/** Location service */
	private BackgroundLocationService locationService;

	/** Are we bound to Location service */
	private boolean locationServiceIsBound = false;

	/** Connection to Location service */
	private ServiceConnection locationServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			TripDiaryLogger
					.logDebug("TripViewActivity.locationServiceConnection.onServiceConnected");
			LocationBinder binder = (LocationBinder) service;
			locationService = binder.getService();
			TripDiaryLogger.logDebug("Location Service connected");
		}

		public void onServiceDisconnected(ComponentName className) {
			TripDiaryLogger
					.logDebug("TripViewActivity.locationServiceConnection.onServiceDisconnected");
			locationService = null;
			TripDiaryLogger.logDebug("Location Service disconnected");
		}
	};

	/** Bind with the background Location service */
	private void doBindLocationService() {
		TripDiaryLogger.logDebug("TripViewActivity - doBindService");

		Intent locationIntent = new Intent(this,
				BackgroundLocationService.class);
		if (bindService(locationIntent, locationServiceConnection,
				Context.BIND_AUTO_CREATE))
			locationServiceIsBound = true;
		else
			TripDiaryLogger.logError("Service bound failed");
	}

	/** Unbind from the background Location service */
	private void doUnbindLocationService() {
		TripDiaryLogger.logDebug("TripViewActivity - doUnbindService");

		if (locationServiceIsBound) {
			unbindService(locationServiceConnection);
			locationServiceIsBound = false;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		TripDiaryLogger.logDebug("TripViewActivity - onCreate");

		super.onCreate(savedInstanceState);

		setContentView(R.layout.trip_view);

		mTripStorageMgr = TripStorageManagerFactory
				.getTripStorageManager(getApplicationContext());

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
		spec = tabHost.newTabSpec("gallery")
				.setIndicator("", res.getDrawable(R.drawable.gallery_res))
				.setContent(intent);
		tabHost.addTab(spec);

		// Do the same for the other tab
		intent = new Intent().setClass(this, TripMapActivity.class);
		intent.putExtra(AppDataDefs.KEY_TRIP_ID, thisTripId);
		spec = tabHost.newTabSpec("map")
				.setIndicator("", res.getDrawable(R.drawable.map_res))
				.setContent(intent);
		tabHost.addTab(spec);

		// update the tab heights (to less than the default)
		int tabCount = tabHost.getTabWidget().getChildCount();
		for (int i = 0; i < tabCount; i++) {
			tabHost.getTabWidget().getChildAt(i).getLayoutParams().height = 60;
		}

		// at this point there needs to be a valid thisTripId
		if (thisTripId == 0) {
			TripDiaryLogger.logError("Could not determine trip id.");
			Toast toast = Toast.makeText(this, "Could not find trip!",
					Toast.LENGTH_SHORT);
			toast.show();
			setResult(RESULT_CANCELED);
			finish();
		}

		mCurrTripIndicator = (ImageView) findViewById(R.id.tripViewCurrIndicator);
		// set the right tab to show
		if (thisTripId != AppDataUtil.getCurrentTripId(getApplicationContext())) {
			mCurrTripIndicator
					.setBackgroundResource(R.drawable.noncurrtripbackground);
			tabHost.setCurrentTab(0);
		} else {
			mCurrTripIndicator
					.setBackgroundResource(R.drawable.currtripbackground);
			tabHost.setCurrentTab(1);
		}

		// subscribe to changes to current trip
		// listen for changes to current trip
		mPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {
				if (key == AppDataDefs.CURRENT_TRIP_ID_KEY) {
					if (thisTripId != AppDataUtil
							.getCurrentTripId(getApplicationContext())) {
						mCurrTripIndicator
								.setBackgroundResource(R.drawable.noncurrtripbackground);
					} else {
						mCurrTripIndicator
								.setBackgroundResource(R.drawable.currtripbackground);
					}
				}
			}
		};
		getApplicationContext().getSharedPreferences(AppDataDefs.APPDATA_FILE,
				MODE_PRIVATE).registerOnSharedPreferenceChangeListener(
				mPrefListener);
	}

	protected void onDestroy() {
		super.onDestroy();
		getApplicationContext().getSharedPreferences(AppDataDefs.APPDATA_FILE,
				MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(
				mPrefListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.trip_view_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (thisTripId != AppDataUtil.getCurrentTripId(getApplicationContext())) {
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
			doBindLocationService();
			Intent imageCaptureIntent = new Intent(
					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			mediaFileName = Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ "/DCIM/Camera/"
					+ Util.tripDiaryFileName() + ".jpg";
			imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(mediaFileName)));

			startActivityForResult(imageCaptureIntent, REQUEST_PICTURE);

			break;
		}

		case R.id.add_video: {
			doBindLocationService();
			Intent videoIntent = new Intent(
					android.provider.MediaStore.ACTION_VIDEO_CAPTURE);

			// Adding a max duration for video clips - get the configured value
			// from preferences
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());
			String videoLengthPref = sp.getString(
					AppDataDefs.PREF_KEY_VIDEOLEN, "10");

			Integer videoLengthVal = 10;
			try {
				videoLengthVal = Integer.parseInt(videoLengthPref);
			} catch (NumberFormatException e) {
				TripDiaryLogger
						.logError("Number format exception while configuring trip max. length"
								+ e.getMessage());
				videoLengthVal = 10;
			}

			TripDiaryLogger.logDebug("Configured max duration for video : "
					+ videoLengthPref);
			videoIntent.putExtra("android.intent.extra.durationLimit",
					videoLengthVal);

			// Adding video quality for video clips - get the configured value
			// from preferences
			String videoQualityPref = sp.getString(
					AppDataDefs.PREF_KEY_VIDEOQUALITY, "10");

			Integer videoQualityVal = 0; // low resolution
			try {
				videoQualityVal = Integer.parseInt(videoQualityPref);
			} catch (NumberFormatException e) {
				TripDiaryLogger
						.logError("Number format exception while configuring trip max. length"
								+ e.getMessage());
				videoQualityVal = 0;
			}

			TripDiaryLogger.logDebug("Configured resolution for video : "
					+ videoQualityVal);

			videoIntent.putExtra("EXTRA_VIDEO_QUALITY", videoQualityVal);

			startActivityForResult(videoIntent, REQUEST_VIDEO);

			break;
		}

		case R.id.add_audio: {
			doBindLocationService();
			Intent audioIntent = new Intent().setClass(this,
					AudioRecorder.class);
			startActivityForResult(audioIntent, REQUEST_AUDIO);

			break;
		}

		case R.id.add_text: {
			doBindLocationService();
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
			AppDataUtil.setCurrentTripId(getApplicationContext(), thisTripId);
			LocationController.startLocationLogging(getApplicationContext());
			break;

		case R.id.stop_trip:
			AppDataUtil.setCurrentTripId(getApplicationContext(),
					AppDataDefs.NO_CURRENT_TRIP);
			break;

		case R.id.delete_trip:
			showDialog(DIALOG_CONFIRM_AND_DELETE_TRIP);
			break;

		case R.id.share_trip:
			TripDiaryLogger.logDebug("Start exporting trip for " + thisTripId);

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
					"This will remove all trip entries including location information and notes.\n"
							+ "However, photos, video clips and audio clips will not be deleted.\n"
							+ "Are you sure you want to remove this trip?")
					.setCancelable(true)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									if (thisTripId == AppDataUtil
											.getCurrentTripId(getApplicationContext())) {
										AppDataUtil.setCurrentTripId(
												getApplicationContext(),
												AppDataDefs.NO_CURRENT_TRIP);
									}
									mTripStorageMgr.deleteTrip(thisTripId);
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

	protected void onActivityResult(int requestCode, int resultCode,
			Intent dataIntent) {
		super.onActivityResult(requestCode, resultCode, dataIntent);

		Location bestGuess = LocationController.getLastKnownLocation();
		double bestGuessLat = bestGuess == null ? LAT_UNKNOWN : bestGuess
				.getLatitude();
		double bestGuessLon = bestGuess == null ? LON_UNKNOWN : bestGuess
				.getLongitude();

		switch (requestCode) {
		case REQUEST_PICTURE: {
			if (resultCode == RESULT_OK) {
				try {
					TripEntry tripEntry = new TripEntry(bestGuessLat,
							bestGuessLon, mediaFileName, MediaType.PHOTO);
					addEntryToTrip(tripEntry);

					TripDiaryLogger
							.logDebug("Trip entry created for captured photo : "
									+ mediaFileName);
					Toast.makeText(getBaseContext(),
							"Photo captured and saved in " + mediaFileName,
							Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					TripDiaryLogger
							.logError("Exception while saving a captured photo : "
									+ e.getMessage());
					e.printStackTrace();
				}
			} else {
				Toast.makeText(getBaseContext(), "Photo not captured.",
						Toast.LENGTH_SHORT).show();
				doUnbindLocationService();
			}
			doUnbindLocationService();
			break;
		}

		case REQUEST_VIDEO: {
			if (resultCode == RESULT_OK) {
				try {
					AssetFileDescriptor videoAsset = getContentResolver()
							.openAssetFileDescriptor(dataIntent.getData(), "r");
					FileInputStream fis = videoAsset.createInputStream();

					String videoFileName = Environment
							.getExternalStorageDirectory().getAbsolutePath()
							+ "/DCIM/Camera/video-"
							+ Util.tripDiaryFileName()
							+ ".3gp";
					File videoFile = new File(videoFileName);
					FileOutputStream fos = new FileOutputStream(videoFile);

					byte[] buf = new byte[65535];
					int len;
					while ((len = fis.read(buf)) > 0) {
						fos.write(buf, 0, len);
					}
					fis.close();
					fos.close();

					Toast.makeText(
							getBaseContext(),
							"Video saved in new location : "
									+ videoFile.toString(), Toast.LENGTH_SHORT)
							.show();

					TripEntry tripEntry = new TripEntry(bestGuessLat,
							bestGuessLon, videoFileName.toString(),
							MediaType.VIDEO);
					addEntryToTrip(tripEntry);
				} catch (Exception e) {
					TripDiaryLogger
							.logError("Exception while saving a captured video : "
									+ e.getMessage());
				}
			} else {
				Toast.makeText(getBaseContext(), "Video not captured",
						Toast.LENGTH_SHORT).show();
			}
			doUnbindLocationService();
			break;
		}

		case REQUEST_AUDIO: {
			if (resultCode == RESULT_OK) {
				try {
					String filePath = (String) dataIntent.getExtras().get(
							"returnKey");
					Toast.makeText(getBaseContext(),
							"Audio captured and saved in " + filePath,
							Toast.LENGTH_SHORT).show();

					TripEntry tripEntry = new TripEntry(bestGuessLat,
							bestGuessLon, filePath, MediaType.AUDIO);
					addEntryToTrip(tripEntry);
				} catch (Exception e) {
					if (dataIntent == null)
						TripDiaryLogger.logError("dataIntent is NULL");
					else
						TripDiaryLogger
								.logError("Exception while capturing audio : "
										+ e.getMessage());
				}
			} else {
				Toast.makeText(getBaseContext(), "Audio not captured",
						Toast.LENGTH_SHORT).show();
			}
			doUnbindLocationService();
			break;
		}

		case REQUEST_NOTES: {
			if (resultCode == RESULT_OK) {
				try {
					String text = (String) dataIntent.getExtras().get(
							"capturedText");
					Toast.makeText(getBaseContext(), "Text captured " + text,
							Toast.LENGTH_SHORT).show(); // TODO delete

					TripEntry tripEntry = new TripEntry(bestGuessLat,
							bestGuessLon, text);
					addEntryToTrip(tripEntry);
				} catch (Exception e) {
					if (dataIntent == null)
						TripDiaryLogger.logError("dataIntent is NULL");
					else
						TripDiaryLogger
								.logError("Exception while capturing notes : "
										+ e.getMessage());
				}
			} else {
				Toast.makeText(getBaseContext(), "Note not captured.",
						Toast.LENGTH_SHORT).show();
			}
			doUnbindLocationService();
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
					eMailFile(filePath);
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
		// let's try to delegate this to the location service
		if (locationServiceIsBound && (locationService != null)) {
			TripDiaryLogger
					.logDebug("addEntryToTrip - updateEntryWithBestCurrentLocation");
			locationService.addEntryWithBestCurrentLocation(thisTripId,
					tripEntry);

			// let's also ask the controller to start the service explicitly
			// this will prevent the service from stopping when unbinding
			// happens
			LocationController.startLocationLogging(getApplicationContext());
		} else {
			// let's add the trip entry anyway (we don't need to lose this
			// entry if Location is not available etc.)
			TripDiaryLogger.logDebug("addEntryToTrip - addTripEntry");
			tripEntry.tripEntryId = mTripStorageMgr.addTripEntry(thisTripId,
					tripEntry);
		}
	}

	private void eMailFile(String filePath) {
		TripDetail trip = mTripStorageMgr.getTripDetail(thisTripId);
		StringBuffer subject = new StringBuffer("My trip");
		StringBuffer message = new StringBuffer(
				"Hi,\n\nAttached is a trip I saved using tripdiary.");
		if (trip != null) {
			subject.append(" - ").append(trip.getName());
			String desc = trip.getTripDescription();
			if (desc != null && desc.length() != 0) {
				message.append("\n\n").append(desc);
			}
		}
		message.append("\n\nTo view it, you may import it into Google Maps, or open with "
				+ "the Google Earth application or use any application that supports KML.\n\n"
				+ "This file was created using an android application called "
				+ "tripdiary (at this point available only to a select few!)");

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("message/rfc822");
		intent.putExtra(Intent.EXTRA_SUBJECT, subject.toString());
		intent.putExtra(Intent.EXTRA_TEXT, message.toString());
		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filePath));
		startActivity(Intent.createChooser(intent, "Send Email"));
	}
}
