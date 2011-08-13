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
	    setContentView(R.layout.main);
	    
	    Model.setContext(this);

	    mListView = (ListView) findViewById(R.id.list);
	    mTextView = (TextView) findViewById(R.id.banner);

	    // Get the intent, verify the action and get the query
	    Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    	String query = intent.getStringExtra(SearchManager.QUERY);
	    	showResults(query);
	    }
	}

    /**
     * Searches the dictionary and displays results for the given query.
     * @param query The search query
	 */
    private void showResults(String query) {
    	new SearchQuery(mTextView, mListView).execute(query);
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
    
    class SearchQuery extends AsyncTask<String, Void, Cursor> {
    	
    	private String mQuery = null;
    	
    	private final WeakReference<TextView> mTextViewReference;
    	private final WeakReference<ListView> mListViewReference;
    	
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
				result = null;
				return;
			}
			
			TextView textView = mTextViewReference == null ? null : mTextViewReference.get();
			ListView listView = mListViewReference == null ? null : mListViewReference.get();
			
			if (textView == null || listView == null) return;

	        if (result == null) {
	            textView.setText(getString(R.string.no_results, new Object[] {mQuery}));
	        } else {
	        	
	        	Log.i(getString(R.string.app_name), 
	        			"found " + result.getCount() + " results for \"" + mQuery+ "\"");
	        	Log.i(getString(R.string.app_name),
	        			result.getColumnCount() + " columns");
	        	Log.i(getString(R.string.app_name),
	        			DubsarContentProvider.WORD_NAME_AND_POS + " is at column index " +
	        			result.getColumnIndexOrThrow(DubsarContentProvider.WORD_NAME_AND_POS));
	        	Log.i(getString(R.string.app_name),
	        			DubsarContentProvider.WORD_SUBTITLE + " is at column index " +
	        			result.getColumnIndexOrThrow(DubsarContentProvider.WORD_SUBTITLE));

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
