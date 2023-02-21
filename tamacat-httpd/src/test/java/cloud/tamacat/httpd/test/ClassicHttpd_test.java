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
package cloud.tamacat.httpd.test;

import cloud.tamacat.httpd.ClassicHttpd;
import cloud.tamacat.httpd.config.ReverseConfig;
import cloud.tamacat.httpd.config.ServerConfig;
import cloud.tamacat.httpd.config.ServiceConfig;
import cloud.tamacat.httpd.filter.HtmlConvertFilter;
import cloud.tamacat.httpd.filter.ResponseFilter;

public class ClassicHttpd_test {

	public static void main(String[] args) {
		ClassicHttpd.startup(
			ServerConfig.create().port(80)
				.service(ServiceConfig.create().path("/")
					.docsRoot("${server.home}/src/test/resources/htdocs/")
				)
				
				.service(ServiceConfig.create().path("/test/")
					.reverse(ReverseConfig.create().url("http://localhost:1081/")
				)
				.filter(new HtmlConvertFilter())
				.filter(new ResponseFilter().addHeader("X-Test: ABC"))
			).contentEncoding("gzip")
		);
	}
}
