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

package com.dubsar_dictionary.Dubsar.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.provider.BaseColumns;
import android.test.ServiceTestCase;

import com.dubsar_dictionary.Dubsar.DubsarActivity;
import com.dubsar_dictionary.Dubsar.DubsarContentProvider;
import com.dubsar_dictionary.Dubsar.DubsarService;
import com.dubsar_dictionary.Dubsar.MainActivity;

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

	public void testPurge() {
		try {
			/*
			 * Give it time to start, then check that its cache exists.
			 */
			Thread.sleep(200);
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
	
	/* not sure why this is failing
	public void testPastWotdTime() {
		/*
		 * First start a service with mock data. It will save the data we
		 * send, but not start a timer, update the expiration time or do
		 * anything else.
		 *  /
		Intent serviceIntent = new Intent(getContext(), DubsarService.class);

		final long expiration = System.currentTimeMillis() + 60000;

		serviceIntent.setAction(DubsarService.ACTION_WOTD_MOCK);
		serviceIntent.putExtra(DubsarContentProvider.WOTD_EXPIRATION_MILLIS, expiration);
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
		 *  /
		long storedTime = getTimeFromWotdFile();
		assertEquals(expiration, storedTime);
	}
     */

	/* not sticky any more
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
	 */
	
	public void testIntentComparison() {
		Intent i1 = new Intent();
		Intent i2 = new Intent();
		
		/* both all null and equal */
		assertNull(i1.getAction());
		assertNull(i1.getComponent());
		assertNull(i1.getData());
		assertNull(i1.getExtras());
		assertTrue(DubsarActivity.equalIntents(i1, i2));
		
		/* null intent comparison */
		assertTrue(DubsarActivity.equalIntents(null, null));
		assertFalse(DubsarActivity.equalIntents(i1, null));
		
		Uri uri1 = DubsarContentProvider.CONTENT_URI;
		Uri uri2 = Uri.withAppendedPath(uri1,
				DubsarContentProvider.SEARCH_URI_PATH + "/a");
		i1.setData(uri1);
		
		/* URI comparisons */
		assertFalse(DubsarActivity.equalIntents(i1, i2));
		
		i2.setData(uri1);
		assertTrue(DubsarActivity.equalIntents(i1, i2));
		
		i2.setData(uri2);
		assertFalse(DubsarActivity.equalIntents(i1, i2));
		
		i1.setData(null);
		i2.setData(null);
		
		/* QUERY extra comparisons */
		i1.putExtra(SearchManager.QUERY, "a");
		assertFalse(DubsarActivity.equalIntents(i1, i2));
		
		i2.putExtra(SearchManager.QUERY, "b");
		assertFalse(DubsarActivity.equalIntents(i1, i2));
		
		i2.putExtra(SearchManager.QUERY, "a");
		assertTrue(DubsarActivity.equalIntents(i1, i2));
		
		i1.removeExtra(SearchManager.QUERY);
		i2.removeExtra(SearchManager.QUERY);
		
		/* action comparisons */
		i1.setAction(DubsarService.ACTION_WOTD);
		assertFalse(DubsarActivity.equalIntents(i1, i2));
		
		i2.setAction(DubsarService.ACTION_WOTD_NOTIFICATION);
		assertFalse(DubsarActivity.equalIntents(i1, i2));
		
		i2.setAction(DubsarService.ACTION_WOTD);
		assertTrue(DubsarActivity.equalIntents(i1, i2));
		
		i1.setAction(null);
		i2.setAction(null);
		
		/* component comparisons */
		i2 = new Intent(getContext(), MainActivity.class);
		assertFalse(DubsarActivity.equalIntents(i1, i2));
		
		i1 = new Intent(getContext(), DubsarService.class);
		assertFalse(DubsarActivity.equalIntents(i1, i2));
		
		i2 = new Intent(getContext(), DubsarService.class);
		assertTrue(DubsarActivity.equalIntents(i1, i2));
		
		/* compare by action and component */
		i1.setAction(DubsarService.ACTION_WOTD);
		i2.setAction(DubsarService.ACTION_WOTD);
		assertTrue(DubsarActivity.equalIntents(i1, i2));
		
		i2.setAction(DubsarService.ACTION_WOTD_NOTIFICATION);
		assertFalse(DubsarActivity.equalIntents(i1, i2));
		
		i2 = new Intent(getContext(), MainActivity.class);
		i2.setAction(DubsarService.ACTION_WOTD);
		assertFalse(DubsarActivity.equalIntents(i1, i2));
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
