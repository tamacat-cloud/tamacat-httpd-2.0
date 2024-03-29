/*
 * Copyright 2010 tamacat.org
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
package cloud.tamacat.httpd.reverse.html;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.util.regex.Pattern;

import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cloud.tamacat.httpd.util.HtmlUtils;

public class LinkConvertingEntityTest {

	@BeforeEach
	public void setUp() throws Exception {
	}

	@AfterEach
	public void tearDown() throws Exception {
	}

	@Test
	public void testWriteToOutputStream() throws Exception {
		StringEntity html = new StringEntity("<html><a href=\"/aaa/test.html\">aaa</a></html>\r\n");
		String before = "/aaa/";
		String after = "/bbb/";
		LinkConvertingEntity entity = new LinkConvertingEntity(html, before, after);
		assertNotNull(entity);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		entity.writeTo(out);
		//System.out.println(new String(out.toByteArray()));
		assertEquals(html.getContentLength(), entity.getContentLength());
		assertEquals("<html><a href=\"/bbb/test.html\">aaa</a></html>\r\n", new String(out.toByteArray()));
	}

	@Test
	public void testWriteToOutputStream2() throws Exception {
		StringEntity html = new StringEntity("<html><a href=\"/aaaaa/test.html\">aaa</a></html>\r\n");
		String before = "/aaaaa/";
		String after = "/bbb/";
		LinkConvertingEntity entity = new LinkConvertingEntity(html, before, after);
		assertNotNull(entity);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		entity.writeTo(out);
		assertEquals(html.getContentLength()-2, entity.getContentLength());
		assertEquals("<html><a href=\"/bbb/test.html\">aaa</a></html>\r\n", new String(out.toByteArray()));
	}

	@Test
	public void testWriteToOutputStream3() throws Exception {
		StringEntity html = new StringEntity("<html><a href=\"/aaa/test.html\">aaa</a></html>\r\n");
		String before = "/aaa/";
		String after = "/bbbbb/";
		LinkConvertingEntity entity = new LinkConvertingEntity(html, before, after);
		assertNotNull(entity);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		entity.writeTo(out);
		assertEquals(html.getContentLength()+2, entity.getContentLength());
		assertEquals("<html><a href=\"/bbbbb/test.html\">aaa</a></html>\r\n", new String(out.toByteArray()));
	}

	@Test
	public void testWriteToOutputStream4() throws Exception {
		StringEntity html = new StringEntity("<html><body background=\"/aaa/images/bg.gif\"><a href=\"/aaa/test.html\">aaa</a></body></html>\r\n");
		String before = "/aaa/";
		String after = "/bbbbb/";
		Pattern p = Pattern.compile("<[^<]*\\s+(href|src|action|background|.*[0-9]*;?url)=(?:\'|\")?([^('|\")]*)(?:\'|\")?[^>]*>", Pattern.CASE_INSENSITIVE);
		LinkConvertingEntity entity = new LinkConvertingEntity(html, before, after, p);
		assertNotNull(entity);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		entity.writeTo(out);
		assertEquals(html.getContentLength()+2+2, entity.getContentLength());
		assertEquals("<html><body background=\"/bbbbb/images/bg.gif\"><a href=\"/bbbbb/test.html\">aaa</a></body></html>\r\n", new String(out.toByteArray()));
	}

	@Test
	public void testWriteToOutputStream_NAME() throws Exception {
		StringEntity html = new StringEntity("<html><a href=\"/aaa/test.html\">/aaa/</a></html>\r\n");
		String before = "/aaa/";
		String after = "/bbbbb/";
		LinkConvertingEntity entity = new LinkConvertingEntity(html, before, after);
		assertNotNull(entity);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		entity.writeTo(out);
		assertEquals(html.getContentLength()+2, entity.getContentLength());
		assertEquals("<html><a href=\"/bbbbb/test.html\">/aaa/</a></html>\r\n", new String(out.toByteArray()));
	}

	@Test
	public void testWriteToOutputStream_ERROR() throws Exception {
		StringEntity html = new StringEntity("<html><a href=\"/test.html\">/aaaaa/</a></html>\r\n");
		String before = "/aaaaa/";
		String after = "/bbbbb/";
		LinkConvertingEntity entity = new LinkConvertingEntity(html, before, after);
		assertNotNull(entity);

		try {
			entity.writeTo(null);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Output stream may not be null", e.getMessage());
		}
	}

	@Test
	public void testUseLinkConvert_A_HREF() {
		String html = "<html><a href=\"/test/index.html\">TEST</a></html>";
		html = LinkConvertingEntity.convertLink(html, "/test/", "/zzzz/", HtmlUtils.LINK_PATTERN).getData();
		assertEquals("<html><a href=\"/zzzz/index.html\">TEST</a></html>", html);
	}

	@Test
	public void testUseLinkConvert_A_HREF2() {
		String html = "<html><a href='/test/index.html'>TEST</a></html>";
		html = LinkConvertingEntity.convertLink(html, "/test/", "/zzzz/", HtmlUtils.LINK_PATTERN).getData();
		assertEquals("<html><a href='/zzzz/index.html'>TEST</a></html>", html);
	}

	@Test
	public void testUseLinkConvert_A_HREF3() {
		String html = "<html><a href=/test/index.html>TEST</a></html>";
		html = LinkConvertingEntity.convertLink(html, "/test/", "/zzzz/", HtmlUtils.LINK_PATTERN).getData();
		assertEquals("<html><a href=/zzzz/index.html>TEST</a></html>", html);
	}

	@Test
	public void testUseLinkConvert_A_HREF4() {
		String html = "<html><a href=\"test/index.html\">TEST</a></html>";
		html = LinkConvertingEntity.convertLink(html, "/", "/zzzz/", HtmlUtils.LINK_PATTERN).getData();
		assertEquals("<html><a href=\"test/index.html\">TEST</a></html>", html);
	}
	
	@Test
	public void testUseLinkConvert_ACTION() {
		String html = "<html><form action=\"/test/main.do\">TEST</form></html>";
		html = LinkConvertingEntity.convertLink(html, "/test/", "/zzzz/", HtmlUtils.LINK_PATTERN).getData();
		assertEquals("<html><form action=\"/zzzz/main.do\">TEST</form></html>", html);
	}

	@Test
	public void testUseLinkConvert_ACTION2() {
		String html = "<html><form action='/test/main.do'>TEST</form></html>";
		html = LinkConvertingEntity.convertLink(html, "/test/", "/zzzz/", HtmlUtils.LINK_PATTERN).getData();
		assertEquals("<html><form action='/zzzz/main.do'>TEST</form></html>", html);
	}

	@Test
	public void testUseLinkConvert_ACTION3() {
		String html = "<html><form action=/test/main.do>TEST</form></html>";
		html = LinkConvertingEntity.convertLink(html, "/test/", "/zzzz/", HtmlUtils.LINK_PATTERN).getData();
		assertEquals("<html><form action=/zzzz/main.do>TEST</form></html>", html);
	}

	@Test
	public void testUseLinkConvert_IMG_SRC() {
		String html = "<html><img src=\"/test/images/test.jpg\">TEST</form></html>";
		html = LinkConvertingEntity.convertLink(html, "/test/", "/zzzz/", HtmlUtils.LINK_PATTERN).getData();
		assertEquals("<html><img src=\"/zzzz/images/test.jpg\">TEST</form></html>", html);
	}

	@Test
	public void testUseLinkConvert_IMG_SRC2() {
		String html = "<html><img src='/test/images/test.jpg'>TEST</form></html>";
		html = LinkConvertingEntity.convertLink(html, "/test/", "/zzzz/", HtmlUtils.LINK_PATTERN).getData();
		assertEquals("<html><img src='/zzzz/images/test.jpg'>TEST</form></html>", html);
	}

	@Test
	public void testUseLinkConvert_IMG_SRC3() {
		String html = "<html><img src=/test/images/test.jpg>TEST</form></html>";
		html = LinkConvertingEntity.convertLink(html, "/test/", "/zzzz/", HtmlUtils.LINK_PATTERN).getData();
		assertEquals("<html><img src=/zzzz/images/test.jpg>TEST</form></html>", html);
	}

	@Test
	public void testUseLinkConvert_META_URL() {
		String html = "<html><meta http-equiv=\"Refresh\" content=\"0;url=/test/index.html\" /></html>";
		html = LinkConvertingEntity.convertLink(html, "/test/", "/zzzz/", HtmlUtils.LINK_PATTERN).getData();
		assertEquals("<html><meta http-equiv=\"Refresh\" content=\"0;url=/zzzz/index.html\" /></html>", html);
	}

	@Test
	public void testUseLinkConvert_META_URL2() {
		String html = "<html><meta http-equiv='Refresh' content='100;url=/test/index.html' /></html>";
		html = LinkConvertingEntity.convertLink(html, "/test/", "/zzzz/", HtmlUtils.LINK_PATTERN).getData();
		assertEquals("<html><meta http-equiv='Refresh' content='100;url=/zzzz/index.html' /></html>", html);
	}

	@Test
	public void testUseLinkConvert_META_URL3() {
		String html = "<html><meta http-equiv=Refresh content=\"0;url=/test/index.html\" /></html>";
		html = LinkConvertingEntity.convertLink(html, "/test/", "/zzzz/", HtmlUtils.LINK_PATTERN).getData();
		assertEquals("<html><meta http-equiv=Refresh content=\"0;url=/zzzz/index.html\" /></html>", html);
	}

	@Test
	public void testUseLinkConvert_META_URL4() {
		String html = "<html><meta http-equiv=\"Refresh\" content=\"0; url=/test/index.html\" /></html>";
		html = LinkConvertingEntity.convertLink(html, "/test/", "/zzzz/", HtmlUtils.LINK_PATTERN).getData();
		assertEquals("<html><meta http-equiv=\"Refresh\" content=\"0; url=/zzzz/index.html\" /></html>", html);
	}
}
