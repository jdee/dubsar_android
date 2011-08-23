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

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;

public class DubsarPreferences extends DubsarActivity {
	
	public static final String WOTD_NOTIFICATIONS = "wotd_notifications";
	public static final String DUBSAR_PREFERENCES = "dubsar_preferences";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.preferences);
		
		ToggleButton wotdNotifications = (ToggleButton)findViewById(R.id.wotd_notifications);
		
		SharedPreferences preferences = getSharedPreferences(DUBSAR_PREFERENCES, MODE_PRIVATE);
		wotdNotifications.setChecked(preferences.getBoolean(WOTD_NOTIFICATIONS, true));
		
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
				
				Log.d(getString(R.string.app_name), "saved preferences");
			}
		});
	}

}
