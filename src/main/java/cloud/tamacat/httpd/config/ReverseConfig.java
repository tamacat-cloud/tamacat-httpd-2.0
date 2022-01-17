/*
 * Copyright 2019 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.config;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.hc.core5.http.HttpHost;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import cloud.tamacat.util.JsonUtils;

public class ReverseConfig {
	
	@SerializedName("url")
	@Expose
	String url;
	
	HttpHost target;
	
	ServiceConfig serviceConfig;
	
	private URL host;
	
	public void setServiceConfig(ServiceConfig serviceConfig) {
		this.serviceConfig = serviceConfig;
	}
	
	public ServiceConfig getServiceConfig() {
		return serviceConfig;
	}
	
	public void setUrl(String url) {
		try {
			URL targetUrl = new URL(url);
			this.target = new HttpHost(targetUrl.getHost(), targetUrl.getPort());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public ReverseConfig url(String url) {
		setUrl(url);
		return this;
	}
	
	public String getUrl() {
		return url;
	}
	
	public HttpHost getTarget() {
		if (this.url != null && target == null) {
			setUrl(this.url);
		}
		return target;
	}
	
	public URL getHost() {
		return host;
	}
	
	public void setHost(URL host) {
		if (host != null) {
			try {
				this.host = new URL(host.getProtocol(), host.getHost(), host.getPort(), "");
			} catch (MalformedURLException e) {
			}
		}
	}

	public URL getReverse() {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public URL getReverseUrl(String path) {
		String p = serviceConfig.getPath();
		if (path != null && p != null && path.startsWith(p)) {
			URL reverseUrl = getReverse();
			String distUrl = path.replaceFirst(serviceConfig.getPath(), reverseUrl.getPath());
			try {
				int port = reverseUrl.getPort();
				if (port == -1) {
					port = reverseUrl.getDefaultPort();
				}
				return new URL(reverseUrl.getProtocol(), reverseUrl.getHost(), port, distUrl);
			} catch (MalformedURLException e) {
			}
		}
		return null;
	}
	
	/**
	 * path: http://localhost:8080/examples/servlet
	 *   =>  http://localhost/examples2/servlet
	 */
	public String getConvertRequestedUrl(String path) {
		URL reverseUrl = getReverse();
		URL host = getHost(); // requested URL (path is deleted)
		if (path != null && host != null) {
			return path.replaceFirst(
				reverseUrl.getProtocol() + "://" + reverseUrl.getAuthority(), host.toString())
					.replace(reverseUrl.getPath(), getServiceConfig().getPath());
		} else {
			return path;
		}
	}
	
	public String toJson() {
		return JsonUtils.toJson(this);
	}

	@Override
	public String toString() {
		return "ReverseConfig [url=" + url + "]";
	}
}
