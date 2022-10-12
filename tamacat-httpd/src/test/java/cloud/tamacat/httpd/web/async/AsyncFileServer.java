/*
 * Copyright 2022 tamacat.org
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
package cloud.tamacat.httpd.web.async;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.impl.bootstrap.AsyncServerBootstrap;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncServer;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.ListenerEndpoint;
import org.apache.hc.core5.util.TimeValue;

import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

/**
 * Example of asynchronous embedded HTTP/1.1 file server.
 * 
 * @see https://hc.apache.org/httpcomponents-core-5.0.x/httpcore5/examples/AsyncFileServerExample.java
 */
public class AsyncFileServer {

	static final Log LOG = LogFactory.getLog(AsyncFileServer.class);

	/** Example command line args: {@code "c:\temp" 8080} */
	public static void main(String[] args) throws Exception {
		String htdocs = args.length > 0 ? args[0] : "./htdocs";

		// Document root directory
		final File docsRoot = new File(htdocs);
		int port = 80;
		if (args.length >= 2) {
			port = Integer.parseInt(args[1]);
		}

		final IOReactorConfig config = IOReactorConfig.custom().setSoTimeout(15, TimeUnit.SECONDS).setTcpNoDelay(true).build();

		final HttpAsyncServer server = AsyncServerBootstrap.bootstrap().setIOReactorConfig(config)
				.register("*", new FileServerHandler(docsRoot)).create();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOG.info("HTTP server shutting down");
				server.close(CloseMode.GRACEFUL);
			}
		});

		server.start();
		final Future<ListenerEndpoint> future = server.listen(new InetSocketAddress(port), URIScheme.HTTP);
		final ListenerEndpoint listenerEndpoint = future.get();
		LOG.info("Listening on " + listenerEndpoint.getAddress());
		server.awaitShutdown(TimeValue.MAX_VALUE);
	}
}