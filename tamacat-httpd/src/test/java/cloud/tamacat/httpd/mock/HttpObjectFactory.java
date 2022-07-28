/*
 * Copyright 2022 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.mock;

import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;

public class HttpObjectFactory {

	public static HttpRequest createHttpRequest(String method, String uri) {
		HttpRequest req = null;
		if ("POST".equalsIgnoreCase(method)) {
			req = new BasicClassicHttpRequest(method, uri);
		} else {
			req = new BasicClassicHttpRequest(method, uri);
		}
		req.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
		return req;
	}

	public static HttpResponse createHttpResponse(int status, String reason) {
		HttpResponse resp = new BasicHttpResponse(status, reason);
		resp.setVersion(HttpVersion.HTTP_1_1);
		return resp;
	}

	public static HttpResponse createHttpResponse(ProtocolVersion ver, int status, String reason) {
		HttpResponse resp = new BasicHttpResponse(status, reason);
		resp.setVersion(ver);
		return resp;
	}

	public static HttpContext createHttpContext() {
		return new BasicHttpContext();
	}
}
