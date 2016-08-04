package com.bx.research.net;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.InputStream;
import java.security.KeyStore;

public class SSLCustomSocketFactory {
	public static SSLSocketFactory getSocketFactory(InputStream isTrustKey, InputStream isIDkey, String keyPwd) {
		if (keyPwd == null) {
			keyPwd = "";
		}
		try {
			KeyStore trustStore = KeyStore.getInstance("bks");
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			try {
				trustStore.load(isTrustKey, keyPwd.toCharArray());
				keyStore.load(isIDkey, keyPwd.toCharArray());
			} finally {

				isTrustKey.close();
				isIDkey.close();
			}
			SSLSocketFactory factory = new SSLSocketFactory(keyStore, keyPwd, trustStore);
			return factory;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public static SSLSocketFactory getSingleSocketFactory(InputStream is, String keyPwd) {
		if (keyPwd == null) {
			keyPwd = "";
		}
		try {

			KeyStore trustStore = KeyStore.getInstance("bks");
			try {
				trustStore.load(is, keyPwd.toCharArray());
			} finally {
				is.close();

			}
			SSLSocketFactory factory = new SSLSocketFactory(trustStore);
			return factory;
		} catch (Throwable e) {
			// Log.d(TAG, e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

}