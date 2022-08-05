/*
 * Copyright 2019 tamacat.org
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
package cloud.tamacat.httpd.util;

import java.net.InetSocketAddress;

import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;

import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

public class AccessLogUtils {
	
	static final Log ACCESS = LogFactory.getLog("Access");

	public static String getRemoteAddress(HttpContext httpContext) {
		EndpointDetails ip = (EndpointDetails) httpContext.getAttribute("http.connection-endpoint");
		if (ip != null) {
			return ((InetSocketAddress)ip.getRemoteAddress()).getAddress().getHostAddress();
		} else {
			return "";
		}
	}
	
	public static void log(HttpRequest req, HttpResponse resp, HttpContext context, long responseTime) {
		ACCESS.info(getRemoteAddress(context) +" "+ req+ " "+resp.getCode() + " "+ responseTime+"ms");
	}
	
	public static void log(Log log, HttpRequest req, HttpResponse resp, HttpContext context, long responseTime) {
		log.info(getRemoteAddress(context) +" "+ req+ " "+resp.getCode() + " "+ responseTime+"ms");
	}
}
