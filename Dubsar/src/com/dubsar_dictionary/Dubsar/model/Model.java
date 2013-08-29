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

/**
 * Package for Dubsar model code.
 */
package com.dubsar_dictionary.Dubsar.model;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.util.Log;

import com.dubsar_dictionary.Dubsar.PreferencesActivity;
import com.dubsar_dictionary.Dubsar.R;
import com.dubsar_dictionary.SecureClient.SecureAndroidHttpClient;
import com.dubsar_dictionary.SecureClient.SecureSocketFactory;

/**
 * Base class for all models. Provides communications
 * and a basic JSON parsing framework.
 */
public abstract class Model {
	
	private static HashMap<String, PartOfSpeech> sPosMap = null;
	
	/**
	 * Enumeration to represent parts of speech. 
	 */
	public enum PartOfSpeech {
		Unknown,
		Adjective,
		Adverb,
		Conjunction,
		Interjection,
		Noun,
		Preposition,
		Pronoun,
		Verb
	}
	
	/**
	 * Convenience method to convert the abbreviated pos name
	 * to an enumerated value.
	 * @param pos a string abbreviation (e.g. "adj" or "n")
	 * @return the corresponding enumerated part of speech (Unknown on failure)
	 */
	public static PartOfSpeech partOfSpeechFromPos(String pos) {
		return getPosMap().get(pos);
	}
	
	/**
	 * Convenience function to return an abbreviated string (e.g., "adj" or
	 * "v") given the corresponding enumerated PartOfSpeech.
	 * @param partOfSpeech an enumerated value
	 * @return the corresponding string value ("unk" on failure)
	 */
	public static String posFromPartOfSpeech(PartOfSpeech partOfSpeech) {
		switch (partOfSpeech) {
		case Adjective:
			return "adj";
		case Adverb:
			return "adv";
		case Conjunction:
			return "conj";
		case Interjection:
			return "interj";
		case Noun:
			return "n";
		case Preposition:
			return "prep";
		case Pronoun:
			return "pron";
		case Verb:
			return "v";
		default:
			return "unk";
		}
	}
	
	protected static HashMap<String, PartOfSpeech> getPosMap() {
		if (sPosMap == null) {
			sPosMap = new HashMap<String, PartOfSpeech>();
			
			sPosMap.put("adj", PartOfSpeech.Adjective);
			sPosMap.put("adv", PartOfSpeech.Adverb);
			sPosMap.put("conj", PartOfSpeech.Conjunction);
			sPosMap.put("interj", PartOfSpeech.Interjection);
			sPosMap.put("n", PartOfSpeech.Noun);
			sPosMap.put("prep", PartOfSpeech.Preposition);
			sPosMap.put("pron", PartOfSpeech.Pronoun);
			sPosMap.put("v", PartOfSpeech.Verb);
		}
		
		return sPosMap;
	}
	
	private static HashMap<String,String> sMocks=new HashMap<String,String>();

	protected HashMap<String, List<List<Object> > > mPointers=null;
	protected int mPointerCount=0;

	/**
	 * This protected member variable is set by child classes.
	 * The full URL is generated by prepending 
	 * R.string.dubsar_base_url.
	 */
	protected String mPath;
	
	/**
	 * This protected member variable is populated by the base
	 * class for the child class to parse in parseData().
	 */
	protected String mData;
	
	private static WeakReference<Context> sContextReference=null;
	
	private String mUrl=null;
	private volatile boolean mComplete=false;
	private volatile boolean mError=false;
	private String mErrorMessage=null;
	private PartOfSpeech mPartOfSpeech=PartOfSpeech.Unknown;
	
	/**
	 * default constructor
	 */
	public Model() {
	}
	
	/**
	 * constructor using PartOfSpeech
	 * @param partOfSpeech the PartOfSpeech
	 */
	public Model(PartOfSpeech partOfSpeech) {
		mPartOfSpeech = partOfSpeech;
	}
	
	/**
	 * Constructor using POS
	 * @param pos the POS
	 */
	public Model(String pos) {
		mPartOfSpeech = partOfSpeechFromPos(pos);
	}
	
	/**
	 * Enumerated part of speech
	 * @return PartOfSpeech enumeration
	 */
	public PartOfSpeech getPartOfSpeech() {
		return mPartOfSpeech;
	}
	
