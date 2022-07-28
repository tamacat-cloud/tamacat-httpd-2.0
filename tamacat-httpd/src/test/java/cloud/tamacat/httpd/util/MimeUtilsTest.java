/*
 * Copyright 2014 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MimeUtilsTest {

	@BeforeEach
	public void setUp() throws Exception {
	}

	@AfterEach
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetContentType() {
		assertEquals("text/plain", MimeUtils.getContentType("test.txt"));
		assertEquals("text/html", MimeUtils.getContentType("test.html"));
		
		assertEquals("application/pdf", MimeUtils.getContentType("test.pdf"));
		
		assertEquals("application/javascript", MimeUtils.getContentType("test.js"));
		assertEquals("application/javascript", MimeUtils.getContentType("test.js?12345"));
		
		assertEquals("text/css", MimeUtils.getContentType("test.css"));
		assertEquals("text/css", MimeUtils.getContentType("test.css?ver=1.0"));
		
		assertEquals("application/xml", MimeUtils.getContentType("test.xml"));
		assertEquals("application/json", MimeUtils.getContentType("test.json"));
		
		assertEquals("image/x-icon", MimeUtils.getContentType("/favicon.ico"));
		assertEquals("font/woff", MimeUtils.getContentType("/fonts/test.woff"));
		assertEquals("font/woff2", MimeUtils.getContentType("/fonts/test.woff2"));
		
		assertEquals(null, MimeUtils.getContentType(null));
		assertEquals(null, MimeUtils.getContentType(""));
		assertEquals("text/plain", MimeUtils.getContentType("text"));
		
		//add src/test/resources/mime-types.properties
		assertEquals("application/x-test", MimeUtils.getContentType("test.test"));
		assertEquals("text/plain", MimeUtils.getContentType("test.test.txt"));
	}
}
