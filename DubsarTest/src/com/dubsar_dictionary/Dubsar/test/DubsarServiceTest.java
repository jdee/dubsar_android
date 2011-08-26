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
import com.dubsar_dictionary.Dubsar.model.Model;

public class DubsarServiceTest extends ServiceTestCase<DubsarService> {

	public DubsarServiceTest() {
		super(DubsarService.class);
	}
	
	protected void setUp() {
		Model.addMock("/wotd", "[25441,\"resourcefully\",\"adv\",1,\"\"]");		
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

	public void testBroadcast() {
		
		Intent serviceIntent = new Intent(getContext(), DubsarService.class);
		startService(serviceIntent);
		
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
	
	static class TestReceiver extends BroadcastReceiver {
		
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
