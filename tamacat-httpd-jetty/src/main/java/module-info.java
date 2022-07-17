/*
 * Copyright 2020 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
module cloud.tamacat.httpd.jetty {
	
	exports cloud.tamacat.httpd.jetty;
	
	requires transitive org.apache.httpcomponents.core5.httpcore5;
	requires transitive org.apache.httpcomponents.core5.httpcore5.h2;
	requires transitive thymeleaf;

	requires transitive cloud.tamacat.core;
	requires transitive cloud.tamacat.httpd;
	requires transitive com.google.gson;
	requires transitive org.slf4j;
	
	requires org.eclipse.jetty.server;
	requires org.eclipse.jetty.servlet;
	requires org.eclipse.jetty.webapp;
	requires org.eclipse.jetty.apache.jsp;
	requires org.mortbay.apache.jasper;
	
}