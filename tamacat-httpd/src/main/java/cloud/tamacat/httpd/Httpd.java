/*
 * Copyright 2022 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd;

import cloud.tamacat.httpd.config.ServerConfig;

public class Httpd {

	public static void main(final String[] args) throws Exception {
		startup(args);
	}
	
	public static void startup(final String... args) throws Exception {
		final String json = args.length >= 1 ? args[0] : "service.json";
		final ServerConfig config = ServerConfig.load(json);
		
		if ("async".equalsIgnoreCase(config.getServerType())) {			
			new AsyncHttpd().startup(config);
		} else {
			new ClassicHttpd().startup(config);
		}
	}
}
