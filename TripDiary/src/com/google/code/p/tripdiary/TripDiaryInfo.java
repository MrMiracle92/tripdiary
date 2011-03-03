package com.google.code.p.tripdiary;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.PopupWindow;

/**
 * Display a pop-up window with information regarding the application.
 * @author Arpita Saha
 *
 */
public class TripDiaryInfo extends Activity {
	private PopupWindow infoPopupWindow;
	private View layout;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = (LayoutInflater) TripDiaryInfo.this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		layout = inflater.inflate(R.layout.trip_home,
				(ViewGroup) findViewById(R.id.trip_home_info_popup));

		infoPopupWindow = new PopupWindow(layout, 400, 400, true);
		infoPopupWindow.setTouchable(true);
		infoPopupWindow.setOutsideTouchable(true);
		infoPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		infoPopupWindow.setTouchInterceptor(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				infoPopupWindow.dismiss();
				finish();
				return false;
			}
		});

		layout.findViewById(R.id.trip_home_info_popup).post(new Runnable() {
			public void run() {
				infoPopupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
			}
		});
	}
}
