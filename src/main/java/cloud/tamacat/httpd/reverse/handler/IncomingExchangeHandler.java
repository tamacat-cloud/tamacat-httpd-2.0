/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package cloud.tamacat.httpd.reverse.handler;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HeaderElements;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.impl.BasicEntityDetails;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncRequester;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.nio.AsyncClientEndpoint;
import org.apache.hc.core5.http.nio.AsyncServerExchangeHandler;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.ResponseChannel;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.util.Timeout;

import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

/**
 * @see
 * https://hc.apache.org/httpcomponents-core-5.0.x/httpcore5/examples/AsyncReverseProxyExample.java
 * (AsyncReverseProxyExample.IncomingExchangeHandler class)
 */
public class IncomingExchangeHandler implements AsyncServerExchangeHandler {

	static final Log LOG = LogFactory.getLog(IncomingExchangeHandler.class);
	
	static final int INIT_BUFFER_SIZE = 4096;
	static final int PROXY_BUFFER_SIZE = 1024;
	
	Timeout timeout = Timeout.ofSeconds(30);
	
	private final HttpHost targetHost;
	private final HttpAsyncRequester requester;
	private final ProxyExchangeState exchangeState;

	public IncomingExchangeHandler(final HttpHost targetHost, final HttpAsyncRequester requester) {
		super();
		this.targetHost = targetHost;
		this.requester = requester;
		this.exchangeState = new ProxyExchangeState();
	}

	@Override
	public void handleRequest(final HttpRequest incomingRequest, final EntityDetails entityDetails,
			final ResponseChannel responseChannel, final HttpContext httpContext) throws HttpException, IOException {

		synchronized (exchangeState) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("[client->proxy] "+exchangeState.id+" "+incomingRequest.getMethod()+" "+incomingRequest.getRequestUri());
			}
			exchangeState.request = incomingRequest;
			exchangeState.requestEntityDetails = entityDetails;
			exchangeState.inputEnd = entityDetails == null;
			exchangeState.responseMessageChannel = responseChannel;

