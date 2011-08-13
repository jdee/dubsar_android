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
import com.dubsar_dictionary.Dubsar.model.Model;
import com.dubsar_dictionary.Dubsar.model.Search;
import com.dubsar_dictionary.Dubsar.model.Word;

/**
 * 
 * Dubsar content provider
 *
 */
public class DubsarContentProvider extends ContentProvider {
    public static String AUTHORITY = "com.dubsar_dictionary.Dubsar.DubsarContentProvider";
    public static String CONTENT_URI = "content://" + AUTHORITY;
 
    public static final String WORDS_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
            "/vnd.dubsar_dictionary.Dubsar";
    
    public static final String WORD_ID = "word_id";
    public static final String WORD_NAME = "word_name";
    public static final String WORD_POS = "word_pos";
    public static final String WORD_NAME_AND_POS = "word_name_and_pos";
    public static final String WORD_FREQ_CNT = "word_freq_cnt";
    public static final String WORD_INFLECTIONS = "word_inflections";
    public static final String WORD_SUBTITLE = "word_subtitle";
    
    public static final int SEARCH_WORDS = 0;
    public static final int SEARCH_SUGGEST = 2;
    private static final UriMatcher sURIMatcher = buildUriMatcher();

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
        
        matcher.addURI(AUTHORITY, "search", SEARCH_WORDS);
        // to get suggestions...
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
        case SEARCH_WORDS:
            return WORDS_MIME_TYPE;
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
            	return getSuggestions(uri.getLastPathSegment());
            case SEARCH_WORDS:
                if (selectionArgs == null) {
                    throw new IllegalArgumentException(
                        "selectionArgs must be provided for the Uri: " + uri);
                }
                return search(selectionArgs[0]);
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
		
		String[] columns = new String[2];
		columns[0] = BaseColumns._ID;
		columns[1] = SearchManager.SUGGEST_COLUMN_TEXT_1;
		
		if (mSearchTerm.equals(SearchManager.SUGGEST_URI_PATH_QUERY)) {
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
		}
		
		return cursor;
	}
	
	/**
	 * Return words matching the user-specified term.
	 * @param term the search term specified by the user
	 * @return a Cursor specifying the results (null on error)
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
		columns[0] = WORD_ID;
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

}
