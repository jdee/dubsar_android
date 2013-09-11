package com.dubsar_dictionary.SecureClient;

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

import java.io.IOException;

import org.apache.http.ConnectionClosedException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.util.Log;

public class SecureAndroidHttpClient extends DefaultHttpClient {
	public static final String TAG = "SecureAndroidHttpClient";

	public static final int SOCKET_OPERATION_TIMEOUT = 60000;

	private long lastRequestTimeMillis = 0L;

    /**
     * Create a new HttpClient with reasonable defaults (which you can update).
     * (Lifted and modified from AndroidHttpClient.)
     *
     * @param userAgent to report in your HTTP requests
     * @param context to use for caching SSL sessions (may be null for no caching)
     * @return AndroidHttpClient for you to use for all your requests.
     */
    public static HttpClient newInstance(String userAgent) {
    	Log.d(TAG, "Creating new client instance");

        HttpParams params = new BasicHttpParams();

        // Turn off stale checking.  Our connections break all the time anyway,
        // and it's not worth it to pay the penalty of checking every time.
        HttpConnectionParams.setStaleCheckingEnabled(params, false);

        HttpConnectionParams.setConnectionTimeout(params, SOCKET_OPERATION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, SOCKET_OPERATION_TIMEOUT);
        HttpConnectionParams.setSocketBufferSize(params, 8192);

        // Default to following redirects
        HttpClientParams.setRedirecting(params, true);

        // Set the specified user agent and register standard protocols.
        HttpProtocolParams.setUserAgent(params, userAgent);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        SSLSocketFactory sf = SecureSocketFactory.getHttpSocketFactory(
                SOCKET_OPERATION_TIMEOUT);
        schemeRegistry.register(new Scheme("https", sf, 443));
        schemeRegistry.register(new Scheme("http",
                PlainSocketFactory.getSocketFactory(), 80));

        ClientConnectionManager manager =
                new ThreadSafeClientConnManager(params, schemeRegistry);

        // We use a factory method to modify superclass initialization
        // parameters without the funny call-a-static-method dance.
        return new SecureAndroidHttpClient(manager, params);
    }

    public void close() {
        ClientConnectionManager manager = getConnectionManager();
        if (manager != null) {
            manager.shutdown();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.http.impl.client.AbstractHttpClient#execute(org.apache.http.client.methods.HttpUriRequest, org.apache.http.client.ResponseHandler)
     *
     * The symptom here is that, when an HTTP proxy is set, requests will work at first, but after a period of inactivity, one or more failures will
     * occur. Eventually, everything will work again for a while, but inactivity will eventually trigger the problem again. This does not occur without
     * an HTTP proxy. I wonder if it's related to the SSL keepalive timeout, which on the server is 70 seconds. At any rate, waiting that length of time
     * after a successful request guarantees that the next request will fail if an HTTP proxy is set.
     *
     * The immediate failure is that this method, execute, throws a NoHttpResponseException right away. I was catching that and simply trying again. I'd
     * rather be able to check to see if the socket is still connected, but it's not clear if that's possible. That solution seemed to work. But now it's
     * throwing a NPE somewhere deep in the AbstractHttpClient. So I'm just going to give this class a connection timeout. If there hasn't been a request
     * from this client in some time, this method throws a ConnectionClosedException, which will cause the caller to instantiate a new client, guaranteeing
     * a reconnection. There is doubtless a better solution, but maybe this will at least fix the problem.
     */
    @Override
    public <T> T execute(HttpUriRequest request,
            ResponseHandler<? extends T> responseHandler) throws IOException,
            ClientProtocolException {
        // conveniently, SOCKET_OPERATION_TIMEOUT is about what I wanted, slightly less than the server timeout
        if (lastRequestTimeMillis > 0 && System.currentTimeMillis() - lastRequestTimeMillis >= SOCKET_OPERATION_TIMEOUT) {
            close();
            throw new ConnectionClosedException("connection closed by SecureAndroidHttpClient");
        }

        lastRequestTimeMillis = System.currentTimeMillis();

        return super.execute(request, responseHandler);
    }

	protected SecureAndroidHttpClient(ClientConnectionManager manager, HttpParams params) {
    	super(manager, params);
    }

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}
