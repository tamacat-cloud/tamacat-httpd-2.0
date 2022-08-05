/*
 * Copyright 2019 tamacat.org
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
package cloud.tamacat.httpd.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import cloud.tamacat.di.DI;
import cloud.tamacat.httpd.filter.AsyncFilter;
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
	
	public AsyncFilter getAsyncFilter(ServiceConfig serviceConfig) {
		return DI.configure(config).getBean(id, AsyncFilter.class).serverConfig(serviceConfig);
	}
}
