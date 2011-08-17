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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * 
 * Main Dubsar activity
 *
 */
public class MainActivity extends DubsarActivity {
	Button mWotdWord=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState, R.layout.main);
		
		mWotdWord = (Button)findViewById(R.id.wotd_word);
		
		Uri uri = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI, 
				DubsarContentProvider.WOTD_URI_PATH);

		setupTypefaces();
		
		new DailyWordLoader(mWotdWord).execute(uri);
	}
	
	protected void setupTypefaces() {
		setBoldTypeface((TextView)findViewById(R.id.main_hello));
		setBoldTypeface((TextView)findViewById(R.id.main_wotd));
		setBoldTypeface(mWotdWord);
	}

	class DailyWordLoader extends AsyncTask<Uri, Void, Cursor> {
		
		private WeakReference<Button> mWotdWordReference=null;
		
		public DailyWordLoader(Button wotdWord) {
			mWotdWordReference = new WeakReference<Button>(wotdWord);
		}

		@Override
		protected Cursor doInBackground(Uri... params) {
			return managedQuery(params[0], null, null, null, null);
		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			
			if (isCancelled()) return;
			
			Button wotdWord = mWotdWordReference != null ? mWotdWordReference.get() : null;

			if (wotdWord == null) return;
			
			if (result == null) {
				// DEBT: externalize
				wotdWord.setText("ERROR!");
			}
			else {
				int idColumn = result.getColumnIndex(BaseColumns._ID);
				int nameAndPosColumn = result.getColumnIndex(DubsarContentProvider.WORD_NAME_AND_POS);
				int freqCntColumn = result.getColumnIndex(DubsarContentProvider.WORD_FREQ_CNT);
				
				result.moveToFirst();
				
				final int id = result.getInt(idColumn);
				final String nameAndPos = result.getString(nameAndPosColumn);
				int freqCnt = result.getInt(freqCntColumn);
				String text = nameAndPos;
				if (freqCnt > 0) {
					text += " freq. cnt.:" + freqCnt;
				}
				
				wotdWord.setText(text);
				
				wotdWord.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
		            	Intent wordIntent = new Intent(getApplicationContext(), WordActivity.class);
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
		}
		
	}
}
