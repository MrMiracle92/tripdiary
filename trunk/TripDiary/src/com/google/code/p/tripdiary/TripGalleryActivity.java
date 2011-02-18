/**
 * 
 */
package com.google.code.p.tripdiary;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

/**
 * This is the activity that shows the recorded trip media in a gallery view.
 * 
 * @author Ankan Mukherjee
 *
 */
public class TripGalleryActivity extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.trip_view_grid);

	    GridView gridview = (GridView) findViewById(R.id.gridview);
	    gridview.setAdapter(new ImageAdapter(getApplicationContext()));

	    gridview.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	            Toast.makeText(TripGalleryActivity.this, "" + position, Toast.LENGTH_SHORT).show();
	        }
	    });
	}

}
