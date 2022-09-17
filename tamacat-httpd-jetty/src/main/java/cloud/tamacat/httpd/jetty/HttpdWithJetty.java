/*
 * Copyright 2022 tamacat.org
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
package cloud.tamacat.httpd.jetty;

import cloud.tamacat.httpd.config.ServerConfig;

public class HttpdWithJetty {

	public static void main(final String[] args) {
		startup(args);
	}
	
	public static void startup(final String... args) {
		final String json = args.length >= 1 ? args[0] : "service.json";
		startup(ServerConfig.load(json));
	}
	
	public static void startup(final ServerConfig config) {
		if (config.isAsync()) {
			AsyncHttpdWithJetty.startup(config);
		} else {
			ClassicHttpdWithJetty.startup(config);
		}
	}
}
