/*
 * Copyright 2019 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;
import cloud.tamacat.util.JsonUtils;

public class ConfigTest {

	static final Log LOG = LogFactory.getLog(ConfigTest.class);
	
	@Test
	public void testLoad_service_json() {
		Config config = Config.load("service.json");
		LOG.trace(JsonUtils.stringify(config));
		assertEquals(80, config.getPort());
		assertEquals(false, config.useHttps());
	}
	
	@Test
	public void testLoad_service_https_json() {
		Config config = Config.load("service-https.json");
		LOG.trace(JsonUtils.stringify(config));
		assertEquals(443, config.getPort());
		assertEquals(true, config.useHttps());
		
		assertEquals(true, config.useHttps());
		assertEquals("TLSv1.2", config.getHttpsConfig().getProtocol());
	}
}
