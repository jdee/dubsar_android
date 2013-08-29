/*
 Dubsar Dictionary Project
 Copyright (C) 2010-13 Jimmy Dee
 
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

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
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

public class WordActivity extends DubsarActivity {
	
	public static final String SENSE_IDS = "sense_ids";
	public static final String SENSE_BANNERS = "sense_banners";
	public static final String SENSE_GLOSSES = "sense_glosses";
	public static final String SENSE_SYNONYM_STRINGS = "sense_synonym_strings";
	
	private TextView mBanner=null;
	private TextView mInflections=null;
	private ListView mSenseList=null;
	
	private volatile String mNameAndPos=null;
	private volatile String mSubtitle=null;
	private volatile Cursor mSenses = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.word);

        Intent intent = getIntent();
        Uri uri = intent.getData();
        Bundle extras = intent.getExtras();
        
       	mNameAndPos = extras.getString(DubsarContentProvider.WORD_NAME_AND_POS);
        
        mBanner = (TextView)findViewById(R.id.word_banner);
        mBanner.setText(mNameAndPos);

	    mInflections = (TextView)findViewById(R.id.word_inflections);
	    setBoldItalicTypeface(mInflections);
	    
	    mSenseList = (ListView)findViewById(R.id.word_sense_list);
	    
	    if (savedInstanceState != null) {
    		retrieveInstanceState(savedInstanceState);	    	
	    }
	    
	    if (mSubtitle != null && mSenses != null) {
	    	populateData();
	    }
	    else {
			
			if (!checkNetwork()) return;
	    	new WordLoader(this).execute(uri);
	    }
	    
	    setupListener();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveState(outState);
	}
	
	protected void retrieveInstanceState(Bundle inState) {
		mSubtitle = inState.getString(DubsarContentProvider.WORD_SUBTITLE);
		if (mSubtitle == null) return;
		
		int[] ids = inState.getIntArray(SENSE_IDS);
		String[] banners = inState.getStringArray(SENSE_BANNERS);
		String[] glosses = inState.getStringArray(SENSE_GLOSSES);
		String[] synonyms = inState.getStringArray(SENSE_SYNONYM_STRINGS);
		
		String[] columns = { BaseColumns._ID, 
				DubsarContentProvider.SENSE_SUBTITLE,
				DubsarContentProvider.SENSE_GLOSS, 
				DubsarContentProvider.SENSE_SYNONYMS_AS_STRING };
		MatrixCursor cursor = new MatrixCursor(columns, ids.length);
		MatrixCursor.RowBuilder builder;
		
		for (int j=0; j<ids.length; ++j) {
			builder = cursor.newRow();
			builder.add(Integer.valueOf(ids[j]));
			builder.add(banners[j]);
			builder.add(glosses[j]);
			builder.add(synonyms[j]);
		}
		
		mSenses = cursor;
	}
	
	protected void populateData() {
		mBanner.setText(mNameAndPos);
		hideLoadingSpinner();
    	
    	if (mSubtitle.length() > 0) {
    		mInflections.setText(mSubtitle);
    	}
    	else {
    		hideInflections(mBanner, mInflections);
    	}
	
		String[] from = new String[] { DubsarContentProvider.SENSE_GLOSS, DubsarContentProvider.SENSE_SYNONYMS_AS_STRING, 
				DubsarContentProvider.SENSE_SUBTITLE };
		int[] to = new int[] { R.id.word_sense_gloss, R.id.word_sense_synonyms, R.id.word_sense_banner };
		@SuppressWarnings("deprecation")
		CursorAdapter adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.word_sense, mSenses, from, to);
		mSenseList.setAdapter(adapter);
		mSenseList.setVisibility(View.VISIBLE);
	}
	
	protected void saveState(Bundle outState) {
		if (mSenses == null) return;
		
		int[] ids = new int[mSenses.getCount()];
		String[] banners = new String[mSenses.getCount()];
		String[] glosses = new String[mSenses.getCount()];
		String[] synonyms = new String[mSenses.getCount()];
		
		int idColumn = mSenses.getColumnIndex(BaseColumns._ID);
		int subtitleColumn = mSenses.getColumnIndex(DubsarContentProvider.SENSE_SUBTITLE);
		int glossColumn = mSenses.getColumnIndex(DubsarContentProvider.SENSE_GLOSS);
		int synonymsColumn = mSenses.getColumnIndex(DubsarContentProvider.SENSE_SYNONYMS_AS_STRING);
		
		for (int j=0; j<mSenses.getCount(); ++j) {
			mSenses.moveToPosition(j);
			
			ids[j] = mSenses.getInt(idColumn);
			banners[j] = mSenses.getString(subtitleColumn);
			glosses[j] = mSenses.getString(glossColumn);
			synonyms[j] = mSenses.getString(synonymsColumn);
		}
		
		outState.putString(DubsarContentProvider.WORD_SUBTITLE, mSubtitle);
		outState.putIntArray(SENSE_IDS, ids);
		outState.putStringArray(SENSE_BANNERS, banners);
		outState.putStringArray(SENSE_GLOSSES, glosses);
		outState.putStringArray(SENSE_SYNONYM_STRINGS, synonyms);
	}
	
	protected void saveResults(Cursor cursor) {
		int subtitleColumn = cursor.getColumnIndex(DubsarContentProvider.WORD_SUBTITLE);
		cursor.moveToFirst();
		mSubtitle = cursor.getString(subtitleColumn);
		mSenses = cursor;
	}
	
	protected void setupListener() {
        mSenseList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            		                	
            	Intent senseIntent = new Intent(getApplicationContext(), SenseActivity.class);
            	senseIntent.putExtra(DubsarContentProvider.SENSE_NAME_AND_POS, mNameAndPos);

            	Uri data = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI,
                                                DubsarContentProvider.SENSES_URI_PATH + 
                                                "/" + id);
                senseIntent.setData(data);
                startActivity(senseIntent);
    		
        	}
        });

	}
	
	protected void reportError(String error) {
		super.reportError(error);
		
    	// DEBT: externalize
        mBanner.setText(error);
        hideInflections(mBanner, mInflections);
	}

	static class WordLoader extends AsyncTask<Uri, Void, Cursor> {
    	
    	private final WeakReference<WordActivity> mActivityReference;
    	
    	public WordLoader(WordActivity activity) {
    		mActivityReference = new WeakReference<WordActivity>(activity);
    	}
    	
    	public WordActivity getActivity() {
    		return mActivityReference != null ? mActivityReference.get() : null;
    	}

		@SuppressWarnings("deprecation")
		@Override
		protected Cursor doInBackground(Uri... params) {
			if (getActivity() == null) return null;
			return getActivity().managedQuery(params[0], null, null, null, null);
		}

		@Override
		protected void onPostExecute(Cursor result) {

			super.onPostExecute(result);
			
			if (isCancelled()) return;
			
			if (getActivity() == null) return;

	        if (result == null) {
	        	getActivity().reportError(getActivity().getString(R.string.search_error));
	        } else {
	        	getActivity().saveResults(result);
	        	getActivity().populateData();
	        }
		}
	}
    
    protected static void hideInflections(TextView banner, TextView inflections) {
        banner.setBackgroundResource(R.drawable.rounded_orange_rectangle);
        inflections.setVisibility(View.GONE);        	
    }
}
