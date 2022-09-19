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
package cloud.tamacat.httpd.filter;

import java.io.IOException;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;

import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

/**
 * Response Filter
 */
public class ResponseFilter extends HttpFilter {

	static final Log LOG = LogFactory.getLog(ResponseFilter.class);

	protected HeaderCustomizer headers = new HeaderCustomizer();

	/**
	 * Append Response Headers. ex) "Strict-Transport-Security: max-age=63072000; includeSubDomains; preload"
	 * @param headerValue
	 */
	public void setAddHeader(String headerValue) {
		headers.setAddHeader(headerValue);
	}
	
	public ResponseFilter addHeader(String headerValue) {
		setAddHeader(headerValue);
		return this;
	}
	
	/**
	 * Append Response Headers. ex) "Strict-Transport-Security: max-age=63072000; includeSubDomains; preload"
	 * @param headerValue
	 */
	public void setRemoveHeader(String header) {
		headers.setRemoveHeader(header);
	}
	
	public ResponseFilter removeHeader(String headerName) {
		setRemoveHeader(headerName);
		return this;
	}

	@Override
	protected void handleSubmitResponse(ClassicHttpResponse response) throws HttpException, IOException {
		headers.removeHeaders(response);
		headers.addHeaders(response);
	}
	
	@Override
	public String toString() {
		return "ResponseFilter [headers=" + headers + ", path=" + path + "]";
	}
}
