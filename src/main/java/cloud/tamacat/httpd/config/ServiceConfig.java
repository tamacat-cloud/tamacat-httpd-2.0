/*
 * Copyright (c) 2019 tamacat.org
 */
package cloud.tamacat.httpd.config;

import java.util.Collection;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import cloud.tamacat.util.CollectionUtils;
import cloud.tamacat.util.JsonUtils;

public class ServiceConfig {
	
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
	ReverseConfig reverse;
	
	@SerializedName("reverses")
	@Expose
	Collection<ReverseConfig> reverses = CollectionUtils.newArrayList();
	
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

	public ReverseConfig getReverse() {
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
	
	@Override
	public String toString() {
		return "ServiceConfig [path=" + path + ", type=" + type + ", id=" + id + ", config=" + config + ", handler="
				+ handler + ", docsRoot=" + docsRoot + ", reverse=" + reverse + ", reverses=" + reverses + "]";
	}

	public String toJson() {
		return JsonUtils.toJson(this);
	}
}
