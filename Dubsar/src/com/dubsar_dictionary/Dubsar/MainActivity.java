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
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
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
		
		Uri uri = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI, 
				DubsarContentProvider.WOTD_URI_PATH);

		setupTypefaces();
		
		if (!checkNetwork()) return;
		
		if (savedInstanceState != null) {
			mWotdText = savedInstanceState.getString(WOTD_TEXT);
			mNextWotdTime = savedInstanceState.getLong(WOTD_TIME);
			mWotdId = savedInstanceState.getInt(BaseColumns._ID);
			mWotdNameAndPos = 
					savedInstanceState.getString(DubsarContentProvider.WORD_NAME_AND_POS);
		}
		
		Calendar now = Calendar.getInstance();
		
		if (mWotdText != null && mNextWotdTime > now.getTimeInMillis()) {
			mWotdWord.setText(mWotdText);
		}
		else {
			new DailyWordLoader(mWotdWord).execute(uri);
			computeNextWotdTime();
		}
		
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
		
		outState.putLong(WOTD_TIME, mNextWotdTime);
		outState.putString(WOTD_TEXT, mWotdText);
		outState.putInt(BaseColumns._ID, mWotdId);
		outState.putString(DubsarContentProvider.WORD_NAME_AND_POS, mWotdNameAndPos);
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
		
		Calendar next = new GregorianCalendar();
		next.setTimeInMillis(mNextWotdTime);
		
		_amPm = next.get(Calendar.AM_PM);
		String amPm = _amPm == Calendar.AM ? "am" : "pm";
		
		hour = next.get(Calendar.HOUR);
		
		Log.i(getString(R.string.app_name), "next WOTD time is " + hour + ":00 " + amPm);
		
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
				reportError("ERROR!");
			}
			else {
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

				wotdWord.setText(mWotdText);
			}
		}
		
	}
}
