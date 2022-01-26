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
package cloud.tamacat.httpd.reverse.handler;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncClientEndpoint;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.ResponseChannel;
import org.apache.hc.core5.http.protocol.HttpContext;

import cloud.tamacat.httpd.config.ServiceConfig;

/**
 * @see
 * https://hc.apache.org/httpcomponents-core-5.0.x/httpcore5/examples/AsyncReverseProxyExample.java
 */
public class ProxyExchangeState {
	private static final AtomicLong COUNT = new AtomicLong(0);

	final String id;
	final ServiceConfig serviceConfig;

	HttpRequest request;
	EntityDetails requestEntityDetails;
	DataStreamChannel requestDataChannel;
	CapacityChannel requestCapacityChannel;
	ProxyBuffer inBuf;
	boolean inputEnd;

	HttpResponse response;
	EntityDetails responseEntityDetails;
	ResponseChannel responseMessageChannel;
	DataStreamChannel responseDataChannel;
	CapacityChannel responseCapacityChannel;
	ProxyBuffer outBuf;
	boolean outputEnd;

	HttpContext httpContext;
	
	long startTime;

	AsyncClientEndpoint clientEndpoint;

	ProxyExchangeState(ServiceConfig serviceConfig) {
		this.serviceConfig = serviceConfig;
		this.id = String.format("%010d", COUNT.getAndIncrement());
		this.startTime = System.currentTimeMillis();
	}
	
	long getResponseTime() {
		return System.currentTimeMillis() - startTime;
	}
}
