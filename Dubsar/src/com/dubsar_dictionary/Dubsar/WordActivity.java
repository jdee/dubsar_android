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

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.drawable.Drawable;
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
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.dubsar_dictionary.Dubsar.model.Model;

public class WordActivity extends DubsarActivity {
	
	public static final String SENSE_IDS = "sense_ids";
	public static final String SENSE_BANNERS = "sense_banners";
	public static final String SENSE_GLOSSES = "sense_glosses";
	public static final String SENSE_SYNONYM_STRINGS = "sense_synonym_strings";

	private String mSubtitle=null;
	private Cursor mSenses = null;
	
	private TextView mBanner=null;
	private TextView mInflections=null;
	private ListView mSenseList=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.word);
        
        Model.setContext(this);

        Intent intent = getIntent();
        Uri uri = intent.getData();
        Bundle extras = intent.getExtras();
        
        String nameAndPos = extras.getString(DubsarContentProvider.WORD_NAME_AND_POS);
        
        mBanner = (TextView)findViewById(R.id.word_banner);
        mBanner.setText(nameAndPos);

	    mInflections = (TextView)findViewById(R.id.word_inflections);
	    setBoldItalicTypeface(mInflections);
	    
	    mSenseList = (ListView)findViewById(R.id.word_sense_list);
	    Drawable line = getResources().getDrawable(R.drawable.black_horizontal_line);
	    mSenseList.setDivider(line);
	    
	    if (savedInstanceState != null) {
    		retrieveInstanceState(savedInstanceState);	    	
	    }
	    
	    if (mSubtitle != null && mSenses != null) {
	    	populateData(nameAndPos);
	    }
	    else {
	    	new WordLoader(mBanner, mInflections, mSenseList).execute(uri);
	    }
	    
	    setupListener(nameAndPos);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		saveState(outState);
	}
	
	protected void retrieveInstanceState(Bundle inState) {
		mSubtitle = inState.getString(DubsarContentProvider.WORD_SUBTITLE);
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
			
			Log.d(getString(R.string.app_name), "started with ID " + ids[j] + " at row " + j);
			builder.add(new Integer(ids[j]));
			builder.add(banners[j]);
			builder.add(glosses[j]);
			builder.add(synonyms[j]);
		}
		
		mSenses = cursor;
	}
	
	protected void populateData(String nameAndPos) {
		mBanner.setText(nameAndPos);
    	
    	if (mSubtitle.length() > 0) {
    		mInflections.setText(mSubtitle);
    	}
    	else {
    		hideInflections(mBanner, mInflections);
    	}
	
		String[] from = new String[] { DubsarContentProvider.SENSE_GLOSS, DubsarContentProvider.SENSE_SYNONYMS_AS_STRING, 
				DubsarContentProvider.SENSE_SUBTITLE };
		int[] to = new int[] { R.id.word_sense_gloss, R.id.word_sense_synonyms, R.id.word_sense_banner };
		CursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.word_sense, mSenses, from, to);
		mSenseList.setAdapter(adapter);
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
			Log.d(getString(R.string.app_name), "ID for row " + j + " saved as " + ids[j]);
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
	
	protected void setupListener(final String nameAndPos) {
        mSenseList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            		                	
            	Intent senseIntent = new Intent(getApplicationContext(), SenseActivity.class);
            	senseIntent.putExtra(DubsarContentProvider.SENSE_NAME_AND_POS, nameAndPos);

            	Log.d(getString(R.string.app_name), "selected row " + position + ", ID " + id);
            	Uri data = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI,
                                                DubsarContentProvider.SENSES_URI_PATH + 
                                                "/" + id);
                senseIntent.setData(data);
                startActivity(senseIntent);
    		
        	}
        });

	}

	class WordLoader extends AsyncTask<Uri, Void, Cursor> {
    	
    	private final WeakReference<TextView> mBannerReference;
    	private final WeakReference<TextView> mInflectionsReference;
    	private final WeakReference<ListView> mListViewReference;
    	
    	public WordLoader(TextView banner, TextView inflections, ListView listView) {
    		mBannerReference = new WeakReference<TextView>(banner);
    		mInflectionsReference = new WeakReference<TextView>(inflections);
    		mListViewReference = new WeakReference<ListView>(listView);
    	}

		@Override
		protected Cursor doInBackground(Uri... params) {
			return managedQuery(params[0], null, null, null, null);
		}

		@Override
		protected void onPostExecute(Cursor result) {

			super.onPostExecute(result);
			
			if (isCancelled()) return;
			
			TextView banner = mBannerReference != null ? mBannerReference.get() : null;
			TextView inflections = mInflectionsReference != null ? mInflectionsReference.get() : null;
			ListView listView = mListViewReference != null ? mListViewReference.get() : null;
			
			if (banner == null || inflections == null || listView == null) return;

	        if (result == null) {
	        	// DEBT: externalize
	            banner.setText("ERROR!");
	            hideInflections(banner, inflections);
	        } else {
				saveResults(result);
	        	
	        	if (mSubtitle.length() > 0) {
	        		inflections.setText(mSubtitle);
	        	}
	        	else {
	        		hideInflections(banner, inflections);
	        	}
	        	
	            String[] from = new String[] { DubsarContentProvider.SENSE_SUBTITLE, 
	            		DubsarContentProvider.SENSE_GLOSS,
	            		DubsarContentProvider.SENSE_SYNONYMS_AS_STRING };
	            int[] to = new int[] { R.id.word_sense_banner, R.id.word_sense_gloss, 
	            		R.id.word_sense_synonyms };
	            SimpleCursorAdapter words = new SimpleCursorAdapter(listView.getContext(),
	                                          R.layout.word_sense, result, from, to);
	                                
	            listView.setAdapter(words);
	        }
		}
	}
    
    protected static void hideInflections(TextView banner, TextView inflections) {
        banner.setBackgroundResource(R.drawable.rounded_orange_rectangle);
        inflections.setVisibility(View.GONE);        	
    }
}
