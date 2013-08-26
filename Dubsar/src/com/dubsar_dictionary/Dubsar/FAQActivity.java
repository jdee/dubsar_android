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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.http.HttpHost;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
// import android.util.Log;

public class FAQActivity extends DubsarActivity {
	WebView mWebView=null;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.faq);

		mWebView = (WebView)findViewById(R.id.faq_web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);

        SharedPreferences preferences = getSharedPreferences(PreferencesActivity.DUBSAR_PREFERENCES, MODE_PRIVATE);
        String host = preferences.getString(PreferencesActivity.HTTP_PROXY_HOST, null);
        int port = preferences.getInt(PreferencesActivity.HTTP_PROXY_PORT, 0);

        if (host != null && port != 0) {
            setProxy(mWebView, host, port);
        }

		if (savedInstanceState != null) {
			mWebView.restoreState(savedInstanceState);
		}
		else {
			String html = "<html><body style=\"background-color: #e0e0ff;\"><h1 style=\"color: #1c94c4; text-align: center; margin-top: 2ex; font: bold 18pt sans-serif\">" + 
					getString(R.string.loading_faq) + "</h1></body></html>";
			mWebView.loadData(html, "text/html", "utf-8");
			// Log.i(getString(R.string.app_name), "Initial FAQ HTML: " + html);
		}

        mWebView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            public void onPageFinished(WebView view, String url) {
            	String faqUrl = getString(R.string.faq_url);
            	if (url == null || !url.equals(faqUrl)) {
            		mWebView.loadUrl(faqUrl);
            	}
            }
        });
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mWebView.saveState(outState);
	}

	public static boolean setProxy(WebView webview, String host, int port) {
		// 4.1 or higher (JB)
		if (Build.VERSION.SDK_INT >= 16) {
			return setProxyJBPlus(webview, host, port);
		}
		else {
			return setProxyUpToICS(host, port);
		}
	}

	@SuppressWarnings("all")
	private static boolean setProxyUpToICS(String host, int port) {
		Log.d("Dubsar", "Setting proxy with <= 4.0 API");

		HttpHost proxyServer = new HttpHost(host, port);
		// Getting network
		Class networkClass = null;
		Object network = null;
		try {
			networkClass = Class.forName("android.webkit.Network");
			if (networkClass == null) {
				Log.e("Dubsar", "failed to get class for android.webkit.Network");
				return false;
			}
			Field networkField = networkClass.getDeclaredField("sNetwork");
			if (networkField == null) {
				Log.e("Dubsar", "failed to find sNetwork field in android.webkit.Network");
				return false;
			}
			network = getFieldValueSafely(networkField, null);
		} catch (Exception ex) {
			Log.e("Dubsar", "error getting network");
			return false;
		}
		if (network == null) {
			Log.e("Dubsar", "error getting network : null");
			return false;
		}
		Object requestQueue = null;
		try {
			Field requestQueueField = networkClass
					.getDeclaredField("mRequestQueue");
			requestQueue = getFieldValueSafely(requestQueueField, network);
		} catch (Exception ex) {
			Log.e("Dubsar", "error getting field value");
			return false;
		}
		if (requestQueue == null) {
			Log.e("Dubsar", "Request queue is null");
			return false;
		}
		Field proxyHostField = null;
		try {
			Class requestQueueClass = Class.forName("android.net.http.RequestQueue");
			proxyHostField = requestQueueClass
					.getDeclaredField("mProxyHost");
		} catch (Exception ex) {
			Log.e("Dubsar", "error getting proxy host field");
			return false;
		}

		boolean temp = proxyHostField.isAccessible();
		try {
			proxyHostField.setAccessible(true);
			proxyHostField.set(requestQueue, proxyServer);
		} catch (Exception ex) {
			Log.e("Dubsar", "error setting proxy host");
		} finally {
			proxyHostField.setAccessible(temp);
		}

		Log.d("Dubsar", "Setting proxy with <= 4.0 API successful!");
		return true;
	}

	/**
	 * Set Proxy for Android 4.1 and above.
	 * From http://stackoverflow.com/questions/4488338/webview-android-proxy
	 */
	@SuppressWarnings("all")
	private static boolean setProxyJBPlus(WebView webview, String host, int port) {
	    Log.d("Dubsar", "Setting proxy with >= 4.1 API.");

	    try {
	        Class wvcClass = Class.forName("android.webkit.WebViewClassic");
	        Class wvParams[] = new Class[1];
	        wvParams[0] = Class.forName("android.webkit.WebView");
	        Method fromWebView = wvcClass.getDeclaredMethod("fromWebView", wvParams);
	        Object webViewClassic = fromWebView.invoke(null, webview);

	        Class wv = Class.forName("android.webkit.WebViewClassic");
	        Field mWebViewCoreField = wv.getDeclaredField("mWebViewCore");
	        Object mWebViewCoreFieldInstance = getFieldValueSafely(mWebViewCoreField, webViewClassic);

	        Class wvc = Class.forName("android.webkit.WebViewCore");
	        Field mBrowserFrameField = wvc.getDeclaredField("mBrowserFrame");
	        Object mBrowserFrame = getFieldValueSafely(mBrowserFrameField, mWebViewCoreFieldInstance);

	        Class bf = Class.forName("android.webkit.BrowserFrame");
	        Field sJavaBridgeField = bf.getDeclaredField("sJavaBridge");
	        Object sJavaBridge = getFieldValueSafely(sJavaBridgeField, mBrowserFrame);

	        Class ppclass = Class.forName("android.net.ProxyProperties");
	        Class pparams[] = new Class[3];
	        pparams[0] = String.class;
	        pparams[1] = int.class;
	        pparams[2] = String.class;
	        Constructor ppcont = ppclass.getConstructor(pparams);

	        Class jwcjb = Class.forName("android.webkit.JWebCoreJavaBridge");
	        Class params[] = new Class[1];
	        params[0] = Class.forName("android.net.ProxyProperties");
	        Method updateProxyInstance = jwcjb.getDeclaredMethod("updateProxy", params);

	        updateProxyInstance.invoke(sJavaBridge, ppcont.newInstance(host, port, null));
	    } catch (Exception ex) {
	        Log.e("Dubsar","Setting proxy with >= 4.1 API failed with error: " + ex.getMessage());
	        return false;
	    }

	    Log.d("Dubsar", "Setting proxy with >= 4.1 API successful!");
	    return true;
	}

	private static Object getFieldValueSafely(Field field, Object classInstance) throws IllegalArgumentException, IllegalAccessException {
		boolean oldAccessibleValue = field.isAccessible();
		field.setAccessible(true);
		Object result = field.get(classInstance);
		field.setAccessible(oldAccessibleValue);
		return result;
	}
}
