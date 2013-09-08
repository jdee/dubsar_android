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

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.SecureRandom;
import java.util.Formatter;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.util.Log;

public class DubsarService extends Service {
	public static final String TAG = "DubsarService";

	public static final int WOTD_ID=1;
	public static final int MILLIS_PER_DAY=86400000;
	
	public static final String ACTION_WOTD = "com.dubsar_dictionary.WOTD";
	public static final String ACTION_WOTD_NOTIFICATION = "com.dubsar_dictionary.WOTD_NOTIFICATION";
	public static final String ACTION_WOTD_TIME = "com.dubsar_dictionary.WOTD_TIME";
	public static final String ACTION_WOTD_PURGE = "com.dubsar_dictionary.WOTD_PURGE";
	public static final String ACTION_WOTD_MOCK = "com.dubsar_dictionary.WOTD_MOCK";
	public static final String WOTD_TEXT = "wotd_text";
	public static final String ERROR_MESSAGE = "error_message";
	
	public static final String WOTD_FILE_NAME = "wotd.dat";

	private volatile NotificationManager mNotificationMgr=null;
	
	private volatile int mWotdId=0;
	private volatile String mWotdText=null;
	private volatile String mWotdNameAndPos=null;
	private volatile String mErrorMessage=null;
	private volatile long mExpirationMillis=0;
	private volatile boolean mRequestPending = false;
	
	private boolean mTestMode=false;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.i(TAG, getTimestamp() + ": DubsarService created");
		
		mNotificationMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		loadWotdData();

		/*
		 * mNextWotdTime is 0 on error or it holds the value of mNextWotdTime
		 * from the last time the service ran. If this is the first time the
		 * application has run, mNextWotdTime will be 0, since the file won't
		 * exist.
		 */
		
		Log.d(TAG, "in onCreate: mExpirationMillis = " + formatTime(mExpirationMillis));
		if (mExpirationMillis > 0) {
			setAlarm();
		}

