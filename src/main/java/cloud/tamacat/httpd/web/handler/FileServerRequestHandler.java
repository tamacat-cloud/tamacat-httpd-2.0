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
package cloud.tamacat.httpd.web.handler;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.nio.AsyncRequestConsumer;
import org.apache.hc.core5.http.nio.AsyncServerRequestHandler;
import org.apache.hc.core5.http.nio.entity.FileEntityProducer;
import org.apache.hc.core5.http.nio.entity.NoopEntityConsumer;
import org.apache.hc.core5.http.nio.support.BasicRequestConsumer;
import org.apache.hc.core5.http.nio.support.BasicResponseProducer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;

import cloud.tamacat.httpd.config.ServiceConfig;
import cloud.tamacat.httpd.error.ForbiddenException;
import cloud.tamacat.httpd.error.NotFoundException;
import cloud.tamacat.httpd.error.ThymeleafErrorPage;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;
import cloud.tamacat.util.PropertyUtils;
import cloud.tamacat.util.ResourceNotFoundException;
import cloud.tamacat.util.StringUtils;

/**
 * Asynchronous embedded HTTP/1.1 file server.
 * 
 * @see https://hc.apache.org/httpcomponents-core-5.0.x/httpcore5/examples/AsyncFileServerExample.java
 */
public class FileServerRequestHandler implements AsyncServerRequestHandler<Message<HttpRequest, Void>> {

	static final Log ACCESS = LogFactory.getLog("Access");
	static final Log LOG = LogFactory.getLog(FileServerRequestHandler.class);
	
	protected ClassLoader loader;
	protected ThymeleafErrorPage errorPage;
	protected String welcomeFile = "index.html";
	protected Properties props;

	protected File docsRoot;

	public FileServerRequestHandler(ServiceConfig serviceConfig) {
		this(new File(serviceConfig.getDocsRoot()));
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
	public AsyncRequestConsumer<Message<HttpRequest, Void>> prepare(HttpRequest request,
			EntityDetails entityDetails, HttpContext context) throws HttpException {
		return new BasicRequestConsumer<>(entityDetails != null ? new NoopEntityConsumer() : null);
	}

	@Override
	public void handle(Message<HttpRequest, Void> message, ResponseTrigger responseTrigger, HttpContext context)
			throws HttpException, IOException {
		HttpRequest request = message.getHead();
		try {
			URI requestUri;
			try {
				requestUri = request.getUri();
			} catch (final URISyntaxException ex) {
				throw new NotFoundException(ex.getMessage(), ex);
			}
			String path = requestUri.getPath();
			if (StringUtils.isEmpty(path) || path.contains("..")) {
				throw new NotFoundException();
			}
			File file = new File(docsRoot, getDecodeUri(path));
			if (!file.exists()) {
				throw new NotFoundException("Not found file " + file.getPath());
			} else if (!file.canRead() || file.isDirectory()) {
				if (StringUtils.isNotEmpty(welcomeFile)) {
					file = new File(file.getPath(), welcomeFile);
					if (!file.exists()) {
						throw new NotFoundException("Not found file " + file.getPath());
					}
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
	
			HttpCoreContext coreContext = HttpCoreContext.adapt(context);
			EndpointDetails endpoint = coreContext.getEndpointDetails();
			LOG.debug(endpoint + ": serving file " + file.getPath());
			responseTrigger.submitResponse(
				new BasicResponseProducer(HttpStatus.SC_OK,
				new FileEntityProducer(file, contentType)),
				context
			);
			ACCESS.info(request+" 200 [OK]");
		} catch (NotFoundException e) {
			handleNotFound(request, responseTrigger, context, e);
		} catch (ForbiddenException e) {
			handleForbidden(request, responseTrigger, context, e);
		}
	}

	protected void handleNotFound(HttpRequest request, ResponseTrigger responseTrigger, HttpContext context, NotFoundException e) throws HttpException, IOException {
		LOG.debug(e.getMessage());
		String html = errorPage.getErrorPage(request, new NotFoundException());
		responseTrigger.submitResponse(new BasicResponseProducer(HttpStatus.SC_NOT_FOUND, html, ContentType.TEXT_HTML), context);
		ACCESS.info(request+" 404 [NotFound]");
	}
	
	protected void handleForbidden(HttpRequest request, ResponseTrigger responseTrigger, HttpContext context, ForbiddenException e) throws HttpException, IOException {
		LOG.debug(e.getMessage());
		String html = errorPage.getErrorPage(request, new ForbiddenException());
		responseTrigger.submitResponse(new BasicResponseProducer(HttpStatus.SC_FORBIDDEN, html, ContentType.TEXT_HTML), context);
		ACCESS.info(request+" 403 [Forbidden]");
	}
	
	protected String getDecodeUri(String uri) {
		String decoded = URLDecoder.decode(uri, StandardCharsets.UTF_8);
		if (StringUtils.isEmpty(decoded) || decoded.contains("..")) {
			throw new NotFoundException();
		}
		return decoded;
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
