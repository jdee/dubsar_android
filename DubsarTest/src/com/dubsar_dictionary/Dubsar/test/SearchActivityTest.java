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
import android.view.KeyEvent;
import android.widget.ListView;
import android.widget.TextView;

import com.dubsar_dictionary.Dubsar.R;
import com.dubsar_dictionary.Dubsar.SearchActivity;
import com.dubsar_dictionary.Dubsar.model.Model;


public class SearchActivityTest 
	extends ActivityInstrumentationTestCase2<SearchActivity> {
	SearchActivity mActivity=null;
	ListView mListView=null;
	TextView mTextView=null;

	public SearchActivityTest() {
		super("com.dubsar_dictionary.Dubsar", SearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();

		setActivityInitialTouchMode(false);
		
		mActivity = (SearchActivity) getActivity();
		
		mListView = (ListView)mActivity.findViewById(R.id.search_word_list);
		mTextView = (TextView)mActivity.findViewById(R.id.search_banner);
	}
	
	public void testPreConditions() {
		assertNotNull(mActivity);
		assertNotNull(mListView);
		assertNotNull(mTextView);
	}
	
	public void testSearch() {
		Model.addMock("/os?term=already", "[\"already\",[\"already\"]]");
		Model.addMock("/os?term=a", 
				"[\"a\",[\"ask\",\"also\",\"appear\",\"all\",\"again\",\"area\",\"add\",\"always\",\"almost\",\"allow\"]]");
		Model.addMock("/?term=already",
				"[\"already\",[[21774,\"already\",\"adv\",107,\"\"]],1]");
		
		// DEBT: How do I enter text in the search dialog?
		sendKeys(new int[] { 
			KeyEvent.KEYCODE_SEARCH, 
			KeyEvent.KEYCODE_A,
			KeyEvent.KEYCODE_L,
			KeyEvent.KEYCODE_R,
			KeyEvent.KEYCODE_E,
			KeyEvent.KEYCODE_A,
			KeyEvent.KEYCODE_D,
			KeyEvent.KEYCODE_Y,
			KeyEvent.KEYCODE_ENTER 
		});

		// no error
		assertEquals(getActivity().getString(R.string.loading), mTextView.getText());
	}

	public void testMapping() {
		Model.setContext(getActivity());
		
		assertEquals(Model.PartOfSpeech.Adjective, Model.partOfSpeechFromPos("adj"));
		assertEquals(Model.PartOfSpeech.Adverb, Model.partOfSpeechFromPos("adv"));
		assertEquals(Model.PartOfSpeech.Conjunction, Model.partOfSpeechFromPos("conj"));
		assertEquals(Model.PartOfSpeech.Interjection, Model.partOfSpeechFromPos("interj"));
		assertEquals(Model.PartOfSpeech.Noun, Model.partOfSpeechFromPos("n"));
		assertEquals(Model.PartOfSpeech.Preposition, Model.partOfSpeechFromPos("prep"));
		assertEquals(Model.PartOfSpeech.Pronoun, Model.partOfSpeechFromPos("pron"));
		assertEquals(Model.PartOfSpeech.Verb, Model.partOfSpeechFromPos("v"));
		
		assertEquals("adj", Model.posFromPartOfSpeech(Model.PartOfSpeech.Adjective));
		assertEquals("adv", Model.posFromPartOfSpeech(Model.PartOfSpeech.Adverb));
		assertEquals("conj", Model.posFromPartOfSpeech(Model.PartOfSpeech.Conjunction));
		assertEquals("interj", Model.posFromPartOfSpeech(Model.PartOfSpeech.Interjection));
		assertEquals("n", Model.posFromPartOfSpeech(Model.PartOfSpeech.Noun));
		assertEquals("prep", Model.posFromPartOfSpeech(Model.PartOfSpeech.Preposition));
		assertEquals("pron", Model.posFromPartOfSpeech(Model.PartOfSpeech.Pronoun));
		assertEquals("v", Model.posFromPartOfSpeech(Model.PartOfSpeech.Verb));
	}
}
