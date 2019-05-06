/*
 * Copyright (c) 2019 tamacat.org
 */
package cloud.tamacat.httpd.listener;

import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.impl.Http1StreamListener;

import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

public class TraceHttp1StreamListener implements Http1StreamListener {

	static final Log LOG = LogFactory.getLog(TraceHttp1StreamListener.class);

	String name = "httpd<-origin";
	
	public TraceHttp1StreamListener() {}
	public TraceHttp1StreamListener(String name) {}
	
	@Override
	public void onRequestHead(HttpConnection connection, HttpRequest request) {
	}

	@Override
	public void onResponseHead(HttpConnection connection, HttpResponse response) {
	}

	@Override
	public void onExchangeComplete(HttpConnection connection, boolean keepAlive) {
		if (LOG.isTraceEnabled()) {
			LOG.trace("["+name+"] connection " + connection.getLocalAddress() + "->"
				+ connection.getRemoteAddress()
				+ (keepAlive ? " kept alive" : " cannot be kept alive"));
		}
	}
}
