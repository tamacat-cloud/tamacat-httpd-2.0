/*
 * Copyright 2020 tamacat.org
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
package cloud.tamacat.httpd.filter;

import java.io.IOException;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpFilterChain;
import org.apache.hc.core5.http.io.HttpFilterHandler;
import org.apache.hc.core5.http.protocol.HttpContext;

import cloud.tamacat.httpd.config.ServiceConfig;

/**
 * Request/Response Filter
 */
public abstract class HttpFilter implements HttpFilterHandler {

	protected ServiceConfig serviceConfig;
	protected String path;
	
	public void setServerConfig(ServiceConfig serviceConfig) {
		this.serviceConfig = serviceConfig;
		this.path = serviceConfig.getPath();
	}
	
	public HttpFilter serverConfig(ServiceConfig serviceConfig) {
		setServerConfig(serviceConfig);
		return this;
	}
	
	public ServiceConfig getServiceConfig() {
		return serviceConfig;
	}
	
	@Override
	public void handle(ClassicHttpRequest request, HttpFilterChain.ResponseTrigger responseTrigger, 
			HttpContext context, HttpFilterChain chain) throws HttpException, IOException {
		
		if (request.getPath().startsWith(serviceConfig.getPath()) == false) {
			chain.proceed(request, responseTrigger, context);
		} else {
			handleRequest(request, context);
			
			chain.proceed(request, new HttpFilterChain.ResponseTrigger() {
				@Override
				public void sendInformation(ClassicHttpResponse response) throws HttpException, IOException {
					handleSendInformation(response, context);
					responseTrigger.sendInformation(response);
				}
	
				@Override
				public void submitResponse(ClassicHttpResponse response) throws HttpException, IOException {
					handleSubmitResponse(response, context);
					responseTrigger.submitResponse(response);
				}
			}, context);
		}
	}
	
	protected void handleRequest(ClassicHttpRequest request, HttpContext context) throws HttpException, IOException {
	}
	
	protected void handleSendInformation(ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
	}

	protected void handleSubmitResponse(ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
	}
}
