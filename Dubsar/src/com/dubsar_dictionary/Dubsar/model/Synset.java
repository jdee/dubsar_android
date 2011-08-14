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

import org.json.JSONException;

public class Synset extends Model {
	
	private int mId=0;
	private String mGloss=null;
	private String mLexname=null;
	private PartOfSpeech mPartOfSpeech=PartOfSpeech.Unknown;
	
	public Synset(int id, String gloss, PartOfSpeech partOfSpeech) {
		mId = id;
		mGloss = new String(gloss);
		mPartOfSpeech = partOfSpeech;
	}
	
	public int getId() {
		return mId;
	}
	
	public final String getGloss() {
		return mGloss;
	}
	
	public PartOfSpeech getPartOfSpeech() {
		return mPartOfSpeech;
	}
	
	public final String getLexname() {
		return mLexname;
	}
	
	public void setLexname(String lexname) {
		mLexname = new String(lexname);
	}

	@Override
	public void parseData(Object jsonResponse) throws JSONException {
		// TODO Auto-generated method stub

	}

}
