/*
 * Copyright 2022 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.jetty;

import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;

import cloud.tamacat.httpd.ClassicHttpd;
import cloud.tamacat.httpd.config.ServerConfig;
import cloud.tamacat.httpd.config.ServiceConfig;
import cloud.tamacat.httpd.reverse.ReverseProxyHandler;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

/**
 * Embedded Classic I/O HTTP/1.1 server. configration: service.json
 */
public class ClassicHttpdWithJetty extends ClassicHttpd {

	static final Log LOG = LogFactory.getLog(ClassicHttpdWithJetty.class);

	public static void main(final String[] args) throws Exception {
		ClassicHttpdWithJetty.startup(args);
	}

	public static void startup(final String... args) throws Exception {
		final String json = args.length>=1 ? args[0] : "service.json";
		final ServerConfig config = ServerConfig.load(json);
		new ClassicHttpdWithJetty().startup(config);
	}
	
	@Override
	public void startup(final ServerConfig config) throws Exception {
		final int port = config.getPort();

		final HttpServer server = createHttpServer(config);
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

		server.start();
		LOG.info("Listening on port " + port);
		server.awaitTermination(TimeValue.MAX_VALUE);
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
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
