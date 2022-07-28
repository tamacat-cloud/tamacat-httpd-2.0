/*
 * Copyright 2009 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cloud.tamacat.httpd.config.ServerConfig;
import cloud.tamacat.httpd.config.ServiceConfig;
import cloud.tamacat.httpd.core.RequestParameters;
import cloud.tamacat.httpd.mock.HttpObjectFactory;

public class RequestUtilsTest {

	private HttpContext context;

	@BeforeEach
	public void setUp() throws Exception {
		context = HttpObjectFactory.createHttpContext();
		InetAddress address = InetAddress.getByName("127.0.0.1");
		context.setAttribute(RequestUtils.REMOTE_ADDRESS, address);
	}

	@AfterEach
	public void tearDown() throws Exception {
	}

	@Test
	public void testRequestUtils() {
		new RequestUtils();
	}

	@Test
	public void testRequestLine() throws Exception {
		RequestLine line = new RequestLine("GET", "http://localhost/", HttpVersion.HTTP_1_1);
		assertEquals("/", RequestUtils.getRequestLine(line).getUri());

		RequestLine line2 = new RequestLine("GET", "/", HttpVersion.HTTP_1_1);
		assertEquals("/", RequestUtils.getRequestLine(line2).getUri());
		assertSame(line2, RequestUtils.getRequestLine(line2));
	}

	@Test
	public void testRequestPathWithQueryString() throws Exception {
		assertEquals("/", RequestUtils.getRequestPathWithQuery("http://localhost/"));
		assertEquals("/test", RequestUtils.getRequestPathWithQuery("http://localhost/test"));
		assertEquals("/test/", RequestUtils.getRequestPathWithQuery("http://localhost/test/"));
		assertEquals("/test?test=test", RequestUtils.getRequestPathWithQuery("http://localhost/test?test=test"));
		assertEquals("/", RequestUtils.getRequestPathWithQuery("https://localhost/"));

		assertEquals("/", RequestUtils.getRequestPathWithQuery("http://localhost:8080/"));
		assertEquals("/test/", RequestUtils.getRequestPathWithQuery("http://localhost:8080/test/"));
		assertEquals("http://localhost", RequestUtils.getRequestPathWithQuery("http://localhost"));
		assertEquals("ttp://localhost/", RequestUtils.getRequestPathWithQuery("ttp://localhost/"));
		assertEquals("http//localhost/", RequestUtils.getRequestPathWithQuery("http//localhost/"));

		assertEquals("/", RequestUtils.getRequestPathWithQuery("/"));
		assertEquals("/test", RequestUtils.getRequestPathWithQuery("/test"));
		assertEquals("/test/", RequestUtils.getRequestPathWithQuery("/test/"));
		assertEquals("", RequestUtils.getRequestPathWithQuery(""));
	}

	@Test
	public void testSetParameters() throws Exception {
		ClassicHttpRequest request = new BasicClassicHttpRequest("POST", "/test.html");
		RequestUtils.parseParameters(request, new StringEntity("<html></html>"), StandardCharsets.UTF_8);
	}

	@Test
	public void testSetParametersHttpContextRequestParameters() throws Exception {
		RequestParameters params = new RequestParameters();
		params.setParameter("key1", "value1");
		params.setParameter("key2", "");
		RequestUtils.setParameters(context, params);
		
		assertEquals("value1", params.getParameter("key1"));
		assertEquals("", params.getParameter("key2"));
		assertEquals(null, params.getParameter("key3"));
	}

	@Test
	public void testGetRequestPath() {
		assertEquals("/test.html", RequestUtils.getPath("/test.html"));
		assertEquals("/test.html", RequestUtils.getPath("/test.html?id=test"));
	}

	@Test
	public void testGetRemoteIPAddress() {
		String ipaddress = RequestUtils.getRemoteIPAddress(context);
		assertEquals("127.0.0.1", ipaddress);

		HttpContext ctx = new BasicHttpContext();
		assertEquals("", RequestUtils.getRemoteIPAddress(ctx));
	}
	
	@Test
	public void testIsRemoteIPv6Address() {
		HttpContext ctx = new BasicHttpContext();
		ctx.setAttribute(RequestUtils.REMOTE_ADDRESS, null);
		assertEquals(false, RequestUtils.isRemoteIPv6Address(ctx));
	}
	
	@Test
	public void testGetRequestHost() throws Exception {
		HttpRequest request = new BasicHttpRequest("GET", "/test.html");

		URL url = RequestUtils.getRequestURL(request, null);
		assertNull(url);

		request.setHeader(HttpHeaders.HOST, "example.com");
		url = RequestUtils.getRequestURL(request, null);
		assertEquals("http://example.com/test.html", url.toString());

		ServerConfig serverConfig = new ServerConfig();
		serverConfig.port(8080);
		ServiceConfig serviceUrl = new ServiceConfig();
		serviceUrl.setServerConfig(serverConfig);
		url = RequestUtils.getRequestURL(request, null, serviceUrl);
		assertEquals("http://example.com:8080/test.html", url.toString());

		serverConfig = new ServerConfig();
		serverConfig.port(443);
		serverConfig.protocol("https");
		serviceUrl = new ServiceConfig();
		serviceUrl.setServerConfig(serverConfig);
		url = RequestUtils.getRequestURL(request, null, serviceUrl);
		assertEquals("https://example.com/test.html", url.toString());
	}

	@Test
	public void testGetRequestHostURL() {
		HttpRequest request = new BasicHttpRequest("GET", "/test.html");

		String url = RequestUtils.getRequestHostURL(request, null, null);
		assertNull(url);

		request.setHeader(HttpHeaders.HOST, "example.com");
		url = RequestUtils.getRequestHostURL(request, null, null);
		assertEquals("http://example.com", url);

		ServerConfig serverConfig = new ServerConfig();
		serverConfig.port(8080);
		ServiceConfig serviceUrl = new ServiceConfig();
		serviceUrl.setServerConfig(serverConfig);
		url = RequestUtils.getRequestHostURL(request, null, serviceUrl);
		assertEquals("http://example.com:8080", url.toString());

		serverConfig = new ServerConfig();
		serverConfig.port(443);
		serverConfig.protocol("https");
		serviceUrl = new ServiceConfig();
		serviceUrl.setServerConfig(serverConfig);
		url = RequestUtils.getRequestHostURL(request, null, serviceUrl);
		assertEquals("https://example.com", url.toString());
	}

	@Test
	public void testGetRequestHostHttpRequestHttpContext() {
		HttpRequest request = new BasicHttpRequest("GET", "/test.html");

		String url = RequestUtils.getRequestHost(request, context);
		assertNull(url);

		request.setHeader(HttpHeaders.HOST, "example.com");
		url = RequestUtils.getRequestHost(request, context);
		assertEquals("example.com", url);

		request.setHeader(HttpHeaders.HOST, "example.com:8080");
		url = RequestUtils.getRequestHost(request, context);
		assertEquals("example.com", url);

		request.setHeader(HttpHeaders.HOST, "example.com:80");
		url = RequestUtils.getRequestHost(request, context);
		assertEquals("example.com", url);
	}

	@Test
	public void testGetInputStream() throws IOException {
		assertNotNull(RequestUtils.getInputStream(new StringEntity("<html></html>")));
		assertNull(RequestUtils.getInputStream(null));
	}

	@Test
	public void testIsMultipart() {
		Header header = new BasicHeader(HttpHeaders.CONTENT_TYPE, "multipart/form-data");
		HttpRequest request = new BasicHttpRequest("POST", "/test.html");
		request.setHeader(header);

		assertTrue(RequestUtils.isMultipart(request));

		HttpRequest request2 = new BasicHttpRequest("GET", "/test.html");
		assertFalse(RequestUtils.isMultipart(request2));
	}

	@Test
	public void testDecode() {
		assertEquals("", RequestUtils.decode("", "UTF-8"));
		assertEquals("abc def", RequestUtils.decode("abc%20def", "UTF-8"));
	}

	@Test
	public void testGetPathPrefix() {
		HttpRequest request = new BasicHttpRequest("GET", "/test.html");
		assertEquals("/", RequestUtils.getPathPrefix(request));

		request = new BasicHttpRequest("GET", "/test/index.html");
		assertEquals("/test/", RequestUtils.getPathPrefix(request));

		request = new BasicHttpRequest("GET", "/test/aaaa/index.html");
		assertEquals("/test/aaaa/", RequestUtils.getPathPrefix(request));
	}
	
//	@Test
//	public void testGetTlsClientAuthPrincipal() throws Exception {
//		DefaultSSLContextCreator creator = new DefaultSSLContextCreator();
//		creator.setKeyStoreFile("https/client-cert/localhost.p12");
//		creator.setKeyPassword("changeit");
//		creator.setKeyStoreType("PKCS12");
//		creator.setSSLProtocol("TLSv1.2");
//		
//		ServerHttpConnection conn = new ServerHttpConnection(8192);
//		//conn.bind(socket);
//		RequestUtils.setTlsClientAuthPrincipal(conn, context);
//		
//		context.setAttribute(RequestUtils.TLS_CLIENT_AUTH_PRINCIPAL_CONTEXT_KEY, "test");
//		RequestUtils.setTlsClientAuthPrincipal(context);
//		assertEquals("test", RequestUtils.getTlsClientAuthPrincipal(context));
//	}
}
