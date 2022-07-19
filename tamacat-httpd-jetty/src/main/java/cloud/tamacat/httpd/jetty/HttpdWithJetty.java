/*
 * Copyright 2022 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.jetty;

import cloud.tamacat.httpd.config.ServerConfig;

public class HttpdWithJetty {

	public static void main(final String[] args) {
		startup(args);
	}
	
	public static void startup(final String... args) {
		final String json = args.length >= 1 ? args[0] : "service.json";
		final ServerConfig config = ServerConfig.load(json);
		
		if ("async".equalsIgnoreCase(config.getServerType())) {			
			new AsyncHttpdWithJetty().startup(config);
		} else {
			new ClassicHttpdWithJetty().startup(config);
		}
	}
}
