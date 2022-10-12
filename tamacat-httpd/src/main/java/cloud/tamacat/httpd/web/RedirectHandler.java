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
package cloud.tamacat.httpd.web;

import java.io.IOException;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;

import cloud.tamacat.httpd.config.ReverseConfig;
import cloud.tamacat.httpd.config.ServiceConfig;
import cloud.tamacat.httpd.error.NotFoundException;
import cloud.tamacat.httpd.util.ReverseUtils;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

public class RedirectHandler implements HttpRequestHandler {
	
	static final Log ACCESS = LogFactory.getLog("Access");
	static final Log LOG = LogFactory.getLog(RedirectHandler.class);
	
	String target;
	int statusCode = HttpStatus.SC_MOVED_TEMPORARILY;
	
	protected final HttpHost targetHost;
	protected final ServiceConfig serviceConfig;
	protected final ReverseConfig reverseConfig;
	
	public RedirectHandler(final HttpHost targetHost, final ServiceConfig serviceConfig) {
		this.targetHost = targetHost;
		this.serviceConfig = serviceConfig;
		this.reverseConfig = serviceConfig.getReverse();
	}
	
	@Override
	public void handle(final ClassicHttpRequest request, final ClassicHttpResponse response, final HttpContext context)
			throws HttpException, IOException {
		try {			
			final String reverseTargetPath = ReverseUtils.getReverseTargetPath(reverseConfig, request.getPath());
			final ClassicHttpRequest outgoingRequest = new BasicClassicHttpRequest(
					request.getMethod(), targetHost, reverseTargetPath);
						
			LOG.debug(request + " [redirect] -> " + outgoingRequest);
			response.setHeader("Location", outgoingRequest.getUri().toString());
			response.setCode(statusCode);
			ACCESS.info(request+" 302 [Found]");
		} catch (Exception e) {
			throw new NotFoundException(e.getMessage(), e);
		}			
	}
	
	public RedirectHandler redirect(String target) {
		this.target = target;
		return this;
	}
	
	public RedirectHandler statusCode(int statusCode) {
		this.statusCode = statusCode;
		return this;
	}
	
}
