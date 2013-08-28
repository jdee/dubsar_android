package com.dubsar_dictionary.Dubsar.util;

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

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLSocket;

import org.apache.harmony.xnet.provider.jsse.OpenSSLContextImpl;
import org.apache.http.conn.ssl.SSLSocketFactory;

import android.net.SSLSessionCache;
import android.os.Build;
import android.util.Log;

/**
 * Forces use of highly secure crypto.
 * This client is intended for use with one server, which is configured to support
 * TLSv1.2 and ECDHE-RSA-RC4-SHA. These options are supported in OpenSSL
 * 1.0.1, which is available in JB 4.2+. When available, use these options.
 */
public class SecureSocketFactory extends SSLSocketFactory {
	public static final String[] TLSv12_CIPHER_SUITES = new String[] { "ECDHE-RSA-RC4-SHA" };
	public static final String[] TLSv12_PROTOCOLS = new String[] { "TLSv1.2" };
	public static final String TAG = "SecureSocketFactory";
	
	private volatile javax.net.ssl.SSLSocketFactory mDelegate = null;
	// private int mHandshakeTimeoutMillis = 0;

	private static SecureSocketFactory sFactory = null;
	
	public static SSLSocketFactory getHttpSocketFactory(int handshakeTimeoutMillis, SSLSessionCache cache) {
		if (sFactory == null) {
			try {
				sFactory = new SecureSocketFactory(handshakeTimeoutMillis);
			}
			catch (Exception e) {
				Log.wtf(TAG, e);
			}
		}
		return sFactory;
	}
	
	SecureSocketFactory(int handshakeTimeoutMillis) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
		super(null);
		
		// mHandshakeTimeoutMillis = handshakeTimeoutMillis;
		Log.d(TAG, "Created new SecureSocketFactory");
	}

	@Override
	public Socket createSocket() throws IOException {
		// Log.d(TAG, "in createSocket()");
		SSLSocket socket = (SSLSocket) getDelegate().createSocket();
		setupCrypto(socket);
		return socket;
	}

	@Override
	public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
		Log.d(TAG, "in createSocket(Socket, String, int, boolean): " + host + ":" + port);
		SSLSocket socket = (SSLSocket) getDelegate().createSocket(s, host, port, autoClose);
		setupCrypto(socket);
		return socket;
	}

	protected synchronized javax.net.ssl.SSLSocketFactory getDelegate() {
		if (mDelegate == null) {
			mDelegate = makeSocketFactory();
		}
		
		return mDelegate;
	}
	
	protected boolean isTLSv12Available() {
		// Should I query getDelegate().getSupportedCiphers() instead? This is probably
		// faster and easier.
		return Build.VERSION.SDK_INT >= 17;
	}
	
	private void setupCrypto(SSLSocket socket) {
		// Log.d(TAG, "in setupCrypto");
		if (isTLSv12Available()) {
			// Log.d(TAG, "Enabling TLSv1.2 on SSLSocket (" + (socket != null ? "not " : "")  + "null)");
			
			socket.setEnabledProtocols(TLSv12_PROTOCOLS);
			String[] protocols = socket.getEnabledProtocols();
			if (protocols == null) {
				Log.d(TAG, "protocols is null");
				return;
			}
			for (String protocol : protocols) {
				Log.d(TAG, protocol + " is enabled");
			}
			
			try {
				socket.setEnabledCipherSuites(TLSv12_CIPHER_SUITES);
			}
			catch (Throwable e) {
				Log.wtf(TAG, e);
				return;
			}

			String[] ciphers = socket.getEnabledCipherSuites();
			if (ciphers == null) {
				Log.d(TAG, "ciphers is null");
				return;
			}
			else {
				// Log.d(TAG, "using " + ciphers.length + " ciphers");
			}
			
			for (String cipher : ciphers) {
				Log.d(TAG, cipher + " is enabled");
			}

			// Log.d(TAG, "Enabled TLSv1.2 on SSLSocket");
		}
		
		// otherwise, use defaults

		// no?
		// socket.setHandshakeTimeout(mHandshakeTimeoutMillis);
	}

    private javax.net.ssl.SSLSocketFactory makeSocketFactory() {
        try {
            OpenSSLContextImpl sslContext = new OpenSSLContextImpl();
            sslContext.engineInit(null, null, null);
            return sslContext.engineGetSocketFactory();
        } catch (KeyManagementException e) {
            Log.wtf(TAG, e);
            return (javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory.getDefault();  // Fallback
        }
    }
}
