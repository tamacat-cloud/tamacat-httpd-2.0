/*
 * Copyright 2020 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.config;

public class HttpsConig {

	String keyStoreFile;
	String keyPassword;
	String keyStoreType = "PKCS12";
	String protocol = "TLSv1_2";
	String supportProtocol = "TLSv1_2";
	String defaultAlias;
	
	public String getKeyStoreFile() {
		return keyStoreFile;
	}
	
	public void setKeyStoreFile(String keyStoreFile) {
		this.keyStoreFile = keyStoreFile;
	}
	
	public String getKeyPassword() {
		return keyPassword;
	}
	
	public void setKeyPassword(String keyPassword) {
		this.keyPassword = keyPassword;
	}
	
	public String getKeyStoreType() {
		return keyStoreType;
	}
	
	public void setKeyStoreType(String keyStoreType) {
		this.keyStoreType = keyStoreType;
	}
	
	public String getProtocol() {
		return protocol;
	}
	
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
	public String getSupportProtocol() {
		return supportProtocol;
	}
	
	public void setSupportProtocol(String supportProtocol) {
		this.supportProtocol = supportProtocol;
	}
	
	public String getDefaultAlias() {
		return defaultAlias;
	}
	
	public void setDefaultAlias(String defaultAlias) {
		this.defaultAlias = defaultAlias;
	}
}
