/*
 * Copyright 2009 tamacat.org
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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.protocol.HttpContext;

import cloud.tamacat.httpd.config.ReverseConfig;
import cloud.tamacat.httpd.config.ServerConfig;
import cloud.tamacat.httpd.config.ServiceConfig;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;
import cloud.tamacat.util.PropertyUtils;
import cloud.tamacat.util.ResourceNotFoundException;
import cloud.tamacat.util.StringUtils;

/**
 * <p>The utility class for reverse proxy.<br>
 *  When customize a request/response header to remove in reverse
 *  to the origin server, create the "reverse-header.properties" in CLASSPATH.
 * <pre>Configuration: reverse-header.properties
 * {@code
 * request.removeHeaders=Content-Length,Transfer-Encoding,Accept-Encoding,...
 * response.removeHeaders=Content-Type,Content-Encoding,Content-Length,...
 * }</pre>
 */
public class ReverseUtils {

	static final Log LOG = LogFactory.getLog(ReverseUtils.class);

	static Pattern PATTERN = Pattern.compile(
		"<[^<]*\\s+(href|src|action)=('|\")([^('|\")]*)('|\")[^>]*>",
		Pattern.CASE_INSENSITIVE
	);

	private static final String HEADER_PROPERTIES = "reverse-header.properties";
	private static final String DEFAULT_HEADER_PROPERTIES = "cloud/tamacat/httpd/util/reverse-header.properties";

	private static final Set<String> removeRequestHeaders = new HashSet<>();
	private static final Set<String> removeResponseHeaders = new HashSet<>();

	//Configuration of remove request/response headers.
	static {
		Properties props = null;
		try {
			props = PropertyUtils.getProperties(HEADER_PROPERTIES);
		} catch (ResourceNotFoundException e) {
			props = PropertyUtils.getProperties(DEFAULT_HEADER_PROPERTIES);
		}
		if (props != null) {
			String removeHeaders1 = props.getProperty("request.removeHeaders");
			String[] headers1 = removeHeaders1.split(",");
			for (String h : headers1) {
				removeRequestHeaders.add(h.trim());
			}
			String removeHeaders2 = props.getProperty("response.removeHeaders");
			String[]headers2 = removeHeaders2.split(",");
			for (String h : headers2) {
				removeResponseHeaders.add(h.trim());
			}
		}
	}
	
	public static String getReverseTargetPath(ReverseConfig reverseConfig, String incomingRequestPath) {
		return reverseConfig != null ? reverseConfig.getReverseUrl(incomingRequestPath).getFile() : incomingRequestPath;
	}

	/**
	 * <p>Remove hop-by-hop headers.
	 * @param request
	 */
	public static void removeRequestHeaders(HttpRequest request) {
		for (String h : removeRequestHeaders) {
			if (LOG.isTraceEnabled()) LOG.trace("remove:"+h);
			request.removeHeaders(h);
		}
	}

	/**
	 * <p>Copy the response headers.
	 * @param targetResponse
	 * @param response
	 */
	public static void copyHttpResponse(HttpResponse targetResponse, HttpResponse response) {
		// Remove hop-by-hop headers
		for (String h : removeResponseHeaders) {
			targetResponse.removeHeaders(h);
		}

		response.setCode(targetResponse.getCode());
		response.setReasonPhrase(targetResponse.getReasonPhrase());
		response.setVersion(targetResponse.getVersion());
		
		Header[] headers = response.getHeaders("Set-Cookie"); //backup Set-Cookie header.
		response.setHeaders(targetResponse.getHeaders()); //clean and reset all headers.
		for (Header h : headers) { //add Set-Cookie headers.
			response.addHeader(h);
		}
	}

	/**
	 * Rewrite a response HTTP version in status line from reuested version.
	 * @param request
	 * @param response
	 * @since 1.0.4
	 */
	public static void rewriteStatusLine(HttpRequest request, HttpResponse response) {
		response.setVersion(request.getVersion());
	}

	/**
	 * <p>Rewrite the Content-Location response headers.
	 * @param response
	 * @param reverseUrl
	 */
	public static void rewriteContentLocationHeader(
			HttpRequest request, HttpResponse response, ReverseConfig reverseUrl) {
		Header[] locationHeaders = response.getHeaders("Content-Location");
		response.removeHeaders("Content-Location");
		for (Header location : locationHeaders) {
			String value = deleteCRLF(location.getValue());
			String convertUrl = reverseUrl.getConvertRequestedUrl(value);
			if (convertUrl != null) {
				response.addHeader("Content-Location", convertUrl);
			}
		}
	}

	/**
	 * <p>Rewrite the Location response headers.
	 * @param response
	 * @param reverseUrl
	 */
	public static void rewriteLocationHeader(
			HttpRequest request, HttpResponse response, ReverseConfig reverseUrl) {
		Header[] locationHeaders = response.getHeaders("Location");
		response.removeHeaders("Location");
		for (Header location : locationHeaders) {
			String value = deleteCRLF(location.getValue());
			String convertUrl = reverseUrl.getConvertRequestedUrl(value);
			if (convertUrl != null) {
				response.addHeader("Location", convertUrl);
			}
		}
	}

