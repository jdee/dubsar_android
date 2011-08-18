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

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dubsar_dictionary.Dubsar.model.ForwardStack;

public class DubsarActivity extends Activity implements GestureDetector.OnGestureListener {
	
	private float mCurrentPosition=0f;

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	public static final String EXPANDED = "expanded";
	public static final String POINTER_IDS = "pointer_ids";
	public static final String POINTER_TYPES = "pointer_types";
	public static final String POINTER_TARGET_IDS = "pointer_target_ids";
	public static final String POINTER_TARGET_TYPES = "pointer_target_types";
	public static final String POINTER_TARGET_TEXTS = "pointer_target_texts";
	public static final String POINTER_TARGET_GLOSSES =  "pointer_target_glosses";
	
	private Button mLeftArrow=null;
	private Button mRightArrow=null;
	
	protected static volatile ForwardStack sForwardStack=new ForwardStack();
	private ConnectivityManager mConnectivityMgr=null;

	protected void onCreate(Bundle savedInstanceState, int layout) {
		super.onCreate(savedInstanceState);
		mConnectivityMgr = 
				(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		setContentView(layout);
		adjustForwardStack();
		setupNavigation();
	}

	/**
	 * If the forward button was not enabled the last time we visited
	 * the page we just went back to, it does not get redrawn, and the
	 * button remains disabled. This method fixes that.
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		
		setButtonState(mRightArrow, !sForwardStack.isEmpty());		
	}
	
	@Override
	public void onBackPressed() {
		sForwardStack.push(getIntent());
		mRightArrow.setEnabled(true);
		super.onBackPressed();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.home:
        		startMainActivity();
        		return true;
            case R.id.search:
                onSearchRequested();
                return true;
            default:
                return false;
        }
    }
   
    /**
     * The SDK doesn't provide support for querying column types until
     * SDK 11.
     */
    public enum FieldType {
    	Unknown,
    	Integer,
    	String
    }
    
    public static final String TYPEFACE_FAMILY = "Tahoma";
    public static final Typeface BOLD_TYPEFACE = Typeface.create(TYPEFACE_FAMILY, Typeface.BOLD);
    public static final Typeface NORMAL_TYPEFACE = Typeface.create(TYPEFACE_FAMILY, Typeface.NORMAL);
    public static final Typeface ITALIC_TYPEFACE = Typeface.create(TYPEFACE_FAMILY, Typeface.ITALIC);
    public static final Typeface BOLD_ITALIC_TYPEFACE = Typeface.create(TYPEFACE_FAMILY, Typeface.BOLD_ITALIC);

    /**
     * Set the specified TextView to use the Dubsar bold font.
     * @param textView the TextView whose font to set
     */
    public static void setBoldTypeface(TextView textView) {
    	textView.setTypeface(BOLD_TYPEFACE, Typeface.BOLD);
    }
    
    /**
     * Set the specified TextView to use the Dubsar normal font.
     * @param textView the TextView whose font to set
     */
    public static void setNormalTypeface(TextView textView) {
    	textView.setTypeface(NORMAL_TYPEFACE, Typeface.NORMAL);
    }

    /**
     * Set the specified TextView to use the Dubsar bold font.
     * @param textView the TextView whose font to set
     */
    public static void setItalicTypeface(TextView textView) {
    	textView.setTypeface(ITALIC_TYPEFACE, Typeface.ITALIC);
    }
    
    /**
     * Set the specified TextView to use the Dubsar normal font.
     * @param textView the TextView whose font to set
     */
    public static void setBoldItalicTypeface(TextView textView) {
    	textView.setTypeface(BOLD_ITALIC_TYPEFACE, Typeface.BOLD_ITALIC);
    }
    
    protected void startMainActivity() {
    	Intent intent = new Intent(getApplicationContext(), MainActivity.class);
    	startActivity(intent);
    }
    
    protected static void setButtonState(Button button, boolean state) {
    	button.setEnabled(state);
    	button.setFocusable(state);
    }
	
    protected void adjustForwardStack() {
    	if (sForwardStack.isEmpty()) return;
    	
    	Log.d(getString(R.string.app_name), 
    			"starting new intent with URI " + getIntent().getData());
		if (!equalIntents(getIntent(), sForwardStack.peek())) {
			// user is visiting something other than the previous forward
			// link, clear the stack
			sForwardStack.clear();
		}
		else {
			// user went forward. pop this entry from the stack
			sForwardStack.pop();
		}

    }

    protected void setupNavigation() {
		mLeftArrow = (Button)findViewById(R.id.left_arrow);
		mRightArrow = (Button)findViewById(R.id.right_arrow);

		setButtonState(mRightArrow, !sForwardStack.isEmpty());		
		
		mLeftArrow.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		mRightArrow.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// get the top of the stack
				Intent intent = sForwardStack.peek();
				startActivity(intent);
			}
		});
		
		GestureDetector dector = new GestureDetector(this, this);
	}
    
    /**
     * To determine whether we're restarting the activity at the top of
     * the forward stack, we cannot just compare Intent references. We 
     * must examine the contents. Most intents have a URI in getData().
     * Some search queries have a null URI but a string extra 
     * SearchManager.QUERY. The Main intent has neither.
     * @param i1 one Intent
     * @param i2 another Intent
     * @return whether the two Intents represent the same action
     */
    protected static boolean equalIntents(Intent i1, Intent i2) {
    	if (i1 == null || i2 == null) 
    		throw new NullPointerException("not expecting null Intents");
    	
    	Uri uri1 = i1.getData();
    	Uri uri2 = i2.getData();
    	
    	String query1 = i1.getStringExtra(SearchManager.QUERY);
    	String query2 = i2.getStringExtra(SearchManager.QUERY);
    	
    	if (uri1 == null && uri2 == null) {
    		if (query1 == null || query2 == null) {
    			// one intent is for the main activity
    			return query1 == query2;
    		}
    		
    		// two search queries
    		return query1.equals(query2);
    	}
    	
    	if (uri1 == null || uri2 == null) {
    		return false;
    	}
    	boolean equal = uri1.equals(uri2);
    	
    	return equal;
    }
	
	/**
	 * Determine whether the network is currently available. There must
	 * be a better way to do this.
	 * @return true if the network is available; false otherwise
	 */
	protected boolean isNetworkAvailable() {
		NetworkInfo wifiInfo = mConnectivityMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobileInfo = mConnectivityMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
		return wifiInfo.isConnected() || mobileInfo.isConnected();
	}
	
	protected boolean checkNetwork() {
		boolean passed = isNetworkAvailable();
		
		if (!passed) {
			Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
			reportError(getString(R.string.no_network));
		}
		
		return passed;
	}
    
    /**
     * Descendants implement this method to adjust their views
     * and report any errors (e.g., when the network is down)
     * @param error
     */
    protected void reportError(String error) {
    	Log.e(getString(R.string.app_name), error);
    }
}
