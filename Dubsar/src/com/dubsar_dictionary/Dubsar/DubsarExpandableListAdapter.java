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

package com.dubsar_dictionary.Dubsar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.dubsar_dictionary.Dubsar.model.PointerDictionary;

public class DubsarExpandableListAdapter extends BaseExpandableListAdapter {
	public static final String SYNONYM_LABEL = "Synonyms";
	public static final String VERB_FRAME_LABEL = "Verb Frames";
	public static final String SAMPLE_LABEL = "Sample Sentences";
	
	public static final String SYNONYM_HELP = "words that share this meaning";
	public static final String SAMPLE_HELP = "examples of usage for this word and syonyms";
	
	private final WeakReference<Activity> mActivityReference;
	private Cursor mCursor=null;
	
	private ArrayList<Group> mGroups = new ArrayList<Group>();
	private boolean[] mExpanded = null;
	
	protected DubsarExpandableListAdapter(Activity activity, Cursor cursor) {
		mActivityReference = new WeakReference<Activity>(activity);
		mCursor = cursor;
	}
	
	public Context getContext() {
		if (getActivity() == null) return null;
		return getActivity().getApplicationContext();
	}
	
	protected Activity getActivity() {
		return mActivityReference != null ? mActivityReference.get() : null;
	}
	
	public final Cursor getCursor() {
		return mCursor;
	}
	
	public final boolean[] getExpanded() {
		return mExpanded;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mGroups != null ? mGroups.get(groupPosition).getChild(childPosition) : null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return mGroups != null ? mGroups.get(groupPosition).getChild(childPosition).getId() : 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		
		if (mGroups == null) {
			return null;
		}

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
		return mGroups != null ? mGroups.get(groupPosition) : null;
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
		if (getContext() == null) return null;
		
		LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		int layout = isExpanded ? 
				R.layout.expanded_list_label : R.layout.list_label;
		
		convertView = inflater.inflate(layout, null);

		TextView listLabel = (TextView)convertView.findViewById(R.id.list_label);
		listLabel.setText(mGroups.get(groupPosition).getName());
		
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
		default:
			break;
		}
		
		return false;
	}

	@Override
	public void onGroupCollapsed(int groupPosition) {
		super.onGroupCollapsed(groupPosition);
		mExpanded[groupPosition] = false;
	}

	@Override
	public void onGroupExpanded(int groupPosition) {
		super.onGroupExpanded(groupPosition);
		mExpanded[groupPosition] = true;
		mGroups.get(groupPosition).getHelp().show();
	}
	
	protected void setupExpandedList() {
		mExpanded = new boolean[mGroups.size()];
		for (int j=0; j<mGroups.size(); ++j) {
			mExpanded[j] = false;
		}
	}

	protected View pointerView(View convertView, Group group, int childPosition) {
		if (getContext() == null) return null;
		
		if (convertView == null || convertView.findViewById(R.id.pointer_text) == null) {
			LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.pointer, null);
		}
		
		TextView title = (TextView)convertView.findViewById(R.id.pointer_text);
		TextView subtitle = (TextView)convertView.findViewById(R.id.pointer_subtitle);
		
		final Child child = group.getChild(childPosition);
		title.setText(child.getTitle());
		subtitle.setText(child.getSubtitle());
		
		final String nameAndPos = child.getNameAndPos();
		convertView.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				
				if (getContext() == null) return;

				Intent intent;
				if (child.getPath().equals(DubsarContentProvider.SENSES_URI_PATH)) {
					intent = new Intent(getContext(), SenseActivity.class);
					if (nameAndPos != null) {
						intent.putExtra(DubsarContentProvider.SENSE_NAME_AND_POS, nameAndPos);
					}
				}
				else if (child.getPath().equals(DubsarContentProvider.SYNSETS_URI_PATH)) {
					intent = new Intent(getContext(), SynsetActivity.class);
				}
				else {
					return;
				}
				
				Uri data = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI,
				                        child.getPath() + "/" + child.getId());
				intent.setData(data);
				
				// if getActivity() == null, then getContext() == null,
				// so the check at the beginning of the method covers this
				// issue (unlikely since they user just tapped something)
				getActivity().startActivity(intent);
				
			}
		});
		
		return convertView;
	}

	protected View sampleView(View convertView, Group group, int childPosition) {
		
		if (getContext() == null) return null;
		
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
	
	protected void buildPointers(int firstRow, int numRows) {
		Group group = null;
		
		getCursor().moveToPosition(firstRow);
		
		while (getCursor().getPosition() < firstRow + numRows) {
			int ptypeColumn = getCursor().getColumnIndex(DubsarContentProvider.POINTER_TYPE);
			int targetTypeColumn = getCursor().getColumnIndex(DubsarContentProvider.POINTER_TARGET_TYPE);
			int targetIdColumn = getCursor().getColumnIndex(DubsarContentProvider.POINTER_TARGET_ID);
			int targetTextColumn = getCursor().getColumnIndex(DubsarContentProvider.POINTER_TARGET_TEXT);
			int targetGlossColumn = getCursor().getColumnIndex(DubsarContentProvider.POINTER_TARGET_GLOSS);
			
			String ptype = getCursor().getString(ptypeColumn);
			String label = PointerDictionary.labelFromPtype(ptype);
			if (group == null || !label.equals(group.getName())) {
				group = new Group(GroupType.Pointer, label, 
						PointerDictionary.helpFromPtype(ptype));
				addGroup(group);
			}
			
			String targetType = getCursor().getString(targetTypeColumn);
			int targetId = getCursor().getInt(targetIdColumn);
			String targetText = getCursor().getString(targetTextColumn);
			String targetGloss = getCursor().getString(targetGlossColumn);
			
			Child child = new Child(group, targetText, targetGloss, targetId);
			child.setPath(targetType + "s");
			group.addChild(child);
			
			getCursor().moveToNext();
		}
	}
	
	enum GroupType {
		Unknown,
		Sample,
		Pointer
	};
	
	class Group {
		private GroupType mType=GroupType.Unknown;
		private String mName=null;
		private Toast mHelp=null;
		private ArrayList<Child> mChildren=new ArrayList<Child>();
		
		public Group(GroupType type, String name, String help) {
			mType = type;
			mName = name;
			
			if (getContext() != null) {
				LayoutInflater inflater = 
						(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View toastView = inflater.inflate(R.layout.toast, null);
				TextView toastText = (TextView)toastView.findViewById(R.id.toast);

				mHelp = new Toast(getContext());
				toastText.setText(help);
				
				mHelp.setView(toastView);
			}
		}
		
		public GroupType getType() {
			return mType;
		}
		
		public final String getName() {
			return mName;
		}
		
		public final Toast getHelp() {
			return mHelp;
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
		private String mPath;
		
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
		
		public final String getPath() {
			return mPath;
		}
		
		public void setPath(String path) {
			mPath = path;
		}
	}
}
