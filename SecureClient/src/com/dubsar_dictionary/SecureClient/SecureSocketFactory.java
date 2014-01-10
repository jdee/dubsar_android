package com.dubsar_dictionary.SecureClient;

/*
 Dubsar Dictionary Project
 Copyright (C) 2010-14 Jimmy Dee
 
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

import org.apache.harmony.xnet.provider.jsse.NativeCrypto;
import org.apache.harmony.xnet.provider.jsse.OpenSSLContextImpl;
import org.apache.http.conn.ssl.SSLSocketFactory;

import android.util.Log;

public class SecureSocketFactory extends SSLSocketFactory {
	public static final String TAG = "SecureSocketFactory";
	
	private volatile javax.net.ssl.SSLSocketFactory mDelegate = null;
	// private int mHandshakeTimeoutMillis = 0;

	private static SecureSocketFactory sFactory = null;
	private static volatile String[] sCipherSuites = null;
	private static volatile String[] sProtocols = null;
	
	public static SSLSocketFactory getHttpSocketFactory(int handshakeTimeoutMillis) {
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
	
	/**
	 * Order matters here. Specify cipher suites in order of preference. You can use either
	 * the standard or the OpenSSL convention for naming a suite, e.g.
	 * TLS_ECDHE_RSA_WITH_RC4_128_SHA or ECDHE-RSA-RC4-SHA.
	 * @param cipherSuites an array of strings specifying individual cipher suites
	 */
	public static synchronized void setEnabledCipherSuites(String[] cipherSuites) {
		sCipherSuites = cipherSuites;
	}
	
	/**
	 * Returns whatever was last passed to setEnabledCipherSuites. If that is null, or
	 * if setEnabledCipherSuites was never called, returns all supported cipher suites.
	 * @return
	 */
	public static synchronized String[] getEnabledCipherSuites() {
		return sCipherSuites != null ? sCipherSuites : NativeCrypto.getSupportedCipherSuites();
	}
	
	/**
	 * Order does not matter. Simply specify the protocols you are willing to use.
	 * Options include SSLv3, TLSv1, TLSv1.1 and TLSv1.2.
	 * @param protocols an array of strings specifying individual protocols
	 */
	public static synchronized void setEnabledProtocols(String[] protocols) {
		sProtocols = protocols;
	}
	
	/**
	 * Returns whatever was last passed to setEnabledProtocols. If that is null, or if
	 * setEnabledProtocols was never called, returns all supported protocols.
	 * @return
	 */
	public static synchronized String[] getEnabledProtocols() {
		return sProtocols != null ? sProtocols : NativeCrypto.getSupportedProtocols();
	}
	
	SecureSocketFactory(int handshakeTimeoutMillis) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
		super(null);
		
		// mHandshakeTimeoutMillis = handshakeTimeoutMillis;
		// Log.d(TAG, "Created new SecureSocketFactory");
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
		// Log.d(TAG, "in createSocket(Socket, String, int, boolean): " + host + ":" + port);
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

	private void setupCrypto(SSLSocket socket) {
		// Log.d(TAG, "in setupCrypto");

		String[] protocols = getEnabledProtocols();
		if (protocols != null) {
			socket.setEnabledProtocols(protocols);
		}
		
		String[] ciphers = getEnabledCipherSuites();
		if (ciphers != null) {
			socket.setEnabledCipherSuites(ciphers);
		}

		protocols = socket.getEnabledProtocols();
		if (protocols == null) {
			Log.e(TAG, "protocols is null");
			return;
		}
		for (String protocol : protocols) {
			Log.d(TAG, protocol + " is enabled");
		}

		ciphers = socket.getEnabledCipherSuites();
		if (ciphers == null) {
			Log.e(TAG, "ciphers is null");
			return;
		}		
		for (String cipher : ciphers) {
			Log.d(TAG, cipher + " is enabled");
		}

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
