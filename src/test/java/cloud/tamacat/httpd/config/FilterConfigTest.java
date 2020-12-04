/*
 * Copyright 2019 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.config;

import org.junit.jupiter.api.Test;

import cloud.tamacat.util.JsonUtils;

public class FilterConfigTest {

	@Test
	public void testLoad() {
		Config config = Config.load("service.json");
		System.out.println(JsonUtils.stringify(config));
		
		for (ServiceConfig service : config.getServices()) {
			
			service.getFilters().forEach((id, filter) -> {
				System.out.println(id+"="+filter.getFilter(service));
			});
		}
	}
}
