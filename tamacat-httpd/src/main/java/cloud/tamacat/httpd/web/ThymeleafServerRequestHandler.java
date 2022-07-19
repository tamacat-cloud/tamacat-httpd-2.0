/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package cloud.tamacat.httpd.web;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.thymeleaf.context.Context;

import cloud.tamacat.httpd.config.ServiceConfig;
import cloud.tamacat.httpd.error.ForbiddenException;
import cloud.tamacat.httpd.error.NotFoundException;
import cloud.tamacat.httpd.error.ThymeleafErrorPage;
import cloud.tamacat.httpd.error.ThymeleafPage;
import cloud.tamacat.httpd.util.MimeUtils;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;
import cloud.tamacat.util.PropertyUtils;
import cloud.tamacat.util.ResourceNotFoundException;
import cloud.tamacat.util.StringUtils;

/**
 * Embedded HTTP/1.1 file server using classic I/O.
 * 
 * @see https://github.com/apache/httpcomponents-core/blob/5.1.x/httpcore5/src/test/java/org/apache/hc/core5/http/examples/ClassicFileServerExample.java
 */
public class ThymeleafServerRequestHandler implements HttpRequestHandler {

	static final Log ACCESS = LogFactory.getLog("Access");
	static final Log LOG = LogFactory.getLog(ThymeleafServerRequestHandler.class);
	static final String DEFAULT_CONTENT_TYPE = "text/html; charset=UTF-8";

	protected ClassLoader loader;
	protected ThymeleafPage page;
	protected ThymeleafErrorPage errorPage;
	protected String welcomeFile = "index.html";
	protected Properties props;

	protected String docsRoot;

	protected final Set<String> urlPatterns = new HashSet<>();
	ServiceConfig serviceConfig;
	
	public void setUrlPatterns(String patterns) {
		for (String pattern : patterns.split(",")) {
			urlPatterns.add(pattern.trim());
		}
	}
	
	public ThymeleafServerRequestHandler(ServiceConfig serviceConfig) {
		this.serviceConfig = serviceConfig;
		init(serviceConfig.getDocsRoot());
	}

	protected void init(String docsRoot) {
		if (docsRoot.endsWith("/")) {
			this.docsRoot = docsRoot;	
		} else {
			this.docsRoot = docsRoot + "/";
		}
		
		Properties props = new Properties();
		try {
			props = PropertyUtils.getProperties("application.properties", getClassLoader());
		} catch (ResourceNotFoundException e) {
			LOG.warn(e.getMessage());
		}
		page = new ThymeleafPage(props, this.docsRoot);
		errorPage = new ThymeleafErrorPage(props);
	}
	
	@Override
	public void handle(
			ClassicHttpRequest request,
			ClassicHttpResponse response,
			HttpContext context) throws HttpException, IOException {
		try {
			Context ctx = (Context) context.getAttribute(Context.class.getName());
			if (ctx == null) {
				ctx = new Context();
			}
			
			URI requestUri = request.getUri();			
			HttpCoreContext coreContext = HttpCoreContext.adapt(context);
			EndpointDetails endpoint = coreContext.getEndpointDetails();
			
			String path = requestUri.getPath();
			if (StringUtils.isEmpty(path) || path.contains("..")) {
				throw new NotFoundException();
			}
			//ctx.setVariable("param", RequestUtils.parseParameters(request, context, encoding).getParameterMap());
			ctx.setVariable("contextRoot", serviceConfig.getPath().replaceFirst("/$", ""));
			if (isMatchUrlPattern(path) || path.endsWith("/")) {
				if (path.endsWith("/")) {
					if (welcomeFile == null) {
						path = path + "index.html";
					} else {
						path = path + welcomeFile;
					}
				}
				
				// delete the extention of file name. (index.html -> index)
				String file = path.indexOf(".") >= 0 ? path.split("\\.")[0] : path;
				String html = page.getPage(request, path);
				LOG.debug(endpoint + ": path " + file);
				setEntity(response, new StringEntity(html, ContentType.TEXT_HTML));
				response.setCode(HttpStatus.SC_OK);
			} else {
				File file = new File(docsRoot, getDecodeUri(path));
				if (!file.exists()) {
					throw new NotFoundException("Not found file " + file.getPath());
				} else if (file.canRead() == false) {
					throw new ForbiddenException("Cannot read file " + file.getPath());
				}
				
				ContentType contentType = ContentType.parse(DEFAULT_CONTENT_TYPE);
				try {
					contentType = ContentType.parse(getContentType(file));
				} catch (Exception e) {
					contentType = ContentType.DEFAULT_BINARY;
				}
				LOG.debug(endpoint + ": serving file " + file);
				setEntity(response, new FileEntity(file, contentType));
				response.setCode(HttpStatus.SC_OK);
			}
	
			ACCESS.info(request+" 200 [OK]");
		} catch (NotFoundException e) {
			handleNotFound(request, response, context, e);
		} catch (ForbiddenException e) {
			handleForbidden(request, response, context, e);
		} catch (Exception e) {
			handleNotFound(request, response, context, new NotFoundException(e));
		}
	}

