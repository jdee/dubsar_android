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

package com.dubsar_dictionary.Dubsar.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

public class Search extends Model {
	private List<Word> mResults=null;
	private String mTerm=null;
	private int mTotalPages=0;
	
	public Search(String term) {
		mTerm = new String(term);
		
		// DEBT: Take from strings file
		mPath = new String("/?term=") + mTerm;
	}
	
	public final List<Word> getResults() {
		return mResults;
	}
	
	public int getTotalPages() {
		return mTotalPages;
	}

	@Override
	public void parseData(Object jsonResponse) throws JSONException {
		JSONArray response = (JSONArray)jsonResponse;
		JSONArray list = response.getJSONArray(1);
		
		mTotalPages = response.getInt(2);
		mResults = new ArrayList<Word>(list.length());
		
		for (int j=0; j<list.length(); ++j) {
			JSONArray entry = list.getJSONArray(j);
			
			int _id           = entry.getInt(0);
			String name       = entry.getString(1);
			String posString  = entry.getString(2);
			int freqCnt       = entry.getInt(3);
			String otherForms = entry.getString(4);
		}
	}

}