	/**
	 * <p>Rewrite the Set-Cookie response headers.
	 * @param response
	 * @param reverseUrl
	 */
	public static void rewriteSetCookieHeader(
			HttpRequest request, HttpResponse response, ReverseConfig reverseUrl) {
		Header[] cookies = response.getHeaders("Set-Cookie");
		List<String> newValues = new ArrayList<>();
		for (Header h : cookies) {
			String value = h.getValue();
			String newValue = ReverseUtils.getConvertedSetCookieHeader(request, reverseUrl, value);
			if (StringUtils.isNotEmpty(newValue)) {
				newValues.add(newValue);
				response.removeHeader(h);
			}
		}
		for (String newValue : newValues) {
			response.addHeader("Set-Cookie", newValue);
			LOG.trace("[after] Set-Cookie: "+newValue);
		}
	}

	public static void rewriteServerHeader(HttpResponse response, ReverseConfig reverseUrl) {
		ServiceConfig serviceUrl = reverseUrl.getServiceConfig();
		if (serviceUrl != null) {
			response.setHeader(HttpHeaders.SERVER, serviceUrl.getServerConfig().getServerName());
		}
	}

	/**
	 * <p>Set the remote IP address to {@code X-Forwarded-For} request header
	 * for origin server.
	 * @param request
	 * @param context
	 * @param useForwardHeader
	 * @param forwardHeader "X-Forwarded-For"
	 * @since 1.3
	 */
	public static void setXForwardedFor(HttpRequest request, HttpContext context, boolean useForwardHeader, String forwardHeader) {
		request.setHeader(forwardHeader, RequestUtils.getRemoteIPAddress(request, context, useForwardHeader, forwardHeader));
	}
	
	/**
	 * <p>Set the forwarded Host request header for origin server.
	 * @param request
	 */
	public static void setXForwardedHost(HttpRequest request) {
		Header hostHeader = request.getFirstHeader(HttpHeaders.HOST);
		if (hostHeader != null) {
			request.setHeader("X-Forwarded-Host", hostHeader.getValue());
		}
	}
	
	/**
	 * <p>Set the forwarded proto request header for origin server.
	 * @param request
	 * @param config
	 * @since 1.4-20190416
	 */
	public static void setXForwardedProto(HttpRequest request, ServerConfig config) {
		String proto = HeaderUtils.getHeader(request, "X-Forwarded-Proto");
		if (StringUtils.isEmpty(proto)) {
			if (config.useHttps()) {
				request.setHeader("X-Forwarded-Proto", "https");
			} else {
				request.setHeader("X-Forwarded-Proto", "http");
			}
		}
	}

	/**
	 * <p>Set the forwarded port request header for origin server.
	 * @param request
	 * @param config
	 * @since 1.4-20190416
	 */
	public static void setXForwardedPort(HttpRequest request, ServerConfig config) {
		String port = HeaderUtils.getHeader(request, "X-Forwarded-Port");
		if (StringUtils.isEmpty(port)) {
			int serverPort = config.getPort();
			if (serverPort > 0) {
				request.setHeader("X-Forwarded-Port", String.valueOf(serverPort));
			} else {
				if (config.useHttps()) {
					request.setHeader("X-Forwarded-Port", "443");
				} else {
					request.setHeader("X-Forwarded-Port", "80");
				}
			}
		}
	}

	/**
	 * <p>Set the remote username to request header.
	 * @param request
	 * @param context
	 * @param headerName
	 */
	public static void setReverseProxyAuthorization(HttpRequest request, HttpContext context, String headerName) {
		if (StringUtils.isNotEmpty(headerName)) {
			Object user = context.getAttribute("REMOTE_USER"); //TODO
			if (user != null && user instanceof String) {
				request.setHeader(headerName, (String)user);
			} else {
				request.removeHeaders(headerName);
			}
		}
	}
	
	public static void appendHostHeader(HttpRequest request, ReverseConfig reverseUrl) {
		if (request.getVersion().lessEquals(HttpVersion.HTTP_1_0)
		 && StringUtils.isEmpty(HeaderUtils.getHeader(request, HttpHeaders.HOST))) {
			request.setHeader(HttpHeaders.HOST, reverseUrl.getTarget().getHostName());
			LOG.debug("Host(add): "+HeaderUtils.getHeader(request, HttpHeaders.HOST));
		}
	}
	
	//rewrite Host Header
	public static void rewriteHostHeader(HttpRequest request, HttpContext context, ReverseConfig reverseUrl) {
		Header[] hostHeaders = request.getHeaders(HttpHeaders.HOST);
		for (Header hostHeader : hostHeaders) {
			String value = hostHeader.getValue();
			URL host = RequestUtils.getRequestURL(request, context, reverseUrl.getServiceConfig());
			reverseUrl.setHost(host);
			String before = host.getAuthority();
			int beforePort = host.getPort();
			if (beforePort != 80 && beforePort > 0) {
				before = before + ":" + beforePort;
			}
			String after = reverseUrl.getReverse().getHost();
			int afterPort = reverseUrl.getReverse().getPort();
			if (afterPort != 80 && afterPort > 0) {
				after = after + ":" + afterPort;
			}
			String newValue = value.replace(before, after);

			LOG.trace("Host: " + value + " >> " + newValue);
			Header newHeader = new BasicHeader(hostHeader.getName(), newValue);
			request.removeHeader(hostHeader);
			request.addHeader(newHeader);
		}
	}

