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

public class FilterConfigTest {
	
	static final Log LOG = LogFactory.getLog(FilterConfigTest.class);

	@Test
	public void testLoad() {
		Config config = Config.load("service.json");
		LOG.trace(JsonUtils.stringify(config));
		
		assertEquals(4, config.getServices().size());
		for (ServiceConfig service : config.getServices()) {
			LOG.trace(JsonUtils.stringify(service));
			service.getFilters().forEach((id, filter) -> {
				LOG.trace(id+"="+filter.getFilter(service));
			});
		}
	}
}
