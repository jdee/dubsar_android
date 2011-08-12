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

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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

    	Uri.Builder builder = new Uri.Builder();
    	builder.scheme("content");
    	builder.authority(DubsarContentProvider.AUTHORITY);
    	builder.path(SearchManager.SUGGEST_URI_PATH_QUERY + "/" + query);
    	
        Cursor cursor = managedQuery(builder.build(), null, null, null, null);

        if (cursor == null) {
            mTextView.setText(getString(R.string.no_results, new Object[] {query}));
        } else {

            // Specify the columns we want to display in the result
            String[] from = new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1 };

            // Specify the corresponding layout elements where we want the columns to go
            int[] to = new int[] { R.id.word };

            // Create a simple cursor adapter for the definitions and apply them to the ListView
            SimpleCursorAdapter words = new SimpleCursorAdapter(this,
                                          R.layout.result, cursor, from, to);
            mListView.setAdapter(words);

        }
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
}
