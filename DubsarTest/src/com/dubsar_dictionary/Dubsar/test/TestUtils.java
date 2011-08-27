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

package com.dubsar_dictionary.Dubsar.test;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.BaseColumns;

import com.dubsar_dictionary.Dubsar.DubsarContentProvider;
import com.dubsar_dictionary.Dubsar.DubsarService;

public class TestUtils {

	public static void cleanupAfterService(Context context) {
		/*
		 * Remove any sticky broadcast
		 */
		TestReceiver receiver = new TestReceiver();
		IntentFilter filter = new IntentFilter(DubsarService.ACTION_WOTD);

		Intent broadcast = context.registerReceiver(receiver, filter);
		if (broadcast != null) {
			context.removeStickyBroadcast(broadcast);
		}
		context.unregisterReceiver(receiver);

		/*
		 * Purge the service cache and shut it down.
		 */
		Intent purgeIntent = new Intent(context, DubsarService.class);
		purgeIntent.setAction(DubsarService.ACTION_WOTD_PURGE);
		context.startService(purgeIntent);
		context.stopService(purgeIntent);

		/*
		 * Get rid of any status bar notifications we generated
		 */
		NotificationManager mgr =
				(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		mgr.cancelAll();
	}
	
	static class TestReceiver extends BroadcastReceiver {
		public int id=0;
		public String text=null;
		public String nameAndPos=null;
		public String errorMessage=null;

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			
			id = extras.getInt(BaseColumns._ID);
			text = extras.getString(DubsarService.WOTD_TEXT);
			nameAndPos = extras.getString(DubsarContentProvider.WORD_NAME_AND_POS);
			errorMessage = extras.getString(DubsarService.ERROR_MESSAGE);
		}
	}
}
