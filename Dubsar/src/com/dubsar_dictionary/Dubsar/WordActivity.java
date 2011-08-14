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

import com.dubsar_dictionary.Dubsar.model.Model;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class WordActivity extends DubsarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.word);
        
        Model.setContext(this);

        Intent intent = getIntent();
        Uri uri = intent.getData();
        Bundle extras = intent.getExtras();
        
        String nameAndPos = extras.getString(DubsarContentProvider.WORD_NAME_AND_POS);
        
        TextView banner = (TextView)findViewById(R.id.word_banner);
        banner.setText(nameAndPos);
	    /*
	     * Why can't I do this in XML?
	     */
	    banner.setBackgroundColor(Color.rgb(0xf5, 0x84, 0x00));
	    setBoldTypeface(banner);
	    
	    TextView inflections = (TextView)findViewById(R.id.word_inflections);
	    inflections.setBackgroundColor(Color.rgb(255, 255, 255));
	    setBoldItalicTypeface(inflections);
       
        new WordLoader(banner, inflections, (ListView)findViewById(R.id.word_sense_list)).execute(uri);
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
	            inflections.setText("ERROR!");
	        } else {
	        	result.moveToFirst();
	        	int subtitleIndex = result.getColumnIndex(DubsarContentProvider.WORD_SUBTITLE);
	        	String subtitle = result.getString(subtitleIndex);
	        	inflections.setText(subtitle);
	        	
	            String[] from = new String[] { DubsarContentProvider.SENSE_SUBTITLE, 
	            		DubsarContentProvider.SENSE_GLOSS,
	            		DubsarContentProvider.SENSE_SYNONYMS_AS_STRING };
	            int[] to = new int[] { R.id.sense_banner, R.id.sense_gloss, 
	            		R.id.sense_synonyms };
	            SimpleCursorAdapter words = new SimpleCursorAdapter(listView.getContext(),
	                                          R.layout.sense, result, from, to);
	                                
	            listView.setAdapter(words);
	        }
		}
	}
}
