/*
 * Copyright 2021 tamacat.org
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
package cloud.tamacat.httpd.jetty;

import java.net.InetSocketAddress;

import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.impl.bootstrap.AsyncServerBootstrap;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncServer;
import org.apache.hc.core5.http.nio.AsyncServerExchangeHandler;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;

import cloud.tamacat.httpd.AsyncHttpd;
import cloud.tamacat.httpd.config.ServerConfig;
import cloud.tamacat.httpd.config.ServiceConfig;
import cloud.tamacat.httpd.reverse.async.IncomingExchangeHandler;
import cloud.tamacat.httpd.reverse.html.HtmlLinkConvertInterceptor;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

/**
 * Asynchronous embedded HTTP/1.1 server.
 * configration: service.json
 */
public class AsyncHttpdWithJetty extends AsyncHttpd {

	static final Log LOG = LogFactory.getLog(AsyncHttpdWithJetty.class);

	public static void main(final String[] args) {
		AsyncHttpdWithJetty.startup(args);
	}
		
	public static void startup(final String... args) {
		final String json = args.length>=1 ? args[0] : "service.json";
		final ServerConfig config = ServerConfig.load(json);
		new AsyncHttpdWithJetty().startup(config);
	}
	
	@Override
	public void startup(final ServerConfig config) {
		final HttpAsyncServer server = createHttpAsyncServer(config);
		final int port = config.getPort();

		JettyManager.getInstance().start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOG.info(config.getServerName()+":"+port+" shutting down");
				JettyManager.getInstance().stop();
				
				server.close(CloseMode.GRACEFUL);
			}
		});

		server.start();
		
		server.listen(new InetSocketAddress(port), config.getURIScheme());
		LOG.info("Listening on port " + port);

		try {
			server.awaitShutdown(TimeValue.MAX_VALUE);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void register(final ServiceConfig serviceConfig, final AsyncServerBootstrap bootstrap) {
		if ("jetty".equals(serviceConfig.getType())) {
			registerJettyEmbedded(serviceConfig, bootstrap);
		} else {
			super.register(serviceConfig, bootstrap);
		}
	}
	
	protected void registerJettyEmbedded(final ServiceConfig serviceConfig, final AsyncServerBootstrap bootstrap) {
		try {
			final JettyDeployment jettyDeploy = new JettyDeployment();
			jettyDeploy.deploy(serviceConfig);
			
			final HttpHost targetHost = HttpHost.create(serviceConfig.getReverse().getTarget().toURI());
			LOG.info("register: " + serviceConfig.getPath() + "* ReverseProxy+JettyEmbedded to " + targetHost);
			bootstrap.register(serviceConfig.getPath() + "*", new Supplier<AsyncServerExchangeHandler>() {

				@Override
				public AsyncServerExchangeHandler get() {
					return new IncomingExchangeHandler(targetHost, serviceConfig);
				}

			});
			
			httpResponseInterceptors.add(new HtmlLinkConvertInterceptor());
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
