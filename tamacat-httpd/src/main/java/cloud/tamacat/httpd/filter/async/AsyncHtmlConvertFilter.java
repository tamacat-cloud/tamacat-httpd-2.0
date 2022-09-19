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
package cloud.tamacat.httpd.filter.async;

import java.io.IOException;

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.protocol.HttpContext;

import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

/**
 * Response Filter
 */
public class AsyncHtmlConvertFilter extends AsyncHttpFilter {

	static final Log LOG = LogFactory.getLog(AsyncHtmlConvertFilter.class);
	
	@Override
	protected void handleRequest(HttpRequest request, EntityDetails entityDetails, HttpContext context) throws HttpException, IOException {
		request.removeHeaders("Accept-Encoding");
	}
	
	@Override
	protected void handleSubmitResponse(HttpResponse response, AsyncEntityProducer entityProducer) throws HttpException, IOException {
		LOG.trace("#submitResponse");
	}
}
