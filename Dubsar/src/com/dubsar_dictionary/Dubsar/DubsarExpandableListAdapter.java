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

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class DubsarExpandableListAdapter extends BaseExpandableListAdapter {
	public static final String SYNONYM_LABEL = "Synonyms";
	public static final String VERB_FRAME_LABEL = "Verb Frames";
	public static final String SAMPLE_LABEL = "Sample Sentences";
	
	private Activity mActivity=null;
	private Cursor mCursor=null;
	
	private ArrayList<Group> mGroups = new ArrayList<Group>();
	
	public DubsarExpandableListAdapter(Activity activity, Cursor cursor) {
		mActivity = activity;
		mCursor = cursor;
	}
	
	public Context getContext() {
		return mActivity;
	}
	
	public final Cursor getCursor() {
		return mCursor;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mGroups.get(groupPosition).getChild(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return mGroups.get(groupPosition).getChild(childPosition).getId();
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		Group group = mGroups.get(groupPosition);
		
		switch (group.getType()) {
		case Pointer:
			return pointerView(convertView, group, childPosition);
		case Sample:
			return sampleView(convertView, group, childPosition);
		default:
			return null;
		}
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mGroups.get(groupPosition).getChildCount();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mGroups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mGroups.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.list_label, null);
		}
		TextView listLabel = (TextView)convertView.findViewById(R.id.list_label);
		listLabel.setText(mGroups.get(groupPosition).getName());
		listLabel.setBackgroundColor(Color.rgb(0xf5, 0x84, 0x00));

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		switch (mGroups.get(groupPosition).getType()) {
		case Pointer:
			return true;
		}
		
		return false;
	}

	protected View pointerView(View convertView, Group group, int childPosition) {
		
		if (convertView == null || convertView.findViewById(R.id.pointer_text) == null) {
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.pointer, null);
		}
		
		TextView title = (TextView)convertView.findViewById(R.id.pointer_text);
		TextView subtitle = (TextView)convertView.findViewById(R.id.pointer_subtitle);
		
		Child child = group.getChild(childPosition);
		title.setText(child.getTitle());
		subtitle.setText(child.getSubtitle());
		
		final long pointerId = child.getId();
		final String nameAndPos = child.getNameAndPos();
		convertView.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
            	
				Intent senseIntent = new Intent(mActivity.getApplicationContext(), SenseActivity.class);
				senseIntent.putExtra(DubsarContentProvider.SENSE_NAME_AND_POS, nameAndPos);
				
				Uri data = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI,
				                        DubsarContentProvider.SENSES_URI_PATH + 
				                        "/" + pointerId);
				senseIntent.setData(data);
				mActivity.startActivity(senseIntent);
				
			}
		});
		
		return convertView;
	}

	protected View sampleView(View convertView, Group group, int childPosition) {
		
		if (convertView == null || convertView.findViewById(R.id.sample) == null) {
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.sample, null);
		}
		
		TextView sample = (TextView)convertView.findViewById(R.id.sample);
		sample.setText(group.getChild(childPosition).getTitle());
		
		return convertView;
	}
	
	protected void addGroup(Group group) {
		mGroups.add(group);
	}
	
	enum GroupType {
		Unknown,
		Sample,
		Pointer
	};
	
	class Group {
		private GroupType mType=GroupType.Unknown;
		private String mName=null;
		private ArrayList<Child> mChildren=new ArrayList<Child>();
		
		public Group(GroupType type, String name) {
			mType = type;
			mName = name;
		}
		
		public GroupType getType() {
			return mType;
		}
		
		public final String getName() {
			return mName;
		}
		
		public int getChildCount() {
			return mChildren.size();
		}
		
		public Child getChild(int childPosition) {
			return mChildren.get(childPosition);
		}
		
		public void addChild(Child child) {
			mChildren.add(child);
		}
	}
	
	class Child {
		private Group mGroup=null;
		private final String mTitle;
		private final String mSubtitle;
		private long mId;
		private String mNameAndPos;
		
		public Child(Group group, String title, String subtitle, long id) {
			mGroup = group;
			mTitle = title;
			mSubtitle = subtitle;
			mId = id;
		}
		
		public Group getGroup() {
			return mGroup;
		}
		
		public GroupType getGroupType() {
			return mGroup.getType();
		}
		
		public final String getTitle() {
			return mTitle;
		}
		
		public final String getSubtitle() {
			return mSubtitle;
		}
		
		public long getId() {
			return mId;
		}
		
		public final String getNameAndPos() {
			return mNameAndPos;
		}
		
		public void setNameAndPos(String nameAndPos) {
			mNameAndPos = nameAndPos;
		}
	}
}
