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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

/**
 * 
 * Sense activity
 *
 */
public class SenseActivity extends DubsarActivity {
	
	private TextView mTitle=null;
	private TextView mBanner=null;
	private TextView mGloss=null;
	private ExpandableListView mLists=null;
	private int mWordId=0;
	private int mSynsetId=0;
	private String mNameAndPos=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sense);
		
		mTitle = (TextView)findViewById(R.id.sense_title);
		mBanner = (TextView)findViewById(R.id.sense_banner);
		mGloss = (TextView)findViewById(R.id.sense_gloss);
		mLists = (ExpandableListView)findViewById(R.id.sense_lists);
		
		setupFonts();
		
		Intent intent = getIntent();
		Uri uri = intent.getData();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mNameAndPos = extras.getString(DubsarContentProvider.SENSE_NAME_AND_POS);
			if (mNameAndPos != null) mTitle.setText(mNameAndPos);
		}
		
		new SenseQuery(mTitle, mBanner, mGloss, mLists).execute(uri);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sense_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                onSearchRequested();
                return true;
            case R.id.word:
            	requestWord();
            	return true;
            case R.id.synset:
            	requestSynset();
            	return true;
            default:
                return false;
        }
    }
    
	protected void setupFonts() {
		setBoldItalicTypeface(mBanner);
	}
	
	protected Activity getActivity() {
		return this;
	}

	protected void requestWord() {    	
    	Intent wordIntent = new Intent(getApplicationContext(), WordActivity.class);
    	wordIntent.putExtra(DubsarContentProvider.WORD_NAME_AND_POS, mNameAndPos);

    	Uri data = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI,
                                        DubsarContentProvider.WORDS_URI_PATH + 
                                        "/" + mWordId);
        wordIntent.setData(data);
        startActivity(wordIntent);
	}
	
	protected void requestSynset() {
		Intent synsetIntent = new Intent(getApplicationContext(), SynsetActivity.class);
		Uri data = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI, 
				DubsarContentProvider.SYNSETS_URI_PATH + "/" + mSynsetId);
		
		synsetIntent.setData(data);
		startActivity(synsetIntent);
	} 
	
	protected void setWordId(int wordId) {
		mWordId = wordId;
	}
	
	protected void setSynsetId(int synsetId) {
		mSynsetId = synsetId;
	}
	
	class SenseQuery extends AsyncTask<Uri, Void, Cursor> {
		
		private final WeakReference<TextView> mTitleReference;
		private final WeakReference<TextView> mBannerReference;
		private final WeakReference<TextView> mGlossReference;
		private final WeakReference<ExpandableListView> mListsReference;
		
		public SenseQuery(TextView title, TextView banner, TextView gloss,
				ExpandableListView lists) {
			mTitleReference = new WeakReference<TextView>(title);
			mBannerReference = new WeakReference<TextView>(banner);
			mGlossReference = new WeakReference<TextView>(gloss);
			mListsReference = new WeakReference<ExpandableListView>(lists);
		}

		@Override
		protected Cursor doInBackground(Uri... params) {
			return managedQuery(params[0], null, null, null, null);
		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			
			if (isCancelled()) {
				return;
			}
			
			TextView title = mTitleReference != null ? mTitleReference.get() : null;
			TextView banner = mBannerReference != null ? mBannerReference.get() : null;
			TextView gloss = mGlossReference != null ? mGlossReference.get() : null;
			ExpandableListView lists = mListsReference != null ? mListsReference.get() : null;
			
			if (title == null ||
				banner == null ||
				gloss == null ||
				lists == null) return;
			
			if (result == null) {
				// DEBT: Externalize
				title.setText("ERROR!");
	            title.setBackgroundResource(R.drawable.rounded_orange_rectangle);

				banner.setVisibility(View.GONE);
				gloss.setVisibility(View.GONE);
				lists.setVisibility(View.GONE);
			}
			else {
				result.moveToFirst();
				
				// first set the scalar field values
				int nameAndPosColumn = result.getColumnIndex(DubsarContentProvider.SENSE_NAME_AND_POS);
				int subtitleColumn = result.getColumnIndex(DubsarContentProvider.SENSE_SUBTITLE);
				int glossColumn = result.getColumnIndex(DubsarContentProvider.SENSE_GLOSS);
				int wordIdColumn = result.getColumnIndex(DubsarContentProvider.SENSE_WORD_ID);
				int synsetIdColumn = result.getColumnIndex(DubsarContentProvider.SENSE_SYNSET_ID);
				
				mNameAndPos = result.getString(nameAndPosColumn);
				mWordId = result.getInt(wordIdColumn);
				mSynsetId = result.getInt(synsetIdColumn);
				
				title.setText(mNameAndPos);
				banner.setText(result.getString(subtitleColumn));
				gloss.setText(result.getString(glossColumn));
				
				// set up the expandable list view
				ExpandableListAdapter adapter = 
						new SenseExpandableListAdapter(getActivity(), result);
				lists.setAdapter(adapter);
			}
		}
		
	}

}
