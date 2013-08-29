SecureClient Android HTTP Client library
========================================

This Android library project is a work in progress. It may be spun into its
own open-source MIT package if it evolves into something a bit more useful and
independent.

If you spend some time looking at Qualys&apos; [SSL Test](https://www.ssllabs.com/ssltest/)
site, you will learn quite a bit about how to configure an HTTPS server to use
state-of-the-art cryptographic standards such as TLSv1.2 and forward secrecy.
These same standards are employed by all current browsers, so if you adopt these
recommendations, users of your site can browse with some expectation of privacy.

If you are then building, say, a mobile client for your web service, you will
likely wish to use HTTPS for your server communications, but having spent all that
time with Qualys, you&apos;re going to be very picky about which cipher suites you
allow, and so on. Then you will start wondering just how you would go about specifying
that in your client, on, say, Android.

The rules for SSL/TLS handshakes specify that while the server can choose which cipher
suites to support and which to reject, the client specifies the priority. You need a
way to do this in your client, and on Android, it is a little involved. Hence this
package.

The full range of cipher suites supported by OpenSSL can be found
[here](https://www.openssl.org/docs/apps/ciphers.html).

Those available on the current version of Android are [here](https://android.googlesource.com/platform/libcore2/+/master/luni/src/main/java/org/apache/harmony/xnet/provider/jsse/NativeCrypto.java)
(starting around line 380).
OpenSSL 1.0.1 is required for TLSv1.1 and TLSv1.2 support. This was introduced in
[Android 4.2](https://source.android.com/devices/tech/security/enhancements42.html).
The list of available ciphers depends strongly on the version of Android you are
using. In general, you&apos;ll have fuller access with later versions of Android, and
Jellybean 4.2 (SDK level 17) represents the latest expansion to that list.
Some notable exceptions to the list are GCM-based ciphers.

You can use this client thusly:

    import org.apache.http.client.HttpClient;
    import com.dubsar_dictionary.SecureClient.SecureAndroidHttpClient;
    import com.dubsar_dictionary.SecureClient.SecureSocketFactory;

    /*
     * Static initializer in some class
     */
    static {
        // order doesn't matter here
        SecureSocketFactory.setEnabledProtocols(new String[] { "TLSv1.1", "TLSv1.2" });

        // order does matter here. specify ciphers in preference order
        SecureSocketFactory.setEnabledCipherSuites(new String[] {
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
            "TLS_ECDHE_RSA_WITH_RC4_128_SHA"
        });
    }

    /*
     * Then, create a client
     */
    String userAgent;
    HttpClient client = SecureAndroidHttpClient.newInstance(userAgent);

Copyright
=========

Copyright (C) 2013 Jimmy Dee except as otherwise attributed.

License
=======

Dubsar is free, open-source software, distributed under Version 2 of
the GNU General Public License (GPL):

>  Dubsar Dictionary Project
>  Copyright (C) 2010-13 Jimmy Dee
>
>  This program is free software; you can redistribute it and/or
>  modify it under the terms of the GNU General Public License
>  as published by the Free Software Foundation; either version 2
>  of the License, or (at your option) any later version.
>
>  This program is distributed in the hope that it will be useful,
>  but WITHOUT ANY WARRANTY; without even the implied warranty of
>  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
>  GNU General Public License for more details.
>
>  You should have received a copy of the GNU General Public License
>  along with this program; if not, write to the Free Software
>  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
