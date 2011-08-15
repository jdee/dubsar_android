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
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.dubsar_dictionary.Dubsar.DubsarActivity.FieldType;

/**
 * 
 * Sense activity
 *
 */
public class SenseActivity extends Activity {
	
	private TextView mTitle=null;
	private TextView mBanner=null;
	private TextView mGloss=null;
	private TextView mSynonyms=null;
	private TextView mVerbFrameLabel=null;
	private TextView mSampleLabel=null;
	private ListView mVerbFrames=null;
	private ListView mSamples=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sense);
		
		mTitle = (TextView)findViewById(R.id.sense_title);
		mBanner = (TextView)findViewById(R.id.sense_banner);
		mGloss = (TextView)findViewById(R.id.sense_gloss);
		mSynonyms = (TextView)findViewById(R.id.sense_synonyms);
		mVerbFrameLabel = (TextView)findViewById(R.id.sense_verb_frame_label);
		mSampleLabel = (TextView)findViewById(R.id.sense_sample_label);
		mVerbFrames = (ListView)findViewById(R.id.sense_verb_frames);
		mSamples = (ListView)findViewById(R.id.sense_samples);
		
		setupColors();
		
		Uri uri = getIntent().getData();
		String nameAndPos = getIntent().getExtras().getString(DubsarContentProvider.SENSE_NAME_AND_POS);
		mTitle.setText(nameAndPos);
		
		new SenseQuery(mTitle, mBanner, mGloss, mSynonyms, mVerbFrameLabel, mSampleLabel, mVerbFrames, mSamples).execute(uri);
	}
	
	protected void setupColors() {
		int orange = Color.rgb(0xf5, 0x84, 0x00);
		int white = Color.rgb(0xff, 0xff, 0xff);
		
		mTitle.setBackgroundColor(orange);
		mGloss.setBackgroundColor(white);
		mVerbFrameLabel.setBackgroundColor(orange);
		mSampleLabel.setBackgroundColor(orange);
	}
	
	class SenseQuery extends AsyncTask<Uri, Void, Cursor> {
		
		private final WeakReference<TextView> mTitleReference;
		private final WeakReference<TextView> mBannerReference;
		private final WeakReference<TextView> mGlossReference;
		private final WeakReference<TextView> mSynonymsReference;
		private final WeakReference<TextView> mVerbFrameLabelReference;
		private final WeakReference<TextView> mSampleLabelReference;
		private final WeakReference<ListView> mVerbFramesReference;
		private final WeakReference<ListView> mSamplesReference;
		
		public SenseQuery(TextView title, TextView banner, TextView gloss,
				TextView synonyms, TextView verbFrameLabel, TextView sampleLabel,
				ListView verbFrames, ListView samples) {
			mTitleReference = new WeakReference<TextView>(title);
			mBannerReference = new WeakReference<TextView>(banner);
			mGlossReference = new WeakReference<TextView>(gloss);
			mSynonymsReference = new WeakReference<TextView>(synonyms);
			mVerbFrameLabelReference = new WeakReference<TextView>(verbFrameLabel);
			mSampleLabelReference = new WeakReference<TextView>(sampleLabel);
			mVerbFramesReference = new WeakReference<ListView>(verbFrames);
			mSamplesReference = new WeakReference<ListView>(samples);
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
			TextView synonyms = mSynonymsReference != null ? mSynonymsReference.get() : null;
			TextView verbFrameLabel = mVerbFrameLabelReference != null ? mVerbFrameLabelReference.get() : null;
			TextView sampleLabel = mSampleLabelReference != null ? mSampleLabelReference.get() : null;
			ListView verbFrames = mVerbFramesReference != null ? mVerbFramesReference.get() : null;
			ListView samples = mSamplesReference != null ? mSamplesReference.get() : null;
			
			if (title == null ||
				banner == null ||
				gloss == null ||
				synonyms == null ||
				verbFrameLabel == null ||
				sampleLabel == null ||
				verbFrames == null ||
				samples == null) return;
			
			if (result == null) {
				// DEBT: Externalize
				title.setText("ERROR!");
				banner.setText("ERROR!");
				gloss.setText("ERROR!");
				synonyms.setText("ERROR!");
				
				verbFrameLabel.setVisibility(View.GONE);
				verbFrames.setVisibility(View.GONE);

				sampleLabel.setVisibility(View.GONE);
				samples.setVisibility(View.GONE);
			}
			else {
				result.moveToFirst();
				
				// first set the scalar field values
				int nameAndPosColumn = result.getColumnIndex(DubsarContentProvider.SENSE_NAME_AND_POS);
				int subtitleColumn = result.getColumnIndex(DubsarContentProvider.SENSE_SUBTITLE);
				int glossColumn = result.getColumnIndex(DubsarContentProvider.SENSE_GLOSS);
				int synonymsColumn = result.getColumnIndex(DubsarContentProvider.SENSE_SYNONYMS_AS_STRING);
				
				title.setText(result.getString(nameAndPosColumn));
				banner.setText(result.getString(subtitleColumn));
				gloss.setText(result.getString(glossColumn));
				synonyms.setText(result.getString(synonymsColumn));
				
				// now get the number of each vector field
				int verbFrameCountColumn = result.getColumnIndex(DubsarContentProvider.SENSE_VERB_FRAME_COUNT);
				int sampleCountColumn = result.getColumnIndex(DubsarContentProvider.SENSE_SAMPLE_COUNT);

				int verbFrameCount = result.getInt(verbFrameCountColumn);
				int sampleCount = result.getInt(sampleCountColumn);
				
				if (verbFrameCount == 0) {
					verbFrameLabel.setVisibility(View.GONE);
					verbFrames.setVisibility(View.GONE);
				}
				
				if (sampleCount == 0) {
					sampleLabel.setVisibility(View.GONE);
					samples.setVisibility(View.GONE);				
				}

				if (verbFrameCount == 0 && sampleCount == 0) return;
								
				if (verbFrameCount > 0) {
					String[] columns = new String[] { BaseColumns._ID, DubsarContentProvider.SENSE_VERB_FRAME };
					FieldType[] types = new FieldType[] { FieldType.Integer, FieldType.String };

					String[] from = new String[] { DubsarContentProvider.SENSE_VERB_FRAME };
					int[] to = new int[] { R.id.sample };
					
					Cursor cursor = DubsarActivity.extractSubCursor(result, columns, types, 0, verbFrameCount-1);
					SimpleCursorAdapter adapter = new SimpleCursorAdapter(verbFrames.getContext(), 
						R.layout.sample, cursor, from, to);
					verbFrames.setAdapter(adapter);
				}
				
				if (sampleCount > 0) {
					String[] columns = new String[] { BaseColumns._ID, DubsarContentProvider.SENSE_SAMPLE };
					FieldType[] types = new FieldType[] { FieldType.Integer, FieldType.String };
					
					String[] from = new String[] { DubsarContentProvider.SENSE_SAMPLE };
					int[] to = new int[] { R.id.sample };
					
					Cursor cursor = DubsarActivity.extractSubCursor(result, columns, types,
						verbFrameCount, verbFrameCount+sampleCount-1);
					SimpleCursorAdapter adapter = new SimpleCursorAdapter(samples.getContext(), 
						R.layout.sample, cursor, from, to);
					samples.setAdapter(adapter);
				}
			}
		}
		
	}

}
