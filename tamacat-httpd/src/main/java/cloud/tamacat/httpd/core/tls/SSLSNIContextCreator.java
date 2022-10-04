/*
 * Copyright 2014 tamacat.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cloud.tamacat.httpd.core.tls;

import java.net.URL;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Enumeration;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509ExtendedKeyManager;

import cloud.tamacat.httpd.config.ServerConfig;
import cloud.tamacat.httpd.error.InternalServerErrorException;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;
import cloud.tamacat.util.ClassUtils;
import cloud.tamacat.util.StringUtils;

/**
 * SSLContext for SNI (Multiple domain support)
 * "Server Name Indication" of TLS Extensions (RFC 6066).
 *
 * Add default domain in server.properties.
 * ex) https.defaultAlias=www.examples.com
 */
public class SSLSNIContextCreator extends DefaultSSLContextCreator {

	static final Log LOG = LogFactory.getLog(SSLSNIContextCreator.class);

	protected String defaultAlias;

	public SSLSNIContextCreator() {}

	public SSLSNIContextCreator(ServerConfig serverConfig) {
		super(serverConfig);
	}

	@Override
	public void setServerConfig(ServerConfig serverConfig) {
		super.setServerConfig(serverConfig);
		setDefaultAlias(serverConfig.getHttpsConfig().getDefaultAlias());
	}

	public SSLContext getSSLContext() {
		String defaultAlias = getDefaultAlias();
		if (StringUtils.isEmpty(defaultAlias)) {
			return super.getSSLContext();
		}
		try {
			URL url = ClassUtils.getURL(keyStoreFile);
			if (url == null) {
				throw new IllegalArgumentException("https.keyStoreFile ["+keyStoreFile+"] file not found.");
			}
			KeyStore keystore = KeyStore.getInstance(type.name());
			keystore.load(url.openStream(), keyPassword);
			KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmfactory.init(keystore, keyPassword);

			X509ExtendedKeyManager x509KeyManager = null;
			KeyManager[] keymanagers = kmfactory.getKeyManagers();
			for (KeyManager keyManager : keymanagers) {
				if (keyManager instanceof X509ExtendedKeyManager) {
					x509KeyManager = (X509ExtendedKeyManager) keyManager;
					break;
				}
			}
			SSLContext sslcontext = SSLContext.getInstance(protocol.version.getProtocol());
			if (x509KeyManager == null) {
				sslcontext.init(keymanagers, getTrustManager(), null);
			} else {
				SNIKeyManager sniKeyManager = new SNIKeyManager(x509KeyManager, defaultAlias);
				sslcontext.init(new KeyManager[] { sniKeyManager }, getTrustManager(), null);
				if (LOG.isDebugEnabled()) {
					LOG.debug("TLS/SNI default=" + defaultAlias);
					Enumeration<String> en = keystore.aliases();
					while (en.hasMoreElements()) {
						LOG.debug("TLS/SNI alias=" + en.nextElement());
					}
				}
			}
			return sslcontext;
		} catch (Exception e) {
			throw new InternalServerErrorException(e);
			//throw new RuntimeIOException(e);
		}
	}

	public void setDefaultAlias(String defaultAlias) {
		if (StringUtils.isNotEmpty(defaultAlias)) {
			this.defaultAlias = defaultAlias;
		}
	}

	public String getDefaultAlias() {
		return defaultAlias;
	}

	@Override
	public String toString() {
		return "SSLSNIContextCreator [defaultAlias=" + defaultAlias + ", keyStoreFile=" + keyStoreFile
				+ ", keyPassword=" + Arrays.toString(keyPassword) + ", type=" + type + ", protocol=" + protocol + "]";
	}
}