		Log.d(TAG, "Finished onCreate()");
	}

	@Override
	public void onDestroy() {
		// Log.i(getString(R.string.app_name), getTimestamp() + ": DubsarService destroyed");
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.i(TAG, getTimestamp() + ": start command received, action=" +
				(intent != null ? intent.getAction() : "(null intent)"));
		
		/*
		 * Special non-sticky actions for testing and maintenance.
		 */
		
		/* Ignore redelivery and retry of purge intents */
		if (flags == 0 && intent != null && intent.getAction() != null &&
			intent.getAction().equals(ACTION_WOTD_PURGE)) {
			// Log.d(getString(R.string.app_name), "WOTD PURGE");

			purgeData();
			return START_NOT_STICKY;
		}
		/* mock for test mode */
		else if (intent != null && intent.getAction() != null &&
			intent.getAction().equals(ACTION_WOTD_MOCK)) {
			setupMock(intent);
			return START_NOT_STICKY;
		}
		else if (mTestMode) {
			return START_NOT_STICKY;
		}

		/*
		 * Issue #5 (https://github.com/jdee/dubsar_android/issues/5)
		 * The timer frequently does not fire if the service has been running
		 * for a long time. As a workaround, we reset the timer any time we
		 * receive any start command. If the service is running, but the app
		 * is not, if the user starts the app, this will have the effect of
		 * kicking the timer. We also check to see if the timer didn't fire.
		 */

		/*
		 * Check to see if the timer has fired. We add a 2-second cushion
		 * to avoid a duplicate request, in the event that the timer just fired
		 * now. mNextWotdTime is updated whenever a response is received.
		 */
		boolean requestNow = !mRequestPending && (hasError() || mExpirationMillis == 0 || mExpirationMillis <= System.currentTimeMillis());

		/*
		 * Now make the immediate request if the data are not current, and the
		 * timer we just set is not about to fire, or if background data usage
		 * is turned off, in which case there's no timer, and this is our 
		 * chance to do it in the foreground (more or less).
		 */
		if (requestNow) {
			Log.d(TAG,
				"requesting now; mExpirationMillis: " + formatTime(mExpirationMillis));
			requestNow();
		}

		if (intent == null || intent.getAction() == null) {
			return START_NOT_STICKY;
		}

		/*
		 * If we just requested this, don't bother.
		 */
		if (!requestNow && ACTION_WOTD_NOTIFICATION.equals(intent.getAction())) {
			/*
			 * If in an error state, and the user activates notifications,
			 * we pop up the last WOTD.
			 */
			generateNotification();
		}
		else if (ACTION_WOTD.equals(intent.getAction())) {
			Log.d(TAG, "only broadcasting");
			generateBroadcast();
		}
		
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onLowMemory() {
		/* hardly a mission-critical component */
		/*
		Log.i(getString(R.string.app_name), getTimestamp() +
				": Exiting due to low memory.");
		 */
		stopSelf();
	}
	
	public int getWotdHour() {
		SharedPreferences preferences =
				getSharedPreferences(PreferencesActivity.DUBSAR_PREFERENCES, MODE_PRIVATE);
		return preferences.getInt(PreferencesActivity.WOTD_HOUR,
				PreferencesActivity.WOTD_HOUR_DEFAULT);		
	}
	
	public int getWotdMinute() {
		SharedPreferences preferences =
				getSharedPreferences(PreferencesActivity.DUBSAR_PREFERENCES, MODE_PRIVATE);
		return preferences.getInt(PreferencesActivity.WOTD_MINUTE,
				PreferencesActivity.WOTD_MINUTE_DEFAULT);		
	}

	public boolean notificationsEnabled() {
		SharedPreferences preferences =
				getSharedPreferences(PreferencesActivity.DUBSAR_PREFERENCES, MODE_PRIVATE);
		return preferences.getBoolean(PreferencesActivity.WOTD_NOTIFICATIONS, PreferencesActivity.WOTD_DEFAULT_NOTIFICATION_SETTING);
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
	
	public boolean isNetworkAvailable() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		return info != null && info.isConnected();
	}

	protected void setAlarm() {
		Intent serviceIntent = new Intent(getApplicationContext(), DubsarService.class);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, serviceIntent, 0);
		AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

		// pad by between 2 and 32 s
		SecureRandom sr = new SecureRandom();
		byte[] buffer = new byte[8];
		sr.nextBytes(buffer);
		long delay = decodeLong(buffer);
		if (delay < 0) delay = -delay;
		delay = delay % 30000 + 2000;
		Log.d(TAG, "setting alarm for " + formatTime(mExpirationMillis+delay));
		alarmManager.set(AlarmManager.RTC, mExpirationMillis+delay, pendingIntent);
	}
	
	protected void clearError() {
		// Log.d(getString(R.string.app_name), "clearing error, resetting timer");
		mErrorMessage = null;
	}

	protected void setupMock(Intent intent) {
		Bundle extras = intent.getExtras();

		mWotdId = extras.getInt(BaseColumns._ID);
		mWotdText = extras.getString(WOTD_TEXT);
		mWotdNameAndPos = extras.getString(DubsarContentProvider.WORD_NAME_AND_POS);
		mExpirationMillis = extras.getInt(DubsarContentProvider.WOTD_EXPIRATION_MILLIS);

		/*
		Log.d(getString(R.string.app_name), "mock service with ID=" +
			mWotdId + ", text=\"" + mWotdText + "\", name and pos=\"" +
			mWotdNameAndPos + "\"");
		 */

		generateBroadcast();
		saveWotdData();

		mTestMode = true;
	}
	
	/**
	 * Store WOTD data on the local filesystem.
	 */
	protected void saveWotdData() {
		try {
			FileOutputStream fos=null;
			try {
				fos = openFileOutput(WOTD_FILE_NAME, MODE_PRIVATE);
	
				fos.write(encodeLong(mExpirationMillis));
				fos.write(encodeLong(mWotdId));

				byte[] data;
				
				if (mWotdText != null) {
					data = mWotdText.getBytes();
					fos.write(encodeLong(data.length));
					fos.write(data);
				}
				else {
					fos.write(encodeLong(0));
				}
				
				if (mWotdNameAndPos != null) {
					data = mWotdNameAndPos.getBytes();
					fos.write(encodeLong(data.length));
					fos.write(data);
				}
				else {
					fos.write(encodeLong(0));
				}
				
				Log.i(TAG, "Wrote WOTD data to " +
						WOTD_FILE_NAME);
			}
			catch (FileNotFoundException e) {
				Log.e(TAG,
						"OPEN " + WOTD_FILE_NAME + ": " + e.getMessage());
			}
			finally {
				if (fos != null) fos.close();
			}
		}
		catch (IOException e) {
			Log.e(TAG,
					"WRITE " + WOTD_FILE_NAME + ": " + e.getMessage());
		}
	}

	/**
	 * Load saved data from storage.
	 */
	protected void loadWotdData() {
		Log.i(TAG, "Loading WOTD data from " + WOTD_FILE_NAME);
		try {
			BufferedInputStream input=null;
			try {
				input = new BufferedInputStream(openFileInput(WOTD_FILE_NAME), 256);
				Log.d(TAG, " Opened " + WOTD_FILE_NAME);
				
				byte[] lbuffer = new byte[8];
				byte[] sbuffer;
				int length;
	
				/* WOTD expiration millis */
				input.read(lbuffer);
				mExpirationMillis = decodeLong(lbuffer);
				
				Log.d(TAG, " loaded WOTD expiration millis: " +
						formatTime(mExpirationMillis));
				
				/* WOTD ID */
				input.read(lbuffer);
				mWotdId = (int)decodeLong(lbuffer);
				
				Log.d(TAG, " loaded WOTD ID: " + mWotdId);
				
				/* WOTD text */
				input.read(lbuffer);
				length = (int)decodeLong(lbuffer);
				
				Log.d(TAG, " length of WOTD text is " + length);
				if (length > 256) {
					throw new Exception("invalid data length: " + length);
				}

				if (length > 0) {
					sbuffer = new byte[length];
					input.read(sbuffer);
					mWotdText = new String(sbuffer);
					Log.d(TAG, " loaded WOTD text: " + mWotdText);
				}
				
				/* WOTD name and pos */
				input.read(lbuffer);
				length = (int)decodeLong(lbuffer);
				
				Log.d(getString(R.string.app_name), " length of WOTD name and pos is " + length);
				if (length > 256) {
					throw new Exception("invalid data length: " + length);
				}

				if (length > 0) {
					sbuffer = new byte[length];
					input.read(sbuffer);
					mWotdNameAndPos = new String(sbuffer);
					Log.d(TAG, " loaded WOTD name and pos: " + mWotdNameAndPos);
				}
			}
			catch (FileNotFoundException e) {
				Log.w(TAG, "OPEN " + WOTD_FILE_NAME + ": " + e.getMessage());
			}
			finally {
				if (input != null) input.close();
			}
		}
		catch (Exception e) {
			Log.e(TAG, "READ " + WOTD_FILE_NAME + ": " + e.getMessage());
			mWotdId = 0;
			mWotdText = mWotdNameAndPos = null;
		}
	}
	
	protected void purgeData() {
		deleteFile(WOTD_FILE_NAME);
	}
	
	protected void saveResults(Cursor cursor) {
		int idColumn = cursor.getColumnIndex(BaseColumns._ID);
		int nameAndPosColumn = cursor.getColumnIndex(DubsarContentProvider.WORD_NAME_AND_POS);
		int freqCntColumn = cursor.getColumnIndex(DubsarContentProvider.WORD_FREQ_CNT);
		int expirationColumn = cursor.getColumnIndex(DubsarContentProvider.WOTD_EXPIRATION_MILLIS);
		
		cursor.moveToFirst();
		
		mWotdId = cursor.getInt(idColumn);
		mWotdNameAndPos = cursor.getString(nameAndPosColumn);
		
		/*
		 * Only set the alarm if previously unset (mExpirationMillis was 0 the first time through),
		 * or if the WOTD is stale.
		 */
		boolean mustSetAlarm = mExpirationMillis < System.currentTimeMillis();
		mExpirationMillis = cursor.getLong(expirationColumn);
		
		int freqCnt = cursor.getInt(freqCntColumn);
		mWotdText = new String(mWotdNameAndPos);
		if (freqCnt > 0) {
			mWotdText += " freq. cnt.: " + freqCnt;
		}
		
		Log.d(TAG, "WOTD ID = " + mWotdId);
		Log.d(TAG, "WOTD TEXT = " + mWotdText);
		Log.d(TAG, "WOTD NAME AND POS = " + mWotdNameAndPos);
		Log.d(TAG, "WOTD EXPIRATION MILLIS = " + mExpirationMillis + " (" + formatTime(mExpirationMillis) + ")");
		
		saveWotdData();
		
		if (mExpirationMillis < System.currentTimeMillis()) {
			/*
			 * If the expiration we just now received is in the past, something's wrong. At any rate,
			 * don't treat this like an expiration and just immediately re-request. Treat it like
			 * an error and go into a periodic retry loop.
			 */
			startRerequesting();
		}
		else if (mustSetAlarm) {
			setAlarm();
		}
	}
	
	@SuppressWarnings("deprecation")
	protected void generateNotification() {
		if (notificationsEnabled() && !hasError()) {
			Log.i(TAG, "generating notification");
			Notification notification = new Notification(R.drawable.ic_dubsar_rounded,
					getString(R.string.dubsar_wotd), mExpirationMillis - MILLIS_PER_DAY);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			notification.icon = R.drawable.ic_dubsar_rounded_small;
			
			Intent wordIntent = new Intent(this, WordActivity.class);
			wordIntent.putExtra(DubsarContentProvider.WORD_NAME_AND_POS, mWotdText);
			Uri uri = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI, 
					DubsarContentProvider.WORDS_URI_PATH + "/" + mWotdId);
			wordIntent.setData(uri);
			
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, wordIntent, 0);
			
			notification.setLatestEventInfo(this, getString(R.string.dubsar_wotd), 
					mWotdText, contentIntent);
			if (!mTestMode) {
				/* obnoxious in automated testing */
				notification.defaults = Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE;
				notification.flags |= Notification.FLAG_SHOW_LIGHTS;
				notification.ledARGB = 0xff0000ff;
				notification.ledOnMS = 800;
				notification.ledOffMS = 600;
			}
			
			mNotificationMgr.notify(WOTD_ID, notification);
		}
		else if (getErrorMessage() != null) {
			Log.d(TAG, "not generating notification because of error: " + getErrorMessage());
		}
		else {
			Log.d(TAG, "not generating notification because notifications disabled");
		}
		
		generateBroadcast();
	}

	protected void generateBroadcast() {
		if (!hasError() &&
			(mWotdId == 0 || mWotdText == null || mWotdNameAndPos == null)) {
			return;
		}
		
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_WOTD);
		if (!hasError()) {
			Log.i(TAG, "Broadcast: ID=" +
				mWotdId + ", text=\"" + mWotdText + "\", name and pos=\"" +
				mWotdNameAndPos + "\"");
			broadcastIntent.putExtra(BaseColumns._ID, mWotdId);
			broadcastIntent.putExtra(WOTD_TEXT, mWotdText);
			broadcastIntent.putExtra(DubsarContentProvider.WORD_NAME_AND_POS,
					mWotdNameAndPos);
		}
		else {
			Log.i(TAG, "Broadcast: error=" + mErrorMessage);
			broadcastIntent.putExtra(ERROR_MESSAGE, mErrorMessage);
		}
		
		sendBroadcast(broadcastIntent);
	}
	
	protected static String getTimestamp() {
		return formatTime(System.currentTimeMillis());
	}

	/**
	 * Return a string with the provided time formatted as yyyy-mm-dd HH:MM:SS.
	 * @param time the time to format, in milliseconds
	 * @return a string containing the formatted time
	 */
	protected static String formatTime(long time) {
		StringBuffer output = new StringBuffer();
		Formatter formatter = new Formatter(output);
		formatter.format("%tY-%tm-%td %tT", time, time, time, time);
		formatter.close();
		return output.toString();
	}

	protected void requestNow() {
		setRequestPending(true);
		new WotdTask(this).execute();
	}

	protected void startRerequesting() {
		// begin rechecking every 15 minutes
		Intent serviceIntent = new Intent(getApplicationContext(), DubsarService.class);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, serviceIntent, 0);
		AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
		alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + 900000, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
	}

	protected void noNetworkError() {
		if (!hasError() || !mErrorMessage.equals(getString(R.string.no_network))) {
			mErrorMessage = getString(R.string.no_network);
			// Log.e(TAG, mErrorMessage);

			generateBroadcast();
			startRerequesting();
		}
	}
	
	protected void setRequestPending(boolean flag) {
		mRequestPending = flag;
	}
	
	/*
	 * This is when it really sucks to use Java
	 */
	public static byte[] encodeLong(long data) {
		byte[] buffer = new byte[8];
		long mask = 0x00000000000000ff;
		for (int j=0; j<8; ++j) {
			long shifted = data >> (8*(8-j));
			buffer[j] = (byte)(shifted&mask);
		}

		return buffer;
	}
	
	public static long decodeLong(byte[] buffer) {
		long data=0x0000000000000000;
		for (int j=0; j<8; ++j) {
			long _byte = (long)buffer[j];
			if (_byte < 0) _byte = 256 + _byte;

			data |= (_byte << (8*(8-j)));
		}

		return data;
	}
	
	static class WotdTask extends AsyncTask<Void, Integer, Void> {
		private final WeakReference<DubsarService> mServiceReference;
		
		public WotdTask(DubsarService service) {
			mServiceReference = new WeakReference<DubsarService>(service);
		}
		
		public DubsarService getService() {
			return mServiceReference != null ? mServiceReference.get() : null;
		}

		@Override
		protected Void doInBackground(Void... params) {
			Log.d(TAG, getTimestamp() + ": timer fired");
			DubsarService service = getService();
			if (service == null) return null;

			if (!service.isNetworkAvailable()) {
				service.noNetworkError();
				return null;
			}
			/* If I'm recovering from a network outage, clear my state */
			else if (service.hasError() &&
				service.getErrorMessage().equals(service.getString(R.string.no_network))) {
				service.clearError();
			}

			Uri uri = Uri.withAppendedPath(DubsarContentProvider.CONTENT_URI, 
					DubsarContentProvider.WOTD_URI_PATH);
		
			Log.i(TAG, "requesting WOTD, URI " + uri);
			
			ContentResolver resolver = service.getContentResolver();

			// relinquish this reference while the command executes
			service = null;

			Cursor cursor = resolver.query(uri, null, null, null, null);
			
			Log.i(TAG, getTimestamp() + ": Request completed");
			
			// the request should not take long, but since we have a weak
			// reference:
			service = getService();
			if (service == null) return null;
			service.setRequestPending(false);
			
			if (cursor == null) {
				if (!service.hasError() || !service.getErrorMessage().equals(service.getString(R.string.search_error))) {
					service.setErrorMessage(service.getString(R.string.search_error));
					service.generateBroadcast();
					service.startRerequesting();
				}
				return null;
			}
			
			/* Success */
			
			/* If I'm recovering from a search error, reset my state */
			if (service.hasError()) {
				service.clearError();
			}
			
			service.saveResults(cursor);
			service.generateNotification();

			cursor.close();
			return null;
		}
	}
}
