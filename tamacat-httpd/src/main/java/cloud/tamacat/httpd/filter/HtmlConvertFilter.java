/*
 * Copyright 2020 tamacat.org
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
package cloud.tamacat.httpd.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.protocol.HttpContext;

import cloud.tamacat.httpd.config.ReverseConfig;
import cloud.tamacat.httpd.reverse.html.LinkConvertingEntity;
import cloud.tamacat.httpd.util.HeaderUtils;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;
import cloud.tamacat.util.StringUtils;

/**
 * Response Filter
 */
public class HtmlConvertFilter extends HttpFilter {

	static final Log LOG = LogFactory.getLog(HtmlConvertFilter.class);

	protected Set<String> contentTypes = new HashSet<String>();
	protected List<Pattern> linkPatterns = new ArrayList<>();

	public HtmlConvertFilter() {
		contentTypes.add("html");
		setContentType("text/html");
	}
	
	/**
	 * Add link convert pattern.
	 * @param regex The expression to be compiled.(case insensitive)
	 */
	public HtmlConvertFilter addLinkPattern(String regex) {
		this.linkPatterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
		return this;
	}

	@Override
	protected void handleRequest(ClassicHttpRequest request, HttpContext context) throws HttpException, IOException {
		request.removeHeaders(HttpHeaders.ACCEPT_ENCODING); //disabled encoding
	}
	
	@Override
	protected void handleSubmitResponse(ClassicHttpResponse response, HttpContext context) throws HttpException, IOException {
		if (HeaderUtils.inContentType(contentTypes, response.getFirstHeader(HttpHeaders.CONTENT_TYPE))) {
			ReverseConfig reverse = (ReverseConfig) context.getAttribute(ReverseConfig.class.getName());
			if (reverse != null) {
				HttpEntity entity = response.getEntity();
				if (entity == null) return;
				
				String before = reverse.getReverse().getPath();
				String after = reverse.getServiceConfig().getPath();
				if (before.equals(after) == false) {
					LOG.trace("[enabled] "+before+"->"+after);
					response.setEntity(new LinkConvertingEntity(entity, before, after, linkPatterns));
					//response.setHeader("Transfer-Encoding", "chunked"); //Transfer-Encoding:chunked
					response.removeHeaders(HttpHeaders.CONTENT_LENGTH);
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
