/*
 Dubsar Dictionary Project
 Copyright (C) 2010-11 Jimmy Dee
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.dubsar_dictionary.Dubsar;

import java.util.Formatter;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

public class DubsarPreferences extends DubsarActivity implements OnTimeSetListener {

	public static final String WOTD_NOTIFICATIONS = "wotd_notifications";
	public static final String WOTD_HOUR = "wotd_hour";
	public static final String WOTD_MINUTE = "wotd_minute";
	public static final String DUBSAR_PREFERENCES = "dubsar_preferences";
	
	public static final int WOTD_TIME_PICKER_DIALOG_ID = 1;
	
	// by default, fire the timer each day between 00:01:00 and 00:01:59 UTC
	public static final int WOTD_HOUR_DEFAULT = 0;
	public static final int WOTD_MINUTE_DEFAULT = 1;
	
	private TimePickerDialog mDialog = null;
	private View mWotdServiceControl = null;
	private GestureDetector mDetector = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.preferences);
		
		mWotdServiceControl = findViewById(R.id.wotd_service_control);
		
		mDetector = new GestureDetector(new GestureHandler());
		
		ToggleButton wotdNotifications = (ToggleButton)findViewById(R.id.wotd_notifications);
		Button wotdTime = (Button)findViewById(R.id.wotd_time);
		Button wotdPurge = (Button)findViewById(R.id.wotd_purge);
		
		SharedPreferences preferences = getSharedPreferences(DUBSAR_PREFERENCES, MODE_PRIVATE);
		wotdNotifications.setChecked(preferences.getBoolean(WOTD_NOTIFICATIONS, true));
		
		setLabel(preferences.getInt(WOTD_HOUR, WOTD_HOUR_DEFAULT),
				preferences.getInt(WOTD_MINUTE, WOTD_MINUTE_DEFAULT));
		
		wotdNotifications.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ToggleButton box = (ToggleButton)v;
				SharedPreferences preferences = 
						getSharedPreferences(DUBSAR_PREFERENCES, MODE_PRIVATE);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean(WOTD_NOTIFICATIONS, box.isChecked());
				editor.commit();
				
				if (!box.isChecked()) {
					((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
				}
				else {
					/* request retransmission of the WOTD status bar notification */
					Intent serviceIntent = new Intent(getApplicationContext(),
							DubsarService.class);
					serviceIntent.setAction(DubsarService.ACTION_WOTD_NOTIFICATION);
					startService(serviceIntent);
				}
			}
		});
		
		wotdTime.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(WOTD_TIME_PICKER_DIALOG_ID);
			}
		});
		
		wotdPurge.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent serviceIntent = new Intent(getApplicationContext(),
						DubsarService.class);
				serviceIntent.setAction(DubsarService.ACTION_WOTD_PURGE);
				// purge stops the service
				startService(serviceIntent);
				
				// restart it
				serviceIntent.setAction(null);
				startService(serviceIntent);
			}
		});
	}
	
	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		// modify preferences
		SharedPreferences preferences = 
				getSharedPreferences(DUBSAR_PREFERENCES, MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(WOTD_HOUR, hourOfDay);
		editor.putInt(WOTD_MINUTE, minute);
		editor.commit();
		
		// notify the service that the preferences have changed
		Intent serviceIntent = new Intent(getApplicationContext(), DubsarService.class);
		serviceIntent.setAction(DubsarService.ACTION_WOTD_TIME);
		startService(serviceIntent);

		// refresh the preferences display
		setLabel(hourOfDay, minute);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case WOTD_TIME_PICKER_DIALOG_ID:
			SharedPreferences preferences = 
				getSharedPreferences(DUBSAR_PREFERENCES, MODE_PRIVATE);
			int hour = preferences.getInt(WOTD_HOUR, WOTD_HOUR_DEFAULT);
			int minute = preferences.getInt(WOTD_MINUTE, WOTD_MINUTE_DEFAULT);

			mDialog = new TimePickerDialog(this, 
				getActivity(), 
				hour, 
				minute, 
				true);
			mDialog.setTitle(getString(R.string.utc_time));
			return mDialog;
		default:
			return null;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mDetector.onTouchEvent(event) || super.onTouchEvent(event);
	}
	
	protected void setLabel(int hour, int minute) {
		TextView wotdTimeLabel = (TextView)findViewById(R.id.wotd_time_label);

		StringBuffer buffer = new StringBuffer();
		Formatter formatter = new Formatter(buffer);
		formatter.format("%02d:%02d", new Integer(hour), new Integer(minute));
		
		wotdTimeLabel.setText(buffer);
		
	}
	
	protected DubsarPreferences getActivity() {
		return this;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mDialog != null) {
			removeDialog(WOTD_TIME_PICKER_DIALOG_ID);
		}
	}

	class GestureHandler extends SimpleOnGestureListener implements AnimationListener {
		@Override
		public void onAnimationEnd(Animation animation) {
			mWotdServiceControl.setVisibility(View.INVISIBLE);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			
		}

		@Override
		public void onAnimationStart(Animation animation) {
			
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			AlphaAnimation animation;
			switch (mWotdServiceControl.getVisibility()) {
			case View.INVISIBLE:
				animation = new AlphaAnimation(0f, 1f);
				animation.setDuration(600);
				mWotdServiceControl.setVisibility(View.VISIBLE);
				mWotdServiceControl.startAnimation(animation);
				break;
			case View.VISIBLE:
				animation = new AlphaAnimation(1f, 0f);
				animation.setDuration(400);
				animation.setAnimationListener(this);
				mWotdServiceControl.startAnimation(animation);
				break;
			default:
				break;
			}
			return false;
		}
	}
	
}
