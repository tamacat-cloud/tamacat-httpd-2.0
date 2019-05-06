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
import java.util.Locale;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.nio.AsyncRequestConsumer;
import org.apache.hc.core5.http.nio.AsyncServerRequestHandler;
import org.apache.hc.core5.http.nio.BasicRequestConsumer;
import org.apache.hc.core5.http.nio.BasicResponseProducer;
import org.apache.hc.core5.http.nio.entity.FileEntityProducer;
import org.apache.hc.core5.http.nio.entity.NoopEntityConsumer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;

/**
 * Asynchronous embedded HTTP/1.1 file server.
 * 
 * @see https://hc.apache.org/httpcomponents-core-5.0.x/httpcore5/examples/AsyncFileServerExample.java
 */
public class AsyncFileServerRequestHandler implements AsyncServerRequestHandler<Message<HttpRequest, Void>> {

	File docsRoot;
	
	public AsyncFileServerRequestHandler() {}
	
	public AsyncFileServerRequestHandler(File docsRoot) {
		this.docsRoot = docsRoot;
	}
	
    @Override
    public AsyncRequestConsumer<Message<HttpRequest, Void>> prepare(
            final HttpRequest request,
            final EntityDetails entityDetails,
            final HttpContext context) throws HttpException {
        return new BasicRequestConsumer<>(entityDetails != null ? new NoopEntityConsumer() : null);
    }

    @Override
    public void handle(
            final Message<HttpRequest, Void> message,
            final ResponseTrigger responseTrigger,
            final HttpContext context) throws HttpException, IOException {
        final HttpRequest request = message.getHead();
        final URI requestUri;
        try {
            requestUri = request.getUri();
        } catch (final URISyntaxException ex) {
            throw new ProtocolException(ex.getMessage(), ex);
        }
        final String path = requestUri.getPath();
        final File file = new File(docsRoot, path);
        if (!file.exists()) {

            System.out.println("File " + file.getPath() + " not found");
            responseTrigger.submitResponse(new BasicResponseProducer(
                    HttpStatus.SC_NOT_FOUND,
                    "<html><body><h1>File" + file.getPath() +
                            " not found</h1></body></html>",
                    ContentType.TEXT_HTML), context);

        } else if (!file.canRead() || file.isDirectory()) {

            System.out.println("Cannot read file " + file.getPath());
            responseTrigger.submitResponse(new BasicResponseProducer(
                    HttpStatus.SC_FORBIDDEN,
                    "<html><body><h1>Access denied</h1></body></html>",
                    ContentType.TEXT_HTML), context);

        } else {

            final ContentType contentType;
            final String filename = file.getName().toLowerCase(Locale.ROOT);
            if (filename.endsWith(".txt")) {
                contentType = ContentType.TEXT_PLAIN;
            } else if (filename.endsWith(".html") || filename.endsWith(".htm")) {
                contentType = ContentType.TEXT_HTML;
            } else if (filename.endsWith(".xml")) {
                contentType = ContentType.TEXT_XML;
            } else {
                contentType = ContentType.DEFAULT_BINARY;
            }

            final HttpCoreContext coreContext = HttpCoreContext.adapt(context);
            final EndpointDetails endpoint = coreContext.getEndpointDetails();
            System.out.println(endpoint + ": serving file " + file.getPath());
            responseTrigger.submitResponse(new BasicResponseProducer(
                    HttpStatus.SC_OK, new FileEntityProducer(file, contentType)), context);
        }
    }
}
