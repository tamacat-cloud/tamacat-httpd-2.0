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
package cloud.tamacat.httpd.reverse;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.impl.Http1StreamListener;
import org.apache.hc.core5.http.impl.bootstrap.AsyncRequesterBootstrap;
import org.apache.hc.core5.http.impl.bootstrap.AsyncServerBootstrap;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncRequester;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncServer;
import org.apache.hc.core5.http.nio.AsyncServerExchangeHandler;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.pool.ConnPoolListener;
import org.apache.hc.core5.pool.ConnPoolStats;
import org.apache.hc.core5.pool.PoolStats;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.TimeValue;

import cloud.tamacat.httpd.config.Config;
import cloud.tamacat.httpd.config.ServiceConfig;
import cloud.tamacat.httpd.reverse.handler.IncomingExchangeHandler;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

/**
 * Example of asynchronous embedded HTTP/1.1 reverse proxy with full content streaming.
 * 
 * @see
 * https://hc.apache.org/httpcomponents-core-5.0.x/httpcore5/examples/AsyncReverseProxyExample.java
 */
public class AsyncReverseProxy {

	static final Log LOG = LogFactory.getLog("httpd");
	static int maxTotal = 100;
	static int defaultMaxPerRoute = 20;
	
	public void startup() throws Exception {
		Config config = Config.load("service.json");
		
		Collection<ServiceConfig> configs = config.getConfigs();
		ServiceConfig serviceConfig = configs.iterator().next();

		HttpHost targetHost = HttpHost.create(serviceConfig.getReverse().getTarget().toURI());
		int port = config.getPort();

		LOG.info("Reverse proxy to " + targetHost);

		IOReactorConfig reactor = IOReactorConfig.custom().setSoTimeout(1, TimeUnit.MINUTES).build();

		HttpAsyncRequester requester = AsyncRequesterBootstrap.bootstrap().setIOReactorConfig(reactor)
			.setConnPoolListener(new ConnPoolListener<HttpHost>() {

			@Override
			public void onLease(HttpHost route, ConnPoolStats<HttpHost> connPoolStats) {
				StringBuilder buf = new StringBuilder();
				buf.append("[proxy->origin] connection leased ").append(route);
				if (LOG.isTraceEnabled()) {
					LOG.trace(buf.toString());
				}
			}

			@Override
			public void onRelease(HttpHost route, ConnPoolStats<HttpHost> connPoolStats) {
				StringBuilder buf = new StringBuilder();
				buf.append("[proxy->origin] connection released ").append(route);
				PoolStats totals = connPoolStats.getTotalStats();
				buf.append("; total kept alive: ").append(totals.getAvailable()).append("; ");
				buf.append("total allocated: ").append(totals.getLeased() + totals.getAvailable());
				buf.append(" of ").append(totals.getMax());
				if (LOG.isTraceEnabled()) {
					LOG.trace(buf.toString());
				}
			}

		}).setStreamListener(new Http1StreamListener() {

			@Override
			public void onRequestHead(HttpConnection connection, HttpRequest request) {
				// empty
			}

			@Override
			public void onResponseHead(HttpConnection connection, HttpResponse response) {
				// empty
			}

			@Override
			public void onExchangeComplete(HttpConnection connection, boolean keepAlive) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("[proxy<-origin] connection " + connection.getLocalAddress() + "->"
						+ connection.getRemoteAddress()
						+ (keepAlive ? " kept alive" : " cannot be kept alive"));
				}
			}

		}).setMaxTotal(maxTotal).setDefaultMaxPerRoute(defaultMaxPerRoute).create();

		HttpAsyncServer server = AsyncServerBootstrap.bootstrap().setIOReactorConfig(reactor)
			.setStreamListener(new Http1StreamListener() {

			@Override
			public void onRequestHead(HttpConnection connection, HttpRequest request) {
				LOG.info(request.getMethod()+" "+request.getRequestUri()+" "+request.getVersion());
			}

			@Override
			public void onResponseHead(HttpConnection connection, HttpResponse response) {
				LOG.info(response.getCode()+" "+response.getReasonPhrase());
			}

			@Override
			public void onExchangeComplete(HttpConnection connection, boolean keepAlive) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("[client<-proxy] connection " + connection.getLocalAddress() + "->"
						+ connection.getRemoteAddress()
						+ (keepAlive ? " kept alive" : " cannot be kept alive"));
				}
			}

		}).register(serviceConfig.getPath()+"*", new Supplier<AsyncServerExchangeHandler>() {

			@Override
			public AsyncServerExchangeHandler get() {
				return new IncomingExchangeHandler(targetHost, requester);
			}

		}).create();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOG.info("Reverse proxy shutting down");
				server.close(CloseMode.GRACEFUL);
				requester.close(CloseMode.GRACEFUL);
			}
		});

		requester.start();
		server.start();
		server.listen(new InetSocketAddress(port));
		LOG.info("Listening on port " + port);

		server.awaitShutdown(TimeValue.MAX_VALUE);
	}
}