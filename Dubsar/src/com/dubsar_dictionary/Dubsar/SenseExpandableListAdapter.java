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
import android.database.Cursor;
import android.provider.BaseColumns;

public class SenseExpandableListAdapter extends DubsarExpandableListAdapter {
	
	public SenseExpandableListAdapter(Activity activity, Cursor cursor) {
		super(activity, cursor);
		
		buildGroups();
	}

	protected void buildGroups() {
		int synonymCountColumn = getCursor().getColumnIndex(DubsarContentProvider.SENSE_SYNONYM_COUNT);
		int verbFrameCountColumn = getCursor().getColumnIndex(DubsarContentProvider.SENSE_VERB_FRAME_COUNT);
		int sampleCountColumn = getCursor().getColumnIndex(DubsarContentProvider.SENSE_SAMPLE_COUNT);

		int synonymCount = getCursor().getInt(synonymCountColumn);
		int verbFrameCount = getCursor().getInt(verbFrameCountColumn);
		int sampleCount = getCursor().getInt(sampleCountColumn);

		if (synonymCount + verbFrameCount + sampleCount == 0) return;
		
		if (synonymCount > 0) {
			buildSynonyms(0, synonymCount);
		}
		
		if (verbFrameCount > 0) {
			buildVerbFrames(synonymCount, verbFrameCount);
		}
		
		if (sampleCount > 0) {
			buildSamples(synonymCount+verbFrameCount, sampleCount);
		}
	}
	
	protected void buildSynonyms(int firstRow, int numRows) {
		Group group = new Group(GroupType.Pointer, SYNONYM_LABEL);
		
		getCursor().moveToPosition(firstRow);
		
		int synonymIdColumn = getCursor().getColumnIndex(BaseColumns._ID);
		int synonymNameColumn = getCursor().getColumnIndex(DubsarContentProvider.SENSE_SYNONYM);
		int synonymMarkerColumn = getCursor().getColumnIndex(DubsarContentProvider.SENSE_SYNONYM_MARKER);
		
		int posColumn = getCursor().getColumnIndex(DubsarContentProvider.SENSE_POS);
		String pos = getCursor().getString(posColumn);
		
		while (getCursor().getPosition() < firstRow + numRows) {
			final long synonymId = getCursor().getInt(synonymIdColumn);
		
			String synonymName = getCursor().getString(synonymNameColumn);
			String synonymMarker = getCursor().getString(synonymMarkerColumn);
			
			String marker = "";
			if (synonymMarker != null) {
				marker = "(" + synonymMarker + ")";
			}
			
			Child child = new Child(group, synonymName, marker, synonymId);
			child.setNameAndPos(synonymName + " (" + pos + ".)");
			group.addChild(child);

			getCursor().moveToNext();
		}
		
		addGroup(group);		
	}
	
	protected void buildVerbFrames(int firstRow, int numRows) {
		Group group = new Group(GroupType.Sample, VERB_FRAME_LABEL);
		
		getCursor().moveToPosition(firstRow);
		
		int idColumn = getCursor().getColumnIndex(BaseColumns._ID);
		int textColumn = getCursor().getColumnIndex(DubsarContentProvider.SENSE_VERB_FRAME);
		
		while (getCursor().getPosition() < firstRow + numRows) {
			long id = getCursor().getInt(idColumn);
			String text = getCursor().getString(textColumn);
			
			Child child = new Child(group, text, null, id);
			group.addChild(child);

			getCursor().moveToNext();
		}
		
		addGroup(group);		
	}
	
	protected void buildSamples(int firstRow, int numRows) {
		Group group = new Group(GroupType.Sample, SAMPLE_LABEL);
		
		getCursor().moveToPosition(firstRow);
		
		int idColumn = getCursor().getColumnIndex(BaseColumns._ID);
		int textColumn = getCursor().getColumnIndex(DubsarContentProvider.SENSE_SAMPLE);
		
		while (getCursor().getPosition() < firstRow + numRows) {
			long id = getCursor().getInt(idColumn);
			String text = getCursor().getString(textColumn);
			
			Child child = new Child(group, text, null, id);
			group.addChild(child);

			getCursor().moveToNext();
		}
		
		addGroup(group);		
	}
}
