/*
 * Copyright 2019 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.util;

import java.net.InetAddress;

import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cloud.tamacat.httpd.mock.HttpObjectFactory;

public class AccessLogUtilsTest {
	
	HttpRequest request;
	HttpResponse response;
	HttpContext context;

	@BeforeEach
	public void setUp() throws Exception {
		context = HttpObjectFactory.createHttpContext();
		request = HttpObjectFactory.createHttpRequest("GET", "/test/");
		response = HttpObjectFactory.createHttpResponse(200, "OK");
		
		InetAddress address = InetAddress.getByName("127.0.0.1");
		context.setAttribute(RequestUtils.REMOTE_ADDRESS, address);
	}

	@AfterEach
	public void tearDown() throws Exception {
	}

	@Test
	public void testWriteAccessLog() {
		long time = 123L;
		AccessLogUtils.log(request, response, context, time);
	}
}
