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
package cloud.tamacat.httpd.reverse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.impl.bootstrap.HttpRequester;
import org.apache.hc.core5.http.impl.bootstrap.RequesterBootstrap;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

import cloud.tamacat.httpd.config.ReverseConfig;
import cloud.tamacat.httpd.config.ServiceConfig;
import cloud.tamacat.httpd.reverse.listener.TraceConnPoolListener;
import cloud.tamacat.httpd.reverse.listener.TraceHttp1StreamListener;
import cloud.tamacat.httpd.util.AccessLogUtils;
import cloud.tamacat.httpd.util.ReverseUtils;
import cloud.tamacat.httpd.util.TextUtils;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

/**
 * HTTP/1.1 reverse proxy using classic I/O.
 * 
 * @see
 * https://github.com/apache/httpcomponents-core/blob/5.1.x/httpcore5/src/test/java/org/apache/hc/core5/http/examples/ClassicReverseProxyExample.java
 */
public class ReverseProxyHandler implements HttpRequestHandler {

	static final Log LOG = LogFactory.getLog(ReverseProxyHandler.class);

	//https://datatracker.ietf.org/doc/html/rfc2616#section-13.5.1
	final static Set<String> HOP_BY_HOP = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
		TextUtils.toLowerCase(HttpHeaders.HOST),
		TextUtils.toLowerCase(HttpHeaders.CONTENT_LENGTH),
		TextUtils.toLowerCase(HttpHeaders.CONNECTION),
		TextUtils.toLowerCase(HttpHeaders.KEEP_ALIVE),
		TextUtils.toLowerCase(HttpHeaders.PROXY_AUTHENTICATE),
		TextUtils.toLowerCase(HttpHeaders.PROXY_AUTHORIZATION),
		TextUtils.toLowerCase(HttpHeaders.TE),
		TextUtils.toLowerCase(HttpHeaders.TRAILER),
		TextUtils.toLowerCase(HttpHeaders.TRANSFER_ENCODING),
		TextUtils.toLowerCase(HttpHeaders.UPGRADE)
	)));

	protected final HttpHost targetHost;
	protected final ServiceConfig serviceConfig;
	protected final ReverseConfig reverseConfig;
	protected final RequesterBootstrap requesterBootstrap;
	
	public ReverseProxyHandler(final HttpHost targetHost, final ServiceConfig serviceConfig) {
		this.targetHost = targetHost;
		this.serviceConfig = serviceConfig;
		this.reverseConfig = serviceConfig.getReverse();
		this.requesterBootstrap = createRequesterBootstrap();
	}

	@Override
	public void handle(final ClassicHttpRequest incomingRequest, final ClassicHttpResponse outgoingResponse,
			final HttpContext serverContext) throws HttpException, IOException {
		final long startTime = System.currentTimeMillis();
		final HttpCoreContext clientContext = HttpCoreContext.create();
		serverContext.setAttribute(ReverseConfig.class.getName(), reverseConfig);
		final String reverseTargetPath = ReverseUtils.getReverseTargetPath(reverseConfig, incomingRequest.getPath());
		final ClassicHttpRequest outgoingRequest = new BasicClassicHttpRequest(
				incomingRequest.getMethod(), targetHost, reverseTargetPath);
		outgoingRequest.setVersion(HttpVersion.HTTP_1_1); //force HTTP/1.1

		for (final Iterator<Header> it = incomingRequest.headerIterator(); it.hasNext();) {
			final Header header = it.next();
			//Delete End-to-end and Hop-by-hop Headers
			if (!HOP_BY_HOP.contains(TextUtils.toLowerCase(header.getName()))) {
				outgoingRequest.addHeader(header);
			}
		}
		
		ReverseUtils.appendHostHeader(outgoingRequest, reverseConfig);
		ReverseUtils.rewriteHostHeader(outgoingRequest, serverContext, reverseConfig);
		
		//Add X-Forwarded headers
		outgoingRequest.setHeader("X-Forwarded-For", AccessLogUtils.getRemoteAddress(serverContext));
		outgoingRequest.setHeader("X-Forwarded-Proto", incomingRequest.getScheme());
		
		outgoingRequest.setEntity(incomingRequest.getEntity());
		final HttpRequester requester = requesterBootstrap.create();
		LOG.debug("[proxy->origin] ConnPool: "+requester.getConnPoolControl().getTotalStats());
		//Runtime.getRuntime().addShutdownHook(new Thread() {
		//	@Override
		//	public void run() {
		//		requester.close(CloseMode.GRACEFUL);
		//	}
		//});
		final ClassicHttpResponse incomingResponse = requester.execute(targetHost, outgoingRequest, Timeout.ofMinutes(3), clientContext);
		
		outgoingResponse.setCode(incomingResponse.getCode());
		outgoingResponse.setVersion(incomingRequest.getVersion());
		//Backend access log
		AccessLogUtils.log(LOG, outgoingRequest, outgoingResponse, clientContext, (System.currentTimeMillis()-startTime));
			
		//Copy response headers
		for (final Iterator<Header> it = incomingResponse.headerIterator(); it.hasNext();) {
			final Header header = it.next();
			//Delete End-to-end and Hop-by-hop Headers
			if (!HOP_BY_HOP.contains(TextUtils.toLowerCase(header.getName()))) {
				outgoingResponse.addHeader(header);
			}
		}
		
		//Rewite Response
		rewriteResponseHeaders(outgoingRequest, outgoingResponse);
		ReverseUtils.rewriteStatusLine(outgoingRequest, outgoingResponse);

		outgoingResponse.setEntity(incomingResponse.getEntity());
		AccessLogUtils.log(incomingRequest, incomingResponse, clientContext, (System.currentTimeMillis()-startTime));
		
		requester.closeIdle(TimeValue.ofMilliseconds(3000));
	}
	
	protected void rewriteResponseHeaders(final ClassicHttpRequest outgoingRequest, final ClassicHttpResponse outgoingResponse) {
		ReverseUtils.rewriteContentLocationHeader(outgoingRequest, outgoingResponse, reverseConfig);
		ReverseUtils.rewriteServerHeader(outgoingResponse, reverseConfig);
	
		//Location Header convert.
		ReverseUtils.rewriteLocationHeader(outgoingRequest, outgoingResponse, reverseConfig);
	
		//Set-Cookie Header convert.
		ReverseUtils.rewriteSetCookieHeader(outgoingRequest, outgoingResponse, reverseConfig);
	}
	
	protected RequesterBootstrap createRequesterBootstrap() {
		return RequesterBootstrap.bootstrap().setConnPoolListener(new TraceConnPoolListener())
				.setStreamListener(new TraceHttp1StreamListener()).setMaxTotal(serviceConfig.getServerConfig().getMaxTotal())
				.setDefaultMaxPerRoute(serviceConfig.getServerConfig().getMaxParRoute());
	}	
}