	/**
	 * <p>Convert backend hostname to original hostname.
	 * @param reverseUrl
	 * @param line cookie header line.
	 * @return converted Set-Cookie response header line.
	 */
	public static String getConvertedSetCookieHeader(
			HttpRequest request, ReverseConfig reverseUrl, String line) {
		if (line == null) return "";
		String dist = reverseUrl.getReverse().getHost();
		URL url = RequestUtils.getRequestURL(request, null);
		if (url == null) return "";
		String src = url.getHost();
		return getConvertedSetCookieHeader(
				reverseUrl.getReverse().getPath(),
				reverseUrl.getServiceConfig().getPath(),
				Pattern.compile("domain=" + dist, Pattern.CASE_INSENSITIVE)
					.matcher(line).replaceAll("domain=" + src)
		);
	}

	/**
	 * <p>Convert cookie path.
	 * <pre>
	 *   BEFORE: JSESSIONID=1234567890ABCDEFGHIJKLMNOPQRSTUV; Path=/dist
	 *   AFTER : JSESSIONID=1234567890ABCDEFGHIJKLMNOPQRSTUV; Path=/src
	 * </pre>
	 */
	static String getConvertedSetCookieHeader(String dist, String src, String line) {
		if (line != null) {
			String d = stripEnd(dist, "/");
			String s = stripEnd(src, "/");
			return Pattern.compile(";\\s*Path=" + d, Pattern.CASE_INSENSITIVE)
					.matcher(line).replaceAll("; Path=" + s);
		} else {
			return line;
		}
	}

	static String stripEnd(String str, String stripChars) {
		int end;
		if (str == null || (end = str.length()) == 0) {
			return str;
		}
		if (stripChars == null) {
			while ((end != 0) && Character.isWhitespace(str.charAt(end - 1))) {
				end--;
			}
		} else if (stripChars.length() == 0) {
			return str;
		} else {
			while ((end != 0)
					&& (stripChars.indexOf(str.charAt(end - 1)) != -1)) {
				end--;
			}
		}
		return str.substring(0, end);
	}
	/**
	 * delete CRLF
	 * @param str
	 * @since 1.1
	 */
	static String deleteCRLF(String str) {
		if (str != null && str.length() > 0 ) {
			return str.replace("\r", "").replace("\n","");
		} else {
			return str;
		}
	}
	
//	public static Socket createSSLSocket(ReverseUrl reverseUrl, String protocol, HttpProxyConfig proxyConfig) {
//		if (proxyConfig == null || proxyConfig.isDirect()) {
//			return createSSLSocket(reverseUrl, protocol);
//		}
//		try {
//			InetSocketAddress address = reverseUrl.getTargetAddress();
//			SSLContext ssl = SSLContext.getInstance(protocol);
//			ssl.init(null, new TrustManager[]{createGenerousTrustManager()}, null);
//			SSLSocketFactory factory = ssl.getSocketFactory();
//			Socket socket = proxyConfig.tunnel(reverseUrl.getTargetHost());
//			return factory.createSocket(socket, address.getHostName(), address.getPort(), true);
//		} catch (Exception e) {
//			LOG.warn(e.getMessage());
//			return null;
//		}
//	}
//	
//	/**
//	 * Create SSL Socket for connect to backend server.
//	 * @param reverseUrl
//	 * @param protocol "TLS" or "SSL"
//	 */
//	public static Socket createSSLSocket(ReverseUrl reverseUrl, String protocol) {
//		try {
//			InetSocketAddress address = reverseUrl.getTargetAddress();
//			return createSSLSocketFactory(protocol).createLayeredSocket(
//				new Socket(address.getHostName(), address.getPort()), 
//				address.getHostName(), address.getPort(),
//				new BasicHttpContext()
//			);
//		} catch (Exception e) {
//			LOG.warn(e.getMessage());
//			return null;
//		}
//	}
//	
//	public static SSLConnectionSocketFactory createSSLSocketFactory(String protocol) {
//		SSLContext sslContext;
//		try {
//			sslContext = SSLContext.getInstance(protocol);
//			sslContext.init(null, new TrustManager[] { createGenerousTrustManager() }, new SecureRandom());
//		} catch (Exception e) {
//			return null;
//		}
//		return new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
//	}
//
//	public static X509TrustManager createGenerousTrustManager() {
//		return new X509TrustManager() {
//			@Override
//			public void checkClientTrusted(X509Certificate[] cert, String s) throws CertificateException {
//			}
//
//			@Override
//			public void checkServerTrusted(X509Certificate[] cert, String s) throws CertificateException {
//			}
//
//			@Override
//			public X509Certificate[] getAcceptedIssuers() {
//				return null;
//			}
//		};
//	}
}
