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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

/**
 * 
 * Main Dubsar activity
 *
 */
public class MainActivity extends Activity {
	TextView mWotdWord=null;
	TextView mWotdSubtitle=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mWotdWord = (TextView)findViewById(R.id.wotd_word);
		mWotdSubtitle = (TextView)findViewById(R.id.wotd_subtitle);
		
		Uri uri = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI, 
				DubsarContentProvider.WOTD_URI_PATH);
		
		new DailyWordLoader(mWotdWord, mWotdSubtitle).execute(uri);
	}

	class DailyWordLoader extends AsyncTask<Uri, Void, Cursor> {
		
		private WeakReference<TextView> mWotdWordReference=null;
		private WeakReference<TextView> mWotdSubtitleReference=null;
		
		public DailyWordLoader(TextView wotdWord, TextView wotdSubtitle) {
			mWotdWordReference = new WeakReference<TextView>(wotdWord);
			mWotdSubtitleReference = new WeakReference<TextView>(wotdSubtitle);
		}

		@Override
		protected Cursor doInBackground(Uri... params) {
			return managedQuery(params[0], null, null, null, null);
		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			
			if (isCancelled()) return;
			
			TextView wotdWord = mWotdWordReference != null ? mWotdWordReference.get() : null;
			TextView wotdSubtitle = mWotdSubtitleReference != null ? mWotdSubtitleReference.get() : null;

			if (wotdWord == null || wotdSubtitle == null) return;
			
			if (result == null) {
				// DEBT: externalize
				wotdWord.setText("ERROR!");
				wotdWord.setText("ERROR!");
			}
			else {
				int nameAndPosColumn = result.getColumnIndex(DubsarContentProvider.WORD_NAME_AND_POS);
				int subtitleColumn = result.getColumnIndex(DubsarContentProvider.WORD_SUBTITLE);
				
				result.moveToFirst();
				
				String nameAndPos = result.getString(nameAndPosColumn);
				String subtitle = result.getString(subtitleColumn);
				
				wotdWord.setText(nameAndPos);
				wotdSubtitle.setText(subtitle);
			}
		}
		
	}
}
