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

package com.dubsar_dictionary.Dubsar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.dubsar_dictionary.Dubsar.model.Model;

public class SearchActivity extends DubsarActivity {

	public static final String WORD_IDS = "word_ids";
	public static final String WORD_TITLES = "word_titles";
	public static final String WORD_SUBTITLES = "word_subtitles";
	
	private ListView mListView = null;
	private TextView mTextView = null;
	
	private List<HashMap<String,Object> > mResults = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.search);
	    
	    Model.setContext(this);

	    mListView = (ListView) findViewById(R.id.search_word_list);
	    mTextView = (TextView) findViewById(R.id.search_banner);
	    
	    setBoldTypeface(mTextView);

	    // Get the intent, verify the action and get the query
	    Intent intent = getIntent();
    	Uri uri = intent.getData();
    	
    	Log.i(getString(R.string.app_name), "URI = " + uri);

	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {	    	
	    	String query;
	    	if (uri != null) {
	    		query = uri.getLastPathSegment();
	    	}
	    	else {
	    		query = intent.getStringExtra(SearchManager.QUERY);
	    	}
	    	
	    	if (savedInstanceState != null) {
	    		retrieveInstanceState(savedInstanceState);
	    	}
	    	
	    	if (mResults != null) {
	    		populateResults(query);
	    	}
	    	else {
	    		fetchResults(query);
	    	}
	    }
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		saveState(outState);
	}

    /**
     * Searches the dictionary and displays results for the given query.
     * @param query The search query
	 */
    private void fetchResults(String query) {
    	mTextView.setText(getString(R.string.loading));
    	
    	new SearchQuery(mTextView, mListView).execute(query);

        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	
            	// get the word basics associated with this row and bundle it up with
            	// the intent
            	CursorAdapter adapter = (CursorAdapter)parent.getAdapter();
            	Cursor cursor = adapter.getCursor();
            	cursor.moveToPosition(position);
            	
            	int columnIndex = 
            			cursor.getColumnIndex(DubsarContentProvider.WORD_NAME_AND_POS);
            	String nameAndPos = cursor.getString(columnIndex);
            	
            	Intent wordIntent = new Intent(getApplicationContext(), WordActivity.class);
            	wordIntent.putExtra(DubsarContentProvider.WORD_NAME_AND_POS, nameAndPos);

            	// URI for the word request
            	Uri data = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI,
                                                DubsarContentProvider.WORDS_URI_PATH + 
                                                "/" + id);
                wordIntent.setData(data);
                startActivity(wordIntent);
            }
        });
    }
    
    protected void saveResults(Cursor cursor) {
    	int idColumn = cursor.getColumnIndex(BaseColumns._ID);
    	int nameAndPosColumn = cursor.getColumnIndex(DubsarContentProvider.WORD_NAME_AND_POS);
    	int subtitleColumn = cursor.getColumnIndex(DubsarContentProvider.WORD_SUBTITLE);
    	
    	mResults = new ArrayList<HashMap<String, Object> >(cursor.getCount());
    	
    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
    		int id = cursor.getInt(idColumn);
    		String nameAndPos = cursor.getString(nameAndPosColumn);
    		String subtitle = cursor.getString(subtitleColumn);
    		
    		HashMap<String, Object> map = new HashMap<String, Object>();
    		
    		map.put(BaseColumns._ID, new Integer(id));
    		map.put(DubsarContentProvider.WORD_NAME_AND_POS, nameAndPos);
    		map.put(DubsarContentProvider.WORD_SUBTITLE, subtitle);
    		mResults.add(map);
    	}
    }
    
    protected void retrieveInstanceState(Bundle icicle) {
    	int[] ids = icicle.getIntArray(WORD_IDS);
    	String[] titles = icicle.getStringArray(WORD_TITLES);
    	String[] subtitles = icicle.getStringArray(WORD_SUBTITLES);

    	mResults = new ArrayList<HashMap<String, Object> >(ids.length);
    	
    	for (int j=0; j<ids.length; ++j) {
    		HashMap<String, Object> map = new HashMap<String, Object>();
    		map.put(BaseColumns._ID, new Integer(ids[j]));
    		map.put(DubsarContentProvider.WORD_NAME_AND_POS, titles[j]);
    		map.put(DubsarContentProvider.WORD_SUBTITLE, subtitles[j]);
    		mResults.add(map);
    	}
    }
    
    protected void saveState(Bundle outState) {
    	if (mResults == null) return;
    	
    	int[] ids = new int[mResults.size()];
    	String[] titles = new String[mResults.size()];
    	String[] subtitles = new String[mResults.size()];
    	
    	for (int j=0; j<mResults.size(); ++j) {
    		HashMap<String, Object> result = mResults.get(j);
    		ids[j] = (Integer)result.get(BaseColumns._ID);
    		titles[j] = (String)result.get(DubsarContentProvider.WORD_NAME_AND_POS);
    		subtitles[j] = (String)result.get(DubsarContentProvider.WORD_SUBTITLE);
    	}
    	
    	outState.putIntArray(WORD_IDS, ids);
    	outState.putStringArray(WORD_TITLES, titles);
    	outState.putStringArray(WORD_SUBTITLES, subtitles);
    }
    
    protected void populateResults(String query) {
    	mTextView.setText(getString(R.string.search_results, new Object[] {query}));

    	String[] from = new String[] { DubsarContentProvider.WORD_NAME_AND_POS, DubsarContentProvider.WORD_SUBTITLE };
    	int[] to = new int[] { R.id.word_name, R.id.word_subtitle };
    	SimpleAdapter adapter = new SimpleAdapter(this, mResults, R.layout.result, from, to);
    	mListView.setAdapter(adapter);
    }
    
    
    /**
     * 
     * Inner class to do asynchronous retrieval from the provider.
     *
     */
    class SearchQuery extends AsyncTask<String, Void, Cursor> {
    	
    	private String mQuery = null;
    	
    	private final WeakReference<TextView> mTextViewReference;
    	private final WeakReference<ListView> mListViewReference;
    	
    	/**
    	 * Constructor. The arguments passed in are held as WeakReferences.
    	 * @param textView a text view for error messages
    	 * @param listView a list view to populate with results
    	 */
    	public SearchQuery(TextView textView, ListView listView) {
    		mTextViewReference = new WeakReference<TextView>(textView);
    		mListViewReference = new WeakReference<ListView>(listView);
    	}

		@Override
		protected Cursor doInBackground(String... params) {

	    	Uri.Builder builder = new Uri.Builder();
	    	builder.scheme("content");
	    	builder.authority(DubsarContentProvider.AUTHORITY);
	    	builder.path(DubsarContentProvider.SEARCH_URI_PATH);
	    	
	    	mQuery = new String(params[0]);
	    	
	        return managedQuery(builder.build(), null, null, params, null);
		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			
			if (isCancelled()) {
				return;
			}
			
			saveResults(result);
			
			TextView textView = mTextViewReference == null ? null : mTextViewReference.get();
			ListView listView = mListViewReference == null ? null : mListViewReference.get();
			
			if (textView == null || listView == null) return;

	        if (result == null) {
	        	// DEBT: externalize
	        	textView.setText("ERROR!");
	        } 
	        else if (result.getCount() == 0) {
	            textView.setText(getString(R.string.no_results, new Object[] {mQuery}));
	        } 
	        else {
	        	textView.setText(getString(R.string.search_results, new Object[] {mQuery}));
	        	
	            // Specify the columns we want to display in the result
	            String[] from = new String[] { DubsarContentProvider.WORD_NAME_AND_POS, 
	            		DubsarContentProvider.WORD_SUBTITLE };

	            // Specify the corresponding layout elements where we want the columns to go
	            int[] to = new int[] { R.id.word_name, R.id.word_subtitle };

	            // Create a simple cursor adapter for the definitions and apply them to the ListView
	            SimpleCursorAdapter words = 
	            		new SimpleCursorAdapter(listView.getContext(),
	                                          R.layout.result, result, from, to);
	                                
	            listView.setAdapter(words);

	        }
		}
    	
    }
}
