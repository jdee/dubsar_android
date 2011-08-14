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

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;

import com.dubsar_dictionary.Dubsar.MainActivity;
import com.dubsar_dictionary.Dubsar.R;
import com.dubsar_dictionary.Dubsar.model.Model;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

	public MainActivityTest() {
		super("com.dubsar_dictionary.Dubsar", MainActivity.class);
	}
	
	protected void setUp() {
		Model.addMock("/wotd", "[25441,\"resourcefully\",\"adv\",0,\"\"]");
	}
	
	public void testWotd() {
		Button wotdWord = (Button)getActivity().findViewById(R.id.wotd_word);
		
		try {
			// delay long enough to let the mock data populate the view
			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			fail("sleep interrupted");
		}
		
		assertEquals("resourcefully (adv.)", wotdWord.getText());
	}
}
