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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

/**
 * 
 * Class to represent word senses
 *
 */
public class Sense extends Model {
	
	private int mId=0;
	private String mName=null;
	private PartOfSpeech mPartOfSpeech=PartOfSpeech.Unknown;
	private String mGloss=null;
	private List<Sense> mSynonyms=null;
	private String mLexname=null;
	private int mFreqCnt=0;
	private String mMarker=null;
	private WeakReference<Word> mWordReference=null;
	
	/**
	 * Constructor for sense synonyms in word parsing
	 * @param id a sense ID
	 * @param name the name of the associated word
	 * @param partOfSpeech part of speech
	 */
	public Sense(int id, String name, PartOfSpeech partOfSpeech) {
		mId = id;
		mName = new String(name);
		mPartOfSpeech = partOfSpeech;
	}
	
	/**
	 * Constructor for senses in word parsing
	 * @param id a sense ID
	 * @param gloss the associated synset's gloss
	 * @param synonyms a list of Sense objects belonging to the same synset
	 * @param word the associated word
	 */
	public Sense(int id, String gloss, List<Sense>synonyms, Word word) {
		mId = id;
		mGloss = new String(gloss);
		mSynonyms = new ArrayList<Sense>(synonyms);
		mWordReference = new WeakReference<Word>(word);
	}
	
	/**
	 * Sense ID
	 * @return the sense ID (from the database)
	 */
	public int getId() {
		return mId;
	}
	
	/**
	 * Sense name
	 * @return the name of the associated word
	 */
	public final String getName() {
		return mName;
	}
	
	/**
	 * Sense part of speech
	 * @return this sense's part of speech
	 */
	public PartOfSpeech getPartOfSpeech() {
		return mPartOfSpeech;
	}
	
	/**
	 * part of speech abbreviation
	 * @return the abbreviated part of speech
	 */
	public final String getPos() {
		return posFromPartOfSpeech(getPartOfSpeech());
	}
	
	/**
	 * Name and abbreviated part of speech, e.g. "word (n.)"
	 * @return the name and part of speech
	 */
	public final String getNameAndPos() {
		return getName() + " (" + getPos() + ".)";
	}

	/**
	 * Sense gloss
	 * @return the gloss associated with the synset
	 */
	public final String getGloss() {
		return mGloss;
	}
	
	/**
	 * Synonyms
	 * @return this sense's synonyms
	 */
	public final List<Sense> getSynonyms() {
		return mSynonyms;
	}
	
	/**
	 * A list of synonyms as a string
	 * @return a comma-separated list (never null)
	 */
	public final String getSynonymsAsString() {
		if (mSynonyms == null) return "";
		
		String result = new String();
		for (int j=0; j<mSynonyms.size(); ++j) {
			Sense synonym = mSynonyms.get(j);
			result += synonym.getName();
			if (j < mSynonyms.size()-1) {
				result += ", ";
			}
		}
		
		return result;
	}
	
	/**
	 * Lexname
	 * @return the lexname associated with this sense
	 */
	public final String getLexname() {
		return mLexname;
	}
	
	/**
	 * Set the lexname
	 * @param lexname new lexname for this sense
	 */
	public void setLexname(String lexname) {
		mLexname = new String(lexname);
	}
	
	/**
	 * frequency count accessor
	 * @return this sense's frequency count
	 */
	public int getFreqCnt() {
		return mFreqCnt;
	}
	
	/**
	 * frequency count mutator
	 * @param freqCnt new frequency count
	 */
	public void setFreqCnt(int freqCnt) {
		mFreqCnt = freqCnt;
	}
	
	/**
	 * adjective marker associated with this sense (can be null)
	 * @return the adjective marker
	 */
	public final String getMarker() {
		return mMarker;
	}
	
	/**
	 * adjective marker mutator
	 * @param marker new adjectiver marker (can be null)
	 */
	public void setMarker(String marker) {
		if (marker == null) {
			mMarker = null;
			return;
		}
		
		mMarker = new String(marker);
	}
	
	public final String getSubtitle() {
		String subtitle = new String();
		
		if (getFreqCnt() > 0) {
			subtitle = "freq. cnt.: " + getFreqCnt() + " ";
		}
		
		subtitle += "<" + getLexname() + ">";
		
		if (getMarker() != null) {
			subtitle += "(" + getMarker() + ")";
		}
		
		return subtitle;
	}
	
	/**
	 * The word
	 * @return the word associated with this sense
	 */
	public final Word getWord() {
		return mWordReference != null ? mWordReference.get() : null;
	}
	
	@Override
	public void parseData(Object jsonResponse) throws JSONException {
		// TODO Auto-generated method stub

	}

}
