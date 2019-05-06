/*
 * Copyright (c) 2019 tamacat.org
 */
package cloud.tamacat.httpd.config;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.hc.core5.http.HttpHost;

public class ReverseConfig {
	
	URL url;
	HttpHost target;
	
	public ReverseConfig url(String url) {
		try {
			this.url = new URL(url);
			this.target = new HttpHost(this.url.getHost(), this.url.getPort());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public URL getUrl() {
		return url;
	}
	
	public HttpHost getTarget() {
		return target;
	}
}
