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

		String[] args = new String[1];
		args[0] = uri.toString();
		Cursor cursor = resolver.query(uri, null, null, args, null);
		
		assertEquals("li", provider.getSearchTerm());
		
		assertNotNull(cursor);
		assertEquals(3, cursor.getCount());
	}
}
