/*
 * Copyright 2020 tamacat.org
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
package cloud.tamacat.httpd.filter;

import java.io.IOException;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.protocol.HttpContext;

import cloud.tamacat.httpd.reverse.html.LinkConvertingEntity;
import cloud.tamacat.httpd.util.HeaderUtils;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

/**
 * Response Filter
 */
public class HtmlConvertFilter extends HttpFilter {

	static final Log LOG = LogFactory.getLog(HtmlConvertFilter.class);

	@Override
	protected void handleRequest(ClassicHttpRequest request, HttpContext context) throws HttpException, IOException {
		request.removeHeaders("Accept-Encoding");
	}
	
	@Override
	protected void handleSubmitResponse(ClassicHttpResponse response) throws HttpException, IOException {
		if (HeaderUtils.getHeader(response, "Content-Type").startsWith("text/html")) {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				response.setEntity(new LinkConvertingEntity(entity, "/", "/"));
				//response.setHeader("Transfer-Encoding", "chunked"); //Transfer-Encoding:chunked
				//response.removeHeaders("Content-Length");
			}
		}
	}
}
