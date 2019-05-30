/*
 * Copyright (c) 2009 tamacat.org
 * All rights reserved.
 */
package cloud.tamacat.httpd.error;

import java.io.StringWriter;
import java.util.Locale;
import java.util.Properties;

import org.apache.hc.core5.http.HttpRequest;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import cloud.tamacat.httpd.error.HttpException;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

/**
 * <p>It is the HTTP error page that used Velocity template.
 */
public class VelocityErrorPage {

	static final Log LOG = LogFactory.getLog(VelocityErrorPage.class);

	static final String DEFAULT_CONTENT_TYPE = "text/html; charset=UTF-8";

	static final String DEFAULT_ERROR_HTML
		= "<html><body><p>Error.</p></body></html>";

	protected String charset = "UTF-8";
	protected VelocityEngine velocityEngine;
	protected Properties props;
	protected String templatesPath = "pages";

	public VelocityErrorPage(Properties props) {
		this.props = props;
		init();
	}

	protected void init() {
		try {
			velocityEngine = new VelocityEngine();
			//velocityEngine.setApplicationAttribute(VelocityEngine.RESOURCE_LOADER,
			//        new ClasspathResourceLoader());
			velocityEngine.setProperty("resource.loaders", "error");
			velocityEngine.setProperty("resource.loader.error.instance", new ClasspathResourceLoader());
			velocityEngine.init(props);
			String path = props.getProperty("templates.path");
			if (path != null) {
				templatesPath = path;
			}
		} catch (Exception e) {
			LOG.warn(e.getMessage());
		}
	}

	public String getErrorPage(HttpRequest request, HttpException exception) {
		VelocityContext context = new VelocityContext();
		return getErrorPage(request, context, exception);
	}

	public String getErrorPage(HttpRequest request, VelocityContext context, HttpException exception) {
		if (LOG.isTraceEnabled() && exception.getHttpStatus().isServerError()) {
			LOG.trace(exception); //exception.printStackTrace();
		}
		try {
			context.put("url", request.getRequestUri());
			context.put("method", request.getMethod().toUpperCase(Locale.ENGLISH));
			context.put("exception", exception);

			Template template = getTemplate(
				"error" + exception.getHttpStatus().getStatusCode() + ".vm");
			StringWriter writer = new StringWriter();
			template.merge(context, writer);
			return writer.toString();
		} catch (Exception e) {
			LOG.warn(e.getMessage());
			return getDefaultErrorHtml(exception);
		}
	}

	protected Template getTemplate(String page) throws Exception {
		try {
			return velocityEngine.getTemplate(templatesPath + "/" + page, charset);
		} catch (Exception e) {
			return velocityEngine.getTemplate("templates/error.vm", charset);
		}
	}

	protected String getDefaultErrorHtml(HttpException exception) {
		String errorMessage = exception.getHttpStatus().getStatusCode()
				+ " " + exception.getHttpStatus().getReasonPhrase();
		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">");
		html.append("<html><head>");
		html.append("<title>" + errorMessage + "</title>");
		html.append("</head><body>");
		html.append("<h1>" + errorMessage + "</h1>");
		html.append("</body></html>");
		return html.toString();
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}
}
