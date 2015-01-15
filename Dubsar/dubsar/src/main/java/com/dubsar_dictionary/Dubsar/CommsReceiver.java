/*
 Dubsar Dictionary Project
 Copyright (C) 2010-14 Jimmy Dee
 
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
// import android.util.Log;

public class CommsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null || intent.getAction() == null)
			return;
		
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			// Log.i(context.getString(R.string.app_name), "Boot completed");
		}
		else {
			Log.w(context.getString(R.string.app_name),
					"CommsReceiver ignoring unexpected action " + intent.getAction());
			return;
		}
		
		Intent serviceIntent = new Intent(context, DubsarService.class);
		context.startService(serviceIntent);
	}

}
