/*
 * Copyright (c) 2019 tamacat.org
 */
package cloud.tamacat.httpd.config;

import org.junit.jupiter.api.Test;

import cloud.tamacat.util.JsonUtils;

public class ConfigTest {

	@Test
	public void testLoad() {
		Config config = Config.load("service.json");
		System.out.println(JsonUtils.stringify(config.toJson().toString()));
	}
}
