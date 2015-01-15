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

import com.dubsar_dictionary.Dubsar.model.Autocompleter;

public class AutocompleterTest extends TestCase {
	
	public void testParsing() {
		String stringData = "[ \"li\", [ \"like\", \"link\", \"lion\" ] ]";
		
		Autocompleter autocompleter = new Autocompleter("li");
		autocompleter.setData(stringData);
		
		try {
			JSONTokener tokener = new JSONTokener(stringData);
			autocompleter.parseData(tokener.nextValue());
		}
		catch (JSONException e) {
			fail("JSON parsing failed with error " + e.getMessage());
			return;
		}
		
		List<String> results = autocompleter.getResults();
		
		assertEquals(3, results.size());

		assertEquals("like", results.get(0));
		assertEquals("link", results.get(1));
		assertEquals("lion", results.get(2));
	}

}
