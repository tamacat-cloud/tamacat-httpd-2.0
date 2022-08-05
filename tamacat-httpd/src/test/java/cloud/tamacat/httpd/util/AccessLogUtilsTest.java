/*
 * Copyright 2019 tamacat.org
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
