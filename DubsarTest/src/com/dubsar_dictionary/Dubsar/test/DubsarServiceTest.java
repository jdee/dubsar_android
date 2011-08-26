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

import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.test.ServiceTestCase;

import com.dubsar_dictionary.Dubsar.DubsarContentProvider;
import com.dubsar_dictionary.Dubsar.DubsarService;

public class DubsarServiceTest extends ServiceTestCase<DubsarService> {

	public DubsarServiceTest() {
		super(DubsarService.class);
	}
	
	protected void setUp() {
		Intent serviceIntent = new Intent(getContext(), DubsarService.class);
		startService(serviceIntent);

		serviceIntent.setAction(DubsarService.ACTION_WOTD_MOCK);
		serviceIntent.putExtra(BaseColumns._ID, 25441);
		serviceIntent.putExtra(DubsarService.WOTD_TEXT, "resourcefully (adv.)");
		serviceIntent.putExtra(DubsarContentProvider.WORD_NAME_AND_POS,
				"resourcefully (adv.)");
		getContext().startService(serviceIntent);
	}
	
	protected void tearDown() {
		shutdownService();

		/*
		 * Remove any sticky broadcast
		 */
		TestReceiver receiver = new TestReceiver();
		IntentFilter filter = new IntentFilter(DubsarService.ACTION_WOTD);

		Intent broadcast = getContext().registerReceiver(receiver, filter);
		if (broadcast != null) {
			getContext().removeStickyBroadcast(broadcast);
		}
		getContext().unregisterReceiver(receiver);
		
		/* also purge the service cache */
		Intent purgeIntent = new Intent(getContext(), DubsarService.class);
		purgeIntent.setAction(DubsarService.ACTION_WOTD_PURGE);
		startService(purgeIntent);
		
		shutdownService();
	}
	
	public void testWotdTime() {
		Calendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		int _amPm = now.get(Calendar.AM_PM);
		int hour = now.get(Calendar.HOUR);
		if (_amPm == Calendar.PM) hour += 12;
				
		// later time of day
		if (hour == 23) hour = -1;
		long result = DubsarService.computeNextWotdTime(hour+1, 0);
		now.setTimeInMillis(result);
		
		int calendarHour = now.get(Calendar.HOUR);
		_amPm = now.get(Calendar.AM_PM);
		if (_amPm == Calendar.PM) calendarHour += 12;
		
		assertEquals(hour+1, calendarHour);

		// earlier time of day
		now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		_amPm = now.get(Calendar.AM_PM);
		hour = now.get(Calendar.HOUR);
		if (_amPm == Calendar.PM) hour += 12;

		if (hour == 0) hour = 24;
		result = DubsarService.computeNextWotdTime(hour-1, 0);
		now.setTimeInMillis(result);
		
		calendarHour = now.get(Calendar.HOUR);
		_amPm = now.get(Calendar.AM_PM);
		if (_amPm == Calendar.PM) calendarHour += 12;
		
		assertEquals(hour-1, calendarHour);
	}

	public void testPurge() {
		try {
			/*
			 * Give it time to start, then check that its cache exists.
			 */
			Thread.sleep(1000);
			getContext().openFileInput(DubsarService.WOTD_FILE_NAME);
		}
		catch (InterruptedException e) {
			fail("sleep interrupted");
		}
		catch (FileNotFoundException e) {
			fail(DubsarService.WOTD_FILE_NAME + ": " + e.getMessage());
		}
		
		/*
		 * Now tell the service to delete its cache.
		 */
		Intent purgeIntent = new Intent(getContext(), DubsarService.class);
		purgeIntent.setAction(DubsarService.ACTION_WOTD_PURGE);
		getContext().startService(purgeIntent);

		try {
			/*
			 * Give it time to process the intent, then check that it's
			 * been deleted.
			 */
			Thread.sleep(1000);

			/* this method should throw a FileNotFoundException now */
			getContext().openFileInput(DubsarService.WOTD_FILE_NAME);
			fail("purge failed");
		}
		catch (FileNotFoundException e) {
		}
		catch (InterruptedException e) {
			fail("sleep interrupted");
		}
	}

	public void testBroadcast() {
		// give the service time to start
		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			fail("sleep interrupted");
		}
		
		// should generate a sticky broadcast
		TestReceiver receiver = new TestReceiver();
		IntentFilter filter = new IntentFilter(DubsarService.ACTION_WOTD);
		
		Intent broadcast = getContext().registerReceiver(receiver, filter);
		assertNotNull(broadcast);
		assertEquals(broadcast.getAction(), DubsarService.ACTION_WOTD);
	}
	
	protected static class TestReceiver extends BroadcastReceiver {
		
		public int id=0;
		public String text=null;
		public String nameAndPos=null;
		public String errorMessage=null;

		@Override
		public void onReceive(Context context, Intent intent) {
			assertEquals(intent.getAction(), DubsarService.ACTION_WOTD);
			
			Bundle extras = intent.getExtras();
			assertNotNull(extras);
			
			id = extras.getInt(BaseColumns._ID);
			text = extras.getString(DubsarService.WOTD_TEXT);
			nameAndPos = extras.getString(DubsarContentProvider.WORD_NAME_AND_POS);
			errorMessage = extras.getString(DubsarService.ERROR_MESSAGE);
		}
		
	}
}