			if (entityDetails != null) {
				final Header h = incomingRequest.getFirstHeader(HttpHeaders.EXPECT);
				if (h != null && "100-continue".equalsIgnoreCase(h.getValue())) {
					responseChannel.sendInformation(new BasicHttpResponse(HttpStatus.SC_CONTINUE), httpContext);
				}
			}
		}

		if (LOG.isTraceEnabled()) {
			LOG.trace("[proxy->origin] " + exchangeState.id + " request connection to " + targetHost);
		}

		requester.connect(targetHost, timeout, null, new FutureCallback<AsyncClientEndpoint>() {

			@Override
			public void completed(final AsyncClientEndpoint clientEndpoint) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("[proxy->origin] " + exchangeState.id + " connection leased");
				}
				synchronized (exchangeState) {
					exchangeState.clientEndpoint = clientEndpoint;
				}
				clientEndpoint.execute(new OutgoingExchangeHandler(targetHost, clientEndpoint, exchangeState),
						HttpCoreContext.create());
			}

			@Override
			public void failed(final Exception cause) {
				final HttpResponse outgoingResponse = new BasicHttpResponse(HttpStatus.SC_SERVICE_UNAVAILABLE);
				outgoingResponse.addHeader(HttpHeaders.CONNECTION, HeaderElements.CLOSE);
				final ByteBuffer msg = StandardCharsets.US_ASCII.encode(CharBuffer.wrap(cause.getMessage()));
				final EntityDetails exEntityDetails = new BasicEntityDetails(msg.remaining(), ContentType.TEXT_PLAIN);
				synchronized (exchangeState) {
					exchangeState.response = outgoingResponse;
					exchangeState.responseEntityDetails = exEntityDetails;
					exchangeState.outBuf = new ProxyBuffer(PROXY_BUFFER_SIZE);
					exchangeState.outBuf.put(msg);
					exchangeState.outputEnd = true;
				}
				if (LOG.isTraceEnabled()) {
					LOG.trace("[client<-proxy] " + exchangeState.id + " status " + outgoingResponse.getCode());
				}

				try {
					responseChannel.sendResponse(outgoingResponse, exEntityDetails, httpContext);
				} catch (final HttpException | IOException ignore) {
					// ignore
					if (LOG.isTraceEnabled()) {
						LOG.trace(ignore);
					}
				}
			}

			@Override
			public void cancelled() {
				failed(new InterruptedIOException());
			}
		});
	}

	@Override
	public void updateCapacity(final CapacityChannel capacityChannel) throws IOException {
		synchronized (exchangeState) {
			exchangeState.requestCapacityChannel = capacityChannel;
			final int capacity = exchangeState.inBuf != null ? exchangeState.inBuf.capacity() : INIT_BUFFER_SIZE;
			if (capacity > 0) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("[client<-proxy] " + exchangeState.id + " input capacity: " + capacity);
				}
				capacityChannel.update(capacity);
			}
		}
	}

	@Override
	public void consume(final ByteBuffer src) throws IOException {
		synchronized (exchangeState) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("[client->proxy] " + exchangeState.id + " " + src.remaining() + " bytes received");
			}
			final DataStreamChannel dataChannel = exchangeState.requestDataChannel;
			if (dataChannel != null && exchangeState.inBuf != null) {
				if (exchangeState.inBuf.hasData()) {
					final int bytesWritten = exchangeState.inBuf.write(dataChannel);
					if (LOG.isTraceEnabled()) {
						LOG.trace("[proxy->origin] " + exchangeState.id + " " + bytesWritten + " bytes sent");
					}
				}
				if (!exchangeState.inBuf.hasData()) {
					final int bytesWritten = dataChannel.write(src);
					if (LOG.isTraceEnabled()) {
						LOG.trace("[proxy->origin] " + exchangeState.id + " " + bytesWritten + " bytes sent");
					}
				}
			}
			if (src.hasRemaining()) {
				if (exchangeState.inBuf == null) {
					exchangeState.inBuf = new ProxyBuffer(INIT_BUFFER_SIZE);
				}
				exchangeState.inBuf.put(src);
			}
			final int capacity = exchangeState.inBuf != null ? exchangeState.inBuf.capacity() : INIT_BUFFER_SIZE;
			if (LOG.isTraceEnabled()) {
				LOG.trace("[client<-proxy] " + exchangeState.id + " input capacity: " + capacity);
			}
			if (dataChannel != null) {
				dataChannel.requestOutput();
			}
		}
	}

	@Override
	public void streamEnd(final List<? extends Header> trailers) throws HttpException, IOException {
		synchronized (exchangeState) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("[client->proxy] " + exchangeState.id + " end of input");
			}
			exchangeState.inputEnd = true;
			final DataStreamChannel dataChannel = exchangeState.requestDataChannel;
			if (dataChannel != null && (exchangeState.inBuf == null || !exchangeState.inBuf.hasData())) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("[proxy->origin] " + exchangeState.id + " end of output");
				}
				dataChannel.endStream();
			}
		}
	}

	@Override
	public int available() {
		synchronized (exchangeState) {
			final int available = exchangeState.outBuf != null ? exchangeState.outBuf.length() : 0;
			if (LOG.isTraceEnabled()) {
				LOG.trace("[client<-proxy] " + exchangeState.id + " output available: " + available);
			}
			return available;
		}
	}

	@Override
	public void produce(final DataStreamChannel channel) throws IOException {
		synchronized (exchangeState) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("[client<-proxy] " + exchangeState.id + " produce output");
			}
			exchangeState.responseDataChannel = channel;

			if (exchangeState.outBuf != null) {
				if (exchangeState.outBuf.hasData()) {
					final int bytesWritten = exchangeState.outBuf.write(channel);
					if (LOG.isTraceEnabled()) {
						LOG.trace("[client<-proxy] " + exchangeState.id + " " + bytesWritten + " bytes sent");
					}
				}
				if (exchangeState.outputEnd && !exchangeState.outBuf.hasData()) {
					channel.endStream();
					if (LOG.isTraceEnabled()) {
						LOG.trace("[client<-proxy] " + exchangeState.id + " end of output");
					}
				}
				if (!exchangeState.outputEnd) {
					final CapacityChannel capacityChannel = exchangeState.responseCapacityChannel;
					if (capacityChannel != null) {
						final int capacity = exchangeState.outBuf.capacity();
						if (capacity > 0) {
							if (LOG.isTraceEnabled()) {
								LOG.trace("[proxy->origin] " + exchangeState.id + " input capacity: " + capacity);
							}
							capacityChannel.update(capacity);
						}
					}
				}
			}
		}
	}

	@Override
	public void failed(final Exception cause) {
		LOG.debug("[client<-proxy] " + exchangeState.id + " " + cause.getMessage());
		if (!(cause instanceof ConnectionClosedException)) {
			cause.printStackTrace(System.out);
		}
		synchronized (exchangeState) {
			if (exchangeState.clientEndpoint != null) {
				exchangeState.clientEndpoint.releaseAndDiscard();
			}
		}
	}

	@Override
	public void releaseResources() {
		synchronized (exchangeState) {
			exchangeState.responseMessageChannel = null;
			exchangeState.responseDataChannel = null;
			exchangeState.requestCapacityChannel = null;
		}
	}
}
