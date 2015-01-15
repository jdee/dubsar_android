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
import com.dubsar_dictionary.Dubsar.model.Synset;

public class SynsetTest extends TestCase {

	public void testParsing() {
		String stringData = 
				"[21803,\"n\",\"noun.Tops\",\"synset gloss\",[],[[35629,\"food\",null,29],[35630,\"nutrient\",null,1]],30,[[\"hypernym\",\"synset\",21801,\"substance\",\"hypernym gloss\"]]]";
		
		Synset synset = new Synset(21803);
		synset.setData(stringData);
		
		try {
			JSONTokener tokener = new JSONTokener(stringData);
			synset.parseData(tokener.nextValue());
		}
		catch (JSONException e) {
			fail("JSON parsing failed with error " + e.getMessage());
		}
		
		assertEquals(Model.PartOfSpeech.Noun, synset.getPartOfSpeech());
		assertEquals("noun.Tops", synset.getLexname());
		assertEquals("synset gloss", synset.getGloss());
		assertEquals(0, synset.getSamples().size());
		
		List<Sense> senses = synset.getSenses();
		assertEquals(2, senses.size());
		
		assertEquals(30, synset.getFreqCnt());
	}
}
