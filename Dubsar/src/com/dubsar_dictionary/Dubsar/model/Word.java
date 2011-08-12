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
 * Class to represent a single Word entry
 *
 */
public class Word extends Model {
	
	private int mId=0;
	private String mName=null;
	private PartOfSpeech mPartOfSpeech=PartOfSpeech.Unknown;
	private int mFreqCnt=0;
	private String mInflections=null;
	
	/**
	 * Create a word using numeric ID, name and pos abbreviation.
	 * @param _id numeric (database) ID
	 * @param name word as a string
	 * @param pos abbreviated part of speech ("n", "adv", etc.)
	 */
	public Word(int _id, String name, String pos) {
		mId = _id;
		mName = new String(name);
		mPartOfSpeech = partOfSpeechFromPos(pos);
	}
	
	/**
	 * Create a word using numeric ID, name and enumerated part of speech
	 * @param _id numeric (database) ID
	 * @param name word as a string
	 * @param partOfSpeech enumerated part of speech
	 */
	public Word(int _id, String name, PartOfSpeech partOfSpeech) {
		mId = _id;
		mName = new String(name);
		mPartOfSpeech = partOfSpeech;
	}
	
	/**
	 * Numeric ID from the database
	 * @return word ID
	 */
	public int getId() {
		return mId;
	}
	
	/**
	 * Word as a string
	 * @return word name
	 */
	public final String getName() {
		return mName;
	}
	
	/**
	 * Enumerated part of speech
	 * @return PartOfSpeech enumeration
	 */
	public PartOfSpeech getPartOfSpeech() {
		return mPartOfSpeech;
	}
	
	/**
	 * Part of speech abbreviation
	 * @return abbreviated form ("adv", "v", etc)
	 */
	public final String getPos() {
		return posFromPartOfSpeech(mPartOfSpeech);
	}
	
	/**
	 * Frequency count from a reference text
	 * @return frequency count
	 */
	public int getFreqCnt() {
		return mFreqCnt;
	}
	
	/**
	 * Other forms for this word (plural, past tense, etc.)
	 * @return comma-separated list
	 */
	public final String getInflections() {
		return mInflections;
	}
	
	/**
	 * Set the frequency count
	 * @param freqCnt new frequency count
	 */
	public void setFreqCnt(int freqCnt) {
		mFreqCnt = freqCnt;
	}
	
	/**
	 * Set this word's inflections
	 * @param inflections comma-separated list
	 */
	public void setInflections(String inflections) {
		if (inflections == null) {
			mInflections = null;
			return;
		}
		
		mInflections = new String(inflections);
	}

	@Override
	public void parseData(Object jsonResponse) throws JSONException {
		// TODO Auto-generated method stub

	}

}
