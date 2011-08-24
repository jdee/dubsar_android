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
import java.util.Formatter;
import java.util.GregorianCalendar;
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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.util.Log;

public class DubsarService extends Service {

	public static final int WOTD_ID=1;
	public static final int MILLIS_PER_DAY=86400000;
	
	public static final String ACTION_WOTD = "action_wotd";
	public static final String ACTION_WOTD_NOTIFICATION = "action_wotd_notification";
	public static final String WOTD_TEXT = "wotd_text";
	public static final String ERROR_MESSAGE = "error_message";

	private Timer mTimer=new Timer();
	private NotificationManager mNotificationMgr=null;
	private ConnectivityManager mConnectivityMgr=null;
	private long mNextWotdTime=0;
	
	private volatile int mWotdId=0;
	private volatile String mWotdText=null;
	private volatile String mWotdNameAndPos=null;
	private volatile String mErrorMessage=null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.i(getString(R.string.app_name), "DubsarService created");
		
		mNotificationMgr =
				(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		mConnectivityMgr =
				(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		
		long nextWotdTime = computeNextWotdTime(getString(R.string.wotd_time_utc));
		
		if (nextWotdTime - System.currentTimeMillis() > 2000) {
			/*
			 * If it's more than 2 seconds till the next WOTD, 
			 * request the last one immediately and set the time to 
			 * the (approximate) time it was generated. 
			 */
			requestNow();
		}
		else {
			// setup the periodic request
			setNextWotdTime();
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

		if (intent == null || intent.getAction() == null) return START_STICKY;

		if (ACTION_WOTD_NOTIFICATION.equals(intent.getAction())) {
			long nextWotdTime = computeNextWotdTime(getString(R.string.wotd_time_utc));
			generateNotification(nextWotdTime-MILLIS_PER_DAY);
		}
		else if (ACTION_WOTD.equals(intent.getAction())) {
			generateBroadcast();
		}

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public boolean notificationsEnabled() {
		SharedPreferences preferences = getSharedPreferences(DubsarPreferences.DUBSAR_PREFERENCES, MODE_PRIVATE);
		return preferences.getBoolean(DubsarPreferences.WOTD_NOTIFICATIONS, true);
	}

	public boolean hasError() {
		return mErrorMessage != null;
	}
	
	public final String getErrorMessage() {
		return mErrorMessage;
	}
	
	public void setErrorMessage(String errorMessage) {
		mErrorMessage = errorMessage;
	}

	protected void clearError() {
		resetTimer();
		mErrorMessage = null;
	}

	protected void resetTimer() {
		mTimer.cancel();
		mTimer = new Timer();
	}

	/**
	 * Determine whether the network is currently available. There must
	 * be a better way to do this.
	 * @return true if the network is available; false otherwise
	 */
	protected boolean isNetworkAvailable() {
		NetworkInfo wifiInfo = mConnectivityMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobileInfo = mConnectivityMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		return wifiInfo.isConnected() || mobileInfo.isConnected();
	}
	
	protected void saveResults(Cursor cursor) {
		int idColumn = cursor.getColumnIndex(BaseColumns._ID);
		int nameAndPosColumn = cursor.getColumnIndex(DubsarContentProvider.WORD_NAME_AND_POS);
		int freqCntColumn = cursor.getColumnIndex(DubsarContentProvider.WORD_FREQ_CNT);
		
		cursor.moveToFirst();
		
		mWotdId = cursor.getInt(idColumn);
		mWotdNameAndPos = cursor.getString(nameAndPosColumn);
		
		int freqCnt = cursor.getInt(freqCntColumn);
		mWotdText = new String(mWotdNameAndPos);
		if (freqCnt > 0) {
			mWotdText += " freq. cnt.: " + freqCnt;
		}
		
		Log.d(getString(R.string.app_name), "WOTD ID = " + mWotdId);
		Log.d(getString(R.string.app_name), "WOTD TEXT = " + mWotdText);
		Log.d(getString(R.string.app_name), "WOTD NAME AND POS = " + mWotdNameAndPos);
	}
	
	protected void generateNotification(long time) {
		if (notificationsEnabled() && !hasError()) {
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
					mWotdText, contentIntent);
			
			mNotificationMgr.notify(WOTD_ID, notification);
		}
		
		generateBroadcast();
		
		setNextWotdTime();
	}

	protected void generateBroadcast() {
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_WOTD);
		if (!hasError()) {
			broadcastIntent.putExtra(BaseColumns._ID, mWotdId);
			broadcastIntent.putExtra(WOTD_TEXT, mWotdText);
			broadcastIntent.putExtra(DubsarContentProvider.WORD_NAME_AND_POS,
					mWotdNameAndPos);
		}
		else {
			broadcastIntent.putExtra(ERROR_MESSAGE, mErrorMessage);
		}
		
		sendStickyBroadcast(broadcastIntent);
	}

	/**
	 * Expects a 24-hour time in the form HH:MM (e.g., 14:00)
	 * @param timeOfDay the time of day to request the WOTD each day
	 */
	protected void setNextWotdTime() {
		mNextWotdTime = computeNextWotdTime(getString(R.string.wotd_time_utc));
		
		mTimer.schedule(new WotdTimer(this), mNextWotdTime - System.currentTimeMillis());
		
		StringBuffer output = new StringBuffer();
		Formatter formatter = new Formatter(output);
		formatter.format("%tY-%tm-%td %tT", mNextWotdTime, mNextWotdTime, mNextWotdTime, 
				mNextWotdTime);
		Log.i(getString(R.string.app_name), "Next WOTD at " + output);
	}
	
	public static long computeNextWotdTime(String timeOfDay) {
		// TODO: better error handling
		int hourUtc=19;
		int minuteUtc=0;
		int index = timeOfDay.indexOf(":");
		if (index != -1) {
			hourUtc = Integer.parseInt(timeOfDay.substring(0,index));
			minuteUtc = Integer.parseInt(timeOfDay.substring(index+1,index+3));
		}

		Calendar time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		int _amPm = time.get(Calendar.AM_PM);
		int hour = time.get(Calendar.HOUR);
		int minute = time.get(Calendar.MINUTE);
		int second = time.get(Calendar.SECOND);
		int millis = time.get(Calendar.MILLISECOND);
		
		if (_amPm == Calendar.PM) hour += 12;
		
		/* 
		 * This may be an earlier time of day (e.g., the time is now 19:30,
		 * and the timer is set to fire at 14:00.
		 */
		if (hour > hourUtc ||
		    (hour == hourUtc && minute >= minuteUtc)) hourUtc += 24;
		
		long millisTillNext = (hourUtc-hour)*3600000 + 
				(minuteUtc-minute-1)*60000 + 
				(59-second)*1000 +
				(1000-millis);
		
		time.setTimeInMillis(time.getTimeInMillis() + millisTillNext);
		
		return time.getTimeInMillis();
	}
	
	protected void requestNow() {
		/*
		 * Use the TimerTask as an AsyncTask, in effect.
		 */
		long lastWotdTime = mNextWotdTime - MILLIS_PER_DAY;
		mTimer.schedule(new WotdTimer(this, lastWotdTime), 0);
	}

	protected void startRerequesting() {
		resetTimer();

		// begin rechecking every 5 seconds
		mTimer.scheduleAtFixedRate(new WotdTimer(this), 5000, 5000);
	}

	protected void noNetworkError() {
		if (!hasError() || !mErrorMessage.equals(getString(R.string.no_network))) {
			mErrorMessage = getString(R.string.no_network);
			Log.d(getString(R.string.app_name), mErrorMessage);

			generateBroadcast();
			startRerequesting();
		}
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
			StringBuffer output = new StringBuffer();
			Formatter formatter = new Formatter(output);
			Calendar cal = new GregorianCalendar();
			formatter.format("%tY-%tm-%td %tT", cal, cal, cal, cal);
			
			Log.d("Dubsar", output + ": timer fired");
			if (getService() == null) return;

			if (!getService().isNetworkAvailable()) {
				getService().noNetworkError();
				return;
			}
			/* If I'm recovering from a network outage, clear my state */
			else if (getService().hasError() &&
				getService().getErrorMessage().equals(getService().getString(R.string.no_network))) {
				getService().clearError();
				getService().setNextWotdTime();
				
				// TODO: avoid out-of-order responses in the event that
				// we recover just before a transition (cf. onCreate)
			}

			Uri uri = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI, 
					DubsarContentProvider.WOTD_URI_PATH);
		
			Log.d(getService().getString(R.string.app_name), "requesting WOTD, URI " + uri);
			
			ContentResolver resolver = getService().getContentResolver();
			Cursor cursor = resolver.query(uri, null, null, null, null);
			
			Log.d("Dubsar", "request completed");
			
			// the request should not take long, but since we have a weak
			// reference:
			if (getService() == null) return;
			
			if (cursor == null) {
				if (!getService().hasError() || !getService().getErrorMessage().equals(R.string.search_error)) {
					getService().setErrorMessage(getService().getString(R.string.search_error));
					getService().generateBroadcast();
					getService().startRerequesting();
				}
				return;
			}
			
			/* Success */
			
			/* If I'm recovering from a search error, reset my state */
			if (getService().hasError()) {
				getService().clearError();
				getService().setNextWotdTime();
			}
			
			long notificationTime = mWotdTime != 0 ? mWotdTime : System.currentTimeMillis();
			
			getService().saveResults(cursor);
			getService().generateNotification(notificationTime);

			cursor.close();
		}
	}
}
