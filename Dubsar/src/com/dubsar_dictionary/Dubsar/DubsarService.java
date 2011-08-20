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

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.util.Log;

public class DubsarService extends Service {

	public static final int WOTD_ID=1;
	public static final int MILLIS_PER_DAY=86400000;
	
	public static final String ACTION_WOTD = "action_wotd";
	public static final String WOTD_TEXT = "wotd_text";

	private Timer mTimer=new Timer(true);
	private NotificationManager mNotificationMgr = null;
	private long mNextWotdTime=0;
	
	private int mWotdId=0;
	private String mWotdText=null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.i(getString(R.string.app_name), "DubsarService created");
		
		mNotificationMgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		
		computeNextWotdTime();
		
		if (mNextWotdTime - System.currentTimeMillis() > 2000) {
			/*
			 * If it's more than 2 seconds till the next WOTD, 
			 * request the last one immediately and set the time to 
			 * the time it was generated. 
			 */
			
			long lastWotdTime = mNextWotdTime - MILLIS_PER_DAY;
			mTimer.schedule(new WotdTimer(this, lastWotdTime), 0);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(getString(R.string.app_name), "DubsarService destroyed");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		Log.i(getString(R.string.app_name), "DubsarService received start command");
		
		/* schedule requests for WOTD once a day */
		mTimer.scheduleAtFixedRate(new WotdTimer(this), 
				mNextWotdTime - System.currentTimeMillis(),
				MILLIS_PER_DAY);
		
		generateBroadcast();
		
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	protected void saveResults(Cursor cursor) {
		if (cursor == null) return;
		
		int idColumn = cursor.getColumnIndex(BaseColumns._ID);
		int nameAndPosColumn = cursor.getColumnIndex(DubsarContentProvider.WORD_NAME_AND_POS);
		int freqCntColumn = cursor.getColumnIndex(DubsarContentProvider.WORD_FREQ_CNT);
		
		cursor.moveToFirst();
		
		mWotdId = cursor.getInt(idColumn);
		String nameAndPos = cursor.getString(nameAndPosColumn);
		
		int freqCnt = cursor.getInt(freqCntColumn);
		mWotdText = nameAndPos;
		if (freqCnt > 0) {
			mWotdText += " freq. cnt.:" + freqCnt;
		}
	}
	
	protected void generateNotification(long time) {
		Notification notification = new Notification(R.drawable.ic_dubsar_rounded,
				getString(R.string.dubsar_wotd), time);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		Intent wordIntent = new Intent(this, WordActivity.class);
		wordIntent.putExtra(DubsarContentProvider.WORD_NAME_AND_POS, mWotdText);
		Uri uri = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI, 
				DubsarContentProvider.WORDS_URI_PATH + "/" + mWotdId);
		wordIntent.setData(uri);
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, wordIntent, 0);
		
		notification.setLatestEventInfo(this, getString(R.string.dubsar_wotd), 
				getString(R.string.dubsar_wotd) + ": " + mWotdText, contentIntent);
		
		mNotificationMgr.notify(WOTD_ID, notification);
		
		generateBroadcast();
		
		computeNextWotdTime();
	}
	
	protected void generateBroadcast() {
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_WOTD);
		broadcastIntent.putExtra(BaseColumns._ID, mWotdId);
		broadcastIntent.putExtra(WOTD_TEXT, mWotdText);
		
		sendBroadcast(broadcastIntent);
	}
	
	protected void computeNextWotdTime() {
		Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		int _amPm = now.get(Calendar.AM_PM);
		int hour = now.get(Calendar.HOUR);
		int minute = now.get(Calendar.MINUTE);
		int second = now.get(Calendar.SECOND);
		
		if (_amPm == Calendar.PM) hour += 12;
		
		int secondsTillNext = (23-hour)*3600 + (59-minute)*60 + 60 - second;
		
		// add a 30-second pad
		secondsTillNext += 30;
		
		mNextWotdTime = now.getTimeInMillis() + secondsTillNext*1000;
	}
	
	static class WotdTimer extends TimerTask {
		
		private final WeakReference<DubsarService> mServiceReference;
		private long mWotdTime=0;
		
		public WotdTimer(DubsarService service) {
			mServiceReference = new WeakReference<DubsarService>(service);
		}
		
		public WotdTimer(DubsarService service, long wotdTime) {
			mServiceReference = new WeakReference<DubsarService>(service);
			mWotdTime = wotdTime;
		}
		
		public DubsarService getService() {
			return mServiceReference != null ? mServiceReference.get() : null;
		}

		@Override
		public void run() {
			if (getService() == null) return;
			
			Uri uri = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI, 
					DubsarContentProvider.WOTD_URI_PATH);
		
			ContentResolver resolver = getService().getContentResolver();
			Cursor cursor = resolver.query(uri, null, null, null, null);
			
			// the request should not take long, but since we have a weak
			// reference:
			if (getService() == null) return;
			
			long notificationTime = mWotdTime != 0 ? mWotdTime : System.currentTimeMillis();
			
			getService().saveResults(cursor);
			getService().generateNotification(notificationTime);

			cursor.close();
		}
	}
}
