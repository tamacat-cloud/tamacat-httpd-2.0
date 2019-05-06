/*
 * Copyright (c) 2019 tamacat.org
 */
package cloud.tamacat.httpd.config;

import java.util.Collection;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import cloud.tamacat.util.CollectionUtils;

public class ServiceConfig {
	String path;
	String type;
	
	String id;
	String config;
	String handler;
	
	ReverseConfig reverse;
	
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
	
	public JsonObject toJson() {
		JsonObjectBuilder json = Json.createObjectBuilder();
		if (path != null) json.add("path", path);
		if (type != null) json.add("type", type);
		if (id != null) json.add("id", id);
		if (config != null) json.add("config", config);
		if (handler != null) json.add("handler", handler);
		if (reverse != null) {
			json.add("reverse", Json.createObjectBuilder().add("url", reverse.url.getPath()));
		}
		if (reverses.size() > 0) {
			JsonArrayBuilder list = Json.createArrayBuilder();
			for (ReverseConfig reverse : reverses) {
				if (reverse.url != null) {
					list.add(Json.createObjectBuilder().add("url",reverse.url.getPath()));
				}
			}
			json.add("reverses", list);
		}
		return json.build();
	}
}
