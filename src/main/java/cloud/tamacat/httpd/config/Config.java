/*
 * Copyright 2019 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import cloud.tamacat.util.JsonUtils;
import cloud.tamacat.util.StringUtils;

public class Config implements Serializable {
	private final static long serialVersionUID = -7256101660692911262L;

	@SerializedName("serverName")
	@Expose
	String serverName = "Httpd";

	@SerializedName("host")
	@Expose
	String host;

	String protocol = "http";
	
	@SerializedName("port")
	@Expose
	int port = 80;

	@SerializedName("maxTotal")
	@Expose
	int maxTotal = 100;
	
	@SerializedName("maxPerRoute")
	@Expose
	int maxParRoute = 20;

	@SerializedName("SoTimeout")
	@Expose
	int soTimeout = 60;
		
	@SerializedName("services")
	@Expose
	Collection<ServiceConfig> services = new ArrayList<>();

	public String getServerName() {
		return serverName;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getProtocol() {
		return protocol;
	}

	public int getMaxTotal() {
		return maxTotal;
	}

	public int getMaxParRoute() {
		return maxParRoute;
	}
	
	public int getSoTimeout() {
		return soTimeout;
	}

	public Config host(String host) {
		if (StringUtils.isNotEmpty(host)) {
			this.host = host;
		}
		return this;
	}

	public Config protocol(String protocol) {
		if (StringUtils.isNotEmpty(protocol)) {
			this.protocol = protocol;
		}
		return this;
	}

	public Config port(Integer port) {
		if (port != null) {
			this.port = port.intValue();
		}
		return this;
	}

	public Config serverName(String serverName) {
		if (StringUtils.isNotEmpty(serverName)) {
			this.serverName = serverName;
		}
		return this;
	}

	public Config maxTotal(Integer maxTotal) {
		if (maxTotal != null) {
			this.maxTotal = maxTotal.intValue();
		}
		return this;
	}

	public Config maxParRoute(Integer maxParRoute) {
		if (maxParRoute != null) {
			this.maxParRoute = maxParRoute.intValue();
		}
		return this;
	}
	
	public Config soTimeout(Integer soTimeout) {
		if (soTimeout != null) {
			this.soTimeout = soTimeout.intValue();
		}
		return this;
	}

	public Collection<ServiceConfig> getServices() {
		return services;
	}

	public void setServices(Collection<ServiceConfig> services) {
		this.services = services;
	}

	@Override
	public String toString() {
		return "Config [serverName=" + serverName + ", host=" + host + ", protocol=" + protocol + ", port=" + port
				+ ", maxTotal=" + maxTotal + ", maxParRoute=" + maxParRoute + ", soTimeout=" + soTimeout + ", services="
				+ services + "]";
	}

	public static Config load(String json) {
		return JsonUtils.fromJsonInClasspath(json, Config.class);
	}

	public String toJson() {
		return JsonUtils.toJson(this);
	}
}
