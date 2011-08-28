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

import android.content.Intent;
import android.provider.Settings;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;
import android.widget.Button;

import com.dubsar_dictionary.Dubsar.DubsarActivity;
import com.dubsar_dictionary.Dubsar.PreferencesActivity;
import com.dubsar_dictionary.Dubsar.R;

public class PreferencesActivityTest extends
		ActivityInstrumentationTestCase2<PreferencesActivity> {

	public PreferencesActivityTest() {
		super("com.dubsar_dictionary.Dubsar", PreferencesActivity.class);
	}
	
	protected void tearDown() {
		getActivity().finish();
	}

	public void testForwardStack() {
		final Button bgDataChange = (Button)getActivity().findViewById(R.id.bg_data_change);
		getActivity().runOnUiThread(new Runnable() {
			public void run() {
				bgDataChange.performClick();
			}
		});
		
		try {
			Thread.sleep(100);
		}
		catch (InterruptedException e) {
			fail("sleep interrupted");
		}

		assertFalse(DubsarActivity.isForwardStackEmpty());
		Intent fwdIntent = DubsarActivity.forwardIntent();
		assertNotNull(fwdIntent);
		assertNotNull(fwdIntent.getAction());
		assertEquals(Settings.ACTION_SYNC_SETTINGS, fwdIntent.getAction());
		
		getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
	}
}
