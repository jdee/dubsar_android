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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
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
	private int mFreqCnt=0;
	private List<String> mSamples=null;
	private List<Sense> mSenses=null;
	
	/**
	 * Construct a Synset for request by the provider
	 * @param id Synset ID
	 */
	public Synset(int id) {
		mId = id;
		setupUrl();
	}
	
	/**
	 * Co12nstruct a Synset in a Sense response
	 * @param id a Synset ID
	 * @param gloss the synset gloss
	 * @param partOfSpeech the synset part of speech
	 */
	public Synset(int id, String gloss, PartOfSpeech partOfSpeech) {
		super(partOfSpeech);
		mId = id;
		mGloss = new String(gloss);
		setupUrl();
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
	
	/**
	 * Get the frequency count
	 * @return the frequency count
	 */
	public int getFreqCnt() {
		return mFreqCnt;
	}
	
	public final String getSubtitle() {
		String subtitle = new String();
		
		if (getFreqCnt() > 0) {
			subtitle = "freq. cnt.: " + getFreqCnt() + " ";
		}
		
		subtitle += "<" + getLexname() + "> ";
		
		return subtitle.trim();
	}
	
	/**
	 * Get sample sentences
	 * @return a list of sample sentences (may be empty, never null)
	 */
	public final List<String> getSamples() {
		return mSamples;
	}
	/**
	 * This Synset's senses
	 * @return a list of Sense objects
	 */
	public final List<Sense> getSenses() {
		return mSenses;
	}

	@Override
	public void parseData(Object jsonResponse) throws JSONException {
		JSONArray response = (JSONArray)jsonResponse;
		
		setPos(response.getString(1));
		setLexname(response.getString(2));
		mGloss = response.getString(3);
		
		JSONArray samples = response.getJSONArray(4);
		mSamples = new ArrayList<String>(samples.length());
		int j;
		for (j=0; j<samples.length(); ++j) {
			mSamples.add(samples.getString(j));
		}
		
		JSONArray senses = response.getJSONArray(5);
		mSenses = new ArrayList<Sense>(senses.length());
		for (j=0; j<senses.length(); ++j) {
			JSONArray _sense = senses.getJSONArray(j);
			
			int senseId = _sense.getInt(0);
			String senseName = _sense.getString(1);
			
			Sense sense = new Sense(senseId, senseName, this);
			sense.setLexname(getLexname());
			if (!_sense.isNull(2)) {
				sense.setMarker(_sense.getString(2));
			}
			sense.setFreqCnt(_sense.getInt(3));
			
			mSenses.add(sense);
		}
		
		mFreqCnt = response.getInt(6);
		
		parsePointers(response.getJSONArray(7));
	}

	protected void setupUrl() {
		mPath = "/synsets/" + getId();
	}
}
