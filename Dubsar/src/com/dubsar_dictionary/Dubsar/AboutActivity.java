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
import android.widget.Button;
import android.widget.ViewFlipper;

public class AboutActivity extends DubsarActivity {

	public static final String VIEW_INDEX = "view_index";
	
	private Button mAboutButton = null;
	private Button mLicenseButton = null;
	private ViewFlipper mFlipper = null;
	private int mViewIndex = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.about);
		
		mAboutButton = (Button)findViewById(R.id.about_button);
		mLicenseButton = (Button)findViewById(R.id.license_button);
		mFlipper = (ViewFlipper)findViewById(R.id.flipper);
		
		// It's a little klugey to use these two literal indices
		// here, but for now it's ok.
		mAboutButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mFlipper.showPrevious();
				mViewIndex = 0;
			}
		});
		
		mLicenseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mFlipper.showNext();
				mViewIndex = 1;
			}
		});
		
		if (savedInstanceState != null) {
			// if not set, will default to 0, which is the default behavior
			// anyway
			mViewIndex = savedInstanceState.getInt(VIEW_INDEX);
			mFlipper.setDisplayedChild(mViewIndex);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt(VIEW_INDEX, mViewIndex);
	}

}
