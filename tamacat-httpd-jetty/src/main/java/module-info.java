/*
 * Copyright 2020 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
module cloud.tamacat.httpd.jetty {
	
	exports cloud.tamacat.httpd.jetty;
	opens cloud.tamacat.httpd.jetty;
	
	requires org.eclipse.jetty.server;
	requires org.eclipse.jetty.servlet;
	requires org.eclipse.jetty.apache.jsp;

	requires transitive cloud.tamacat.httpd;
	requires transitive org.eclipse.jetty.webapp;
	requires transitive org.mortbay.apache.jasper;
}