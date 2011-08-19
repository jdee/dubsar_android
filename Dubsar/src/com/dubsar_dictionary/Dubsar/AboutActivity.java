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

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ViewAnimator;

public class AboutActivity extends DubsarActivity {

	public static final String VIEW_INDEX = "view_index";
	
	private Button mAboutButton = null;
	private Button mLicenseButton = null;
	private ViewAnimator mAnimator = null;
	private int mViewIndex = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.about);
		
		mAboutButton = (Button)findViewById(R.id.about_button);
		mLicenseButton = (Button)findViewById(R.id.license_button);
		mAnimator = (ViewAnimator)findViewById(R.id.animator);
		
		mAnimator.setAnimateFirstView(false);

		// It's a little klugey to use these two literal indices
		// here, but for now it's ok.
		mAboutButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mAnimator.showPrevious();
				mViewIndex = 0;
			}
		});
		
		mLicenseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mAnimator.showNext();
				mViewIndex = 1;
			}
		});
		
		if (savedInstanceState != null) {
			// if not set, will default to 0, which is the default behavior
			// anyway
			mViewIndex = savedInstanceState.getInt(VIEW_INDEX);
			mAnimator.setDisplayedChild(mViewIndex);
		}
		
		adjustAboutHeight();
		adjustLicenseHeight();
	}
	
	protected void adjustAboutHeight() {

		View header = findViewById(R.id.header);
		View headerDivider = findViewById(R.id.header_divider);
	    
		View aboutTop = findViewById(R.id.about_top);
		View aboutMiddle = findViewById(R.id.about_middle);
	    LinearLayout aboutLayout = (LinearLayout)findViewById(R.id.about_layout);
	    
	    int totalAboutHeight = getTotalViewHeight(header) +
	    		getTotalViewHeight(headerDivider) +
	    		getTotalViewHeight(aboutTop) +
	    		getTotalViewHeight(aboutMiddle);
	    
	    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)aboutLayout.getLayoutParams();

	    aboutLayout.setMinimumHeight(getDisplayHeight() - totalAboutHeight - 
	    		params.topMargin - params.bottomMargin - aboutLayout.getPaddingTop() - 
	    		aboutLayout.getPaddingBottom());
	}
	
	protected void adjustLicenseHeight() {
		View header = findViewById(R.id.header);
		View headerDivider = findViewById(R.id.header_divider);
		
	    LinearLayout licenseLayout = (LinearLayout)findViewById(R.id.license_layout);
	    
	    int totalLicenseHeight = getTotalViewHeight(header) +
	    		getTotalViewHeight(headerDivider);
	    
	    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)licenseLayout.getLayoutParams();

	    licenseLayout.setMinimumHeight(getDisplayHeight() - totalLicenseHeight - 
	    		params.topMargin - params.bottomMargin - licenseLayout.getPaddingTop() - 
	    		licenseLayout.getPaddingBottom());
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt(VIEW_INDEX, mViewIndex);
	}

}
