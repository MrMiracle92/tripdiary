/**
 * 
 */
package com.google.code.p.tripdiary;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.TabHost;

/**
 * This activity takes care of showing the tabbed views of the trips (on the gallery tab and the map tab.)
 * 
 * @author Ankan Mukherjee
 * 
 */
public class TripViewActivity extends TabActivity {

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

		tabHost.setCurrentTab(0);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.trip_view_menu, menu);
	    return true;
	}

}
