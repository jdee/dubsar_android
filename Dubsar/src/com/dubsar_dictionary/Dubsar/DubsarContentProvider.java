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

/**
 * Top-level Dubsar application package
 */
package com.dubsar_dictionary.Dubsar;

import java.util.List;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.dubsar_dictionary.Dubsar.model.Autocompleter;
import com.dubsar_dictionary.Dubsar.model.DailyWord;
import com.dubsar_dictionary.Dubsar.model.Model;
import com.dubsar_dictionary.Dubsar.model.Search;
import com.dubsar_dictionary.Dubsar.model.Sense;
import com.dubsar_dictionary.Dubsar.model.Synset;
import com.dubsar_dictionary.Dubsar.model.Word;

/**
 * 
 * Dubsar content provider
 *
 */
public class DubsarContentProvider extends ContentProvider {
    public static final String AUTHORITY = "com.dubsar_dictionary.Dubsar.DubsarContentProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String WOTD_URI_PATH = "wotd";
    public static final String SEARCH_URI_PATH = "search";
    public static final String WORDS_URI_PATH = "words";
    public static final String SENSES_URI_PATH = "senses";
    public static final String SYNSETS_URI_PATH = "synsets";
 
    // MIME types
    public static final String SEARCH_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/vnd.dubsar_dictionary.Dubsar";
    public static final String WORD_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.dubsar_dictionary.Dubsar.word";
    public static final String SENSE_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
    		"/vnd.dubsar_dictionary.Dubsar.sense";
    public static final String SYNSET_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + 
    		"/vnd.dubsar_dictionary.Dubsar.synset";
    
    // word fields
    public static final String WORD_NAME = "word_name";
    public static final String WORD_POS = "word_pos";
    public static final String WORD_NAME_AND_POS = "word_name_and_pos";
    public static final String WORD_FREQ_CNT = "word_freq_cnt";
    public static final String WORD_INFLECTIONS = "word_inflections";
    public static final String WORD_SUBTITLE = "word_subtitle";
    
    // sense fields
    public static final String SENSE_WORD_ID = "sense_word_id";
    public static final String SENSE_SYNSET_ID = "sense_synset_id";
    public static final String SENSE_NAME = "sense_name";
    public static final String SENSE_POS = "sense_pos";
    public static final String SENSE_NAME_AND_POS = "sense_name_and_pos";
    public static final String SENSE_FREQ_CNT = "sense_freq_cnt";
    public static final String SENSE_MARKER = "sense_marker";
    public static final String SENSE_LEXNAME = "sense_lexname";
    public static final String SENSE_GLOSS = "sense_gloss";
    public static final String SENSE_SYNONYMS_AS_STRING = "sense_synonyms_as_string";
    public static final String SENSE_SUBTITLE = "sense_subtitle";
    public static final String SENSE_SYNONYM_COUNT = "sense_synonym_count";
    public static final String SENSE_VERB_FRAME_COUNT = "sense_verb_frame_count";
    public static final String SENSE_SAMPLE_COUNT = "sense_sample_count";
    public static final String SENSE_SYNONYM = "sense_synonym";
    public static final String SENSE_SYNONYM_MARKER = "sense_synonym_marker";
    public static final String SENSE_VERB_FRAME = "sense_verb_frame";
    public static final String SENSE_SAMPLE = "sense_sample";
    
    // synset fields
    public static final String SYNSET_POS = "synset_pos";
    public static final String SYNSET_FREQ_CNT = "synset_freq_cnt";
    public static final String SYNSET_LEXNAME = "synset_lexname";
    public static final String SYNSET_SUBTITLE = "synset_subtitle";
    public static final String SYNSET_GLOSS = "synset_gloss";
    public static final String SYNSET_SAMPLE_COUNT = "synset_sample_count";
    public static final String SYNSET_SAMPLE = "synset_sample";
    public static final String SYNSET_SENSE_COUNT = "synset_sense_count";
    
