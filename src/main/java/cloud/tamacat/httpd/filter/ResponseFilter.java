/*
 * Copyright 2020 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.filter;

import java.io.IOException;
import java.util.Map;

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
import cloud.tamacat.util.CollectionUtils;
import cloud.tamacat.util.StringUtils;

/**
 * Response Filter
 */
public class ResponseFilter extends Filter {

	static final Log LOG = LogFactory.getLog(ResponseFilter.class);
	
	protected Map<String, String> appendResponseHeaders = CollectionUtils.newLinkedHashMap();

	/**
	 * Append Response Headers.
	 * ex) "Strict-Transport-Security: max-age=63072000; includeSubDomains; preload"
	 * @param headerValue
	 */
	public void setAppendResponseHeader(String headerValue) {
		String[] nameValue = StringUtils.split(headerValue, ":");
		if (nameValue.length >= 2) {
			String name = nameValue[0].trim();
			String value = headerValue.replace(nameValue[0]+":", "").trim();
			if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(value)) {
				appendResponseHeaders.put(name, value);
			}
		}
	}
	
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
				responseTrigger.sendInformation(response);
			}
			
			@Override
			public void submitResponse(HttpResponse response, AsyncEntityProducer entityProducer)
					throws HttpException, IOException {
				//Append Response Headers (DO NOT Override exists headers)
				if (appendResponseHeaders.size() >= 1) {
					for (String name : appendResponseHeaders.keySet()) {
						if (response.containsHeader(name) == false) {
							String value = appendResponseHeaders.get(name);
							response.setHeader(name, value);
							LOG.trace("[set header] "+name+": "+value);
						}
					}
				}
                responseTrigger.submitResponse(response, entityProducer);
			}

			@Override
			public void pushPromise(HttpRequest promise, AsyncPushProducer responseProducer) throws HttpException, IOException {
                responseTrigger.pushPromise(promise, responseProducer);
			}
		});
	}

}
