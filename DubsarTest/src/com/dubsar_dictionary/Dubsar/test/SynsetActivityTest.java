/*
 Dubsar Dictionary Project
 Copyright (C) 2010-13 Jimmy Dee
 
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

package com.dubsar_dictionary.Dubsar.test;

import android.content.Intent;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.dubsar_dictionary.Dubsar.DubsarContentProvider;
import com.dubsar_dictionary.Dubsar.R;
import com.dubsar_dictionary.Dubsar.SynsetActivity;
import com.dubsar_dictionary.Dubsar.model.Model;

public class SynsetActivityTest extends ActivityInstrumentationTestCase2<SynsetActivity> {
	
	public SynsetActivityTest() {
		super("com.dubsar_dictionary.Dubsar", SynsetActivity.class);
	}
	
	protected void setUp() {
		Model.addMock("/synsets/21803", "[21803,\"n\",\"noun.Tops\",\"synset gloss\",[],[[35629,\"food\",null,29],[35630,\"nutrient\",null,1]],30,[[\"hypernym\",\"synset\",21801,\"substance\",\"hypernym gloss\"]]]");

		// specify which synset we want to retrieve from the provider
    	Intent synsetIntent = new Intent();
    	Uri data = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI,
                                        DubsarContentProvider.SYNSETS_URI_PATH + 
                                        "/21803");
        synsetIntent.setData(data);
        setActivityIntent(synsetIntent);
	}
	
	protected void tearDown() {
		TestUtils.cleanupAfterService(getActivity());
	}
	
	public void testActivity() {
		TextView gloss = (TextView)getActivity().findViewById(R.id.synset_gloss);
		TextView banner = (TextView)getActivity().findViewById(R.id.synset_banner);
		
		try {
			Thread.sleep(2000);
		}
		catch (InterruptedException e) {
			fail("sleep interrupted");
		}
		
		assertEquals("synset gloss", gloss.getText());
		assertEquals("freq. cnt.: 30 <noun.Tops>", banner.getText());
	}

}
