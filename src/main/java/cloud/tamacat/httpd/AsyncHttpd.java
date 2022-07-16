/*
 * Copyright 2019 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.impl.HttpProcessors;
import org.apache.hc.core5.http.impl.bootstrap.AsyncServerBootstrap;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncServer;
import org.apache.hc.core5.http.nio.AsyncServerExchangeHandler;
import org.apache.hc.core5.http.nio.ssl.BasicServerTlsStrategy;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.http.ssl.TlsCiphers;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.ssl.SSLSessionInitializer;
import org.apache.hc.core5.util.TimeValue;

import cloud.tamacat.httpd.async.IncomingExchangeHandler;
import cloud.tamacat.httpd.config.HttpsConfig;
import cloud.tamacat.httpd.config.ServerConfig;
import cloud.tamacat.httpd.config.ServiceConfig;
import cloud.tamacat.httpd.jetty.JettyDeployment;
import cloud.tamacat.httpd.jetty.JettyManager;
import cloud.tamacat.httpd.listener.TraceHttp1StreamListener;
import cloud.tamacat.httpd.tls.SSLSNIContextCreator;
import cloud.tamacat.httpd.web.async.FileServerRequestHandler;
import cloud.tamacat.httpd.web.async.ThymeleafServerRequestHandler;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

/**
 * Asynchronous embedded HTTP/1.1 server.
 * configration: service.json
 */
public class AsyncHttpd {

	static final Log LOG = LogFactory.getLog(AsyncHttpd.class);

	public static void main(final String[] args) throws Exception {
		AsyncHttpd.startup(args);
	}
		
	public static void startup(final String... args) throws Exception {
		final String json = args.length>=1 ? args[0] : "service.json";
		final ServerConfig config = ServerConfig.load(json);
		new AsyncHttpd().startup(config);
	}
	
	public void startup(final ServerConfig config) throws Exception {
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

		server.awaitShutdown(TimeValue.MAX_VALUE);
	}
	
	protected HttpAsyncServer createHttpAsyncServer(final ServerConfig config) {
		final Collection<ServiceConfig> configs = config.getServices();

		final IOReactorConfig reactor = IOReactorConfig.custom()
				.setSoTimeout(config.getSoTimeout(), TimeUnit.SECONDS).build();
			
		final AsyncServerBootstrap bootstrap = AsyncServerBootstrap.bootstrap()
			.setHttpProcessor(HttpProcessors.customServer(config.getServerName()).build())
			.setIOReactorConfig(reactor)
			.setStreamListener(new TraceHttp1StreamListener("client<-httpd"));

		LOG.trace(config.getHttpsConfig());
		
		//HTTPS
		if (config.useHttps()) {
			final HttpsConfig https = config.getHttpsConfig();
			final SSLContext sslContext = new SSLSNIContextCreator(config).getSSLContext();
			final SSLSessionInitializer initializer = new SSLSessionInitializer() {	
				@Override
				public void initialize(final NamedEndpoint endpoint, final SSLEngine sslEngine) {
					final SSLParameters sslParameters = sslEngine.getSSLParameters();
		            sslParameters.setProtocols(TLS.excludeWeak(sslParameters.getProtocols()));
		            sslParameters.setCipherSuites(TlsCiphers.excludeWeak(sslParameters.getCipherSuites()));
		            sslEngine.setSSLParameters(sslParameters);
		            sslEngine.setNeedClientAuth(https.useClientAuth());
				}
		    };
			bootstrap.setTlsStrategy(new BasicServerTlsStrategy(sslContext, initializer, null));
		}
		
		for (ServiceConfig serviceConfig : configs) {
			serviceConfig.setServerConfig(config);
			
			if (serviceConfig.isReverseProxy()) {
				registerReverseProxy(serviceConfig, bootstrap);
			} else if ("jetty".equals(serviceConfig.getType())) {
				registerJettyEmbedded(serviceConfig, bootstrap);
			} else if ("thymeleaf".equals(serviceConfig.getType())) {
				registerThymeleafServer(serviceConfig, bootstrap);
			} else {
				registerFileServer(serviceConfig, bootstrap);
			}
			
			//add filter
			serviceConfig.getFilters().forEach((id, filter)->{
				bootstrap.addFilterFirst(id, filter.getAsyncFilter(serviceConfig));
			});
		}
				
		final HttpAsyncServer server = bootstrap.create();
		return server;
	}
	
	protected void registerFileServer(final ServiceConfig serviceConfig, final AsyncServerBootstrap bootstrap) {
		try {
			LOG.info("register: " + serviceConfig.getPath() + "* FileServer");
			bootstrap.register(serviceConfig.getPath() + "*", new FileServerRequestHandler(serviceConfig));
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
		}
	}

	protected void registerThymeleafServer(final ServiceConfig serviceConfig, final AsyncServerBootstrap bootstrap) {
		try {
			LOG.info("register: " + serviceConfig.getPath() + "* ThymeleafServer");
			bootstrap.register(serviceConfig.getPath() + "*", new ThymeleafServerRequestHandler(serviceConfig));
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
		}
	}
	
	protected void registerReverseProxy(final ServiceConfig serviceConfig, final AsyncServerBootstrap bootstrap) {
		try {
			final HttpHost targetHost = HttpHost.create(serviceConfig.getReverse().getTarget().toURI());
			LOG.info("register: " + serviceConfig.getPath() + "* ReverseProxy to " + targetHost);
			bootstrap.register(serviceConfig.getPath() + "*", new Supplier<AsyncServerExchangeHandler>() {

				@Override
				public AsyncServerExchangeHandler get() {
					return new IncomingExchangeHandler(targetHost, serviceConfig);
				}

			});
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
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
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