    // query types
    public static final int SEARCH_WORDS = 0;
    public static final int GET_WORD = 1;
    public static final int SEARCH_SUGGEST = 2;
    public static final int GET_WOTD = 3;
    public static final int GET_SENSE = 4;
    public static final int GET_SYNSET = 5;
    private static final UriMatcher sURIMatcher = buildUriMatcher();

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
        
        matcher.addURI(AUTHORITY, WOTD_URI_PATH, GET_WOTD);
        matcher.addURI(AUTHORITY, SEARCH_URI_PATH, SEARCH_WORDS);
        matcher.addURI(AUTHORITY, WORDS_URI_PATH + "/*", GET_WORD);
        matcher.addURI(AUTHORITY, SENSES_URI_PATH + "/*", GET_SENSE);
        matcher.addURI(AUTHORITY, SYNSETS_URI_PATH + "/*", GET_SYNSET);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
        return matcher;
    }

    private String mSearchTerm=null;
    
    /**
     * Get the term associated with the last search (or autocompletion).
     * Useful for testing.
     * @return the search term
     */
    public final String getSearchTerm() {
    	return mSearchTerm;
    }

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// unsupported (return error?)
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		/*
		 * Abbreviated from SearchableDictionary
		 */
        switch (sURIMatcher.match(uri)) {
        case SEARCH_SUGGEST:
            return SearchManager.SUGGEST_MIME_TYPE;
        case GET_WORD:
        	return WORD_MIME_TYPE;
        case SEARCH_WORDS:
        case GET_WOTD:
            return SEARCH_MIME_TYPE;
        case GET_SENSE:
        	return SENSE_MIME_TYPE;
        case GET_SYNSET:
        	return SYNSET_MIME_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URL " + uri);
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// unsupported (return error?)
		return null;
	}

	@Override
	public boolean onCreate() {
		Model.setContext(this.getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		/*
		 * Abbreviated from SearchableDictionary
		 */
        // Use the UriMatcher to see what kind of query we have and format the db query accordingly
        switch (sURIMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                if (selectionArgs == null) {
                    throw new IllegalArgumentException(
                        "selectionArgs must be provided for the Uri: " + uri);
                }
                return getSuggestions(selectionArgs[0]);
            case GET_WORD:
            	return getWord(uri);
            case SEARCH_WORDS:
                if (selectionArgs == null) {
                    throw new IllegalArgumentException(
                        "selectionArgs must be provided for the Uri: " + uri);
                }
                return search(selectionArgs[0]);
            case GET_WOTD:
            	return getWotd();
            case GET_SENSE:
            	return getSense(uri);
            case GET_SYNSET:
            	return getSynset(uri);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// unsupported (return error?)
		return 0;
	}
	
	/**
	 * Retrieve a list of suggestions for the autocompleter. Returns
	 * null on error or an empty cursor (with the correct columns) in
	 * the case of no matching results.
	 * 
	 * @param term the user-supplied search term (as typed so far)
	 * @return a Cursor specifying the results (null on error)
	 */
	protected Cursor getSuggestions(String term) {
		mSearchTerm = new String(term);
		
		String[] columns = new String[3];
		columns[0] = BaseColumns._ID;
		columns[1] = SearchManager.SUGGEST_COLUMN_TEXT_1;
		columns[2] = SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID;
		
		if (mSearchTerm.length() == 0 || mSearchTerm.equals(SearchManager.SUGGEST_URI_PATH_QUERY)) {
			/*
			 * If the last path component is search_suggest_query, that
			 * means the URI is
			 * content://com.dubsar_dictionary.Dubsar.DubsarContentProvider/search_suggest_query/
			 * with nothing at the end. I.e., the search box is empty.
			 * Return an empty result.
			 */
			return new MatrixCursor(columns, 0);
		}
				
		Autocompleter autocompleter = new Autocompleter(term);
		autocompleter.load();
		
		if (autocompleter.hasError()) {
			Log.e(getContext().getString(R.string.app_name), 
					getContext().getString(R.string.search_error, new Object[] {autocompleter.getErrorMessage()}));
			return null;
		}
		
		List<String> results = autocompleter.getResults();
		
		MatrixCursor cursor = new MatrixCursor(columns, results.size());
		for (int j=0; j<results.size(); ++j) {
			MatrixCursor.RowBuilder builder = cursor.newRow();
			builder.add(new Integer(j));
			builder.add(results.get(j));
			builder.add(results.get(j));
		}
		
		return cursor;
	}
	
	/**
	 * Retrieve a list of Words matching the search term
	 * @param term a user-specified string
	 * @return a Cursor containing a list of words; null on error; empty if no match
	 */
	protected Cursor search(String term) {
		mSearchTerm = new String(term);
		
		Search search = new Search(term);
		search.load();
		
		if (search.hasError()) {
			Log.e(getContext().getString(R.string.app_name), 
					getContext().getString(R.string.search_error, 
							new Object[] {search.getErrorMessage()}));
			return null;
		}
		
		List<Word> results = search.getResults();
		
		String[] columns = new String[7];
		columns[0] = BaseColumns._ID;
		columns[1] = WORD_NAME;
		columns[2] = WORD_POS;
		columns[3] = WORD_NAME_AND_POS;
		columns[4] = WORD_FREQ_CNT;
		columns[5] = WORD_INFLECTIONS;
		columns[6] = WORD_SUBTITLE;
		
		MatrixCursor cursor = new MatrixCursor(columns, results.size());
		for (int j=0; j<results.size(); ++j) {
			Word word = results.get(j);
			
			MatrixCursor.RowBuilder builder = cursor.newRow();
			builder.add(new Integer(word.getId()));
			builder.add(word.getName());
			builder.add(word.getPos());
			builder.add(word.getNameAndPos());
			builder.add(new Integer(word.getFreqCnt()));
			builder.add(word.getInflections());
			builder.add(word.getSubtitle());
		}
		
		return cursor;
	}
	
	/**
	 * Retrieve a specific word by URI
	 * @param uri word URI
	 * @return a cursor containing one row per word sense (null on error)
	 */
	protected Cursor getWord(Uri uri) {
		int wordId = Integer.parseInt(uri.getLastPathSegment());
		
		Word word = new Word(wordId);
		word.load();
		
		if (word.hasError()) {
			Log.e(getContext().getString(R.string.app_name), 
					getContext().getString(R.string.search_error, 
							new Object[] {word.getErrorMessage()}));
			return null;			
		}
		
		String[] columns = new String[11];
		columns[0] = BaseColumns._ID;
		columns[1] = SENSE_NAME;
		columns[2] = SENSE_POS;
		columns[3] = SENSE_NAME_AND_POS;
		columns[4] = SENSE_FREQ_CNT;
		columns[5] = SENSE_LEXNAME;
		columns[6] = SENSE_MARKER;
		columns[7] = SENSE_GLOSS;
		columns[8] = SENSE_SYNONYMS_AS_STRING;
		columns[9] = SENSE_SUBTITLE;
		columns[10] = WORD_SUBTITLE;
		
		List<Sense> senses = word.getSenses();
		MatrixCursor cursor = new MatrixCursor(columns, senses.size());
		MatrixCursor.RowBuilder builder;
		int j;
		
		for (j=0; j<senses.size(); ++j) {
			Sense sense = senses.get(j);
			builder = cursor.newRow();
			
			builder.add(new Integer(sense.getId()));
			builder.add(sense.getName());
			builder.add(sense.getPos());
			builder.add(sense.getNameAndPos());
			builder.add(new Integer(sense.getFreqCnt()));
			builder.add(sense.getLexname());
			if (sense.getMarker() != null) {
				builder.add(sense.getMarker());
			}
			else {
				builder.add("");
			}
			builder.add(sense.getGloss());
			builder.add(sense.getSynonymsAsString());
			builder.add(sense.getSubtitle());
			builder.add(word.getSubtitle());
		}
		
		return cursor;
	}
	
	/**
	 * Get the word of the day
	 * @return a cursor (null on error)
	 */
	protected Cursor getWotd() {
		DailyWord dailyWord = new DailyWord();
		dailyWord.load();
		if (dailyWord.hasError()) {
			Log.e(getContext().getString(R.string.app_name), 
					getContext().getString(R.string.search_error, 
							new Object[] {dailyWord.getErrorMessage()}));
			return null;			
		}
		
		Word word = dailyWord.getWord();
		
		String[] columns = new String[7];
		columns[0] = BaseColumns._ID;
		columns[1] = WORD_NAME;
		columns[2] = WORD_POS;
		columns[3] = WORD_NAME_AND_POS;
		columns[4] = WORD_FREQ_CNT;
		columns[5] = WORD_INFLECTIONS;
		columns[6] = WORD_SUBTITLE;
		
		MatrixCursor cursor = new MatrixCursor(columns, 1);
		MatrixCursor.RowBuilder builder = cursor.newRow();
		
		builder.add(new Integer(word.getId()));
		builder.add(word.getName());
		builder.add(word.getPos());
		builder.add(word.getNameAndPos());
		builder.add(new Integer(word.getFreqCnt()));
		builder.add(word.getInflections());
		builder.add(word.getSubtitle());
		
		return cursor;
	}
	
	/**
	 * Get the data associated with a word sense
	 * @param uri the URI associated with the sense
	 * @return a Cursor containing data (null on error)
	 */
	protected Cursor getSense(Uri uri) {
		Log.d(getContext().getString(R.string.app_name), "in getSense() with URI " + uri);
		int senseId = Integer.parseInt(uri.getLastPathSegment());
		
		Sense sense = new Sense(senseId);
		Log.i(getContext().getString(R.string.app_name), "requesting sense ID " + senseId);
		sense.load();
		
		if (sense.hasError()) {
			Log.e(getContext().getString(R.string.app_name), 
					getContext().getString(R.string.search_error, 
							new Object[] {sense.getErrorMessage()}));
			return null;					
		}
		
		String[] columns = new String[19];
		columns[0] = BaseColumns._ID;
		columns[1] = SENSE_WORD_ID;
		columns[2] = SENSE_SYNSET_ID;
		columns[3] = SENSE_NAME;
		columns[4] = SENSE_POS;
		columns[5] = SENSE_NAME_AND_POS;
		columns[6] = SENSE_FREQ_CNT;
		columns[7] = SENSE_LEXNAME;
		columns[8] = SENSE_MARKER;
		columns[9] = SENSE_GLOSS;
		columns[10] = SENSE_SYNONYMS_AS_STRING;
		columns[11] = SENSE_SUBTITLE;
		columns[12] = SENSE_SYNONYM_COUNT;
		columns[13] = SENSE_VERB_FRAME_COUNT;
		columns[14] = SENSE_SAMPLE_COUNT;
		columns[15] = SENSE_SYNONYM;
		columns[16] = SENSE_SYNONYM_MARKER;
		columns[17] = SENSE_VERB_FRAME;
		columns[18] = SENSE_SAMPLE;
		
		int synonymCount = sense.getSynonyms().size();
		int verbFrameCount = sense.getVerbFrames().size();
		int sampleCount = sense.getSamples().size();
		
		int totalCount = synonymCount + verbFrameCount + sampleCount;
		
		MatrixCursor cursor = new MatrixCursor(columns, totalCount > 0 ? totalCount : 1);
		MatrixCursor.RowBuilder builder;
		
		Log.d(getContext().getString(R.string.app_name), "found " +
				synonymCount + " synonyms, " +
				verbFrameCount + " verb frames and " + 
				sampleCount + " samples");
		
		if (totalCount == 0) {
			builder = cursor.newRow();
			buildSenseRowBase(sense.getId(), sense, builder);
			builder.add(null);
			builder.add(null);
			builder.add(null);
			builder.add(null);
			return cursor;
		}
		
		// synonyms
		int j;
		for (j=0; j<synonymCount; ++j) {
			builder = cursor.newRow();
			
			Sense synonym = sense.getSynonyms().get(j);
			
			buildSenseRowBase(synonym.getId(), sense, builder);
			builder.add(synonym.getName());
			builder.add(synonym.getMarker());
			builder.add(null);
			builder.add(null);
		}
		
		// verb frames
		for (j=0; j<verbFrameCount; ++j) {
			builder = cursor.newRow();
			buildSenseRowBase(j, sense, builder);
			builder.add(null);
			builder.add(null);
			builder.add(sense.getVerbFrames().get(j));
			builder.add(null);
		}
		
		// sample sentences
		for (j=0; j<sampleCount; ++j) {
			builder = cursor.newRow();
			buildSenseRowBase(j, sense, builder);
			builder.add(null);
			builder.add(null);
			builder.add(null);
			builder.add(sense.getSamples().get(j));
		}

		return cursor;
	}
	
	protected Cursor getSynset(Uri uri) {
		int synsetId = Integer.parseInt(uri.getLastPathSegment());
		
		Synset synset = new Synset(synsetId);
		synset.load();
		
		if (synset.hasError()) {
			Log.e(getContext().getString(R.string.app_name), 
					getContext().getString(R.string.search_error, 
							new Object[] {synset.getErrorMessage()}));
			return null;								
		}
		
		String[] columns = new String[12];
		columns[0] = BaseColumns._ID;
		columns[1] = SYNSET_POS;
		columns[2] = SYNSET_FREQ_CNT;
		columns[3] = SYNSET_LEXNAME;
		columns[4] = SYNSET_SUBTITLE;
		columns[5] = SYNSET_GLOSS;
		columns[6] = SYNSET_SAMPLE_COUNT;
		columns[7] = SYNSET_SENSE_COUNT;
		columns[8] = SYNSET_SAMPLE;
		columns[9] = SENSE_NAME;
		columns[10] = SENSE_FREQ_CNT;
		columns[11] = SENSE_MARKER;
		
		int sampleCount = synset.getSamples().size();
		int senseCount = synset.getSenses().size();
		
		int totalCount = sampleCount + senseCount;
		
		MatrixCursor cursor = new MatrixCursor(columns, totalCount > 0 ? totalCount : 1);
		MatrixCursor.RowBuilder builder;

		if (totalCount == 0) {
			builder = cursor.newRow();
			buildSynsetRowBase(synset.getId(), synset, builder);
			builder.add(null);
			builder.add(null);
			builder.add(null);
			builder.add(null);
			return cursor;
		}
		
		int j;
		
		// sample sentences
		for (j=0; j<sampleCount; ++j) {
			builder = cursor.newRow();
			buildSynsetRowBase(j, synset, builder);
			builder.add(synset.getSamples().get(j));
			builder.add(null);
			builder.add(null);
			builder.add(null);
		}
		
		for (j=0; j<senseCount; ++j) {
			builder = cursor.newRow();

			Sense sense = synset.getSenses().get(j);
			buildSynsetRowBase(sense.getId(), synset, builder);
			builder.add(null);
			builder.add(sense.getName());
			builder.add(new Integer(sense.getFreqCnt()));
			builder.add(sense.getMarker());
		}
		
		return cursor;
	}
	
	private static void buildSenseRowBase(int id, Sense sense, MatrixCursor.RowBuilder builder) {
		builder.add(new Integer(id));
		builder.add(new Integer(sense.getWord().getId()));
		builder.add(new Integer(sense.getSynset().getId()));
		builder.add(sense.getName());
		builder.add(sense.getPos());
		builder.add(sense.getNameAndPos());
		builder.add(new Integer(sense.getFreqCnt()));
		builder.add(sense.getLexname());
		builder.add(sense.getMarker());
		builder.add(sense.getGloss());
		builder.add(sense.getSynonymsAsString());
		builder.add(sense.getSubtitle());
		builder.add(new Integer(sense.getSynonyms().size()));
		builder.add(new Integer(sense.getVerbFrames().size()));
		builder.add(new Integer(sense.getSamples().size()));
	}
	
	private static void buildSynsetRowBase(int id, Synset synset, MatrixCursor.RowBuilder builder) {
		builder.add(new Integer(id));
		builder.add(synset.getPos());
		builder.add(new Integer(synset.getFreqCnt()));
		builder.add(synset.getLexname());
		builder.add(synset.getSubtitle());
		builder.add(synset.getGloss());
		builder.add(new Integer(synset.getSamples().size()));
		builder.add(new Integer(synset.getSenses().size()));
	}
}
