/*
 * Copyright 2019 tamacat.org
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
package cloud.tamacat.httpd.reverse.listener;

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
