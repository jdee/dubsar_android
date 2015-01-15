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

package com.dubsar_dictionary.Dubsar.test.model;

import java.util.List;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONTokener;

import com.dubsar_dictionary.Dubsar.model.Model;
import com.dubsar_dictionary.Dubsar.model.Sense;
import com.dubsar_dictionary.Dubsar.model.Word;

public class WordTest extends TestCase {

	public void testNameAndPos() {
		Word word = new Word(1, "food", Model.PartOfSpeech.Noun);
		
		assertEquals("food (n.)", word.getNameAndPos());
	}
	
	public void testSubtitle() {
		// DEBT: Need to be able to clone models
		Word word1 = new Word(1, "food", Model.PartOfSpeech.Noun);
		Word word2 = new Word(1, "food", Model.PartOfSpeech.Noun);
		Word word3 = new Word(1, "food", Model.PartOfSpeech.Noun);
		Word word4 = new Word(1, "food", Model.PartOfSpeech.Noun);
		
		word2.setFreqCnt(10);
		
		word3.setFreqCnt(10);
		word3.setInflections("foods");
		
		word4.setInflections("foods");
		
		assertEquals("", word1.getSubtitle());
		assertEquals("freq. cnt.: 10", word2.getSubtitle());
		assertEquals("freq. cnt.: 10; also foods", word3.getSubtitle());
		assertEquals("also foods", word4.getSubtitle());
	}
	
	public void testParsing() {
		String stringData = 
				"[21774,\"already\",\"adv\",\"\",[[30315,[],\"prior to a specified or implied time\",\"adv.all\",null,107]],107]";

		// This resembles how words are initially constructed in
		// search responses.
		Word word = new Word(21774, "already", "adv");
		word.setData(stringData);
		
		try {
			JSONTokener tokener = new JSONTokener(stringData);
			word.parseData(tokener.nextValue());
		}
		catch (JSONException e) {
			fail("JSON parsing failed with error " + e.getMessage());
			return;
		}
		
		assertEquals(21774, word.getId());
		assertEquals("already", word.getName());
		assertEquals(Model.PartOfSpeech.Adverb, word.getPartOfSpeech());
		assertEquals("", word.getInflections());
		assertEquals(107, word.getFreqCnt());
		
		List<Sense> senses = word.getSenses();
		assertEquals(1, senses.size());
		
		Sense sense = senses.get(0);
		assertEquals(30315, sense.getId());
		assertEquals(0, sense.getSynonyms().size());
		assertEquals("prior to a specified or implied time", sense.getGloss());
		assertEquals("adv.all", sense.getLexname());
		assertNull(sense.getMarker());
		assertEquals(107, sense.getFreqCnt());
	}
}
