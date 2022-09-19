/*
 * Copyright 2022 tamacat.org
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
package cloud.tamacat.httpd.test;

import java.io.IOException;

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncFilterChain;
import org.apache.hc.core5.http.nio.AsyncPushProducer;
import org.apache.hc.core5.http.protocol.HttpContext;

import cloud.tamacat.httpd.AsyncHttpd;
import cloud.tamacat.httpd.config.ReverseConfig;
import cloud.tamacat.httpd.config.ServerConfig;
import cloud.tamacat.httpd.config.ServiceConfig;
import cloud.tamacat.httpd.filter.async.AsyncHttpFilter;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

public class AsyncHttpd_test {

	static final Log LOG = LogFactory.getLog(AsyncHttpd_test.class);
	
	public static void main(String[] args) {
		AsyncHttpd.startup(ServerConfig.create().port(80)
			.service(ServiceConfig.create().path("/")
				.reverse(ReverseConfig.create().url("http://localhost:10081/")).filter(new AsyncHttpFilter() {
					
					@Override
					public AsyncDataConsumer handle(HttpRequest request, EntityDetails entityDetails, HttpContext context,
							AsyncFilterChain.ResponseTrigger responseTrigger, AsyncFilterChain chain)
							throws HttpException, IOException {
						LOG.debug("#handle: "+request);
						
						return chain.proceed(request, entityDetails, context, new AsyncFilterChain.ResponseTrigger() {

							@Override
							public void sendInformation(HttpResponse response) throws HttpException, IOException {
								LOG.debug("#sendInformation");
								responseTrigger.sendInformation(response);
							}
							
							@Override
							public void submitResponse(HttpResponse response, AsyncEntityProducer entityProducer)
									throws HttpException, IOException {
								LOG.debug("#submitResponse");
								
				                responseTrigger.submitResponse(response, entityProducer);
							}

							@Override
							public void pushPromise(HttpRequest promise, AsyncPushProducer responseProducer) throws HttpException, IOException {
								LOG.debug("#pushPromise");
				                responseTrigger.pushPromise(promise, responseProducer);
							}
						});					}
				})
			)
		);
	}

}
