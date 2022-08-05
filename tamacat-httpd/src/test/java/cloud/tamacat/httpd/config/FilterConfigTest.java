/*
 * Copyright 2019 tamacat.org
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
		ServerConfig config = ServerConfig.load("service.json");
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