	/**
	 * Set a new part of speech
	 * @param partOfSpeech new part of speech
	 */
	public void setPartOfSpeech(PartOfSpeech partOfSpeech) {
		mPartOfSpeech = partOfSpeech;
	}
	
	/**
	 * Part of speech abbreviation
	 * @return abbreviated form ("adv", "v", etc)
	 */
	public final String getPos() {
		return posFromPartOfSpeech(mPartOfSpeech);
	}
	
	/**
	 * Set a new POS
	 * @param pos the new POS
	 */
	public void setPos(String pos) {
		mPartOfSpeech = partOfSpeechFromPos(pos);
	}
	
	/**
	 * Get the URL associated with this model.
	 * @return this model's full URL
	 */
	public final String getUrl() {
		if (mUrl == null) mUrl = getString(R.string.dubsar_base_url) + mPath;
		return mUrl;
	}
	
	/**
	 * Has this request finished receiving its response?
	 * @return true if response received; false otherwise
	 */
	public boolean isComplete() {
		return mComplete;
	}
	
	/**
	 * Did this request generate an error? This value is
	 * not meaningful unless isComplete().
	 * @return true if the request returned an error; false otherwise
	 */
	public boolean hasError() {
		return mError;
	}
	
	/**
	 * If hasError(), this will return a non-null error
	 * message for the end user.
	 * @return a human-readable error message
	 */
	public final String getErrorMessage() {
		return mErrorMessage;
	}
	
	/**
	 * Set the raw data buffer (useful for testing).
	 * @param data a raw JSON buffer to parse
	 */
	public void setData(String data) {
		mData = new String(data);
	}
	
	/**
	 * This model's pointers
	 * @return a map of pointers
	 */
	public final HashMap<String, List<List<Object> > > getPointers() {
		return mPointers;
	}
	
	/**
	 * Get this model's pointer count
	 * @return total number of pointer rows
	 */
	public int getPointerCount() {
		return mPointerCount;
	}
	
	/**
	 * Set the context to use for all model instances when
	 * converting strings, e.g.
	 * @param context the context
	 */
	public static void setContext(Context context) {
		sContextReference = new WeakReference<Context>(context);
	}
	
	/**
	 * The context used for string lookups by the Model class
	 * @return the Context
	 */
	public static Context getContext() {
		return sContextReference != null ? sContextReference.get() : null;
	}
	
	/**
	 * Static convenience function
	 * @param id a numeric string ID (R.string.hello, e.g.)
	 * @return the string value
	 */
	public static final String getString(int id) {
		if (getContext() == null) return null;
		
		return getContext().getString(id);
	}
	
	/**
	 * Fetch data synchronously from the server.
	 */
	public void load() {
		try {
			/* simple HTTP mock for testing */
			mData = getMock();

			if (mData == null) mData = fetchData();
			
			JSONTokener tokener = new JSONTokener(mData);
			parseData(tokener.nextValue());			
		}
		/* JSONException, ClientProtocolException and IOException
		 * For now (and perhaps indefinitely), we handle them all alike.
		 */
		catch (Exception e) {
			mError = true;
			mErrorMessage = e.getMessage();
			Log.wtf(getString(R.string.app_name), e);
		}
		
		mComplete = true;
	}
	
	/**
	 * Must be overridden by child classes to parse the
	 * JSON payload in mData.
	 * @param jsonResponse an object, typically a JSONArray or a JSONObject
	 * @throws JSONException if the tokener encounters a parsing error
	 */
	public abstract void parseData(Object jsonResponse) throws JSONException;
	
	/**
	 * Add a JSON mock for testing. If a mock is present, its value is
	 * returned to the model instead of requesting data from the server.
	 * @param path a path to mock
	 * @param json the JSON response to return for this path instead of querying the server
	 */
	public static void addMock(String path, String json) {
		sMocks.put(path, json);
	}
	
	/**
	 * Get the JSON mock for this path (or null)
	 * @param path a path to check
	 * @return the associated JSON mock
	 */
	public static final String getMock(String path) {
		return sMocks.get(path);
	}
	
	/**
	 * Get this model's mock (if any)
	 * @return the JSON mock associated with this model's path (null if none)
	 */
	public final String getMock() {
		return getMock(mPath);
	}
	
