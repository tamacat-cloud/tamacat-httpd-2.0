/*
 * Copyright 2009-2022 tamacat.org
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
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.io.entity.HttpEntityWrapper;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;

import cloud.tamacat.httpd.util.HeaderUtils;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;
import cloud.tamacat.util.ExceptionUtils;
import cloud.tamacat.util.StringUtils;

/**
 * <p>Server-side interceptor to handle Gzip-encoded responses.<br>
 * The cord of the basis is Apache HttpComponents {@code ResponseGzipCompress.java}.</p>
 *
 * <pre>Example:{@code components.xml}
 * {@code <bean id="gzip" class="org.tamacat.httpd.filter.GzipResponseInterceptor">
 *  <property name="contentType">
 *    <value>html,xml,css,javascript</value>
 *  </property>
 * </bean>
 * }</pre>
 *
 * {@link http://svn.apache.org/repos/asf/httpcomponents/httpcore/trunk/contrib/src/main/java/org/apache/http/contrib/compress/ResponseGzipCompress.java}
 */
public class GzipResponseInterceptor implements HttpResponseInterceptor {

	static final Log LOG = LogFactory.getLog(GzipResponseInterceptor.class);
	protected static final String GZIP_CODEC = "gzip";

	protected Set<String> contentTypes = new HashSet<String>();
	protected boolean useAll = true;

	@Override
	public void process(HttpResponse response, EntityDetails entity, HttpContext context)
			throws HttpException, IOException {
		if (response instanceof HttpEntityContainer == false || entity == null || entity instanceof HttpEntity == false) return;
		if (context == null) {
			throw new IllegalArgumentException("HTTP context may not be null");
		}
		HttpRequest request = (HttpRequest) context.getAttribute(HttpCoreContext.HTTP_REQUEST);
		Header aeheader = request != null ? request.getFirstHeader(HttpHeaders.ACCEPT_ENCODING) : null;
		if (request != null && request.getVersion().greaterEquals(HttpVersion.HTTP_1_1)
				&& aeheader != null && useCompress(response.getFirstHeader(HttpHeaders.CONTENT_TYPE))) {
			String ua = HeaderUtils.getHeader(request, "User-Agent");
			if (ua != null && ua.indexOf("MSIE 6.0") >= 0) {
				return; //Skipped for IE6 bug(KB823386)
			}
			String codecs = aeheader.getValue();
			if (codecs != null && codecs.toLowerCase().contains(GZIP_CODEC)) {
				GzipCompressingEntity gzipEntity = new GzipCompressingEntity((HttpEntity)entity);
				((HttpEntityContainer)response).setEntity(gzipEntity);
				response.setHeader(HttpHeaders.CONTENT_ENCODING, GZIP_CODEC); //Content-Encoding:gzip
				response.setHeader(HttpHeaders.TRANSFER_ENCODING, "chunked"); //Transfer-Encoding:chunked
				response.removeHeaders(HttpHeaders.CONTENT_LENGTH);
				return;
			}
		}
	}

	/**
	 * <p>Set the content type of the gzip compression.<br>
	 * default are all content types to compressed.</p>
	 * <p>The {@code contentType} value is case insensitive,<br>
	 * and the white space of before and after is trimmed.</p>
	 *
	 * <p>Examples: {@code contentType="html, css, javascript, xml" }
	 * <ul>
	 *   <li>text/html</li>
	 *   <li>text/css</li>
	 *   <li>text/javascript</li>
	 *   <li>application/xml</li>
	 *   <li>text/xml</li>
	 * </ul>
	 * @param contentType Comma Separated Value of content-type or sub types.
	 */
	public void setContentType(String contentType) {
		if (StringUtils.isNotEmpty(contentType)) {
			String[] csv = StringUtils.split(contentType, ",");
			for (String t : csv) {
				contentTypes.add(t.toLowerCase());
				useAll = false;
				String[] types = t.split(";")[0].split("/");
				if (types.length >= 2) {
					contentTypes.add(types[1].toLowerCase());
				}
			}
		}
	}

	/**
	 * <p>Check for use compress contents.
	 * @param contentType
	 * @return true use compress.
	 */
	boolean useCompress(Header contentType) {
		if (contentType == null) return false;
		String type = contentType.getValue();
		if (useAll || contentTypes.contains(type)) {
			return true;
		} else {
			//Get the content sub type. (text/html; charset=UTF-8 -> html)
			String[] types = type != null ? type.split(";")[0].split("/") : new String[0];
			if (types.length >= 2 && contentTypes.contains(types[1])) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * <p>Wrapping entity that compresses content when {@link #writeTo writing}.
	 * {@link http://svn.apache.org/repos/asf/httpcomponents/httpcore/trunk/contrib/src/main/java/org/apache/http/contrib/compress/GzipCompressingEntity.java}
	 */
	static class GzipCompressingEntity extends HttpEntityWrapper {
		HttpEntity wrappedEntity;
		
		public GzipCompressingEntity(HttpEntity entity) {
			super(entity);
			wrappedEntity = entity;
		}

		@Override
		public String getContentEncoding() {
			return GZIP_CODEC;
		}

		@Override
		public long getContentLength() {
			return -1;
		}

		@Override
		public boolean isChunked() {
			// force content chunking
			return true;
		}

		@Override
		public void writeTo(OutputStream outstream) throws IOException {
			if (outstream == null) {
				throw new IllegalArgumentException("Output stream may not be null");
			}
			GZIPOutputStream gzip = new GZIPOutputStream(outstream);
			try {
				wrappedEntity.writeTo(gzip);
			} finally {
				try {
					gzip.close();
				} catch (IOException e) {
					LOG.debug(ExceptionUtils.getStackTrace(e, 100));
				}
			}
		}
	}
}
