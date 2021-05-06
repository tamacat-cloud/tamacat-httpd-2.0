/*
 * Copyright 2020 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
module cloud.tamacat.httpd {
	
	exports cloud.tamacat.httpd;
	exports cloud.tamacat.httpd.core;
	exports cloud.tamacat.httpd.filter;
	exports cloud.tamacat.httpd.reverse;
	exports cloud.tamacat.httpd.web;
	exports cloud.tamacat.httpd.config;
	exports cloud.tamacat.httpd.tls;

	opens cloud.tamacat.httpd;
	opens cloud.tamacat.httpd.core;
	opens cloud.tamacat.httpd.config;
	opens cloud.tamacat.httpd.reverse.handler;
	opens cloud.tamacat.httpd.filter;
	opens cloud.tamacat.httpd.web.handler;
	opens cloud.tamacat.httpd.tls;
	
	requires transitive org.apache.httpcomponents.core5.httpcore5;
	requires transitive org.apache.httpcomponents.core5.httpcore5.h2;
	requires transitive thymeleaf;

	requires transitive cloud.tamacat.core;
	requires transitive com.google.gson;
	requires transitive org.slf4j;
	requires org.apache.tomcat.embed.core;
}