/*
 * Copyright 2020 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.filter;

import java.io.IOException;

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncFilterChain;
import org.apache.hc.core5.http.nio.AsyncFilterChain.ResponseTrigger;
import org.apache.hc.core5.http.nio.AsyncPushProducer;
import org.apache.hc.core5.http.protocol.HttpContext;

import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

/**
 * Response Filter
 */
public class HtmlConvertFilter extends Filter {

	static final Log LOG = LogFactory.getLog(HtmlConvertFilter.class);
	
	@Override
	public AsyncDataConsumer handle(HttpRequest request, EntityDetails entityDetails, HttpContext context,
			ResponseTrigger responseTrigger, AsyncFilterChain chain) throws HttpException, IOException {
		
		LOG.trace("execute="+request.getPath().startsWith(serviceConfig.getPath()) );
		if (request.getPath().startsWith(serviceConfig.getPath()) == false) {
			return chain.proceed(request, entityDetails, context, responseTrigger);
		}
		
        return chain.proceed(request, entityDetails, context, new AsyncFilterChain.ResponseTrigger() {

			@Override
			public void sendInformation(HttpResponse response) throws HttpException, IOException {
				LOG.trace("#sendInformation");
				responseTrigger.sendInformation(response);
			}
			
			@Override
			public void submitResponse(HttpResponse response, AsyncEntityProducer entityProducer)
					throws HttpException, IOException {
				LOG.trace("#submitResponse");
				
                responseTrigger.submitResponse(response, entityProducer);
			}

			@Override
			public void pushPromise(HttpRequest promise, AsyncPushProducer responseProducer) throws HttpException, IOException {
				LOG.trace("#pushPromise");
                responseTrigger.pushPromise(promise, responseProducer);
			}
		});
	}
}
