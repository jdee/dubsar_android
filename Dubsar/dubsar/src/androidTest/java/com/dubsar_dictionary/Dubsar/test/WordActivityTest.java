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

import android.content.Intent;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.dubsar_dictionary.Dubsar.DubsarContentProvider;
import com.dubsar_dictionary.Dubsar.R;
import com.dubsar_dictionary.Dubsar.WordActivity;
import com.dubsar_dictionary.Dubsar.model.Model;

public class WordActivityTest extends
		ActivityInstrumentationTestCase2<WordActivity> {

	public WordActivityTest() {
		super(WordActivity.class);
	}
	
	protected void setUp() {
		// add a mock for the content provider
		Model.addMock("/words/27222", 
				"[27222,\"choice\",\"n\",\"choices\",[[91286,[[91287,\"pick\"],[91288,\"selection\"]],\"the person or thing chosen or selected\",\"noun.cognition\",null,11]],22]");
		
		// specify which word we want to retrieve from the provider
    	Intent wordIntent = new Intent();
    	wordIntent.putExtra(DubsarContentProvider.WORD_NAME_AND_POS, "choice (n.)");
    	Uri data = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI,
                                        DubsarContentProvider.WORDS_URI_PATH + 
                                        "/27222");
        wordIntent.setData(data);
        setActivityIntent(wordIntent);
	}

	protected void tearDown() {
		TestUtils.cleanupAfterService(getActivity());
	}
	
	public void testActivity() {
		TextView banner = (TextView)getActivity().findViewById(R.id.word_banner);
		TextView inflections = (TextView)getActivity().findViewById(R.id.word_inflections);
		
		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			fail("sleep interrupted");
		}
		
		assertEquals("choice (n.)", banner.getText());
		assertEquals("freq. cnt.: 22; also choices", inflections.getText());
	}
}
