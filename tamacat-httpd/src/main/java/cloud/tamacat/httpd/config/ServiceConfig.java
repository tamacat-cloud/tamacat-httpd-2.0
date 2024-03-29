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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import cloud.tamacat.httpd.filter.HttpFilter;
import cloud.tamacat.httpd.filter.async.AsyncHttpFilter;
import cloud.tamacat.httpd.util.ServerUtils;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;
import cloud.tamacat.util.CollectionUtils;
import cloud.tamacat.util.JsonUtils;
import cloud.tamacat.util.StringUtils;

public class ServiceConfig {
	
	static final Log LOG = LogFactory.getLog(ServiceConfig.class);
	
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
	
	@SerializedName("listings")
	@Expose
	boolean listings;
	
	@SerializedName("reverse")
	@Expose
	ReverseConfig reverse = new ReverseConfig();
	
	@SerializedName("reverses")
	@Expose
	Collection<ReverseConfig> reverses = CollectionUtils.newArrayList();
	
	@SerializedName("filters")
	@Expose
	Map<String, FilterConfig> filterConfigs = CollectionUtils.newLinkedHashMap();

	Collection<HttpFilter> httpFilters = CollectionUtils.newArrayList();
	Collection<AsyncHttpFilter> asyncFilters = CollectionUtils.newArrayList();

	protected ServerConfig serverConfig;
	protected String serverHome;

	public static ServiceConfig create() {
		return new ServiceConfig();
	}
	
	public void setServerConfig(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
	}
	
	public ServiceConfig serverConfig(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
		return this;
	}
	
	public ServerConfig getServerConfig() {
		return serverConfig;
	}
	
	public String getHostname() {
		return host;
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
	
	/**
	 * Configure Virtual Host
	 * @param host
	 */
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
		String docsRoot = this.docsRoot;
		String serverHome = getServerHome();
		if (docsRoot == null) {
			docsRoot = serverHome + "/htdocs/root";
			LOG.warn("The docsRoot was empty. Use default: "+docsRoot);
		}
		if (docsRoot.indexOf("${server.home}") >= 0) {
			docsRoot = docsRoot.replace("${server.home}", serverHome);
		}
		return docsRoot;
	}

	public ServiceConfig docsRoot(String docsRoot) {
		this.docsRoot = docsRoot;
		return this;
	}
	
	public boolean isListings() {
		return listings;
	}
	
	public boolean isReverseProxy() {
		return isType("reverse") 
			|| (reverses.size()>=1 || (reverse != null && StringUtils.isNotEmpty(reverse.getUrl())));
	}
	
	public boolean isRedirect() {
		return isType("redirect") && (reverse != null && StringUtils.isNotEmpty(reverse.getUrl()));
	}
	
	public boolean isType(String name) {
		return (name).equals(this.type);
	}
	
	public boolean isJetty() {
		return isType("jetty");
	}
	
	public boolean isThymeleaf() {
		return isType("thymeleaf");
	}
	
	public ReverseConfig getReverse() {
		if (reverse.getServiceConfig() == null) {
			reverse.setServiceConfig(this);
		}
		return reverse;
	}

	public ServiceConfig reverse(ReverseConfig reverse) {
		if (reverse != null) {
			this.reverse = reverse;
			this.type = "reverse";
		}
		return this;
	}

	public ServiceConfig redirect(ReverseConfig reverse) {
		if (reverse != null) {
			this.reverse = reverse;
			this.type = "redirect";
		}
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
		return filterConfigs;
	}

	public ServiceConfig filter(HttpFilter filter) {
		this.httpFilters.add(filter);
		return this;
	}
	
	public ServiceConfig filter(AsyncHttpFilter filter) {
		this.asyncFilters.add(filter);
		return this;
	}
	
	public Collection<HttpFilter> getHttpFilters() {
		return httpFilters;
	}
	
	public Collection<AsyncHttpFilter> getAsyncFilters() {
		return asyncFilters;
	}
	
	public void setFilters(Map<String, FilterConfig> filterConfigs) {
		filterConfigs.forEach((id, filterConfig) -> {
			asyncFilters.add(filterConfig.getAsyncFilter(this));
		});
	}
	
	public ServiceConfig filters(Map<String, FilterConfig> filterConfigs) {
		setFilters(filterConfigs);
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
	
	protected String getServerHome() {
		if (StringUtils.isEmpty(serverHome)) {
			serverHome = ServerUtils.getServerHome();
		}
		return serverHome;
	}
}
