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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;
// import android.view.GestureDetector;

public class PreferencesActivity extends DubsarActivity implements OnTimeSetListener {

	public static final String WOTD_NOTIFICATIONS = "wotd_notifications";
	public static final String WOTD_HOUR = "wotd_hour";
	public static final String WOTD_MINUTE = "wotd_minute";
	public static final String DUBSAR_PREFERENCES = "dubsar_preferences";
	public static final String HTTP_PROXY_HOST = "http_proxy_host";
	public static final String HTTP_PROXY_PORT = "http_proxy_port";
	
	public static final int WOTD_TIME_PICKER_DIALOG_ID = 1;
	public static final int WOTD_HTTP_PROXY_DIALOG_ID = 2;
	
	// by default, fire the timer each day between 00:01:00 and 00:01:59 UTC
	public static final int WOTD_HOUR_DEFAULT = 0;
	public static final int WOTD_MINUTE_DEFAULT = 1;
	
	private TimePickerDialog mTimePickerDialog = null;
	private AlertDialog mHttpProxyDialog = null;
	private View mWotdServiceControl = null;
	private TextView mHttpProxySetting = null;
	// private GestureDetector mDetector = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.preferences);
		
		mWotdServiceControl = findViewById(R.id.wotd_service_control);		
		mHttpProxySetting = (TextView)findViewById(R.id.http_proxy_setting);
		
		// mDetector = new GestureDetector(new GestureHandler());
		
		ToggleButton wotdNotifications = (ToggleButton)findViewById(R.id.wotd_notifications);
		Button wotdTime = (Button)findViewById(R.id.wotd_time);
		Button wotdPurge = (Button)findViewById(R.id.wotd_purge);
		Button httpProxySet = (Button)findViewById(R.id.set_http_proxy_button);
		
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
					((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancelAll();
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
		
		httpProxySet.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View v) {
				showDialog(WOTD_HTTP_PROXY_DIALOG_ID);
			}
		});
		
		wotdTime.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View v) {
				showDialog(WOTD_TIME_PICKER_DIALOG_ID);
			}
		});
		
		wotdPurge.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent serviceIntent = new Intent(getApplicationContext(),
						DubsarService.class);
				serviceIntent.setAction(DubsarService.ACTION_WOTD_PURGE);
				
				// purge and stop the service
				startService(serviceIntent);
				stopService(serviceIntent);
				
				// restart it
				serviceIntent.setAction(null);
				startService(serviceIntent);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		SharedPreferences preferences = getSharedPreferences(DUBSAR_PREFERENCES, MODE_PRIVATE);
		String proxyHost = preferences.getString(HTTP_PROXY_HOST, null);
		int proxyPort = preferences.getInt(HTTP_PROXY_PORT, 0);
		
		if (proxyHost != null && proxyPort > 0) {
			Log.d(getString(R.string.app_name), "On resume: proxy setting is \"" + proxyHost + ":" + proxyPort + "\"");
		
			mHttpProxySetting.setText(proxyHost + ":" + proxyPort);
		}
		else {
			Log.d(getString(R.string.app_name), "On resume: no proxy setting");
			mHttpProxySetting.setText(getString(R.string.no_proxy));
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		final SharedPreferences preferences = 
				getSharedPreferences(DUBSAR_PREFERENCES, MODE_PRIVATE);
		switch (id) {
		case WOTD_TIME_PICKER_DIALOG_ID:
			int hour = preferences.getInt(WOTD_HOUR, WOTD_HOUR_DEFAULT);
			int minute = preferences.getInt(WOTD_MINUTE, WOTD_MINUTE_DEFAULT);

			mTimePickerDialog = new TimePickerDialog(this, 
				this, 
				hour, 
				minute, 
				true);
			mTimePickerDialog.setTitle(getString(R.string.utc_time));
			return mTimePickerDialog;
		case WOTD_HTTP_PROXY_DIALOG_ID:
			LayoutInflater inflater = getLayoutInflater();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			final View proxyView = inflater.inflate(R.layout.http_proxy_form, null);
			final EditText httpHost = (EditText)proxyView.findViewById(R.id.http_proxy_host);
			final EditText httpPort = (EditText)proxyView.findViewById(R.id.http_proxy_port);

			builder.setView(proxyView);
			
			String host = preferences.getString(HTTP_PROXY_HOST, "");
			int port = preferences.getInt(HTTP_PROXY_PORT, 0);
			if (host != null) {
				httpHost.setText(host);
			}
			if (port != 0) {
				httpPort.setText("" + port);
			}
			
			builder.setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SharedPreferences.Editor editor = preferences.edit();
					editor.putString(HTTP_PROXY_HOST, httpHost.getText().toString());
					editor.putInt(HTTP_PROXY_PORT, Integer.valueOf(httpPort.getText().toString()));
					editor.commit();
					
					mHttpProxySetting.setText(httpHost.getText() + ":" + httpPort.getText());
				}
			}).setNeutralButton(R.string.clear, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					/*
					 *  DEBT: Confirm this? The user gets no second chance, and the proxy setting is gone.
					 */
					SharedPreferences.Editor editor = preferences.edit();
					editor.putString(HTTP_PROXY_HOST, "");
					editor.putInt(HTTP_PROXY_PORT, 0);
					editor.commit();
					
					httpHost.setText("");
					httpPort.setText("");
					
					mHttpProxySetting.setText(getString(R.string.no_proxy));
				}
			}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {	
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			
			mHttpProxyDialog = builder.create();
			return mHttpProxyDialog;
		default:
			return null;
		}
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
	
	protected void setLabel(int hour, int minute) {
		TextView wotdTimeLabel = (TextView)findViewById(R.id.wotd_time_label);

		StringBuffer buffer = new StringBuffer();
		Formatter formatter = new Formatter(buffer);
		formatter.format("%02d:%02d", Integer.valueOf(hour), Integer.valueOf(minute));
		formatter.close();
		
		wotdTimeLabel.setText(buffer);
		
	}
	
	protected PreferencesActivity getActivity() {
		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mTimePickerDialog != null) {
			removeDialog(WOTD_TIME_PICKER_DIALOG_ID);
		}
		if (mHttpProxyDialog != null) {
			removeDialog(WOTD_HTTP_PROXY_DIALOG_ID);
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
