/*
 Dubsar Dictionary Project
 Copyright (C) 2010-15 Jimmy Dee
 
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

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.provider.BaseColumns;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.Button;

import com.dubsar_dictionary.Dubsar.DubsarContentProvider;
import com.dubsar_dictionary.Dubsar.DubsarService;
import com.dubsar_dictionary.Dubsar.MainActivity;
import com.dubsar_dictionary.Dubsar.R;
import com.dubsar_dictionary.Dubsar.model.Model;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

	public MainActivityTest() {
		super(MainActivity.class);
	}
	
	protected void setUp() {
		Activity activity = getActivity();
		
		/* Start the service with mock data */
		Intent serviceIntent = new Intent(activity, DubsarService.class);
		serviceIntent.setAction(DubsarService.ACTION_WOTD_MOCK);
		serviceIntent.putExtra(BaseColumns._ID, 25441);
		serviceIntent.putExtra(DubsarService.WOTD_TEXT, "resourcefully (adv.)");
		serviceIntent.putExtra(DubsarContentProvider.WORD_NAME_AND_POS,
				"resourcefully (adv.)");
		activity.startService(serviceIntent);

		activity.finish();
	}
	
	protected void tearDown() {
		TestUtils.cleanupAfterService(getActivity());
	}
	
	/* not sure why this hangs at the moment
	public void testWotd() {
		Button wotdWord = (Button)getActivity().findViewById(R.id.wotd_word);
		
		assertEquals("resourcefully (adv.)", wotdWord.getText());
	}
	 */
	
	@UiThreadTest
	public void testBackButton() {
		Model.addMock("/words/25441",
				"[25441,\"resourcefully\",\"adv\",\"\",[[34828,[],\"in a resourceful manner  \",\"adv.all\",null,0]],0]");

		Instrumentation instr = getInstrumentation();

		Button wotdWord = (Button)getActivity().findViewById(R.id.wotd_word);

		wotdWord.performClick();
		
		Button backButton = (Button)getActivity().findViewById(R.id.left_arrow);
		assertNotNull(backButton);
		backButton.performClick();
		
		// cheat?
		instr.callActivityOnResume(getActivity());
		
		Button fwdButton = (Button)getActivity().findViewById(R.id.right_arrow);
		assertNotNull(fwdButton);
		assertTrue(fwdButton.isEnabled());	
	}

	/* Currently doesn't work. Either:
	 * 1. The assertion at the very end fails
	 * 2. The test simply hangs in TouchUtils.dragViewToX
	 * 3. Or TouchUtils.dragViewToX throws a SecurityException because of the
	 *    INJECT_EVENTS permission, which cannot be granted to any but system
	 *    apps.
	public void testScrollNavigation() {
		Model.addMock("/words/25441",
				"[25441,\"resourcefully\",\"adv\",\"\",[[34828,[],\"in a resourceful manner  \",\"adv.all\",null,0]],0]");
		
		final Instrumentation instr = getInstrumentation();

		final Button wotdWord = (Button)getActivity().findViewById(R.id.wotd_word);

		getActivity().runOnUiThread(new Runnable() {
			public void run() {
				wotdWord.performClick();
			}
		});
		
		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			fail("sleep interrupted");
		}

		DubsarActivity activity = (DubsarActivity)getActivity();
		View dubsarView = activity.findViewById(R.id.dubsar_view);
		assertNotNull(dubsarView);
		TouchUtils.dragViewToX(this, dubsarView, Gravity.TOP|Gravity.CENTER_HORIZONTAL,
				(int)(0.75*activity.getDisplayWidth()));
		
		Log.d("Dubsar", "scrolled view");
		// should go back
		
		try {
			Thread.sleep(2000);
		}
		catch (InterruptedException e) {
			fail("sleep interrupted");
		}
		
		getActivity().runOnUiThread(new Runnable() {
			public void run() {
				instr.callActivityOnResume(getActivity());				
			}
		});
		
		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			fail("sleep interrupted");
		}
	
		Button fwdButton = (Button)getActivity().findViewById(R.id.right_arrow);
		assertNotNull(fwdButton);
		assertTrue(fwdButton.isEnabled());	
	}
	 */
}
