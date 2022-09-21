/*
 * Copyright 2009 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.reverse.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;

import cloud.tamacat.httpd.config.ReverseConfig;
import cloud.tamacat.httpd.util.HeaderUtils;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;
import cloud.tamacat.util.StringUtils;

/**
 * <p>
 * HTML link convert for reverse proxy.
 */
public class HtmlLinkConvertInterceptor implements HttpResponseInterceptor {

	static final Log LOG = LogFactory.getLog(HtmlLinkConvertInterceptor.class);
	
	protected Set<String> contentTypes = new HashSet<String>();
	protected List<Pattern> linkPatterns = new ArrayList<Pattern>();

	public HtmlLinkConvertInterceptor() {
		contentTypes.add("html");
	}

	/**
	 * Add link convert pattern.
	 * 
	 * @param regex The expression to be compiled.(case insensitive)
	 */
	public void setLinkPattern(String regex) {
		this.linkPatterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
	}

	@Override
	public void process(HttpResponse response, EntityDetails entity, HttpContext context) throws HttpException, IOException {
		if (response instanceof HttpEntityContainer == false || entity == null || entity instanceof HttpEntity == false) return;
		if (context == null) {
			throw new IllegalArgumentException("HTTP context may not be null");
		}
		ReverseConfig reverseUrl = (ReverseConfig) context.getAttribute(ReverseConfig.class.getName());
		if (reverseUrl != null) {
			Header header = response.getFirstHeader(HttpHeaders.CONTENT_TYPE);
			if (header != null && HeaderUtils.inContentType(contentTypes, header)) {
				String before = reverseUrl.getTarget().toURI(); //.getPath();
				String after = reverseUrl.getServiceConfig().getPath();
				if (before.equals(after) == false) {
					response.setHeader(HttpHeaders.TRANSFER_ENCODING, "chunked"); //Transfer-Encoding:chunked
					response.removeHeaders(HttpHeaders.CONTENT_LENGTH);
					HttpEntity newEntity = new LinkConvertingEntity((HttpEntity)entity, before, after, linkPatterns);
					((HttpEntityContainer)response).setEntity(newEntity);
					LOG.debug("[converted] "+newEntity);
				}
			}
		}
	}

	/**
	 * <p>
	 * Set the content type of the link convertion.<br>
	 * default are "text/html" content types to convert.
	 * </p>
	 * <p>
	 * The {@code contentType} value is case insensitive,<br>
	 * and the white space of before and after is trimmed.
	 * </p>
	 * 
	 * <p>
	 * Examples: {@code contentType="html, css, javascript, xml" }
	 * <ul>
	 * <li>text/html</li>
	 * <li>text/css</li>
	 * <li>text/javascript</li>
	 * <li>application/xml</li>
	 * <li>text/xml</li>
	 * </ul>
	 * 
	 * @param contentType Comma Separated Value of content-type or sub types.
	 */
	public void setContentType(String contentType) {
		if (StringUtils.isNotEmpty(contentType)) {
			String[] csv = StringUtils.split(contentType, ",");
			for (String t : csv) {
				contentTypes.add(t.trim().toLowerCase());
				String[] types = StringUtils.split(t, ";")[0].split("/");
				if (types.length >= 2) {
					contentTypes.add(types[1].trim().toLowerCase());
				}
			}
		}
	}
}
