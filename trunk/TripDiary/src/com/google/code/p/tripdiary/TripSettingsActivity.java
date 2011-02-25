package com.google.code.p.tripdiary;

import static com.google.code.p.tripdiary.AppDataDefs.*;

import java.sql.Date;
import java.text.DateFormat;

import com.google.code.p.tripdiary.TripEntry.MediaType;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

;

/**
 * This activity is used to create and edit trip settings.
 * 
 * @author Ankan Mukherjee
 */
public class TripSettingsActivity extends Activity {
	// private final String TAG = "TripSettingsActivity";

	private TripStorageManager storageMgr;
	private long mThisTripId = 0;
	private boolean mIsNewTrip = false;

	private final int DIALOG_INVALID_INPUT_NAME_REQUIRED = 0;

	// Requests (for activities)
	private final int REQUEST_PICK_TRIP_IMAGE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trip_settings);

		storageMgr = TripStorageManagerFactory.getTripStorageManager();

		// if the activity is resumed
		if (savedInstanceState != null) {
			mThisTripId = savedInstanceState.getLong(KEY_TRIP_ID,
					AppDataDefs.NO_CURRENT_TRIP);
			mIsNewTrip = savedInstanceState.getBoolean(KEY_IS_NEW_TRIP, false);
		} else {
			// if there is a bundle get details from there
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				mThisTripId = extras.getLong(KEY_TRIP_ID,
						AppDataDefs.NO_CURRENT_TRIP);
				mIsNewTrip = extras.getBoolean(KEY_IS_NEW_TRIP, false);
			}
		}

		if (!mIsNewTrip) {
			// this is an existing trip to edit
			TripDetail td = storageMgr.getTripDetail(mThisTripId);
			if (td == null) {
				// no trip with the given trip id exists
				Toast toast = Toast.makeText(getApplicationContext(),
						"Trip with id " + mThisTripId + " not found.",
						Toast.LENGTH_SHORT);
				toast.show();
				setResult(RESULT_CANCELED);
				finish();
				return;
			}
			populateContent();
		} else {
			// this is a new trip
			findViewById(R.id.tvCreatedLbl).setVisibility(View.GONE);
			findViewById(R.id.tvCreated).setVisibility(View.GONE);
			findViewById(R.id.tvLastUpdatedLbl).setVisibility(View.GONE);
			findViewById(R.id.tvLastUpdated).setVisibility(View.GONE);
			findViewById(R.id.settingsTripImage).setVisibility(View.GONE);
			((CheckBox) findViewById(R.id.cbTraceRoute))
					.setChecked(AppDataDefs.DEFAULT_TRACE_ROUTE_ENABLED);
		}

		findViewById(R.id.btnOk).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				validateAndFinish();
			}
		});

		findViewById(R.id.settingsTripImage).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						// use the default image chooser to pick an image
						Intent intent = new Intent();
						intent.setType("image/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);
						startActivityForResult(
								Intent.createChooser(intent, "Select Picture"),
								REQUEST_PICK_TRIP_IMAGE);

					}
				});
	}

	private void populateContent() {
		if (!mIsNewTrip) {
			TripDetail td = storageMgr.getTripDetail(mThisTripId);
			DateFormat df = DateFormat.getDateInstance();
			((TextView) findViewById(R.id.tvCreated)).setText(df
					.format(new Date(td.getCreateTime())));
			((TextView) findViewById(R.id.tvLastUpdated))
					.setText(df.format(new Date(storageMgr
							.getLastUpdatedTime(mThisTripId))));
			((TextView) findViewById(R.id.edName)).setText(td.getName());
			((TextView) findViewById(R.id.edDescription)).setText(td
					.getTripDescription());
			((CheckBox) findViewById(R.id.cbTraceRoute)).setChecked(td
					.isTraceRouteEnabled());

			if (td.getDefaultThumbnail() != null) {
				((ImageView) findViewById(R.id.settingsTripImage))
						.setImageBitmap(ImageCache.getInstance().getBitmap(
								td.getDefaultThumbnail(), MediaType.PHOTO,
								getApplicationContext()));
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_PICK_TRIP_IMAGE) {
				Uri selectedImageUri = data.getData();

				// OI FILE Manager
				String filemanagerstring = selectedImageUri.getPath();

				// MEDIA GALLERY
				String selectedImagePath = getPath(selectedImageUri);

				String pathToTripImage = selectedImagePath == null ? filemanagerstring
						: selectedImagePath;

				if (!mIsNewTrip && pathToTripImage != null) {
					TripDetail td = storageMgr.getTripDetail(mThisTripId);
					storageMgr.updateTrip(mThisTripId, td.getName(),
							td.getTripDescription(), td.isTraceRouteEnabled(),
							pathToTripImage);
					populateContent();
				}
			}
		}
	}

	private String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_INVALID_INPUT_NAME_REQUIRED:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Name cannot be empty.")
					.setCancelable(false)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// do nothing here
								}
							});
			dialog = builder.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putLong(KEY_TRIP_ID, mThisTripId);
		savedInstanceState.putBoolean(KEY_IS_NEW_TRIP, mIsNewTrip);
	}

	private void validateAndFinish() {
		EditText nameEditText = (EditText) findViewById(R.id.edName);
		String name = nameEditText.getText().toString();
		if (name.equals("")) {
			showDialog(DIALOG_INVALID_INPUT_NAME_REQUIRED);
			nameEditText.requestFocus();
			return;
		} else {
			String tripDescription = ((EditText) findViewById(R.id.edDescription))
					.getText().toString();
			boolean traceRouteEnabled = ((CheckBox) findViewById(R.id.cbTraceRoute))
					.isChecked();
			if (mIsNewTrip) {
				mThisTripId = storageMgr.createNewTrip(name, tripDescription,
						traceRouteEnabled);
				SharedPreferences.Editor editPref = getApplicationContext()
						.getSharedPreferences(AppDataDefs.APPDATA_FILE,
								MODE_PRIVATE).edit();
				editPref.putLong(AppDataDefs.CURRENT_TRIP_ID_KEY, mThisTripId);
				editPref.commit();
			} else {
				storageMgr.updateTrip(mThisTripId, name, tripDescription,
						traceRouteEnabled, null);
			}
			// finish with result ok
			finish();
		}
	}
}
