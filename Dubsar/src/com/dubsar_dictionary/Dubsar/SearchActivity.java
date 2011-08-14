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

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.dubsar_dictionary.Dubsar.model.Model;

public class SearchActivity extends Activity {
	ListView mListView = null;
	TextView mTextView = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.search);
	    
	    Model.setContext(this);

	    mListView = (ListView) findViewById(R.id.search_word_list);
	    mTextView = (TextView) findViewById(R.id.search_banner);

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
	    	Log.i(getString(R.string.app_name), "query = \"" + query + "\"");
	    	
	    	showResults(query);
	    }
	}

    /**
     * Searches the dictionary and displays results for the given query.
     * @param query The search query
	 */
    private void showResults(String query) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                onSearchRequested();
                return true;
            default:
                return false;
        }
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
	        	textView.setText(getString(R.string.search_results, new Object[] {mQuery}));
	        	
	            // Specify the columns we want to display in the result
	            String[] from = new String[] { DubsarContentProvider.WORD_NAME_AND_POS, 
	            		DubsarContentProvider.WORD_SUBTITLE };

	            // Specify the corresponding layout elements where we want the columns to go
	            int[] to = new int[] { R.id.word, R.id.word_subtitle };

	            // Create a simple cursor adapter for the definitions and apply them to the ListView
	            SimpleCursorAdapter words = 
	            		new SimpleCursorAdapter(listView.getContext(),
	                                          R.layout.result, result, from, to);
	                                
	            listView.setAdapter(words);

	        }
		}
    	
    }
}
