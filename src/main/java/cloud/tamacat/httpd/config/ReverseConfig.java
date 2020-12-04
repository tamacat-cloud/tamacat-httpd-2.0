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
	
	public String toJson() {
		return JsonUtils.toJson(this);
	}
}
