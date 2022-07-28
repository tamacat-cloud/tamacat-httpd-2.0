/*
 * Copyright 2022 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.reverse.html;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cloud.tamacat.httpd.mock.HttpObjectFactory;

class HtmlLinkConvertInterceptorTest {
	
	private HtmlLinkConvertInterceptor target;
	
	@BeforeEach
	public void setUp() throws Exception {
		target = new HtmlLinkConvertInterceptor();
	}

	@AfterEach
	public void tearDown() throws Exception {
	}
	
	@Test
	void testHtmlLinkConvertInterceptor() {
		
	}

	@Test
	void testSetLinkPattern() {
		target.setLinkPattern("aaa");
		assertEquals("aaa", target.linkPatterns.get(0).pattern());
	}

	@Test
	void testProcess() {
		HttpResponse response = HttpObjectFactory.createHttpResponse(200, "OK");
		HttpContext context = HttpObjectFactory.createHttpContext();
		EntityDetails entity = new StringEntity("");
		try {
			target.process(response, entity, context);
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	void testSetContentType() {
		target.setContentType("text/html");
		target.setContentType("text/html; charset=utf8");
		target.setContentType(" text/x-html ");
		target.setContentType("html,plain,css,javascript");
		target.setContentType("");
		target.setContentType(null);
	}

}
