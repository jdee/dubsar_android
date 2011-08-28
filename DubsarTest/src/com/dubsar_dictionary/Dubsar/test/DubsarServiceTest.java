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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.content.Intent;
import android.content.IntentFilter;
import android.provider.BaseColumns;
import android.test.ServiceTestCase;

import com.dubsar_dictionary.Dubsar.DubsarContentProvider;
import com.dubsar_dictionary.Dubsar.DubsarService;
import com.dubsar_dictionary.Dubsar.test.TestUtils.TestReceiver;

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
		TestUtils.cleanupAfterService(getContext());

		try {
			Thread.sleep(100);
		}
		catch (InterruptedException e) {
			
		}
		
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
			Thread.sleep(100);
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
			Thread.sleep(200);

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
	
	public void testPastWotdTime() {
		/*
		 * First start a service with mock data. It will save the data we
		 * send, but not start a timer, update the expiration time or do
		 * anything else.
		 */
		Intent serviceIntent = new Intent(getContext(), DubsarService.class);

		serviceIntent.setAction(DubsarService.ACTION_WOTD_MOCK);
		serviceIntent.putExtra(DubsarService.WOTD_TIME, 0l);
		serviceIntent.putExtra(BaseColumns._ID, 25441);
		serviceIntent.putExtra(DubsarService.WOTD_TEXT, "resourcefully (adv.)");
		serviceIntent.putExtra(DubsarContentProvider.WORD_NAME_AND_POS,
				"resourcefully (adv.)");
		getContext().startService(serviceIntent);
		
		try {
			Thread.sleep(100);
		}
		catch (InterruptedException e) {
			fail("sleep interrupted");
		}
		
		/*
		 * After it's started, check that the file has the timestamp we sent.
		 */
		assertEquals(getTimeFromWotdFile(), 0);
		/*
		 * Now stop the service with the mock data.
		 */
		getContext().stopService(serviceIntent);
		
		try {
			Thread.sleep(100);
		}
		catch (InterruptedException e) {
			fail("sleep interrupted");
		}
		
		/*
		 * After it's stopped, restart it normally, without mock data. It will
		 * load the 0 timestamp from storage, update it and write out the new
		 * time. It should not blow up in the process.
		 */
		getContext().startService(new Intent(getContext(), DubsarService.class));
		
		try {
			Thread.sleep(200);
		}
		catch (InterruptedException e) {
			fail("sleep interrupted");
		}
		
		/*
		 * Now check that the timestamp is reasonable.
		 */
		assertTrue(getTimeFromWotdFile() > 0);
	}

	public void testBroadcast() {
		// give the service time to start
		try {
			Thread.sleep(200);
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
	
	protected long getTimeFromWotdFile() {
		try {
			FileInputStream input=null;
			try {
				input = getContext().openFileInput(DubsarService.WOTD_FILE_NAME);
				
				byte[] lbuffer = new byte[8];
	
				/* next WOTD time */
				input.read(lbuffer);
				return DubsarService.decodeLong(lbuffer);
			}
			catch (FileNotFoundException e) {
				fail("OPEN " + DubsarService.WOTD_FILE_NAME + ": " + e.getMessage());
			}
			finally {
				if (input != null) input.close();
			}
		}
		catch (Exception e) {
			fail("READ " + DubsarService.WOTD_FILE_NAME + ": " + e.getMessage());
		}
		
		return -1;
	}
}
