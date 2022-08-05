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

import org.apache.hc.core5.http.nio.AsyncFilterHandler;

import cloud.tamacat.httpd.config.ServiceConfig;

/**
 * Request/Response Filter
 */
public abstract class AsyncFilter implements AsyncFilterHandler {

	protected ServiceConfig serviceConfig;
	protected String path;
	
	public void setServerConfig(ServiceConfig serviceConfig) {
		this.serviceConfig = serviceConfig;
		this.path = serviceConfig.getPath();
	}
	
	public AsyncFilter serverConfig(ServiceConfig serviceConfig) {
		setServerConfig(serviceConfig);
		return this;
	}
	
	public ServiceConfig getServiceConfig() {
		return serviceConfig;
	}
}
