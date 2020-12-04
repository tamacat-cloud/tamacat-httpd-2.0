/*
 * Copyright 2019 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import cloud.tamacat.di.DI;
import cloud.tamacat.httpd.filter.Filter;

public class FilterConfig {

	@SerializedName("id")
	@Expose
	String id;
	
	@SerializedName("config")
	@Expose
	String config = "components.xml";
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public FilterConfig id(String id) {
		setId(id);
		return this;
	}

	public Filter getFilter(ServiceConfig serviceConfig) {
		return DI.configure(config).getBean(id, Filter.class).serverConfig(serviceConfig);
	}
}
