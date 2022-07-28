/*
 * Copyright 2009 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class HtmlUtilsTest {

	private static Pattern pattern = HtmlUtils.CHARSET_PATTERN;

	@BeforeEach
	public void setUp() throws Exception {
	}

	@Test
	public void testGetCharset() {
		Header header = new BasicHeader("Content-Type", "text/html; charset=UTF-8");
		assertEquals("utf-8", HtmlUtils.getCharSet(header));
	}

	@Test
	public void testGetCharsetDefault() {
		Header header = new BasicHeader("Content-Type", "text/html");
		assertEquals(null, HtmlUtils.getCharSet(header));
	}

	@Test
	public void testGetCharSetFromMetaTag0() {
		String html1 = "<html><META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=utf-8\"></html>";
		Matcher matcher = pattern.matcher(html1);
		if (matcher.find()) {
			assertEquals("utf-8", matcher.group(4));
		}
	}

	@Test
	public void testGetCharSetFromMetaTag() {
		String html = "<html><META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\"></html>";
		String result = HtmlUtils.getCharSetFromMetaTag(html, "");
		assertEquals("utf-8", result);
	}

	@Test
	public void testGetCharSetFromMetaTag2() {
		String html = "<html><META HTTP-EQUIV='Content-Type' CONTENT='text/html; charset=UTF-8'></html>";
		String result = HtmlUtils.getCharSetFromMetaTag(html, "");
		assertEquals("utf-8", result);
	}

	@Test
	public void testGetCharSetFromMetaTag3() {
		String html = "<html><META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html;charset=UTF-8\"></html>";
		String result = HtmlUtils.getCharSetFromMetaTag(html, "");
		assertEquals("utf-8", result);
	}

	@Test
	public void testGetCharSetFromMetaTagDefault() {
		String html = "<html><META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html\"></html>";
		String result = HtmlUtils.getCharSetFromMetaTag(html, "utf-8");
		assertEquals("utf-8", result);
	}
}
