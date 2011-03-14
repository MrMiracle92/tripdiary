/*
 * Copyright (C) 2011 Arpita Saha
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
 * 
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
