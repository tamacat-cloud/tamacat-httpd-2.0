/*
 * Copyright 2020 tamacat.org
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
open module cloud.tamacat.httpd {
	
	exports cloud.tamacat.httpd;
	exports cloud.tamacat.httpd.core;
	exports cloud.tamacat.httpd.filter;
	exports cloud.tamacat.httpd.filter.async;
	exports cloud.tamacat.httpd.reverse.listener;
	exports cloud.tamacat.httpd.reverse;
	exports cloud.tamacat.httpd.reverse.async;
	exports cloud.tamacat.httpd.reverse.html;
	exports cloud.tamacat.httpd.web;
	exports cloud.tamacat.httpd.web.async;
	exports cloud.tamacat.httpd.config;
	exports cloud.tamacat.httpd.core.tls;
	exports cloud.tamacat.httpd.util;
	
	requires transitive org.apache.httpcomponents.core5.httpcore5;
	requires transitive thymeleaf;

	requires transitive tamacat.core;
	requires transitive com.google.gson;
	requires transitive org.slf4j;	
}