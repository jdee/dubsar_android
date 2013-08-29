/*
 Dubsar Dictionary Project
 Copyright (C) 2010-13 Jimmy Dee
 
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

package com.dubsar_dictionary.Dubsar.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * 
 * Class to encapsulate suggestion requests and results.
 *
 */
public class Autocompleter extends Model {
	private List<String> mResults = null;
	private String mTerm = null;
	
	/**import java.net.URLEncoder;

	 * Request suggestions for the specified term.
	 * @param term the term, as typed by the user so far
	 */
	public Autocompleter(String term) {
		mTerm = new String(term);
		
		// DEBT: Take from strings file
		try {
			mPath = new String("/os?term=") + URLEncoder.encode(mTerm, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			setErrorMessage(e.getMessage());
		}
	}

	/**
	 * The list of results matching mTerm
	 * @return a list of suggestions, as strings
	 */
	public final List<String> getResults() {
		return mResults;
	}

	@Override
	public void parseData(Object jsonResponse) throws JSONException {
		/*
		 * Expect data like [ "term", [ "result1", "result2", ... "result10" ] ]
		 * (based on the OpenSearch protocol)
		 */
		JSONArray response = (JSONArray)jsonResponse;
		JSONArray list = response.getJSONArray(1);

		mResults = new ArrayList<String>(list.length());

		for (int j = 0; j < list.length(); ++j) {
			mResults.add(list.getString(j));
		}
	}

}
