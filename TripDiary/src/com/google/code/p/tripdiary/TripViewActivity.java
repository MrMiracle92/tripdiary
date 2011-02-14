/**
 * 
 */
package com.google.code.p.tripdiary;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.TabActivity;
import android.content.Intent;
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

/**
 * This activity takes care of showing the tabbed views of the trips (on the
 * gallery tab and the map tab.)
 * 
 * @author Ankan Mukherjee
 * @author Arpita Saha
 * 
 */
public class TripViewActivity extends TabActivity {
	
	private static String TAG="TripViewActivity";
	
	public final int REQUEST_CAMERA_PIC = 0;
	public final int REQUEST_VIDEO = 1;
	


	public final static String KEY_TRIP_ID = "tripId";

	private TripStorageManager storageMgr;
	private long thisTripId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trip_view);

		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec = null; // Resusable TabSpec for each tab
		Intent intent = null; // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, TripGalleryActivity.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost
				.newTabSpec("gallery")
				.setIndicator("Gallery",
						res.getDrawable(R.drawable.gallery_res))
				.setContent(intent);
		tabHost.addTab(spec);

		// Do the same for the other tab
		intent = new Intent().setClass(this, TripMapActivity.class);
		spec = tabHost.newTabSpec("map")
				.setIndicator("Map", res.getDrawable(R.drawable.map_res))
				.setContent(intent);
		tabHost.addTab(spec);

		storageMgr = TripStorageManagerFactory.getTripStorageManager();

		// if the activity is resumed
		thisTripId = savedInstanceState != null ? savedInstanceState
				.getLong(KEY_TRIP_ID) : 0;

		// if there is a bundle set tab based on whether trip is current
		if (thisTripId == 0) {
			Bundle extras = getIntent().getExtras();
			thisTripId = extras.getLong(KEY_TRIP_ID);
		}
		if (thisTripId != storageMgr.getCurrentTripId()) {
			tabHost.setCurrentTab(0);
		} else {
			tabHost.setCurrentTab(1);
		}

		// at this point there needs to be a valid thisTripId
		if (thisTripId == 0) {
			Toast toast = Toast.makeText(this, "Could not determine trip id!",
					Toast.LENGTH_SHORT);
			toast.show();
			setResult(RESULT_CANCELED);
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.trip_view_menu, menu);
		return true;
	}
	

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (thisTripId != storageMgr.getCurrentTripId()) {
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
		} else {
			menu.findItem(R.id.resume_trip).setEnabled(false);
			menu.findItem(R.id.resume_trip).setVisible(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}
	

	
    /**
     * This method is called whenever an item from the menu is selected.
     */
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem)
	{
    	switch(menuItem.getItemId())
    	{
    	case R.id.add_photo:
    	{	
    		// Prints on the screen. //TODO remove later
            Toast.makeText(getBaseContext(),
                    "Capturing photo",
                    Toast.LENGTH_SHORT).show(); 
            
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);  
			
			startActivityForResult(cameraIntent, REQUEST_CAMERA_PIC);
			return true;
    	}
    	
    	case R.id.add_video:
    	{
    		// Prints on the screen. //TODO remove later
            Toast.makeText(getBaseContext(),
                    "Capturing video",
                    Toast.LENGTH_SHORT).show(); 
            
            Intent videoIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
            
            // Adding a max duration for video clips
//            videoIntent.putExtra("android.intent.extra.durationLimit", 30000); 
//            videoIntent.putExtra("EXTRA_VIDEO_QUALITY", 0); 
            
            startActivityForResult(videoIntent, REQUEST_VIDEO);
            return true;
    	}
    	
    	case R.id.add_audio:
    	{
    		// Prints on the screen. //TODO remove later
            Toast.makeText(getBaseContext(),
                    "Capturing audio - to be implemented",
                    Toast.LENGTH_SHORT).show(); 
            
    		
    	}
    	
    	case R.id.add_text:
    	{
    		// Prints on the screen. //TODO remove later
            Toast.makeText(getBaseContext(),
                    "Capturing notes - to be implemented",
                    Toast.LENGTH_SHORT).show(); 
    	}
    		
    		
    	}	
		return false;
	}
	
	
	private String getMediaFileName()
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
		Date date = new Date();
		String mediaFileName = dateFormat.format(date);
		
		return mediaFileName;
	}
	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
	
		
		switch(requestCode)
		{
		case REQUEST_CAMERA_PIC:
		{
			// Activity was canceled
			if (resultCode == RESULT_CANCELED) 
			{
				  Toast.makeText(getBaseContext(),
						  "Photo not captured",
						  Toast.LENGTH_SHORT).show();
				  return;
			}
			
			if (resultCode == RESULT_OK)
			{
				Bitmap capturedPic =  (Bitmap) data.getExtras().get("data");  
				
				// save image in /tripDiary
				try
				{
					File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/tripDiary");
					if (!dir.exists()) // first time
					{
						dir.mkdir();
					}
					FileOutputStream fos = new FileOutputStream(dir + "/" + getMediaFileName() + ".jpg");
					capturedPic.compress(Bitmap.CompressFormat.JPEG, 90, fos);
				}
				catch (Exception e)
				{
					Toast.makeText(getBaseContext(),
							  "Exception while saving a captured photo ",
							  Toast.LENGTH_SHORT).show();
					Log.e(TAG, "Exception while saving a captured photo : " + e.getMessage());
				}
				
				Toast.makeText(getBaseContext(),
						  "Photo captured and saved in /tripDiary",
						  Toast.LENGTH_SHORT).show();

				//TODO - add the entry in database
			}
			break;
		}
		
		case REQUEST_VIDEO:
		{
			if (resultCode == RESULT_CANCELED) 
			{
				  Toast.makeText(getBaseContext(),
						  "Photo not captured",
						  Toast.LENGTH_SHORT).show();
				  return;
			}
			
			if (resultCode == RESULT_OK)
			{
				Toast.makeText(getBaseContext(),
						  "Video captured and saved in gallery",
						  Toast.LENGTH_SHORT).show();
				
				try
				{
					Uri videoFileUri = data.getData();
					Toast.makeText(getBaseContext(),
							  "Video saved in " + videoFileUri.toString(),
							  Toast.LENGTH_SHORT).show();
					//TODO save video in the same path as photos /tripDiary

				}
				catch (Exception e)
				{
					Toast.makeText(getBaseContext(),
							  "Exception while saving a captured video ",
							  Toast.LENGTH_SHORT).show();
					Log.e(TAG, "Exception while saving a captured video : " + e.getMessage());
				}
				//TODO - add the entry in database
			}
			break;
		}
		
		default:
			Toast.makeText(getBaseContext(),
					  "Unknwon option!",
					  Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Unknown option");
		} // switch
	}
}
