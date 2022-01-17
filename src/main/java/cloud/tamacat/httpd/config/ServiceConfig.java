/*
 * Copyright 2019 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import cloud.tamacat.util.CollectionUtils;
import cloud.tamacat.util.JsonUtils;
import cloud.tamacat.util.StringUtils;

public class ServiceConfig {
	
	@SerializedName("host")
	@Expose
	String host;

	@SerializedName("path")
	@Expose
	String path;
	
	@SerializedName("type")
	@Expose
	String type;
	
	@SerializedName("id")
	@Expose
	String id;
	
	@SerializedName("config")
	@Expose
	String config;
	
	@SerializedName("handler")
	@Expose
	String handler;
	
	@SerializedName("docsRoot")
	@Expose
	String docsRoot;
	
	@SerializedName("reverse")
	@Expose
	ReverseConfig reverse = new ReverseConfig();
	
	@SerializedName("reverses")
	@Expose
	Collection<ReverseConfig> reverses = CollectionUtils.newArrayList();
	
	@SerializedName("filters")
	@Expose
	Map<String, FilterConfig> filters = CollectionUtils.newLinkedHashMap();

	protected ServerConfig serverConfig;
	
	public void setServerConfig(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
	}
	
	public ServerConfig getServerConfig() {
		return serverConfig;
	}
	
	public URL getHost() {
		try {
			return new URL(host);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	public ServiceConfig host(String host) {
		setHost(host);
		return this;
	}
	
	public String getPath() {
		return path;
	}

	public ServiceConfig path(String path) {
		this.path = path;
		return this;
	}

	public String getType() {
		return type;
	}

	public ServiceConfig type(String type) {
		this.type = type;
		return this;
	}

	public String getId() {
		return id;
	}

	public ServiceConfig id(String id) {
		this.id = id;
		return this;
	}

	public String getConfig() {
		return config;
	}

	public ServiceConfig config(String config) {
		this.config = config;
		return this;
	}

	public String getHandler() {
		return handler;
	}

	public ServiceConfig handler(String handler) {
		this.handler = handler;
		return this;
	}
	
	public String getDocsRoot() {
		return docsRoot;
	}

	public ServiceConfig docsRoot(String docsRoot) {
		this.docsRoot = docsRoot;
		return this;
	}

	public boolean isReverseProxy() {
		return "reverse".equals(type) 
			&& (reverses.size()>=1 || (reverse != null && StringUtils.isNotEmpty(reverse.getUrl())));
	}
	
	public ReverseConfig getReverse() {
		if (reverse.getServiceConfig() == null) {
			reverse.setServiceConfig(this);
		}
		return reverse;
	}

	public ServiceConfig reverse(ReverseConfig reverse) {
		this.reverse = reverse;
		return this;
	}

	public Collection<ReverseConfig> getReverses() {
		return reverses;
	}

	public ServiceConfig reverses(Collection<ReverseConfig> reverses) {
		this.reverses = reverses;
		return this;
	}
	
	public Map<String, FilterConfig> getFilters() {
		return filters;
	}

	public void setFilters(Map<String, FilterConfig> filters) {
		this.filters = filters;
	}
	
	public ServiceConfig filters(Map<String, FilterConfig> filters) {
		setFilters(filters);
		return this;
	}
	
	@Override
	public String toString() {
		return "ServiceConfig [host=" + host + ", path=" + path + ", type=" + type + ", id=" + id + ", config=" + config + ", handler="
				+ handler + ", docsRoot=" + docsRoot + ", reverse=" + reverse + ", reverses=" + reverses + "]";
	}

	public String toJson() {
		return JsonUtils.toJson(this);
	}
}
