/*
 * Copyright 2009 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.tls;

import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.hc.core5.http.ssl.TLS;

import cloud.tamacat.httpd.config.Config;
import cloud.tamacat.util.ClassUtils;
import cloud.tamacat.util.RuntimeIOException;

/**
 * <p>
 * The {@link SSLContext} create from {@link ServerConfig} or setter methods.
 */
public class DefaultSSLContextCreator implements SSLContextCreator {

	protected String keyStoreFile;
	protected char[] keyPassword;
	protected KeyStoreType type = KeyStoreType.PKCS12;
	protected TLS protocol = TLS.V_1_2;

	public void setKeyStoreFile(String keyStoreFile) {
		this.keyStoreFile = keyStoreFile;
	}

	public void setKeyPassword(String keyPassword) {
		this.keyPassword = keyPassword.toCharArray();
	}

	/**
	 * <p>
	 * Default constructor.
	 */
	public DefaultSSLContextCreator() {
	}

	/**
	 * <p>
	 * The constructor of setting values from {@code ServerConfig}.
	 */
	public DefaultSSLContextCreator(Config serverConfig) {
		setServerConfig(serverConfig);
	}

	public void setServerConfig(Config serverConfig) {
		setKeyStoreFile(serverConfig.getHttpsConfig().getKeyStoreFile());
		setKeyPassword(serverConfig.getHttpsConfig().getKeyPassword());
		setKeyStoreType(serverConfig.getHttpsConfig().getKeyStoreType());
		setSSLProtocol(serverConfig.getHttpsConfig().getProtocol());
	}

	public void setKeyStoreType(String type) {
		this.type = KeyStoreType.valueOf(type);
	}

	public void setKeyStoreType(KeyStoreType type) {
		this.type = type;
	}

	public void setSSLProtocol(String protocol) {
		this.protocol = TLS.valueOf(protocol.replace("TLSv", "V_").replace(".", "_"));
	}

	public void setSSLProtocol(TLS protocol) {
		this.protocol = protocol;
	}

	public SSLContext getSSLContext() {
		try {
			URL url = ClassUtils.getURL(keyStoreFile);
			if (url == null) {
				throw new IllegalArgumentException("https.keyStoreFile ["+keyStoreFile+"] file not found.");
			}
			KeyStore keystore = KeyStore.getInstance(type.name());
			keystore.load(url.openStream(), keyPassword);

			KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmfactory.init(keystore, keyPassword);
			KeyManager[] keymanagers = kmfactory.getKeyManagers();
			SSLContext sslcontext = SSLContext.getInstance(protocol.version.getProtocol());
			sslcontext.init(keymanagers, null, null);
			return sslcontext;
		} catch (Exception e) {
			throw new RuntimeIOException(e);
		}
	}
}
