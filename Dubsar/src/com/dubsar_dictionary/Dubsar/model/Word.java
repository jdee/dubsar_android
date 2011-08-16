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

/**
 * 
 * Class to represent a single Word entry
 *
 */
public class Word extends Model {
	
	private int mId=0;
	private String mName=null;
	private int mFreqCnt=0;
	private String mInflections=null;
	private List<Sense> mSenses=null;

	public Word(int _id) {
		mId = _id;
		setupUrl();
	}
	
	/**
	 * Create a word using numeric ID, name and pos abbreviation.
	 * @param _id numeric (database) ID
	 * @param name word as a string
	 * @param pos abbreviated part of speech ("n", "adv", etc.)
	 */
	public Word(int _id, String name, String pos) {
		super(pos);
		mId = _id;
		mName = new String(name);
		setupUrl();
	}
	
	/**
	 * Create a word using numeric ID, name and enumerated part of speech
	 * @param _id numeric (database) ID
	 * @param name word as a string
	 * @param partOfSpeech enumerated part of speech
	 */
	public Word(int _id, String name, PartOfSpeech partOfSpeech) {
		super(partOfSpeech);
		mId = _id;
		mName = new String(name);
		setupUrl();
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
	 * Name and abbreviated part of speech, e.g. "word (n.)"
	 * @return the name and part of speech
	 */
	public final String getNameAndPos() {
		return getName() + " (" + getPos() + ".)";
	}
	
	/**
	 * Return a subtitle for the word, e.g.: freq. cnt: 10; also words
	 * @return the subtitle
	 */
	public final String getSubtitle() {
		String subtitle = new String();
		boolean hasInflections = getInflections() != null && 
				getInflections().length() > 0;
				
		if (getFreqCnt() > 0) {
			subtitle += "freq. cnt.: " + getFreqCnt();
			if (hasInflections) {
				subtitle += "; ";
			}
		}
		if (hasInflections) {
			subtitle += "also " + getInflections();
		}
		return subtitle;
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
	 * The list of senses associated with this word. Not meaningful
	 * if hasError().
	 * @return the list of senses (empty if none)
	 */
	public final List<Sense> getSenses() {
		return mSenses;
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
		JSONArray response = (JSONArray)jsonResponse;
		
		mId = response.getInt(0);
		mName = new String(response.getString(1));
		setPos(response.getString(2));
		
		setInflections(response.getString(3));
		setFreqCnt(response.getInt(5));
		
		JSONArray list = response.getJSONArray(4);
		mSenses = new ArrayList<Sense>(list.length());
		for (int j=0; j<list.length(); ++j) {
			JSONArray entry = list.getJSONArray(j);
			
			int senseId;
			String senseName;
			Sense sense;
			
			JSONArray _synonyms = entry.getJSONArray(1);
			ArrayList<Sense> synonyms = new ArrayList<Sense>(_synonyms.length());
			for (int k=0; k<_synonyms.length(); ++k) {
				JSONArray _synonym = _synonyms.getJSONArray(k);
				senseId = _synonym.getInt(0);
				senseName = _synonym.getString(1);
				
				sense = new Sense(senseId, senseName, getPartOfSpeech());
				synonyms.add(sense);
			}
			
			senseId = entry.getInt(0);
			String gloss = entry.getString(2);
			sense = new Sense(senseId, gloss, synonyms, this);
			
			sense.setLexname(entry.getString(3));
			if (!entry.isNull(4)) {
				sense.setMarker(entry.getString(4));
			}
			sense.setFreqCnt(entry.getInt(5));
			
			mSenses.add(sense);
		}
	}

	protected void setupUrl() {
		mPath = "/words/" + getId();
	}
}
