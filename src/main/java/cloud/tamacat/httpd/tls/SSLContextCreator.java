/*
 * Copyright 2009 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.tls;

import javax.net.ssl.SSLContext;

import cloud.tamacat.httpd.config.Config;

public interface SSLContextCreator {

	void setServerConfig(Config serverConfig);
	
	SSLContext getSSLContext();
}
