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

import android.app.SearchManager;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.test.ProviderTestCase2;

import com.dubsar_dictionary.Dubsar.DubsarContentProvider;
import com.dubsar_dictionary.Dubsar.model.Model;

public class DubsarContentProviderTest extends ProviderTestCase2<DubsarContentProvider> {

	public DubsarContentProviderTest() {
	    super(DubsarContentProvider.class, "com.dubsar_dictionary.Dubsar.DubsarContentProvider");
	}

	public void testCompletion() {
		ContentResolver resolver = getMockContentResolver();
		
		Uri.Builder builder = new Uri.Builder();
		builder.scheme("content");
		builder.authority(DubsarContentProvider.AUTHORITY);
		builder.path(SearchManager.SUGGEST_URI_PATH_QUERY + "/li");

		Uri uri = builder.build();
		
		DubsarContentProvider provider = getProvider();
		assertEquals(provider.getType(uri), SearchManager.SUGGEST_MIME_TYPE);
		
		Model.addMock("/os?term=li", "[ \"li\", [ \"like\", \"link\", \"lion\" ] ]");

		Cursor cursor = resolver.query(uri, null, null, new String[]{"li"}, null);
		
		assertEquals("li", provider.getSearchTerm());
		
		assertNotNull(cursor);
		assertEquals(3, cursor.getCount());
		assertTrue("provider queries must all include BaseColumns._ID", -1 != cursor.getColumnIndex(BaseColumns._ID));
	}
	
	public void testSearch() {
		ContentResolver resolver = getMockContentResolver();
		
		Uri.Builder builder = new Uri.Builder();
		builder.scheme("content");
		builder.authority(DubsarContentProvider.AUTHORITY);
		builder.path(DubsarContentProvider.SEARCH_URI_PATH);
		
		Uri uri = builder.build();
		
		DubsarContentProvider provider = getProvider();
		assertEquals(provider.getType(uri), DubsarContentProvider.SEARCH_MIME_TYPE);
		
		Model.addMock("/?term=already",
				"[\"already\",[[21774,\"already\",\"adv\",107,\"\"]],1]");
		
		String[] args = new String[1];
		args[0] = "already";
		
		Cursor cursor = resolver.query(uri, null, null, args, null);
		
		assertEquals("already", provider.getSearchTerm());
		assertNotNull(cursor);
		assertEquals(1, cursor.getCount());
		assertTrue("provider queries must all include BaseColumns._ID", -1 != cursor.getColumnIndex(BaseColumns._ID));
	}
	
	public void testWord() {
		ContentResolver resolver = getMockContentResolver();
		
		Uri.Builder builder = new Uri.Builder();
		builder.scheme("content");
		builder.authority(DubsarContentProvider.AUTHORITY);
		builder.path(DubsarContentProvider.WORDS_URI_PATH + "/21774");
		
		Uri uri = builder.build();
		
		DubsarContentProvider provider = getProvider();
		assertEquals(provider.getType(uri), DubsarContentProvider.WORD_MIME_TYPE);
		
		Model.addMock("/words/21774",
				"[21774,\"already\",\"adv\",\"\",[[30315,[],\"prior to a specified or implied time\",\"adv.all\",null,107]],107]");
		
		Cursor cursor = resolver.query(uri, null, null, null, null);
		
		assertNotNull(cursor);
		assertEquals(1, cursor.getCount());
		assertTrue("provider queries must all include BaseColumns._ID", -1 != cursor.getColumnIndex(BaseColumns._ID));
		assertTrue("must include word_subtitle", -1 != cursor.getColumnIndex(DubsarContentProvider.WORD_SUBTITLE));
	}
	
	public void testWotd() {
		ContentResolver resolver = getMockContentResolver();
		
		Uri.Builder builder = new Uri.Builder();
		builder.scheme("content");
		builder.authority(DubsarContentProvider.AUTHORITY);
		builder.path(DubsarContentProvider.WOTD_URI_PATH);
		
		Uri uri = builder.build();
		
		DubsarContentProvider provider = getProvider();
		assertEquals(provider.getType(uri), DubsarContentProvider.SEARCH_MIME_TYPE);
		
		Model.addMock("/wotd",
				"[25441,\"resourcefully\",\"adv\",0,\"\"]");
		
		Cursor cursor = resolver.query(uri, null, null, null, null);
		
		assertNotNull(cursor);
		assertEquals(1, cursor.getCount());
		assertTrue("provider queries must all include BaseColumns._ID", -1 != cursor.getColumnIndex(BaseColumns._ID));
		
	}
	
	public void testSense() {
		ContentResolver resolver = getMockContentResolver();
		
		Uri uri = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI, 
				DubsarContentProvider.SENSES_URI_PATH + "/35629");
		
		DubsarContentProvider provider = getProvider();
		assertEquals(provider.getType(uri), DubsarContentProvider.SENSE_MIME_TYPE);
		
		Model.addMock("/senses/35629", "[35629,[26063,\"food\",\"n\"],[21803,\"sense gloss\"],\"noun.Tops\",null,29,[[35630,\"nutrient\",null,1]],[],[],[[\"hypernym\",\"synset\",21801,\"substance\",\"hypernym gloss\"]]]");
		
		Cursor cursor = resolver.query(uri, null, null, null, null);
		
		assertNotNull(cursor);
		assertEquals(1, cursor.getCount());
		assertTrue("provider queries must all include BaseColumns._ID", -1 != cursor.getColumnIndex(BaseColumns._ID));
	}
}
