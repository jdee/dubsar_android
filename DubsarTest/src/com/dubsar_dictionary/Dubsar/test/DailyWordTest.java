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

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONTokener;

import com.dubsar_dictionary.Dubsar.model.DailyWord;
import com.dubsar_dictionary.Dubsar.model.Model;
import com.dubsar_dictionary.Dubsar.model.Word;

public class DailyWordTest extends TestCase {

	public void testParsing() {
		String stringData = "[25441,\"resourcefully\",\"adv\",0,\"\"]";
		
		DailyWord dailyWord = new DailyWord();
		dailyWord.setData(stringData);
		
		try {
			JSONTokener tokener = new JSONTokener(stringData);
			dailyWord.parseData(tokener.nextValue());
		}
		catch (JSONException e) {
			fail("JSON parsing failed with error " + e.getMessage());
		}
		
		Word word = dailyWord.getWord();
		assertEquals(25441, word.getId());
		assertEquals("resourcefully", word.getName());
		assertEquals(Model.PartOfSpeech.Adverb, word.getPartOfSpeech());
		assertEquals(0, word.getFreqCnt());
		assertEquals("", word.getInflections());
	}
}
