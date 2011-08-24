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

import android.app.SearchManager;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.dubsar_dictionary.Dubsar.R;
import com.dubsar_dictionary.Dubsar.SearchActivity;
import com.dubsar_dictionary.Dubsar.model.Model;


public class SearchActivityTest 
	extends ActivityInstrumentationTestCase2<SearchActivity> {

	public SearchActivityTest() {
		super("com.dubsar_dictionary.Dubsar", SearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();

		setActivityInitialTouchMode(false);
	}
	
	public void testSearch() {
		Intent searchIntent = new Intent();
		searchIntent.setAction(Intent.ACTION_SEARCH);
		searchIntent.putExtra(SearchManager.QUERY, "a");
		
		setActivityIntent(searchIntent);

		Model.addMock("/?term=a",
				"[\"a\",[[79620,\"a\",\"n\",0,\"as\"],[70817,\"A\",\"n\",0,\"\"]],1]");

		TextView textView = (TextView)getActivity().findViewById(R.id.search_banner);

		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			fail("sleep interrupted");
		}

		// no error
		assertEquals(getActivity().getString(R.string.search_results, new Object[] {"a"}), 
				textView.getText());
	}

	public void testNoResults() {
		Intent searchIntent = new Intent();
		searchIntent.setAction(Intent.ACTION_SEARCH);
		searchIntent.putExtra(SearchManager.QUERY, "foo");
		
		setActivityIntent(searchIntent);

		Model.addMock("/?term=foo",	"[\"foo\",[],0]");

		TextView textView = (TextView)getActivity().findViewById(R.id.search_banner);

		try {
			Thread.sleep(1500);
		}
		catch (InterruptedException e) {
			fail("sleep interrupted");
		}

		assertEquals(getActivity().getString(R.string.no_results, new Object[] {"foo"}), 
				textView.getText());
	}
}
