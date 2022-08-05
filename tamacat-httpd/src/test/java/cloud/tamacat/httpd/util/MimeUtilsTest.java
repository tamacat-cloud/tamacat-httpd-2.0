/*
 * Copyright 2014 tamacat.org
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
