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

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.dubsar_dictionary.Dubsar.model.Model;

public class SearchActivity extends DubsarActivity {

	public static final String WORD_IDS = "word_ids";
	public static final String WORD_TITLES = "word_titles";
	public static final String WORD_SUBTITLES = "word_subtitles";
	
	private ListView mListView = null;
	private TextView mTextView = null;
	
	private Cursor mResults = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.search);
	    
	    Model.setContext(this);

	    mListView = (ListView) findViewById(R.id.search_word_list);
	    mTextView = (TextView) findViewById(R.id.search_banner);
	    
	    Drawable line = getResources().getDrawable(R.drawable.black_horizontal_line);
	    mListView.setDivider(line);
	    setBoldTypeface(mTextView);

	    // Get the intent, verify the action and get the query
	    Intent intent = getIntent();
    	Uri uri = intent.getData();

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
	    	
	    	setupListener();
	    }
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		saveState(outState);
	}
	
	protected void setupListener() {

        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            	Intent wordIntent = new Intent(getApplicationContext(), WordActivity.class);
            	String nameAndPos = null;
            	if (parent.getAdapter() instanceof CursorAdapter) {
                	// get the word basics associated with this row and bundle it up with
                	// the intent
                	CursorAdapter adapter = (CursorAdapter)parent.getAdapter();
                	Cursor cursor = adapter.getCursor();
                	cursor.moveToPosition(position);
                	
                	int columnIndex = 
                			cursor.getColumnIndex(DubsarContentProvider.WORD_NAME_AND_POS);
                	nameAndPos = cursor.getString(columnIndex);
            	}
            	
            	if (nameAndPos != null) 
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

    /**
     * Searches the dictionary and displays results for the given query.
     * @param query The search query
	 */
    private void fetchResults(String query) {
    	mTextView.setText(getString(R.string.loading));
    	
    	new SearchQuery(mTextView, mListView).execute(query);
    }
    
    protected void saveResults(Cursor cursor) {
    	mResults = cursor;
    }
    
    protected void retrieveInstanceState(Bundle icicle) {
    	int[] ids = icicle.getIntArray(WORD_IDS);
    	String[] titles = icicle.getStringArray(WORD_TITLES);
    	String[] subtitles = icicle.getStringArray(WORD_SUBTITLES);
    	
    	String[] columns = { BaseColumns._ID,
    			DubsarContentProvider.WORD_NAME_AND_POS,
    			DubsarContentProvider.WORD_SUBTITLE
    	};

    	MatrixCursor cursor = new MatrixCursor(columns, ids.length);
    	MatrixCursor.RowBuilder builder;
    	
    	for (int j=0; j<ids.length; ++j) {
    		builder = cursor.newRow();
    		
    		builder.add(new Integer(ids[j]));
    		builder.add(titles[j]);
    		builder.add(subtitles[j]);
    	}
    	
    	mResults = cursor;
    }
    
    protected void saveState(Bundle outState) {
    	if (mResults == null) return;
    	
    	int[] ids = new int[mResults.getCount()];
    	String[] titles = new String[mResults.getCount()];
    	String[] subtitles = new String[mResults.getCount()];
    	
    	int idColumn = mResults.getColumnIndex(BaseColumns._ID);
    	int nameAndPosColumn = mResults.getColumnIndex(DubsarContentProvider.WORD_NAME_AND_POS);
    	int subtitleColumn = mResults.getColumnIndex(DubsarContentProvider.WORD_SUBTITLE);
    	
    	for (int j=0; j<mResults.getCount(); ++j) {
    		mResults.moveToPosition(j);
    		ids[j] = mResults.getInt(idColumn);
    		titles[j] = mResults.getString(nameAndPosColumn);
    		subtitles[j] = mResults.getString(subtitleColumn);
    	}
    	
    	outState.putIntArray(WORD_IDS, ids);
    	outState.putStringArray(WORD_TITLES, titles);
    	outState.putStringArray(WORD_SUBTITLES, subtitles);
    }
    
    protected void populateResults(String query) {
    	mTextView.setText(getString(R.string.search_results, new Object[] {query}));

    	String[] from = new String[] { DubsarContentProvider.WORD_NAME_AND_POS, DubsarContentProvider.WORD_SUBTITLE };
    	int[] to = new int[] { R.id.word_name, R.id.word_subtitle };
    	CursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.result, mResults, from, to);
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
				saveResults(result);
				
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
