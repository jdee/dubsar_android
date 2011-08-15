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

package com.dubsar_dictionary.Dubsar.test;

import android.content.Intent;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.dubsar_dictionary.Dubsar.DubsarContentProvider;
import com.dubsar_dictionary.Dubsar.R;
import com.dubsar_dictionary.Dubsar.SenseActivity;
import com.dubsar_dictionary.Dubsar.model.Model;

public class SenseActivityTest extends
		ActivityInstrumentationTestCase2<SenseActivity> {

	public SenseActivityTest() {
		super("com.dubsar_dictionary.Dubsar", SenseActivity.class);
	}
	
	protected void setUp() {
		Model.addMock("/senses/35629", "[35629,[26063,\"food\",\"n\"],[21803,\"sense gloss\"],\"noun.Tops\",null,29,[[35630,\"nutrient\",null,1]],[],[],[[\"hypernym\",\"synset\",21801,\"substance\",\"hypernym gloss\"]]]");

		// specify which sense we want to retrieve from the provider
    	Intent senseIntent = new Intent();
    	senseIntent.putExtra(DubsarContentProvider.SENSE_NAME_AND_POS, "food (n.)");
    	Uri data = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI,
                                        DubsarContentProvider.SENSES_URI_PATH + 
                                        "/35629");
        senseIntent.setData(data);
        setActivityIntent(senseIntent);
	}

	public void testActivity() {
		TextView title = (TextView)getActivity().findViewById(R.id.sense_title);
		TextView banner = (TextView)getActivity().findViewById(R.id.sense_banner);
		TextView gloss = (TextView)getActivity().findViewById(R.id.sense_gloss);
		
		try {
			Thread.sleep(2000);
		}
		catch (InterruptedException e) {
			fail("sleep interrupted");
		}
		
		assertEquals("food (n.)", title.getText());
		assertEquals("freq. cnt.: 29 <noun.Tops>", banner.getText());
		assertEquals("sense gloss", gloss.getText());
	}
}
