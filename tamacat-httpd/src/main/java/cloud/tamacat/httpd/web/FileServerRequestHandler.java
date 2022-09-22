/*
 * Copyright 2022 tamacat.org
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
import java.util.Locale;
import java.util.Properties;

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

import cloud.tamacat.httpd.config.ServiceConfig;
import cloud.tamacat.httpd.core.BasicHttpStatus;
import cloud.tamacat.httpd.error.ForbiddenException;
import cloud.tamacat.httpd.error.NotFoundException;
import cloud.tamacat.httpd.error.ThymeleafErrorPage;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;
import cloud.tamacat.util.PropertyUtils;
import cloud.tamacat.util.ResourceNotFoundException;
import cloud.tamacat.util.StringUtils;

/**
 * Embedded HTTP/1.1 file server using classic I/O.
 * 
 * @see https://hc.apache.org/httpcomponents-core-5.0.x/httpcore5/examples/ClassicFileServerExample.java
 */
public class FileServerRequestHandler implements HttpRequestHandler {

	static final Log ACCESS = LogFactory.getLog("Access");
	static final Log LOG = LogFactory.getLog(FileServerRequestHandler.class);
	static final String DEFAULT_CONTENT_TYPE = "text/html; charset=UTF-8";

	protected ClassLoader loader;
	protected ThymeleafErrorPage errorPage;
	protected String welcomeFile = "index.html";
	protected Properties props;

	protected ServiceConfig serviceConfig;
	protected File docsRoot;
	protected ThymeleafListingsPage listingPage;
	protected boolean listings;
	
	public FileServerRequestHandler(ServiceConfig serviceConfig) {
		this(new File(serviceConfig.getDocsRoot()));
		this.serviceConfig = serviceConfig;
		listings = serviceConfig.isListings();
		if (listings) {
			listingPage = new ThymeleafListingsPage(new Properties());
		}
	}

	public FileServerRequestHandler(File docsRoot) {
		this.docsRoot = docsRoot;
		Properties props = new Properties();
		try {
			props = PropertyUtils.getProperties("application.properties", getClassLoader());
		} catch (ResourceNotFoundException e) {
			LOG.warn(e.getMessage());
		}
		errorPage = new ThymeleafErrorPage(props);
	}
	
	@Override
	public void handle(
            final ClassicHttpRequest request,
            final ClassicHttpResponse response,
            final HttpContext context) throws HttpException, IOException {
		try {
			URI requestUri = request.getUri();			
			HttpCoreContext coreContext = HttpCoreContext.adapt(context);
			EndpointDetails endpoint = coreContext.getEndpointDetails();
			
			String path = requestUri.getPath();
			if (StringUtils.isEmpty(path) || path.contains("..")) {
				throw new NotFoundException();
			}
			if (path.endsWith("/") && useDirectoryListings() == false) {
				path = path + welcomeFile;
			}
			File file = new File(docsRoot, getDecodeUri(path).replace(serviceConfig.getPath(), ""));
			if (!file.exists()) {
				throw new NotFoundException("Not found file " + file.getPath());
			} else if (!file.canRead() || file.isDirectory()) {
				if (useDirectoryListings()) {					
					String html = listingPage.getListingsPage(request, file);
					response.setHeader("Content-Type", DEFAULT_CONTENT_TYPE);
					response.setCode(BasicHttpStatus.SC_OK.getStatusCode());
					response.setReasonPhrase(BasicHttpStatus.SC_OK.getReasonPhrase());
					response.setEntity(new StringEntity(html));
					ACCESS.info(request+" 200 [OK]");
					return;
				} else {
					throw new ForbiddenException("Cannot read file " + file.getPath());
				}
			}
			ContentType contentType;
			String filename = file.getName().toLowerCase(Locale.ROOT);
			if (filename.endsWith(".txt")) {
				contentType = ContentType.TEXT_PLAIN;
			} else if (filename.endsWith(".html") || filename.endsWith(".htm")) {
				contentType = ContentType.TEXT_HTML;
			} else if (filename.endsWith(".xml")) {
				contentType = ContentType.TEXT_XML;
			} else {
				contentType = ContentType.DEFAULT_BINARY;
			}
			
			LOG.debug(endpoint + ": serving file " + file.getAbsolutePath());
			setEntity(response, new FileEntity(file, contentType));
			response.setCode(HttpStatus.SC_OK);
			ACCESS.info(request+" 200 [OK]");
		} catch (NotFoundException e) {
			handleNotFound(request, response, context, e);
		} catch (ForbiddenException e) {
			handleForbidden(request, response, context, e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new NotFoundException(e.getMessage(), e);
		}
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
	 * <p>Should directory listings be produced
	 * if there is no welcome file in this directory.</p>
	 *
	 * <p>The welcome file becomes unestablished when I set true.<br>
	 * When I set the welcome file, please set it after having
	 * carried out this method.</p>
	 *
	 * @param listings true: directory listings be produced (if welcomeFile is null).
	 */
	public void setListings(boolean listings) {
		this.listings = listings;
		if (listings) {
			this.welcomeFile = null;
		}
	}

	public void setListingsPage(String listingsPage) {
		listingPage.setListingsPage(listingsPage);
	}

	protected boolean useDirectoryListings() {
		if (listings) {
			return true;
		} else {
			return false;
		}
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
