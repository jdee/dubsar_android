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
import java.util.Formatter;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

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
	private List<String> mVerbFrames=null;
	private List<String> mSamples=null;

	private boolean mIsWeakWordReference=false;
	private boolean mIsWeakSynsetReference=false;
	private WeakReference<Word> mWordReference=null;
	private WeakReference<Synset> mSynsetReference=null;
	private Word mWord=null;
	private Synset mSynset=null;
	
	/**
	 * Constructor for content provider
	 * @param id a Sense ID
	 */
	public Sense(int id) {
		mId = id;
		setupUrl();
	}
	
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
		setupUrl();
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
		
		mIsWeakWordReference = true;
		mWordReference = new WeakReference<Word>(word);
		
		mName = new String(getWord().getName());
		
		mPartOfSpeech = getWord().getPartOfSpeech();
		setupUrl();
	}
	
	/**
	 * Constructor for some senses in sense and synset parsing
	 * @param id a sense ID
	 * @param name the associated word's name
	 * @param synset the associated synset
	 */
	public Sense(int id, String name, Synset synset) {
		mId = id;
		mName = new String(name);
		
		mIsWeakSynsetReference = true;
		mSynsetReference = new WeakReference<Synset>(synset);
		
		mPartOfSpeech = getSynset().getPartOfSpeech();
		setupUrl();
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
		
		subtitle += "<" + getLexname() + "> ";
		
		if (getMarker() != null) {
			subtitle += "(" + getMarker() + ")";
		}
		
		return subtitle.trim();
	}
	
	public final String getAbbreviatedSubtitle() {
		String subtitle = new String();
		
		if (getFreqCnt() > 0) {
			subtitle = "freq. cnt.: " + getFreqCnt() + " ";
		}
		
		if (getMarker() != null) {
			subtitle += "(" + getMarker() + ")";
		}
		
		return subtitle.trim();		
	}
	
	/**
	 * The word
	 * @return the word associated with this sense
	 */
	public final Word getWord() {
		if (mIsWeakWordReference) {
			return mWordReference != null ? mWordReference.get() : null;
		}
		return mWord;
	}
	
	/**
	 * The synset
	 * @return the synset associated with this sense
	 */
	public final Synset getSynset() {
		if (mIsWeakSynsetReference) {
			return mSynsetReference != null ? mSynsetReference.get() : null;
		}
		return mSynset;
	}
	
	/**
	 * Generic verb frames
	 * @return list of verb frames
	 */
	public final List<String> getVerbFrames() {
		return mVerbFrames;
	}
	
	/**
	 * Specify new verb frames
	 * @param verbFrames new list of verb frames for this sense
	 */
	public void setVerbFrames(List<String> verbFrames) {
		mVerbFrames = new ArrayList<String>(verbFrames);
	}
	
	/**
	 * Sample sentences
	 * @return list of samples
	 */
	public final List<String> getSamples() {
		return mSamples;
	}
	
	/**
	 * Specify new samples
	 * @param samples new list of sample sentences for this sense
	 */
	public void setSamples(List<String> samples) {
		mSamples = new ArrayList<String>(samples);
	}
	
	@Override
	public void parseData(Object jsonResponse) throws JSONException {
		JSONArray response = (JSONArray)jsonResponse;
		
		JSONArray _word = response.getJSONArray(1);
		JSONArray _synset = response.getJSONArray(2);
		
		int wordId      = _word.getInt(0);
		String wordName = _word.getString(1);
		String wordPos  = _word.getString(2);
		
		int synsetId       = _synset.getInt(0);
		
		mGloss = new String(_synset.getString(1));		
		mPartOfSpeech = partOfSpeechFromPos(wordPos);
		
		mWord = new Word(wordId, wordName, mPartOfSpeech);
		mName = new String(wordName);
		mSynset = new Synset(synsetId, mGloss, mPartOfSpeech);
		
		mIsWeakWordReference = mIsWeakSynsetReference = false;
		mWordReference = null;
		mSynsetReference = null;
		
		setLexname(response.getString(3));
		getSynset().setLexname(getLexname());

		if (!response.isNull(4)) {
			setMarker(response.getString(4));
		}
		
		setFreqCnt(response.getInt(5));
		
		JSONArray _synonyms = response.getJSONArray(6);
		mSynonyms = new ArrayList<Sense>(_synonyms.length());
		
		int j;
		int senseId;
		String senseName;
		for (j=0; j<_synonyms.length(); ++j) {
			JSONArray _synonym = _synonyms.getJSONArray(j);
			
			senseId   = _synonym.getInt(0);
			senseName = _synonym.getString(1);
			String marker = null;
			if (!_synonym.isNull(2)) {
				marker = _synonym.getString(2);
			}
			int freqCnt = _synonym.getInt(3);
			
			Sense synonym = new Sense(senseId, senseName, getSynset());
			synonym.setMarker(marker);
			synonym.setFreqCnt(freqCnt);
			
			mSynonyms.add(synonym);
		}
		
		JSONArray _verbFrames = response.getJSONArray(7);
		mVerbFrames = new ArrayList<String>(_verbFrames.length());
		
		for (j=0; j<_verbFrames.length(); ++j) {
			String frame = _verbFrames.getString(j);
			
			// Replace %s in verb frames with the name of the word
			// TODO: Make that a bold span.
			StringBuffer buffer = new StringBuffer();
			Formatter formatter = new Formatter(buffer);
			formatter.format(frame, new Object[]{getName()});
			
			mVerbFrames.add(buffer.toString());
		}
		
		JSONArray _samples = response.getJSONArray(8);
		mSamples = new ArrayList<String>(_samples.length());
		
		for (j=0; j<_samples.length(); ++j) {
			mSamples.add(_samples.getString(j));
		}
		
		parsePointers(response.getJSONArray(9));
	}
	
	private void setupUrl() {
		mPath = "/senses/" + mId;
	}
}
