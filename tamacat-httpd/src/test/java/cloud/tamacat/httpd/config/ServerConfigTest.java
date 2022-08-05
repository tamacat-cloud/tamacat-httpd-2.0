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

public class ServerConfigTest {

	static final Log LOG = LogFactory.getLog(ServerConfigTest.class);
	
	@Test
	public void testLoad_service_json() {
		ServerConfig config = ServerConfig.load("service.json");
		LOG.trace(JsonUtils.stringify(config));
		assertEquals(80, config.getPort());
		assertEquals(false, config.useHttps());
	}
	
	@Test
	public void testLoad_service_https_json() {
		ServerConfig config = ServerConfig.load("service-https.json");
		LOG.trace(JsonUtils.stringify(config));
		assertEquals(443, config.getPort());
		assertEquals(true, config.useHttps());
		
		assertEquals(true, config.useHttps());
		assertEquals("TLSv1.2", config.getHttpsConfig().getProtocol());
	}
}
