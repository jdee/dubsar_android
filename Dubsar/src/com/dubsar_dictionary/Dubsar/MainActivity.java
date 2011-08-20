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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
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
	private BroadcastReceiver mReceiver=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.main);
		
		mWotdWord = (Button)findViewById(R.id.wotd_word);

		setupTypefaces();
		
		/* 
		 * The service is already started in the base class
		 * constructor above. However, broadcasts are cheap.
		 * This just results in a second broadcast to avoid
		 * a race with the receiver. Alternatives include
		 * moving the broadcast receiver into the base class
		 * or calling startDubsarService() in every child
		 * class' constructor.
		 */
		setupBroadcastReceiver();
		startDubsarService();
		
		if (savedInstanceState != null) {
			restoreInstanceState(savedInstanceState);
		}
				
		if (mWotdText != null && mNextWotdTime > System.currentTimeMillis()) {
			populateData();
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
		// teardownBroadcastReceiver();
	}
	
	protected void saveResults(String text, int id) {
		mWotdText = text;
		mWotdId = id;
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
	
	protected void setupBroadcastReceiver() {
		mReceiver = new WotdReceiver(this);
		getApplicationContext().registerReceiver(mReceiver, 
				new IntentFilter(DubsarService.ACTION_WOTD));
		Log.d("Dubsar", "registered receiver");
	}
	
	protected void teardownBroadcastReceiver() {
		if (mReceiver != null) {
			getApplicationContext().unregisterReceiver(mReceiver);
			Log.d("Dubsar", "unregistering receiver");
		}
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
	
	static class WotdReceiver extends BroadcastReceiver {

		private final WeakReference<MainActivity> mActivityReference;
		
		public WotdReceiver(MainActivity activity) {
			mActivityReference = new WeakReference<MainActivity>(activity);
		}
		
		public MainActivity getActivity() {
			return mActivityReference != null ? mActivityReference.get() : null;
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (getActivity() == null) return;
			
			if (!DubsarService.ACTION_WOTD.equals(intent.getAction())) return;

			Log.d("Dubsar", "received WOTD broadcast");
		
			Bundle extras = intent.getExtras();
			getActivity().saveResults(extras.getString(DubsarService.WOTD_TEXT), 
					extras.getInt(BaseColumns._ID));
			getActivity().populateData();
		}
	}
}
