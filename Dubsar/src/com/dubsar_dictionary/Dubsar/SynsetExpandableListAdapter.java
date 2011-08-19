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

public class SynsetExpandableListAdapter extends DubsarExpandableListAdapter {
	
	public SynsetExpandableListAdapter(Activity activity, Cursor cursor) {
		super(activity, cursor);
		
		if (getCursor() == null) {
			return;
		}
		
		buildGroups();
		setupExpandedList();
	}
	
	protected void buildGroups() {
		int sampleCountColumn = getCursor().getColumnIndex(DubsarContentProvider.SYNSET_SAMPLE_COUNT);
		int senseCountColumn = getCursor().getColumnIndex(DubsarContentProvider.SYNSET_SENSE_COUNT);
		int pointerCountColumn = getCursor().getColumnIndex(DubsarContentProvider.POINTER_COUNT);
		
		getCursor().moveToFirst();
		int sampleCount = getCursor().getInt(sampleCountColumn);
		int senseCount = getCursor().getInt(senseCountColumn);
		int pointerCount = getCursor().getInt(pointerCountColumn);
		
		if (sampleCount + senseCount + pointerCount == 0) return;
		
		if (senseCount > 0) {
			buildSenses(sampleCount, senseCount);
		}
		
		if (sampleCount > 0) {
			buildSamples(0, sampleCount);
		}
		
		if (pointerCount > 0) {
			buildPointers(sampleCount+senseCount, pointerCount);
		}
	}
	
	protected void buildSamples(int firstRow, int numRows) {
		Group group = new Group(GroupType.Sample, SAMPLE_LABEL, SAMPLE_HELP);
		
		getCursor().moveToPosition(firstRow);
		
		int idColumn = getCursor().getColumnIndex(BaseColumns._ID);
		int textColumn = getCursor().getColumnIndex(DubsarContentProvider.SYNSET_SAMPLE);
		
		while (getCursor().getPosition() < firstRow + numRows) {
			long id = getCursor().getInt(idColumn);
			String text = getCursor().getString(textColumn);
			
			Child child = new Child(group, text, null, id);
			group.addChild(child);

			getCursor().moveToNext();
		}
		
		addGroup(group);		
	}
	
	protected void buildSenses(int firstRow, int numRows) {
		Group group = new Group(GroupType.Pointer, SYNONYM_LABEL, SYNONYM_HELP);
		
		getCursor().moveToPosition(firstRow);
		
		int idColumn = getCursor().getColumnIndex(BaseColumns._ID);
		int nameColumn = getCursor().getColumnIndex(DubsarContentProvider.SENSE_NAME);
		int markerColumn = getCursor().getColumnIndex(DubsarContentProvider.SENSE_MARKER);
		int freqCntColumn = getCursor().getColumnIndex(DubsarContentProvider.SENSE_FREQ_CNT);
		
		int posColumn = getCursor().getColumnIndex(DubsarContentProvider.SYNSET_POS);
		String pos = getCursor().getString(posColumn);
		
		while (getCursor().getPosition() < firstRow + numRows) {
			final long id = getCursor().getInt(idColumn);
		
			String name = getCursor().getString(nameColumn);
			String marker = getCursor().getString(markerColumn);
			int freqCnt = getCursor().getInt(freqCntColumn);
			
			String subtitle = "";
			if (freqCnt > 0) {
				subtitle += "freq. cnt.: " + freqCnt + " ";
			}
			
			if (marker != null) {
				subtitle = "(" + marker + ")";
			}
			
			Child child = new Child(group, name, subtitle.trim(), id);
			child.setNameAndPos(name + " (" + pos + ".)");
			child.setPath("senses");
			group.addChild(child);

			getCursor().moveToNext();
		}
		
		addGroup(group);		
	}

}
