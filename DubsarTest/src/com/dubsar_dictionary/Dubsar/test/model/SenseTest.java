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

package com.dubsar_dictionary.Dubsar.test.model;

import java.util.List;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONTokener;

import com.dubsar_dictionary.Dubsar.model.Model;
import com.dubsar_dictionary.Dubsar.model.Sense;
import com.dubsar_dictionary.Dubsar.model.Synset;
import com.dubsar_dictionary.Dubsar.model.Word;

public class SenseTest extends TestCase {
	
	public void testParsing() {
		String stringData = "[35629,[26063,\"food\",\"n\"],[21803,\"sense gloss\"],\"noun.Tops\",null,29,[[35630,\"nutrient\",null,1]],[],[],[[\"hypernym\",\"synset\",21801,\"substance\",\"hypernym gloss\"]]]";

		Sense sense = new Sense(35629);
		sense.setData(stringData);
		
		try {
			JSONTokener tokener = new JSONTokener(stringData);
			sense.parseData(tokener.nextValue());
		}
		catch (JSONException e) {
			fail("JSON parsing failed with error " + e.getMessage());
		}
		
		assertEquals(35629, sense.getId());
		assertEquals("food", sense.getName());
		assertEquals(Model.PartOfSpeech.Noun, sense.getPartOfSpeech());
		assertEquals("noun.Tops", sense.getLexname());
		assertNull(sense.getMarker());
		assertEquals(29, sense.getFreqCnt());
		assertEquals("sense gloss", sense.getGloss());
		
		Word word = sense.getWord();
		Synset synset = sense.getSynset();
		
		assertEquals(26063, word.getId());
		assertEquals("food", word.getName());
		assertEquals(Model.PartOfSpeech.Noun, word.getPartOfSpeech());
		
		assertEquals(21803, synset.getId());
		assertEquals("sense gloss", synset.getGloss());
		
		List<Sense> synonyms = sense.getSynonyms();
		assertEquals(1, synonyms.size());
		
		Sense synonym = synonyms.get(0);
		assertEquals(35630, synonym.getId());
		assertEquals("nutrient", synonym.getName());
		assertNull(synonym.getMarker());
		assertEquals(1, synonym.getFreqCnt());
		
		assertNotNull(sense.getSamples());
		assertNotNull(sense.getVerbFrames());
		assertEquals(0, sense.getSamples().size());
		assertEquals(0, sense.getVerbFrames().size());
	}

}
