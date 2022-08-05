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
package cloud.tamacat.httpd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.impl.HttpProcessors;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.protocol.HttpProcessorBuilder;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.http.ssl.TlsCiphers;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.util.TimeValue;

import cloud.tamacat.httpd.config.HttpsConfig;
import cloud.tamacat.httpd.config.ServerConfig;
import cloud.tamacat.httpd.config.ServiceConfig;
import cloud.tamacat.httpd.listener.TraceExceptionListener;
import cloud.tamacat.httpd.listener.TraceHttp1StreamListener;
import cloud.tamacat.httpd.reverse.ReverseProxyHandler;
import cloud.tamacat.httpd.reverse.html.HtmlLinkConvertInterceptor;
import cloud.tamacat.httpd.tls.SSLSNIContextCreator;
import cloud.tamacat.httpd.web.FileServerRequestHandler;
import cloud.tamacat.httpd.web.ThymeleafServerRequestHandler;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;
import cloud.tamacat.util.StringUtils;

/**
 * Embedded Classic I/O HTTP/1.1 server. configration: service.json
 */
public class ClassicHttpd {

	static final Log LOG = LogFactory.getLog(ClassicHttpd.class);

	protected Collection<HttpRequestInterceptor> httpRequestInterceptors = new ArrayList<>();
	protected Collection<HttpResponseInterceptor> httpResponseInterceptors = new ArrayList<>();

	public static void main(final String[] args) {
		ClassicHttpd.startup(args);
	}

	public static void startup(final String... args) {
		final String json = args.length>=1 ? args[0] : "service.json";
		final ServerConfig config = ServerConfig.load(json);
		new ClassicHttpd().startup(config);
	}
	
	public void startup(final ServerConfig config) {
		final int port = config.getPort();

		final HttpServer server = createHttpServer(config);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOG.info(config.getServerName() + ":" + port + " shutting down");
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

	protected HttpServer createHttpServer(final ServerConfig config) {
		final Collection<ServiceConfig> configs = config.getServices();
		
		final ServerBootstrap bootstrap = ServerBootstrap.bootstrap()
				.setCanonicalHostName(config.getHost())
				.setListenerPort(config.getPort())
				.setStreamListener(new TraceHttp1StreamListener("client<-httpd"))
				.setSocketConfig(SocketConfig.custom()
				.setSoKeepAlive(config.keepAlive())
				.setSoReuseAddress(config.soReuseAddress())
				.setSoTimeout(config.getSoTimeout(), TimeUnit.SECONDS).build());

		LOG.trace(config.getHttpsConfig());

		// HTTPS
		if (config.useHttps()) {
			final HttpsConfig https = config.getHttpsConfig();
			final SSLContext sslContext = new SSLSNIContextCreator(config).getSSLContext();
			bootstrap.setSslSetupHandler(sslParameters -> {
				sslParameters.setProtocols(TLS.excludeWeak(sslParameters.getProtocols()));
				sslParameters.setCipherSuites(TlsCiphers.excludeWeak(sslParameters.getCipherSuites()));
				if (https.useClientAuth()) {
					sslParameters.setNeedClientAuth(true);
				}
			});
			bootstrap.setSslContext(sslContext);
		}

		for (ServiceConfig serviceConfig : configs) {
			if (serviceConfig == null) continue;

			serviceConfig.setServerConfig(config);

			register(serviceConfig, bootstrap);

			// add filters
			serviceConfig.getFilters().forEach((id, filter) -> {
				bootstrap.addFilterFirst(id, filter.getFilter(serviceConfig));
			});
		}

		HttpProcessorBuilder HttpProcessorBuilder = HttpProcessors.customServer(config.getServerName());
		httpRequestInterceptors.forEach(i-> HttpProcessorBuilder.add(i));
		httpResponseInterceptors.forEach(i-> HttpProcessorBuilder.add(i));
		bootstrap.setHttpProcessor(HttpProcessorBuilder.build());

		bootstrap.setStreamListener(new TraceHttp1StreamListener())
				 .setExceptionListener(new TraceExceptionListener());
		final HttpServer server = bootstrap.create();
		return server;
	}
	
	protected void register(final ServiceConfig serviceConfig, final ServerBootstrap bootstrap) {
		if (serviceConfig.isReverseProxy()) {
			registerReverseProxy(serviceConfig, bootstrap);
		} else if (serviceConfig.isThymeleaf()) {
			registerThymeleafServer(serviceConfig, bootstrap);
		} else {
			registerFileServer(serviceConfig, bootstrap);
		}
	}

	protected void register(final ServiceConfig serviceConfig, final ServerBootstrap bootstrap, final HttpRequestHandler handler) {
		try {
			//LOG.trace("hostname="+serviceConfig.getHostname());
			if (StringUtils.isNotEmpty(serviceConfig.getHostname())) {
				bootstrap.registerVirtual(serviceConfig.getHostname(), serviceConfig.getPath() + "*", handler);
			} else {
				bootstrap.register(serviceConfig.getPath() + "*", handler);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
		}
	}
	
	protected void registerFileServer(final ServiceConfig serviceConfig, final ServerBootstrap bootstrap) {
		LOG.info("register: VirtualHost="+getVirtualHost(serviceConfig)+", path="+serviceConfig.getPath() +"* FileServer");
		register(serviceConfig, bootstrap, new FileServerRequestHandler(serviceConfig));
	}

	protected void registerThymeleafServer(final ServiceConfig serviceConfig, final ServerBootstrap bootstrap) {
		LOG.info("register: VirtualHost="+getVirtualHost(serviceConfig)+", path="+serviceConfig.getPath() + "* ThymeleafServer");
		register(serviceConfig, bootstrap, new ThymeleafServerRequestHandler(serviceConfig));
	}

	protected void registerReverseProxy(final ServiceConfig serviceConfig, final ServerBootstrap bootstrap) {
		try {
			final HttpHost targetHost = HttpHost.create(serviceConfig.getReverse().getTarget().toURI());
			LOG.info("register: VirtualHost="+getVirtualHost(serviceConfig)+", path="+serviceConfig.getPath()+"* ReverseProxy to "+targetHost);
			register(serviceConfig, bootstrap, new ReverseProxyHandler(targetHost, serviceConfig));
			
			httpResponseInterceptors.add(new HtmlLinkConvertInterceptor());
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	protected String getVirtualHost(final ServiceConfig serviceConfig) {
		return StringUtils.isNotEmpty(serviceConfig.getHostname()) ? serviceConfig.getHostname() : "default";
	}
	
	public void addHttpRequestInterceptor(HttpRequestInterceptor interceptor) {
		httpRequestInterceptors.add(interceptor);
	}

	public void addHttpResponseInterceptor(HttpResponseInterceptor interceptor) {
		httpResponseInterceptors.add(interceptor);
	}
}
