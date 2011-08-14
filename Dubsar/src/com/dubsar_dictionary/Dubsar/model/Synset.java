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

/**
 * 
 * Synset class
 *
 */
public class Synset extends Model {
	
	private int mId=0;
	private String mGloss=null;
	private String mLexname=null;
	private PartOfSpeech mPartOfSpeech=PartOfSpeech.Unknown;
	
	/**
	 * Construct a Synset in a Sense response
	 * @param id a Synset ID
	 * @param gloss the synset gloss
	 * @param partOfSpeech the synset part of speech
	 */
	public Synset(int id, String gloss, PartOfSpeech partOfSpeech) {
		mId = id;
		mGloss = new String(gloss);
		mPartOfSpeech = partOfSpeech;
	}
	
	/**
	 * The Synset ID in the database
	 * @return Synset ID
	 */
	public int getId() {
		return mId;
	}
	
	/**
	 * This Synset's gloss
	 * @return the Synset gloss
	 */
	public final String getGloss() {
		return mGloss;
	}
	
	/**
	 * This Synset's part of speech
	 * @return the part of speech
	 */
	public PartOfSpeech getPartOfSpeech() {
		return mPartOfSpeech;
	}
	
	/**
	 * This Synset's lexname
	 * @return the lexname
	 */
	public final String getLexname() {
		return mLexname;
	}
	
	/**
	 * Set a new lexname for this Synset
	 * @param lexname a new lexname
	 */
	public void setLexname(String lexname) {
		mLexname = new String(lexname);
	}

	@Override
	public void parseData(Object jsonResponse) throws JSONException {
		// TODO Auto-generated method stub

	}

}
