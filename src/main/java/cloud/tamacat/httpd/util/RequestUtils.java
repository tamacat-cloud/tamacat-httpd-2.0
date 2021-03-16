/*
 * Copyright 2009 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Set;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.io.HttpServerConnection;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;

import cloud.tamacat.httpd.config.ServiceConfig;
import cloud.tamacat.httpd.core.BasicHttpStatus;
import cloud.tamacat.httpd.core.RequestParameters;
import cloud.tamacat.httpd.error.HttpException;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;
import cloud.tamacat.util.StringUtils;


public class RequestUtils {
	
	static final Log LOG = LogFactory.getLog(RequestUtils.class);
	
	static final String HTTP_REQUEST_PARAMETERS = "http.request.parameters";
	
	public static final String X_FORWARDED_FOR = "X-Forwarded-For";
	public static final String REMOTE_ADDRESS = "remote_address";

	static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";

	public static String getRequestLine(HttpRequest request) {
		return request.getMethod() + " "
			+ request.getPath() + " "
			+ request.getVersion();
	}
	
	public static RequestLine getRequestLine(RequestLine requestline) {
		String uri = requestline.getUri();
		String path = getRequestPathWithQuery(uri);
		if (uri.equals(path)) {
			return requestline;
		} else {
			return new RequestLine(requestline.getMethod(),
				path.substring(path.indexOf("/"), path.length()),
				requestline.getProtocolVersion());
		}
	}

	/**
	 * Get request absolute URI to Path (With Query)
	 * @param uri
	 */
	public static String getRequestPathWithQuery(final String uri) {
		try {
			if (uri.indexOf("http")==0 && uri.indexOf("://")>0) {
				int idx = uri.indexOf("://");
				String path = uri.substring(idx+3, uri.length());
				if (path.indexOf("/")>0) {
					return path.substring(path.indexOf("/"), path.length());
				}
			}
		} catch (RuntimeException e) {
			LOG.warn(e.getMessage());
		}
		return uri;
	}

	public static String getPath(String uri) {
		int index = uri.indexOf('?');
		if (index != -1) {
			uri = uri.substring(0, index);
		} else {
			index = uri.indexOf('#');
			if (index != -1) {
				uri = uri.substring(0, index);
			}
		}
		return uri;
	}

	public static RequestParameters parseParameters(HttpRequest request, HttpEntity entity, HttpContext context, String encoding) {
		synchronized (context) {
			RequestParameters parameters = (RequestParameters) context.getAttribute(HTTP_REQUEST_PARAMETERS);
			if (parameters == null) {
				try {
					parameters = parseParameters(request, entity, encoding);
					context.setAttribute(HTTP_REQUEST_PARAMETERS, parameters);
				} catch (Exception e) {
					//BAD REQUEST.
					LOG.warn(e.getMessage());
				}
			}
			return parameters;
		}
	}
	
	public static RequestParameters parseParameters(HttpRequest request, HttpEntity entity, String encoding) {
		RequestParameters parameters = new RequestParameters();
		String path = request.getPath();
		if (path.indexOf('?') >= 0) {
			String[] requestParams = StringUtils.split(path, "?");
			//set request parameters for Custom HttpRequest.
			if (requestParams.length >= 2) {
				String params = requestParams[1];
				String[] param = StringUtils.split(params, "&");
				for (String kv : param) {
					String[] p = StringUtils.split(kv, "=");
					if (p.length >=2) {
						try {
							parameters.setParameter(p[0], URLDecoder.decode(p[1], encoding));
						} catch (Exception e) {
						}
					} else if (p.length == 1) {
						parameters.setParameter(p[0], "");
					}
				}
			}
		}
		if (entity != null && RequestUtils.isFormUrlEncoded(request)) {
			if (entity != null) {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()))) {
					String s;
					StringBuilder sb = new StringBuilder();
					while ((s = reader.readLine()) != null) {
						sb.append(s);
					}
					String requestBody = sb.toString();
					//for Reuse handler
					
					//getHttpEntityEnclosingRequest(request).setEntity(new StringEntity(requestBody, encoding));
					
					String[] params = StringUtils.split(requestBody, "&");
					for (String param : params) {
						String[] keyValue = StringUtils.split(param, "=");
						if (keyValue.length >= 2) {
							try {
								parameters.setParameter(keyValue[0],
									URLDecoder.decode(keyValue[1], encoding));
							} catch (Exception e) {
							}
						} else if (keyValue.length==1) {
							parameters.setParameter(keyValue[0], "");
						}
					}
				} catch (IOException e) {
					throw new HttpException(BasicHttpStatus.SC_BAD_REQUEST, e);
				}
			}
		}
		return parameters;
	}
	
	public static void setParameter(HttpContext context, String name, String... values) {
		RequestParameters parameters = getParameters(context);
		parameters.setParameter(name, values);
	}

	public static void setParameters(HttpContext context, RequestParameters parameters) {
		context.setAttribute(HTTP_REQUEST_PARAMETERS, parameters);
	}

	/**
	 * Get Request parameters
	 * @since 1.4
	 */
	public static RequestParameters getParameters(HttpRequest request, HttpEntity entity, HttpContext context, String encoding) {
		parseParameters(request, entity, context, encoding);
		return getParameters(context);
	}
	
	public static RequestParameters getParameters(HttpContext context) {
		return (RequestParameters) context.getAttribute(HTTP_REQUEST_PARAMETERS);
	}

	public static String getParameter(HttpContext context, String name) {
		RequestParameters params = getParameters(context);
		return params != null ? params.getParameter(name) : null;
	}

	public static String[] getParameters(HttpContext context, String name) {
		RequestParameters params = getParameters(context);
		return params != null ? params.getParameters(name) : null;
	}

	public static Set<String> getParameterNames(HttpContext context) {
		RequestParameters params = getParameters(context);
		return params != null ? params.getParameterNames() : null;
	}

	public static HttpConnection getHttpConnection(HttpContext context) {
		return (HttpConnection) context.getAttribute(HttpCoreContext.CONNECTION_ENDPOINT);
	}

	/**
	 * Set the remote IP address to {@code HttpContext}.
	 * @param context
	 * @param conn instance of HttpInetConnection
	 */
	public static void setRemoteAddress(HttpContext context, HttpServerConnection conn) {
		context.setAttribute(REMOTE_ADDRESS, conn.getRemoteAddress());
	}

	/**
	 * Get the remote IP address in {@code HttpContext} or X-Forwarded-For.
	 * @param request
	 * @param context
	 * @param useXFF Using X-Forwarded-For request header.
	 * @return
	 */
	public static String getRemoteIPAddress(HttpRequest request, HttpContext context, boolean useXFF) {
		return getRemoteIPAddress(request, context, useXFF, X_FORWARDED_FOR);
	}
	
	/**
	 * Get the remote IP address in {@code HttpContext} or X-Forwarded-For.
	 * @param request
	 * @param context
	 * @param useForwardHeader Using X-Forwarded-For request header.
	 * @param forwardHeader ("X-Forwarded-For")
	 * @return
	 */
	public static String getRemoteIPAddress(HttpRequest request, HttpContext context, boolean useForwardHeader, String forwardHeader) {
		String ip = null;
		if (useForwardHeader) {
			ip = HeaderUtils.getHeader(request, StringUtils.isNotEmpty(forwardHeader)? forwardHeader : X_FORWARDED_FOR);
			if (StringUtils.isNotEmpty(ip) && ip.indexOf(",")>=0) {
				String[] address = StringUtils.split(ip, ",");
				if (address.length > 0) {
					return address[0];
				}
			}
		}
		if (StringUtils.isEmpty(ip)) {
			ip = getRemoteIPAddress(context);
		}
		return ip != null ? ip : "";
	}
	
	/**
	 * Get the remote IP address in {@code HttpContext}.
	 * @param context
	 * @return
	 */
	public static String getRemoteIPAddress(HttpContext context) {
		InetAddress address = (InetAddress) context.getAttribute(REMOTE_ADDRESS);
		if (address != null) return address.getHostAddress();
		else return "";
	}

	public static boolean isRemoteIPv6Address(HttpContext context) {
		InetAddress address = (InetAddress) context.getAttribute(REMOTE_ADDRESS);
		if (address != null && address instanceof Inet6Address) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get hostname from Host request header.
	 * @param request
	 * @param context
	 */
	public static String getRequestHost(HttpRequest request, HttpContext context) {
		Header hostHeader = request.getFirstHeader(HttpHeaders.HOST);
		if (hostHeader != null) {
			String hostName = hostHeader.getValue();
			if (hostName != null && hostName.indexOf(':') >= 0) {
				String[] hostAndPort = StringUtils.split(hostName, ":");
				if (hostAndPort.length >= 2) {
					hostName = hostAndPort[0];
				}
			}
			return hostName;
		}
		return null;
	}

	public static String getRequestHostURL(
			HttpRequest request, HttpContext context, ServiceConfig url) {
		URL host = getRequestURL(request, context, url);
		return host != null ? host.getProtocol()
				+ "://" + host.getAuthority() : null;
	}

	public static URL getRequestURL(HttpRequest request, HttpContext context) {
		return getRequestURL(request, context, null);
	}

	public static URL getRequestURL(HttpRequest request, HttpContext context, ServiceConfig url) {
		String protocol = "http";
		String hostName = null;
		int port = -1;
		Header hostHeader = request.getFirstHeader(HttpHeaders.HOST);
		if (hostHeader != null) {
			hostName = hostHeader.getValue();
			if (hostName != null && hostName.indexOf(':') >= 0) {
				String[] hostAndPort = StringUtils.split(hostName, ":");
				if (hostAndPort.length >= 2) {
					hostName = hostAndPort[0];
					port = StringUtils.parse(hostAndPort[1],-1);
				}
			}
		}
		if (url != null) {
			URL configureHost = url.getHost();
			if (configureHost != null) {
				protocol = configureHost.getProtocol();
				if (hostName == null) {
					hostName = configureHost.getHost();
				}
			}
			if (url.getServerConfig().useHttps()) {
				protocol = "https";
			}
			if (hostName != null && hostName.indexOf(':') >= 0) {
				String[] hostAndPort = StringUtils.split(hostName, ":");
				if (hostAndPort.length >= 2) {
					hostName = hostAndPort[0];
					port = StringUtils.parse(hostAndPort[1],-1);
				}
			} else {
				port = url.getServerConfig().getPort();
			}
			if (context != null) {
				HttpConnection con = getHttpConnection(context);
				SocketAddress addr = con.getLocalAddress();
				if (addr != null && addr instanceof InetSocketAddress) {
					port = ((InetSocketAddress)addr).getPort();
				}
				if (hostName == null) {
					hostName = ((InetSocketAddress)addr).getHostName();
				}
			}
		}
		if (("http".equalsIgnoreCase(protocol) && port == 80)
			|| ("https".equalsIgnoreCase(protocol) && port == 443)){
			port = -1;
		}
		if (hostName != null) {
			try {
				return new URL(protocol, hostName, port, request.getPath());
			} catch (MalformedURLException e) {
			}
		}
		return null;
	}

	/**
	 * UnsupportedEncodingException -> value returns.
	 * @param value
	 * @param encoding
	 * @return
	 */
	static String decode(String value, String encoding) {
		String decode = null;
		try {
			decode = URLDecoder.decode(value, encoding);
		} catch (UnsupportedEncodingException e) {
			decode = value;
		}
		return decode;
	}

//	public static boolean isEntityEnclosingRequest(HttpRequest request) {
//		return request != null && request instanceof HttpEntityEnclosingRequest;
//	}
//
//	public static HttpEntity getEntity(HttpRequest request) {
//		
//		if (isEntityEnclosingRequest(request)) {
//			return ((getHttpEntityEnclosingRequest(request).getEntity();
//		} else {
//			return null;
//		}
//	}
//	
//	public static HttpEntityEnclosingRequest getHttpEntityEnclosingRequest(HttpRequest request) {
//		if (isEntityEnclosingRequest(request)) {
//			return ((HttpEntityEnclosingRequest)request);
//		}
//		return null;
//	}

	public static InputStream getInputStream(HttpEntity entity) throws IOException {
		//HttpEntity entity = getEntity(request);
		return entity != null? entity.getContent() : null;
	}

	public static boolean isFormUrlEncoded(HttpRequest request) {
		return HeaderUtils.isFormUrlEncoded(
				HeaderUtils.getHeader(request, HttpHeaders.CONTENT_TYPE));
	}
	
	public static boolean isMultipart(HttpRequest request) {
		if ("post".equalsIgnoreCase(request.getMethod())) {
			return HeaderUtils.isMultipart(
				HeaderUtils.getHeader(request, HttpHeaders.CONTENT_TYPE));
		}
		return false;
	}

	public static String getPathPrefix(HttpRequest request) {
		String path = request.getPath();
		int idx = path.lastIndexOf("/");
		if (idx >=0) {
			return path.substring(0, idx) + "/";
		}
		return path;
	}

	/**
	 *
	 * @param context
	 * @since 1.1
	 */
	public static HttpRequest getHttpRequest(HttpContext context) {
		return (HttpRequest) context.getAttribute(HttpCoreContext.HTTP_REQUEST);
	}
}
