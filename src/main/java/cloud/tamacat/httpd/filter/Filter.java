/*
 * Copyright 2020 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.filter;

import org.apache.hc.core5.http.nio.AsyncFilterHandler;

import cloud.tamacat.httpd.config.ServiceConfig;

/**
 * Request/Response Filter
 */
public abstract class Filter implements AsyncFilterHandler {

	protected ServiceConfig serviceConfig;
	
	public void setServerConfig(ServiceConfig serviceConfig) {
		this.serviceConfig = serviceConfig;
	}
	
	public Filter serverConfig(ServiceConfig serviceConfig) {
		setServerConfig(serviceConfig);
		return this;
	}
	
	public ServiceConfig getServiceConfig() {
		return serviceConfig;
	}
}