	/**
	 * Create or return the common HttpClient used by all model requests.
	 * @return the HttpClient
	 */
	protected static HttpClient newClient() {
		String userAgent = getString(R.string.user_agent);
		userAgent += " (" + getContext().getString(R.string.android_version,
				new Object[]{Build.VERSION.RELEASE});
		userAgent += "; " + getContext().getString(R.string.build, new Object[]{Build.DISPLAY});
		userAgent += ")";

		HttpClient client = null;
		if (Build.VERSION.SDK_INT >= 11) {
			client = SecureAndroidHttpClient.newInstance(userAgent);
		}
		else {
			/*
			 * Below 11, the SecureSocketFactory does nothing. The only other
			 * benefit to not using AndroidHttpClient is that it disables redirect
			 * following, which bit this app before: Until now, it has not been
			 * able to use HTTPS because it would not follow redirects from the
			 * server. So hopefully this at least turns redirects on for 8 and 10.
			 * And at any rate, the SecureSocketFactory blows up on 8. So we do
			 * this.
			 */
			client = AndroidHttpClient.newInstance(userAgent, getContext());
	        HttpClientParams.setRedirecting(client.getParams(), true);
		}
		
		SharedPreferences preferences = getContext().getSharedPreferences(PreferencesActivity.DUBSAR_PREFERENCES, PreferencesActivity.MODE_PRIVATE);
		String host = preferences.getString(PreferencesActivity.HTTP_PROXY_HOST, null);
		int port = preferences.getInt(PreferencesActivity.HTTP_PROXY_PORT, 0);
		
		if (host != null && host.length() > 0 && port > 0) {
			Log.d(getContext().getString(R.string.app_name), "HTTP proxy setting in Model is " + host + ":" + port);
		
			HttpHost httpHost = new HttpHost(host, port);
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, httpHost);
		}
		else {
			Log.d(getContext().getString(R.string.app_name), "No HTTP proxy set in Model");
			// prob. no point to this, since we've just created a new client with default settings
			client.getParams().removeParameter(ConnRoutePNames.DEFAULT_PROXY);
		}
		return client;
	}
	
	/**
	 * Retrieve JSON data for this model instance from the server.
	 * @return the JSON payload
	 * @throws IOException in case of communication error
	 */
	protected String fetchData() throws IOException {
		ResponseHandler<String> handler = new BasicResponseHandler();
		
		// DEBT: Take from strings file or constants
		Header header = new BasicHeader("Accept", "application/json");
		HttpGet request = new HttpGet(getUrl());
		request.addHeader(header);
		
		HttpClient client = newClient();
		String response = client.execute(request, handler);
		return response;
	}
	
	protected void parsePointers(JSONArray pointers) throws JSONException {
		mPointers = new HashMap<String, List<List<Object> > >();
		mPointerCount = pointers.length();
		
		for (int j=0; j<pointers.length(); ++j) {
			JSONArray _pointer = pointers.getJSONArray(j);
			
			String ptype = _pointer.getString(0);
			String targetType = _pointer.getString(1);
			int targetId = _pointer.getInt(2);
			String targetText = _pointer.getString(3);
			String targetGloss = _pointer.getString(4);
			
			List<List<Object> > pointersByType = mPointers.get(ptype);
			if (pointersByType == null) {
				pointersByType = new ArrayList<List<Object> >();
				mPointers.put(ptype, pointersByType);
			}
			
			ArrayList<Object> pointer = new ArrayList<Object>();
			pointer.add(targetType);
			pointer.add(Integer.valueOf(targetId));
			pointer.add(targetText);
			pointer.add(targetGloss);
			
			pointersByType.add(pointer);
		}
	}
	
	protected void setErrorMessage(String message) {
		mErrorMessage = message;
		mError = true;
	}
    
    static {
    	if (Build.VERSION.SDK_INT >= 17) {
    		SecureSocketFactory.setEnabledProtocols(new String[] { "TLSv1.2" } );
    	}
    	
    	if (Build.VERSION.SDK_INT >= 11) {
    		SecureSocketFactory.setEnabledCipherSuites(new String[] { "TLS_ECDHE_RSA_WITH_RC4_128_SHA" } );
    	}
    }
}
