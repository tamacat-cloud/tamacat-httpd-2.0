/*
 * Copyright 2022 tamacat.org
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
