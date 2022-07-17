/*
 * Copyright 2020 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.filter;

import org.apache.hc.core5.http.io.HttpFilterHandler;

import cloud.tamacat.httpd.config.ServiceConfig;

/**
 * Request/Response Filter
 */
public abstract class Filter implements HttpFilterHandler {

	protected ServiceConfig serviceConfig;
	protected String path;
	
	public void setServerConfig(ServiceConfig serviceConfig) {
		this.serviceConfig = serviceConfig;
		this.path = serviceConfig.getPath();
	}
	
	public Filter serverConfig(ServiceConfig serviceConfig) {
		setServerConfig(serviceConfig);
		return this;
	}
	
	public ServiceConfig getServiceConfig() {
		return serviceConfig;
	}
}
