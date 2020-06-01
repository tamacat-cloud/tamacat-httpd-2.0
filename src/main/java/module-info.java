module cloud.tamacat.httpd {
	exports cloud.tamacat.httpd;
	exports cloud.tamacat.httpd.reverse;
	exports cloud.tamacat.httpd.web;
	exports cloud.tamacat.httpd.config;

	opens cloud.tamacat.httpd.config;
	opens cloud.tamacat.httpd.reverse.handler;
	opens cloud.tamacat.httpd.web.handler;
	
	requires httpcore5;
	requires httpcore5.h2;
	requires thymeleaf;

	requires transitive cloud.tamacat.core;
	requires transitive org.slf4j;
}