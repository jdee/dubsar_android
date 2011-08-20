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
import java.util.Calendar;
import java.util.TimeZone;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
	public static final String WOTD_TIME = "wotd_time";
	public static final String WOTD_TEXT = "wotd_text";
			
	private Button mWotdWord=null;
	private long mNextWotdTime=0;
	private String mWotdText=null;
	private String mWotdNameAndPos=null;
	private int mWotdId=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.main);
		
		mWotdWord = (Button)findViewById(R.id.wotd_word);

		setupTypefaces();
		
		
		if (savedInstanceState != null) {
			restoreInstanceState(savedInstanceState);
		}
				
		if (mWotdText != null && mNextWotdTime > System.currentTimeMillis()) {
			populateData();
		}
		else {
			if (!checkNetwork()) return;
			
			mWotdWord.setText(getString(R.string.loading));
			
			Uri uri = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI, 
					DubsarContentProvider.WOTD_URI_PATH);
			new DailyWordLoader(this).execute(uri);
			
			computeNextWotdTime();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_options_menu, menu);
        return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.about:
        	startAboutActivity();
        	return true;
        case R.id.search:
            onSearchRequested();
            return true;
        case R.id.faq:
        	startFAQActivity();
        	return true;
        default:
            return false;
        }
	}
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveState(outState);
	}
	
	protected void saveResults(Cursor result) {
		int idColumn = result.getColumnIndex(BaseColumns._ID);
		int nameAndPosColumn = result.getColumnIndex(DubsarContentProvider.WORD_NAME_AND_POS);
		int freqCntColumn = result.getColumnIndex(DubsarContentProvider.WORD_FREQ_CNT);
		
		result.moveToFirst();
		
		mWotdId = result.getInt(idColumn);
		mWotdNameAndPos = result.getString(nameAndPosColumn);
		
		int freqCnt = result.getInt(freqCntColumn);
		mWotdText = mWotdNameAndPos;
		if (freqCnt > 0) {
			mWotdText += " freq. cnt.:" + freqCnt;
		}
	}
	
	protected void populateData() {
		mWotdWord.setText(mWotdText);
		
		mWotdWord.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mWotdNameAndPos == null || mWotdId == 0) return;
				
            	Intent wordIntent = new Intent(getApplicationContext(), WordActivity.class);
            	wordIntent.putExtra(DubsarContentProvider.WORD_NAME_AND_POS, mWotdNameAndPos);

            	// URI for the word request
            	Uri data = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI,
                                                DubsarContentProvider.WORDS_URI_PATH + 
                                                "/" + mWotdId);
                wordIntent.setData(data);
                startActivity(wordIntent);
			}
		});
	}
	
	protected void saveState(Bundle outState) {
		outState.putLong(WOTD_TIME, mNextWotdTime);
		outState.putString(WOTD_TEXT, mWotdText);
		outState.putInt(BaseColumns._ID, mWotdId);
		outState.putString(DubsarContentProvider.WORD_NAME_AND_POS, mWotdNameAndPos);
	}
	
	protected void restoreInstanceState(Bundle inState) {
		mWotdText = inState.getString(WOTD_TEXT);
		mNextWotdTime = inState.getLong(WOTD_TIME);
		mWotdId = inState.getInt(BaseColumns._ID);
		mWotdNameAndPos = 
				inState.getString(DubsarContentProvider.WORD_NAME_AND_POS);		
	}

    protected void startAboutActivity() {
    	Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
    	startActivity(intent);
    }
    
    protected void startFAQActivity() {
    	Intent intent = new Intent(getApplicationContext(), FAQActivity.class);
    	startActivity(intent);
    }

	protected void reportError(String error) {
		mWotdWord.setText(error);
	}
	
	protected void setupTypefaces() {
		setBoldTypeface((TextView)findViewById(R.id.main_hello));
		setBoldTypeface((TextView)findViewById(R.id.main_wotd));
		setBoldTypeface(mWotdWord);
	}
	
	protected void computeNextWotdTime() {
		Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		int _amPm = now.get(Calendar.AM_PM);
		int hour = now.get(Calendar.HOUR);
		int minute = now.get(Calendar.MINUTE);
		int second = now.get(Calendar.SECOND);
		
		if (_amPm == Calendar.PM) hour += 12;
		
		int secondsTillNext = (23-hour)*3600 + (59-minute)*60 + 60 - second;
		
		// add a 30-second pad
		secondsTillNext += 30;
		
		mNextWotdTime = now.getTimeInMillis() + secondsTillNext*1000;
	}

	static class DailyWordLoader extends AsyncTask<Uri, Void, Cursor> {
		
		private final WeakReference<MainActivity> mActivityReference;
		
		public DailyWordLoader(MainActivity activity) {
			mActivityReference = new WeakReference<MainActivity>(activity);
		}
		
		public MainActivity getActivity() {
			return mActivityReference != null ? mActivityReference.get() : null;
		}

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
				// DEBT: externalize
				getActivity().reportError("ERROR!");
			}
			else {
				getActivity().saveResults(result);
				getActivity().populateData();
				result.close();
			}
		}
		
	}
}
