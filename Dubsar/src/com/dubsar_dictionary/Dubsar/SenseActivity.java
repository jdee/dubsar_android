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
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
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
	private TextView mSynonymLabel=null;
	private TextView mVerbFrameLabel=null;
	private TextView mSampleLabel=null;
	private ListView mSynonyms=null;
	private ListView mVerbFrames=null;
	private ListView mSamples=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sense);
		
		mTitle = (TextView)findViewById(R.id.sense_title);
		mBanner = (TextView)findViewById(R.id.sense_banner);
		mGloss = (TextView)findViewById(R.id.sense_gloss);
		mSynonymLabel = (TextView)findViewById(R.id.sense_synonym_label);
		mVerbFrameLabel = (TextView)findViewById(R.id.sense_verb_frame_label);
		mSampleLabel = (TextView)findViewById(R.id.sense_sample_label);
		mSynonyms = (ListView)findViewById(R.id.sense_synonyms);
		mVerbFrames = (ListView)findViewById(R.id.sense_verb_frames);
		mSamples = (ListView)findViewById(R.id.sense_samples);
		
		setupColors();
		
		Intent intent = getIntent();
		Uri uri = intent.getData();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			String nameAndPos = extras.getString(DubsarContentProvider.SENSE_NAME_AND_POS);
			if (nameAndPos != null) mTitle.setText(nameAndPos);
		}
		
		new SenseQuery(mTitle, mBanner, mGloss, mSynonymLabel, mVerbFrameLabel, mSampleLabel, mSynonyms, mVerbFrames, mSamples).execute(uri);
	}
	
	protected void setupColors() {
		int orange = Color.rgb(0xf5, 0x84, 0x00);
		int white = Color.rgb(0xff, 0xff, 0xff);
		
		mTitle.setBackgroundColor(orange);
		mGloss.setBackgroundColor(white);
		mSynonymLabel.setBackgroundColor(orange);
		mVerbFrameLabel.setBackgroundColor(orange);
		mSampleLabel.setBackgroundColor(orange);
	}
	
	class SenseQuery extends AsyncTask<Uri, Void, Cursor> {
		
		private final WeakReference<TextView> mTitleReference;
		private final WeakReference<TextView> mBannerReference;
		private final WeakReference<TextView> mGlossReference;
		private final WeakReference<TextView> mSynonymLabelReference;
		private final WeakReference<TextView> mVerbFrameLabelReference;
		private final WeakReference<TextView> mSampleLabelReference;
		private final WeakReference<ListView> mSynonymsReference;
		private final WeakReference<ListView> mVerbFramesReference;
		private final WeakReference<ListView> mSamplesReference;
		
		public SenseQuery(TextView title, TextView banner, TextView gloss,
				TextView synonymLabel, TextView verbFrameLabel, TextView sampleLabel,
				ListView synonyms, ListView verbFrames, ListView samples) {
			mTitleReference = new WeakReference<TextView>(title);
			mBannerReference = new WeakReference<TextView>(banner);
			mGlossReference = new WeakReference<TextView>(gloss);
			mSynonymLabelReference = new WeakReference<TextView>(synonymLabel);
			mVerbFrameLabelReference = new WeakReference<TextView>(verbFrameLabel);
			mSampleLabelReference = new WeakReference<TextView>(sampleLabel);
			mSynonymsReference = new WeakReference<ListView>(synonyms);
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
			TextView synonymLabel = mSynonymLabelReference != null ? mSynonymLabelReference.get() : null;
			TextView verbFrameLabel = mVerbFrameLabelReference != null ? mVerbFrameLabelReference.get() : null;
			TextView sampleLabel = mSampleLabelReference != null ? mSampleLabelReference.get() : null;
			ListView synonyms = mSynonymsReference != null ? mSynonymsReference.get() : null;
			ListView verbFrames = mVerbFramesReference != null ? mVerbFramesReference.get() : null;
			ListView samples = mSamplesReference != null ? mSamplesReference.get() : null;
			
			if (title == null ||
				banner == null ||
				gloss == null ||
				synonymLabel == null ||
				verbFrameLabel == null ||
				sampleLabel == null ||
				synonyms == null ||
				verbFrames == null ||
				samples == null) return;
			
			if (result == null) {
				// DEBT: Externalize
				title.setText("ERROR!");
				banner.setVisibility(View.GONE);
				gloss.setVisibility(View.GONE);
				
				synonymLabel.setVisibility(View.GONE);
				synonyms.setVisibility(View.GONE);
				
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
				
				title.setText(result.getString(nameAndPosColumn));
				banner.setText(result.getString(subtitleColumn));
				gloss.setText(result.getString(glossColumn));
				
				// now get the number of each vector field
				int synonymCountColumn = result.getColumnIndex(DubsarContentProvider.SENSE_SYNONYM_COUNT);
				int verbFrameCountColumn = result.getColumnIndex(DubsarContentProvider.SENSE_VERB_FRAME_COUNT);
				int sampleCountColumn = result.getColumnIndex(DubsarContentProvider.SENSE_SAMPLE_COUNT);

				int synonymCount = result.getInt(synonymCountColumn);
				int verbFrameCount = result.getInt(verbFrameCountColumn);
				int sampleCount = result.getInt(sampleCountColumn);
				
				if (synonymCount == 0) {
					synonymLabel.setVisibility(View.GONE);
					synonyms.setVisibility(View.GONE);
				}
				
				if (verbFrameCount == 0) {
					verbFrameLabel.setVisibility(View.GONE);
					verbFrames.setVisibility(View.GONE);
				}
				
				if (sampleCount == 0) {
					sampleLabel.setVisibility(View.GONE);
					samples.setVisibility(View.GONE);				
				}

				if (synonymCount + verbFrameCount + sampleCount == 0) return;
				
				if (synonymCount > 0) {
					String[] columns = new String[] { BaseColumns._ID, DubsarContentProvider.SENSE_SYNONYM };
					FieldType[] types = new FieldType[] { FieldType.Integer, FieldType.String };
					
					String[] from = new String[] { DubsarContentProvider.SENSE_SYNONYM };
					int[] to = new int[] { R.id.sample };
					
					Cursor cursor = DubsarActivity.extractSubCursor(result, columns, types, 0, synonymCount);
					SimpleCursorAdapter adapter = new SimpleCursorAdapter(synonyms.getContext(),
							R.layout.sample, cursor, from, to);
					synonyms.setAdapter(adapter);
					
					synonyms.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		                	
							Intent senseIntent = new Intent(getApplicationContext(), SenseActivity.class);

							Uri data = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI,
                                    DubsarContentProvider.SENSES_URI_PATH + 
                                    "/" + id);
							senseIntent.setData(data);
							startActivity(senseIntent);
						}
					});
				}
								
				if (verbFrameCount > 0) {
					String[] columns = new String[] { BaseColumns._ID, DubsarContentProvider.SENSE_VERB_FRAME };
					FieldType[] types = new FieldType[] { FieldType.Integer, FieldType.String };

					String[] from = new String[] { DubsarContentProvider.SENSE_VERB_FRAME };
					int[] to = new int[] { R.id.sample };
					
					Cursor cursor = DubsarActivity.extractSubCursor(result, columns, types, synonymCount, verbFrameCount);
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
						synonymCount + verbFrameCount, sampleCount);
					SimpleCursorAdapter adapter = new SimpleCursorAdapter(samples.getContext(), 
						R.layout.sample, cursor, from, to);
					samples.setAdapter(adapter);
				}
			}
		}
		
	}

}
