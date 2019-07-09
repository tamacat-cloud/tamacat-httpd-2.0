/*
 * Copyright (c) 2019 tamacat.org
 */
package cloud.tamacat.httpd.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import cloud.tamacat.util.IOUtils;
import cloud.tamacat.util.JsonUtils;
import cloud.tamacat.util.StringUtils;

public class Config {

	String serverName = "Httpd";

	String host;

	String protocol = "http";
	int port = 80;

	int maxTotal = 100;
	int maxParRoute = 20;

	int soTimeout = 60;
	
	String json;
	
	Collection<ServiceConfig> configs = new ArrayList<>();

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

	public Collection<ServiceConfig> getConfigs() {
		return configs;
	}

	public static Config load(String json) {
		Function<JsonObject, ReverseConfig> parseReverseConfig = (arg) -> {
			if (arg.containsKey("url")) {
				return new ReverseConfig().url(arg.getString("url"));
			}
			return null;
		};

		Config config = new Config();
		config.json = json;

		JsonReader reader = Json.createReader(IOUtils.getInputStream(json));
		JsonObject root = reader.readObject();

		config.serverName(JsonUtils.getString.apply(root, "serverName"))
			.host(JsonUtils.getString.apply(root, "host"))
			.port(JsonUtils.getInt.apply(root, "port"))
			.maxTotal(JsonUtils.getInt.apply(root, "maxTotal"))
			.maxParRoute(JsonUtils.getInt.apply(root, "maxParRoute"))
			.soTimeout(JsonUtils.getInt.apply(root, "soTimeout"));

		JsonArray services = JsonUtils.getArray.apply(root, "services");
		for (int i = 0; i < services.size(); i++) {
			ServiceConfig serviceConfig = new ServiceConfig();
			JsonObject service = services.getJsonObject(i);

			serviceConfig.path = JsonUtils.getString.apply(service, "path");
			serviceConfig.type = JsonUtils.getString.apply(service, "type");
			serviceConfig.id = JsonUtils.getString.apply(service, "id");
			serviceConfig.config = JsonUtils.getString.apply(service, "config");
			serviceConfig.docsRoot = JsonUtils.getString.apply(service, "docsRoot");
			
			JsonObject reverseConfig = JsonUtils.getObject.apply(service, "reverse");
			serviceConfig.reverse = parseReverseConfig.apply(reverseConfig);

			JsonArray reverses = JsonUtils.getArray.apply(service, "reverses");
			for (int j = 0; j < reverses.size(); j++) {
				JsonObject reverse = reverses.getJsonObject(j);
				serviceConfig.reverses.add(parseReverseConfig.apply(reverse));
			}
			config.configs.add(serviceConfig);
		}
		return config;
	}

	public JsonObject toJson() {
		JsonObjectBuilder json = Json.createObjectBuilder();
		if (host != null)
			json.add("host", host);
		if (port > 0)
			json.add("port", port);

		JsonArrayBuilder services = Json.createArrayBuilder();
		for (ServiceConfig config : configs) {
			services.add(config.toJson());
		}
		json.add("services", services);
		return json.build();
	}
}
