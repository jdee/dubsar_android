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
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dubsar_dictionary.Dubsar.model.ForwardStack;

public class DubsarActivity extends Activity {

	public static final String EXPANDED = "expanded";
	public static final String POINTER_IDS = "pointer_ids";
	public static final String POINTER_TYPES = "pointer_types";
	public static final String POINTER_TARGET_IDS = "pointer_target_ids";
	public static final String POINTER_TARGET_TYPES = "pointer_target_types";
	public static final String POINTER_TARGET_TEXTS = "pointer_target_texts";
	public static final String POINTER_TARGET_GLOSSES =  "pointer_target_glosses";
	
	private Button mLeftArrow=null;
	private Button mRightArrow=null;
	
	protected static ForwardStack sForwardStack=new ForwardStack();
	private ConnectivityManager mConnectivityMgr=null;
	private GestureDetector mDetector=null;
	private ProgressBar mLoadingSpinner = null;
	private DisplayMetrics mDisplayMetrics = new DisplayMetrics();

	protected void onCreate(Bundle savedInstanceState, int layout) {
		super.onCreate(savedInstanceState);
		mConnectivityMgr = 
				(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

		startDubsarService();
	    setContentView(layout);
	    
	    getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
	    
	    // will be null if no spinner in view
	    mLoadingSpinner = (ProgressBar) findViewById(R.id.loading_spinner);
		
	    /*
	     * The idea behind this method is that a user may go forward in more
	     * ways than one. They may press the forward button or make a fling
	     * gesture, both of which trigger onForwardPressed(). Or the user may
	     * simply tap or search for the same thing again. In that instance,
	     * adjustForwardStack() will simply pop this activity's intent off
	     * the stack. But when we're being recreated from saved state, we
	     * should not expect to find ourselves on the top of the forward
	     * stack.
	     */
	    if (savedInstanceState == null) {
	    	adjustForwardStack();
	    }
	    
		setupNavigation();
	}

	/**
	 * If the forward button was not enabled the last time we visited
	 * the page we just went back to, it does not get redrawn, and the
	 * button remains disabled.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(getString(R.string.app_name), "activity resuming");
		setButtonState(mRightArrow, !sForwardStack.isEmpty());		
	}
	
	@Override
	public void onBackPressed() {
		sForwardStack.push(getIntent());
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

    /* 
     * It appears at API level 8 that only three typefaces are available:
     * monospace, serif and sans-serif.
     */
    public static final String TYPEFACE_FAMILY = "sans-serif";
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
    
    public int getDisplayHeight() {
    	return mDisplayMetrics.heightPixels;
    }
    
    public int getDisplayWidth() {
    	return mDisplayMetrics.widthPixels;
    }
    
    protected void startMainActivity() {
    	Intent intent = new Intent(getApplicationContext(), MainActivity.class);
    	startActivity(intent);
    }
    
    protected void startDubsarService() {
    	Intent intent = new Intent(getApplicationContext(), DubsarService.class);
    	startService(intent);
    }
    
    protected static void setButtonState(Button button, boolean state) {
    	button.setEnabled(state);
    	button.setFocusable(state);
    }
    
    protected void hideLoadingSpinner() {
    	if (mLoadingSpinner == null) return;
    	
    	mLoadingSpinner.setVisibility(View.GONE);
    }
	
    protected void adjustForwardStack() {
    	if (sForwardStack.isEmpty()) return;
    	
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
				onForwardPressed();
			}
		});
		
		mDetector = new GestureDetector(new GestureHandler(this));
	}
    
    protected void onForwardPressed() {
		// get the top of the stack
		Intent intent = sForwardStack.peek();
		if (intent != null) {
			startActivity(intent);
		}
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
			reportError(getString(R.string.no_network));
		}
		
		return passed;
	}
	
	protected void showErrorToast(String text) {
		LayoutInflater inflater = 
				(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View toastView = inflater.inflate(R.layout.toast, null);
		TextView toastText = (TextView)toastView.findViewById(R.id.toast);

		Toast errorToast = new Toast(this);
		toastText.setText(text);
		
		errorToast.setView(toastView);
		errorToast.show();
	}

    /**
     * Descendants implement this method to adjust their views
     * and report any errors (e.g., when the network is down)
     * @param error
     */
    protected void reportError(String error) {
		showErrorToast(getString(R.string.no_network));

		Log.e(getString(R.string.app_name), error);

    	// no-op if no loading spinner in view
    	hideLoadingSpinner();
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mDetector.onTouchEvent(event);
	}
	
	public static int getTotalViewHeight(View v) {
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
		
		return params.topMargin +
				v.getPaddingTop() +
				v.getHeight() +
				v.getPaddingBottom() +
				params.bottomMargin;
	}
	
	static class GestureHandler extends SimpleOnGestureListener {
		private final WeakReference<DubsarActivity> mActivityReference;
		private float mDisplacement=0f;
		
		public GestureHandler(DubsarActivity activity) {
			mActivityReference = new WeakReference<DubsarActivity>(activity);
		}
		
		public DubsarActivity getActivity() {
			return mActivityReference != null ? mActivityReference.get() : null;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (getActivity() == null) return false;
			
			float threshold = (float) (0.5 * getActivity().getDisplayWidth());
			if (mDisplacement <= -threshold) getActivity().onBackPressed();
			else if (mDisplacement >= threshold) getActivity().onForwardPressed();
			
			mDisplacement = 0f;
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
				float distanceY) {
			mDisplacement += distanceX;
			return false;
		}
	}
}
