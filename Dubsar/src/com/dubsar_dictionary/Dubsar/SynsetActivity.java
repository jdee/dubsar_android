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
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class SynsetActivity extends DubsarActivity {
	
	private TextView mBanner=null;
	private TextView mGloss=null;
	private ExpandableListView mLists=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.synset);
		
		mBanner = (TextView)findViewById(R.id.synset_banner);
		mGloss = (TextView)findViewById(R.id.synset_gloss);
		mLists = (ExpandableListView)findViewById(R.id.synset_lists);
		
		setupFonts();
		
		Intent intent = getIntent();
		Uri uri = intent.getData();

		new SynsetQuery(mBanner, mGloss, mLists).execute(uri);
	}
	
	protected void setupFonts() {
		setBoldItalicTypeface(mBanner);
	}
	
	public Activity getActivity() {
		return this;
	}

	class SynsetQuery extends AsyncTask<Uri, Void, Cursor> {
		private final WeakReference<TextView> mBannerReference;
		private final WeakReference<TextView> mGlossReference;
		private final WeakReference<ExpandableListView> mListsReference;
		
		public SynsetQuery(TextView banner, TextView gloss, ExpandableListView lists) {
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
			
			TextView banner = mBannerReference != null ? mBannerReference.get() : null;
			TextView gloss = mGlossReference != null ? mGlossReference.get() : null;
			ExpandableListView lists = mListsReference != null ? mListsReference.get() : null;
			
			if (banner == null ||
				gloss == null ||
				lists == null) return;
			
			if (result == null) {
				// DEBT: Externalize
				gloss.setText("ERROR!");
				gloss.setBackgroundResource(R.drawable.rounded_orange_rectangle);
				banner.setVisibility(View.GONE);
				lists.setVisibility(View.GONE);
			}
			else {
				result.moveToFirst();
				
				// first set the scalar field values
				int glossColumn = result.getColumnIndex(DubsarContentProvider.SYNSET_GLOSS);
				int subtitleColumn = result.getColumnIndex(DubsarContentProvider.SYNSET_SUBTITLE);
				
				gloss.setText(result.getString(glossColumn));
				banner.setText(result.getString(subtitleColumn));
				
				// set up the expandable list view
				ExpandableListAdapter adapter = 
						new SynsetExpandableListAdapter(getActivity(), result);
				lists.setAdapter(adapter);
			}
		}
		
	}
}