	public boolean isMatchUrlPattern(String path) {
		if (urlPatterns.size() > 0) {
			for (String pattern : urlPatterns) {
				if (pattern.endsWith("/") && path.matches(pattern)) {
					return true;
				} else if (path.lastIndexOf(pattern) >= 0) {
					return true;
				}
			}
		} else if (path.lastIndexOf(".html") >= 0) {
			return true;
		}
		return false;
	}
	
	protected void handleNotFound(HttpRequest request, HttpResponse response, HttpContext context, NotFoundException e) throws HttpException, IOException {
		LOG.debug(e.getMessage());
		String html = errorPage.getErrorPage(request, new NotFoundException());
		setEntity(response, new StringEntity(html, ContentType.TEXT_HTML));
		response.setCode(HttpStatus.SC_NOT_FOUND);
		ACCESS.info(request+" 404 [NotFound]");
	}
	
	protected void handleForbidden(HttpRequest request, HttpResponse response, HttpContext context, ForbiddenException e) throws HttpException, IOException {
		LOG.debug(e.getMessage());
		String html = errorPage.getErrorPage(request, new ForbiddenException());
		setEntity(response, new StringEntity(html, ContentType.TEXT_HTML));
		response.setCode(HttpStatus.SC_FORBIDDEN);
		ACCESS.info(request+" 403 [Forbidden]");
	}
	
	protected void setEntity(HttpResponse response, HttpEntity entity) {
		if (response instanceof HttpEntityContainer) {
			((HttpEntityContainer)response).setEntity(entity);
		}
	}
	
	protected String getDecodeUri(String uri) {
		String decoded = URLDecoder.decode(uri, StandardCharsets.UTF_8);
		if (StringUtils.isEmpty(decoded) || decoded.contains("..")) {
			throw new NotFoundException();
		}
		return decoded;
	}
	
	/**
	 * <p>The contents type is acquired from the extension. <br>
	 * The correspondence of the extension and the contents type is
	 *  acquired from the {@code mime-types.properties} file. <br>
	 * When there is no file and the extension cannot be acquired,
	 * an {@link DEFAULT_CONTENT_TYPE} is returned.
	 * @param file
	 * @return contents type
	 */
	protected String getContentType(File file) {
		if (file == null) return DEFAULT_CONTENT_TYPE;
		String fileName = file.getName();
		String contentType =  getContentType(fileName);
		return StringUtils.isNotEmpty(contentType)? contentType : DEFAULT_CONTENT_TYPE;
	}
	/**
	 * <p>The contents type is acquired from the extension. <br>
	 * The correspondence of the extension and the contents type is
	 *  acquired from the {@code mime-types.properties} path. <br>
	 * When there is no file and the extension cannot be acquired,
	 * an null is returned.
	 * @param path
	 * @return contents type
	 * @since 1.1
	 */
	protected String getContentType(String path) {
		return MimeUtils.getContentType(path.toLowerCase(Locale.ROOT));
	}
	
	/**
	 * <p>
	 * Get the ClassLoader, default is getClass().getClassLoader().
	 * @return
	 */
	public ClassLoader getClassLoader() {
		return loader != null ? loader : getClass().getClassLoader();
	}
}
