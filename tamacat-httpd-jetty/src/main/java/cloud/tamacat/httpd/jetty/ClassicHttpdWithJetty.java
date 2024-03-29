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
package cloud.tamacat.httpd.jetty;

import java.io.IOException;

import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;

import cloud.tamacat.httpd.ClassicHttpd;
import cloud.tamacat.httpd.config.ServerConfig;
import cloud.tamacat.httpd.config.ServiceConfig;
import cloud.tamacat.httpd.reverse.ReverseProxyHandler;
import cloud.tamacat.httpd.reverse.html.HtmlLinkConvertInterceptor;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

/**
 * Embedded Classic I/O HTTP/1.1 server. configration: service.json
 */
public class ClassicHttpdWithJetty extends ClassicHttpd {

	static final Log LOG = LogFactory.getLog(ClassicHttpdWithJetty.class);

	public static void main(final String[] args) {
		ClassicHttpdWithJetty.startup(args);
	}

	public static void startup(final String... args) {
		final String json = args.length>=1 ? args[0] : "service.json";
		final ServerConfig config = ServerConfig.load(json);
		ClassicHttpdWithJetty.startup(config);
	}
	
	public static void startup(final ServerConfig config) {
		final int port = config.getPort();

		final HttpServer server = new ClassicHttpdWithJetty().createHttpServer(config);
		if (config.useJetty()) {
			JettyManager.getInstance().start();
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOG.info(config.getServerName() + ":" + port + " shutting down");
				if (config.useJetty()) {
					JettyManager.getInstance().stop();
				}
				server.close(CloseMode.GRACEFUL);
			}
		});

		try {
			server.start();
			LOG.info("Listening on port " + port);
			server.awaitTermination(TimeValue.MAX_VALUE);
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		
	}

	@Override
	protected void register(final ServiceConfig serviceConfig, final ServerBootstrap bootstrap) {
		if (serviceConfig.isJetty()) {
			registerJettyEmbedded(serviceConfig, bootstrap);
		} else {
			super.register(serviceConfig, bootstrap);
		}
	}
	
	protected void registerJettyEmbedded(final ServiceConfig serviceConfig, final ServerBootstrap bootstrap) {
		try {
			final JettyDeployment jettyDeploy = new JettyDeployment();
			jettyDeploy.deploy(serviceConfig);

			final HttpHost targetHost = HttpHost.create(serviceConfig.getReverse().getTarget().toURI());
			LOG.info("register: VirtualHost="+getVirtualHost(serviceConfig)+", path="+serviceConfig.getPath() + "* ReverseProxy+JettyEmbedded to " + targetHost);
			register(serviceConfig, bootstrap, new ReverseProxyHandler(targetHost, serviceConfig));
			
			httpResponseInterceptors.add(new HtmlLinkConvertInterceptor());
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
