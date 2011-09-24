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
import java.util.HashSet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;


public class CommsMonitor extends BroadcastReceiver {
	
	/**
	 * To be implemented by clients to provide a context and a callback 
	 * to be invoked when the background data setting changes.
	 */
	public static interface CommsSubscriber {
		/**
		 * Return a context (often this)
		 * @return a valid context for use by the monitor
		 */
		Context getContext();
		
		/**
		 * This method will be invoked whenever the background data setting
		 * changes.
		 */
		void onBackgroundDataSettingChanged();
	}
	
	volatile boolean backgroundDataUsageAllowed=false;
	volatile boolean networkAvailable=false;

	private final WeakReference<CommsSubscriber> mSubscriberReference;
	private final ConnectivityManager mConnectivityMgr;
	private HashSet<Integer> mConnectedNetworks = new HashSet<Integer>();

	public CommsMonitor(CommsSubscriber subscriber) {
		Context context = subscriber.getContext();
		
		mConnectivityMgr =
				(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		mSubscriberReference = new WeakReference<CommsSubscriber>(subscriber);
		checkBackgroundDataSetting(context);
		// checkNetworkState(service);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED);
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		Intent broadcast = context.registerReceiver(this, filter);
		if (broadcast != null) {
			Log.i(context.getString(R.string.app_name), "processing sticky broadcast");
			onReceive(context, broadcast);
		}
	}

	public void teardownReceiver(Context context) {
		context.unregisterReceiver(this);
	}

	public final CommsSubscriber getSubscriber() {
		return mSubscriberReference.get();
	}
	
	public Context getContext() {
		return getSubscriber() != null ? getSubscriber().getContext() : null;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (getSubscriber() == null) return;

		final String action = intent.getAction();
		if (action.equals(ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED)) {
			Log.i(context.getString(R.string.app_name), "background data setting changed");
			checkBackgroundDataSetting(context);
			getSubscriber().onBackgroundDataSettingChanged();
		}
		else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			handleCommsBroadcast(context, intent);
		}
	}
	
	protected void checkBackgroundDataSetting(Context context) {
		backgroundDataUsageAllowed = mConnectivityMgr.getBackgroundDataSetting();
		Log.i(context.getString(R.string.app_name), "background data setting is " + 
				backgroundDataUsageAllowed);
	}
	
	protected void handleCommsBroadcast(Context context, Intent intent) {
		Log.i(context.getString(R.string.app_name), "received CONNECTIVITY_ACTION broadcast");
		Bundle extras = intent.getExtras();
		
		if (intent.hasExtra(ConnectivityManager.EXTRA_EXTRA_INFO)) {
			Log.i(context.getString(R.string.app_name), " EXTRA_EXTRA_INFO=\"" +
					extras.getString(ConnectivityManager.EXTRA_EXTRA_INFO) + "\"");
		}
		
		if (intent.hasExtra(ConnectivityManager.EXTRA_IS_FAILOVER)) {
			Log.i(context.getString(R.string.app_name), " EXTRA_IS_FAILOVER=\"" +
					extras.getBoolean(ConnectivityManager.EXTRA_IS_FAILOVER) + "\"");
		}
		
		if (intent.hasExtra(ConnectivityManager.EXTRA_NETWORK_INFO)) {
			Log.i(context.getString(R.string.app_name), " EXTRA_NETWORK_INFO present");
			NetworkInfo info = (NetworkInfo)intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			dumpNetworkInfo(context, info);
			
			updateConnectedNetworks(context, info);
		}
		
		if (intent.hasExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY)) {
			Log.i(context.getString(R.string.app_name), " EXTRA_NO_CONNECTIVITY=\"" +
					extras.getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY) + "\"");
			
			/*
			 * This is all we really care about
			 */
			networkAvailable = !extras.getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY);
			Log.i(context.getString(R.string.app_name),
					" Network is " + (networkAvailable ? "" : "not ") + "connected");
		}

		if (intent.hasExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO)) {
			Log.i(context.getString(R.string.app_name), " EXTRA_OTHER_NETWORK_INFO present");
			NetworkInfo info = (NetworkInfo)intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);
			dumpNetworkInfo(context, info);
			
			updateConnectedNetworks(context, info);
		}
		
		if (intent.hasExtra(ConnectivityManager.EXTRA_REASON)) {
			Log.i(context.getString(R.string.app_name), " EXTRA_REASON=\"" +
					extras.getString(ConnectivityManager.EXTRA_REASON) + "\"");
		}
	}
	
	protected void updateConnectedNetworks(Context context, NetworkInfo info) {
		try {
			switch (info.getState()) {
			case CONNECTED:
				mConnectedNetworks.add(info.getType());
				networkAvailable = true;
				Log.i(context.getString(R.string.app_name), "Network is available");
				break;
			default:
				mConnectedNetworks.remove(info.getType());
				networkAvailable = mConnectedNetworks.size() > 0;
				Log.i(context.getString(R.string.app_name), "Network is " + 
						(mConnectedNetworks.size() > 0 ? "" : "not ") + "available");
				break;
			}
		}
		catch (Exception e) {
			Log.e(context.getString(R.string.app_name), "exception in updateConnectedNetworks: " + e.getMessage());
		}
	}
	
	/*
	protected void checkNetworkState(Context context) {
		NetworkInfo wifiInfo = mConnectivityMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobileInfo = mConnectivityMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo wimaxInfo = mConnectivityMgr.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
		
		dumpNetworkInfo(context, wifiInfo);
		dumpNetworkInfo(context, mobileInfo);
		dumpNetworkInfo(context, wimaxInfo);
		
		if (wifiInfo != null) {
			Log.i(context.getString(R.string.app_name),
				" Wi-Fi is " + (wifiInfo.isConnected() ? "" : "not ") + "connected");
		}
		if (mobileInfo != null) {
			Log.i(context.getString(R.string.app_name),
				" 3G is " + (mobileInfo.isConnected() ? "" : "not ") + "connected");
		}
		if (wimaxInfo != null) {
			Log.i(context.getString(R.string.app_name),
				" 4G is " + (mobileInfo.isConnected() ? "" : "not ") + "connected");				
		}

		networkAvailable = (wifiInfo != null && wifiInfo.isConnected()) ||
				(mobileInfo != null && mobileInfo.isConnected()) ||
				(wimaxInfo != null && wimaxInfo.isConnected());
		Log.i(context.getString(R.string.app_name),
				" Network is " + (networkAvailable ? "" : "not ") + "connected");
	}
	 */

	protected void dumpNetworkInfo(Context context, NetworkInfo info) {
		if (info == null) return;

		String title = "Network type " + info.getTypeName();
		if (info.getSubtypeName().length() > 0) {
			title += " (" + info.getSubtypeName() + ")";
		}
		Log.i(context.getString(R.string.app_name), " > " + title + " state: " + info.getState());
	}
}

