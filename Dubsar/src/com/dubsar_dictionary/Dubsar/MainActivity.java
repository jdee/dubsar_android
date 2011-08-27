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
	private BroadcastReceiver mReceiver=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.main);
		mWotdWord = (Button)findViewById(R.id.wotd_word);
		
		Button dubsarSearch = (Button)findViewById(R.id.dubsar_search);
		dubsarSearch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onSearchRequested();
			}
		});

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
        case R.id.preferences:
        	startPreferencesActivity();
        	return true;
        default:
            return false;
        }
	}
	
	protected void saveResults(String text, final String nameAndPos, final int id) {
		mWotdWord.setText(text);
		mWotdWord.setEnabled(true);
		
		mWotdWord.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (nameAndPos == null || id == 0) return;

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
	
	protected void setupBroadcastReceiver() {
		mReceiver = new WotdReceiver(this);
		Intent intent = getApplicationContext().registerReceiver(mReceiver, 
				new IntentFilter(DubsarService.ACTION_WOTD));
		
		if (intent != null) {
			Bundle extras = intent.getExtras();
			String error = extras.getString(DubsarService.ERROR_MESSAGE);
			if (error == null) {
				saveResults(extras.getString(DubsarService.WOTD_TEXT), 
						extras.getString(DubsarContentProvider.WORD_NAME_AND_POS),
						extras.getInt(BaseColumns._ID));
			}
			else {
				reportError(error);
			}
		}
		else {
			// request rebroadcast
			intent = new Intent(getApplicationContext(), DubsarService.class);
			intent.setAction(DubsarService.ACTION_WOTD);
			startService(intent);
		}
	}
	
	protected void teardownBroadcastReceiver() {
		if (mReceiver != null) {
			getApplicationContext().unregisterReceiver(mReceiver);
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
    
    protected void startPreferencesActivity() {
    	Intent intent = new Intent(getApplicationContext(), PreferencesActivity.class);
    	startActivity(intent);
    }

	protected void reportError(String error) {
		mWotdWord.setText(error);
		mWotdWord.setEnabled(false);
		showErrorToast(error);
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
		
			Bundle extras = intent.getExtras();
			String error = extras.getString(DubsarService.ERROR_MESSAGE);

			String text = extras.getString(DubsarService.WOTD_TEXT);
			String nameAndPos = extras.getString(DubsarContentProvider.WORD_NAME_AND_POS);
			int id = extras.getInt(BaseColumns._ID);
			
			/* 
			 * If we don't get a sticky broadcast back when we subscribe,
			 * we tend to get a blank one here immediately.
			 */
			if (id == 0 || text == null || nameAndPos == null) return;
			
			if (error == null) {
				getActivity().saveResults(text, nameAndPos, id);
			}
			else {
				getActivity().reportError(error);
			}
		}
	}
}
