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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class SearchActivity extends DubsarActivity {

	public static final String WORD_IDS = "word_ids";
	public static final String WORD_TITLES = "word_titles";
	public static final String WORD_SUBTITLES = "word_subtitles";
	
	private LinearLayout mPagination = null;
	private TextView mNavigationTitle = null;
	private ListView mListView = null;
	private TextView mTextView = null;
	private Spinner mSpinner = null;
	private Button mPageBack = null;
	private Button mPageForward = null;
	
	private volatile Cursor mResults = null;
	private volatile int mTotalPages = 0;
	private volatile int mCurrentPage = 1;
	private volatile String mQuery = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState, R.layout.search);

	    mPagination = (LinearLayout) findViewById(R.id.pagination);
	    mNavigationTitle = (TextView) findViewById(R.id.navigation_title);
	    mListView = (ListView) findViewById(R.id.search_word_list);
	    mTextView = (TextView) findViewById(R.id.search_banner);
	    mSpinner = (Spinner) findViewById(R.id.search_page_spinner);
	    mPageForward = (Button) findViewById(R.id.page_forward);
	    mPageBack = (Button) findViewById(R.id.page_back);
	    
	    setBoldTypeface(mTextView);

	    // Get the intent, verify the action and get the query
	    Intent intent = getIntent();
    	Uri uri = intent.getData();

	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {	    	
	    	if (uri != null) {
	    		mQuery = uri.getLastPathSegment();
	    	}
	    	else {
	    		Bundle extras = intent.getExtras();
	    		mQuery = extras.getString(SearchManager.QUERY);
	    		if (intent.hasExtra(DubsarContentProvider.SEARCH_CURRENT_PAGE))
	    			mCurrentPage = extras.getInt(DubsarContentProvider.SEARCH_CURRENT_PAGE);
	    	}
	    	
	    	Log.d(getString(R.string.app_name), "starting search activity with query \"" + mQuery + "\", page " + mCurrentPage);
	    	
	    	if (savedInstanceState != null) {
	    		retrieveInstanceState(savedInstanceState);
	    	}
	    	
	    	if (mResults != null) {
	    		populateResults(mQuery);
	    	}
	    	else {
	    		
	    		if (!checkNetwork()) return;
	    		fetchResults(mQuery);
	    	}
	    	
	    	setupListener();
	    }
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
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
	
	public void reportError(String error) {
		super.reportError(error);
		mTextView.setText(error);
	}

    /**
     * Searches the dictionary and displays results for the given query.
     * @param query The search query
	 */
    private void fetchResults(String query) {
    	mTextView.setText(getString(R.string.loading));
    	
    	new SearchQuery(this).execute(query, 
    			new Integer(mCurrentPage).toString());
    }
    
    protected void saveResults(Cursor cursor) {
    	int totalPagesColumn = cursor.getColumnIndex(DubsarContentProvider.SEARCH_TOTAL_PAGES);
    	int currentPageColumn = cursor.getColumnIndex(DubsarContentProvider.SEARCH_CURRENT_PAGE);
    	cursor.moveToFirst();
    	
    	mTotalPages = cursor.getInt(totalPagesColumn);
    	mCurrentPage = cursor.getInt(currentPageColumn);
    	mResults = cursor;
    }
    
    protected void retrieveInstanceState(Bundle icicle) {
    	int[] ids = icicle.getIntArray(WORD_IDS);
    	if (ids == null) return;
    	
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
    	
    	mTotalPages = icicle.getInt(DubsarContentProvider.SEARCH_TOTAL_PAGES);
    	mCurrentPage = icicle.getInt(DubsarContentProvider.SEARCH_CURRENT_PAGE);
    	mQuery = icicle.getString(SearchManager.QUERY);
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
    	outState.putInt(DubsarContentProvider.SEARCH_TOTAL_PAGES, mTotalPages);
    	outState.putInt(DubsarContentProvider.SEARCH_CURRENT_PAGE, mCurrentPage);
    	outState.putString(SearchManager.QUERY, mQuery);
    }
    
    protected void populateResults(String query) {
    	hideLoadingSpinner();
    	if (mResults.getCount() > 0) {
    		mTextView.setText(getString(R.string.search_results, new Object[] {query}));
    		mListView.setVisibility(View.VISIBLE);
    	}
        else {
            mTextView.setText(getString(R.string.no_results, new Object[] {mQuery}));
            return;
        } 
    	
    	String[] from = new String[] { DubsarContentProvider.WORD_NAME_AND_POS, DubsarContentProvider.WORD_SUBTITLE };
    	int[] to = new int[] { R.id.word_name, R.id.word_subtitle };
    	CursorAdapter adapter = new SimpleCursorAdapter(getApplicationContext(), 
    			R.layout.result, mResults, from, to);
    	mListView.setAdapter(adapter);
    	
    	if (mTotalPages > 1) {
    		setupPagination();
    	}
    }    
    
    protected void setupPagination() {
		String[] values = new String[mTotalPages];
		for (int j=0; j<mTotalPages; ++j) {
			values[j] = new Integer(j+1).toString();
		}
		
		ArrayAdapter<String> spinnerAdapter = 
				new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner,
						R.id.spinner, values);
		mSpinner.setAdapter(spinnerAdapter);
		mSpinner.setSelection(mCurrentPage-1);
		mPagination.setVisibility(View.VISIBLE);
		
		mPageBack.setEnabled(mCurrentPage > 1);
		mPageForward.setEnabled(mCurrentPage < mTotalPages);

		// DEBT: externalize
		mNavigationTitle.setText(getString(R.string.menu_search) + " p. " + mCurrentPage + 
				" of " + mTotalPages);
		
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position+1 != mCurrentPage)
					requestPage(position+1);
			}
			
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		
		mPageBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				requestPage(mCurrentPage - 1);
			}
		});
		
		mPageForward.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				requestPage(mCurrentPage + 1);
			}
		});
    }
    
    protected void requestPage(int page) {
    	Intent searchIntent = new Intent(getApplicationContext(), SearchActivity.class);
    	searchIntent.putExtra(SearchManager.QUERY, mQuery);
    	searchIntent.putExtra(DubsarContentProvider.SEARCH_CURRENT_PAGE, page);
    	
    	searchIntent.setAction(Intent.ACTION_SEARCH);
    	
    	startActivity(searchIntent);
    }
    
    /**
     * 
     * Inner class to do asynchronous retrieval from the provider.
     *
     */
    static class SearchQuery extends AsyncTask<String, Void, Cursor> {
    	
    	private String mQuery = null;
    	
    	private final WeakReference<SearchActivity> mActivityReference;
    	
    	/**
    	 * Constructor. The arguments passed in are held as WeakReferences.
    	 * @param textView a text view for error messages
    	 * @param listView a list view to populate with results
    	 */
    	public SearchQuery(SearchActivity activity) {
    		mActivityReference = new WeakReference<SearchActivity>(activity);
    	}
    	
    	protected SearchActivity getActivity() {
    		return mActivityReference != null ? mActivityReference.get() : null;
    	}

		@Override
		protected Cursor doInBackground(String... params) {

	    	Uri.Builder builder = new Uri.Builder();
	    	builder.scheme("content");
	    	builder.authority(DubsarContentProvider.AUTHORITY);
	    	builder.path(DubsarContentProvider.SEARCH_URI_PATH);
	    	
	    	mQuery = new String(params[0]);
	    	
	    	if (getActivity() == null) return null;
	    	
	        return getActivity().managedQuery(builder.build(), null, null, params, null);
		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			
			if (isCancelled()) {
				return;
			}

			if (getActivity() == null) return;

	        if (result == null) {
	        	getActivity().reportError(getActivity().getString(R.string.search_error));
	        } 
	        else {
				getActivity().saveResults(result);
				getActivity().populateResults(mQuery);
	        }
		}
    	
    }
}
