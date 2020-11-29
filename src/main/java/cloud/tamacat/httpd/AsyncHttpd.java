/*
 * Copyright (c) 2019 tamacat.org
 */
package cloud.tamacat.httpd;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.impl.HttpProcessors;
import org.apache.hc.core5.http.impl.bootstrap.AsyncRequesterBootstrap;
import org.apache.hc.core5.http.impl.bootstrap.AsyncServerBootstrap;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncRequester;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncServer;
import org.apache.hc.core5.http.nio.AsyncServerExchangeHandler;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.TimeValue;

import cloud.tamacat.httpd.config.Config;
import cloud.tamacat.httpd.config.ServiceConfig;
import cloud.tamacat.httpd.listener.TraceConnPoolListener;
import cloud.tamacat.httpd.listener.TraceHttp1StreamListener;
import cloud.tamacat.httpd.reverse.handler.IncomingExchangeHandler;
import cloud.tamacat.httpd.util.ServerUtils;
import cloud.tamacat.httpd.web.handler.AsyncFileServerRequestHandler;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

/**
 * Asynchronous embedded HTTP/1.1 server.
 * configration: service.json
 */
public class AsyncHttpd {

	static final Log LOG = LogFactory.getLog(AsyncHttpd.class);

	public static void main(String[] args) throws Exception {
		AsyncHttpd server = new AsyncHttpd();
		server.startup(args);
	}
	
	protected String docsRoot;
	
	public void startup(String... args) throws Exception {
		String json = args.length>=1 ? args[0] : "service.json";
		Config config = Config.load(json);

		IOReactorConfig reactor = IOReactorConfig.custom()
			.setSoTimeout(config.getSoTimeout(), TimeUnit.SECONDS).build();
		
		HttpAsyncRequester requester = createHttpAsyncRequester(config, reactor);

		HttpAsyncServer server = createHttpAsyncServer(config, reactor, requester);
		int port = config.getPort();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOG.info(config.getServerName()+":"+port+" shutting down");
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

	/**
	 * <p>Set the path of document root.
	 * @param docsRoot
	 */
	public void setDocsRoot(String docsRoot) {
		this.docsRoot = ServerUtils.getServerDocsRoot(docsRoot);
	}
	
	protected HttpAsyncRequester createHttpAsyncRequester(Config config, IOReactorConfig reactor) {
		HttpAsyncRequester requester = AsyncRequesterBootstrap.bootstrap()
			.setIOReactorConfig(reactor)
			.setConnPoolListener(new TraceConnPoolListener())
			.setStreamListener(new TraceHttp1StreamListener())
			.setMaxTotal(config.getMaxTotal())
			.setDefaultMaxPerRoute(config.getMaxParRoute())
			.create();
		return requester;
	}
	
	protected HttpAsyncServer createHttpAsyncServer(Config config, IOReactorConfig reactor, HttpAsyncRequester requester) {
		Collection<ServiceConfig> configs = config.getServices();

		AsyncServerBootstrap bootstrap = AsyncServerBootstrap.bootstrap()
			.setHttpProcessor(HttpProcessors.customServer(config.getServerName()).build())
			.setIOReactorConfig(reactor)
			.setStreamListener(new TraceHttp1StreamListener("client<-httpd"));

		for (ServiceConfig serviceConfig : configs) {
			if ("reverse".equals(serviceConfig.getType())) {
				registerReverseProxy(serviceConfig, bootstrap, requester);
			} else {
				registerFileServer(serviceConfig, bootstrap, requester);
			}
		}
		
		HttpAsyncServer server = bootstrap.create();
		return server;
	}
	
	protected void registerFileServer(ServiceConfig serviceConfig, AsyncServerBootstrap bootstrap, HttpAsyncRequester requester) {
		try {
			bootstrap.register(serviceConfig.getPath() + "*", new AsyncFileServerRequestHandler(serviceConfig));
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
		}
	}
	
	protected void registerReverseProxy(ServiceConfig serviceConfig, AsyncServerBootstrap bootstrap, HttpAsyncRequester requester) {
		try {
			HttpHost targetHost = HttpHost.create(serviceConfig.getReverse().getTarget().toURI());
			LOG.info(serviceConfig.getPath() + "* Reverse proxy to " + targetHost);
			bootstrap.register(serviceConfig.getPath() + "*", new Supplier<AsyncServerExchangeHandler>() {

				@Override
				public AsyncServerExchangeHandler get() {
					return new IncomingExchangeHandler(targetHost, requester);
				}

			});
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
