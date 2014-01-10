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

package com.dubsar_dictionary.Dubsar.model;

import org.json.JSONArray;
import org.json.JSONException;

public class DailyWord extends Model {
	
	private Word mWord=null;
	private long mExpirationMillis=0;
	
	public DailyWord() {
		mPath = "/wotd";
	}
	
	public final Word getWord() {
		return mWord;
	}
	
	public long getExpirationMillis() {
		return mExpirationMillis;
	}

	@Override
	public void parseData(Object jsonResponse) throws JSONException {
		JSONArray response = (JSONArray)jsonResponse;
		
		int id = response.getInt(0);
		String name = response.getString(1);
		String pos = response.getString(2);
		int freqCnt = response.getInt(3);
		String inflections = response.getString(4);
		
		mWord = new Word(id, name, pos);
		
		mWord.setFreqCnt(freqCnt);
		mWord.setInflections(inflections);
		
		mExpirationMillis = response.getLong(5) * 1000;
	}

}
