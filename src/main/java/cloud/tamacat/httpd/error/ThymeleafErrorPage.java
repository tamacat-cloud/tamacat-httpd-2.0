/*
 * Copyright (c) 2019, tamacat.org
 * All rights reserved.
 */
package cloud.tamacat.httpd.error;

import java.util.Locale;
import java.util.Properties;

import org.apache.hc.core5.http.HttpRequest;
import org.thymeleaf.context.Context;

import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

/**
 * <p>It is the HTTP error page that used Velocity template.
 */
public class ThymeleafErrorPage extends ThymeleafPage {

	static final Log LOG = LogFactory.getLog(ThymeleafErrorPage.class);

	static final String DEFAULT_CONTENT_TYPE = "text/html; charset=UTF-8";

	static final String DEFAULT_ERROR_HTML
		= "<html><body><p>Error.</p></body></html>";

	public ThymeleafErrorPage() {
    }
	
	public ThymeleafErrorPage(Properties props) {
	    init(props, null);
	}

	public String getErrorPage(HttpRequest request, HttpException exception) {
		return getErrorPage(request, new Context(), exception);
	}

	public String getErrorPage(HttpRequest request, Context context, HttpException exception) {
		//response.setCode(exception.getHttpStatus().getStatusCode());
		//response.setReasonPhrase(exception.getHttpStatus().getReasonPhrase());

		if (LOG.isTraceEnabled() && exception.getHttpStatus().isServerError()) {
			LOG.trace(exception); //exception.printStackTrace();
		}
		
        context.setVariable("url", request.getRequestUri());
        context.setVariable("method", request.getMethod().toUpperCase(Locale.ENGLISH));
        context.setVariable("exception", exception);
        
		try {
		    return getTemplatePage(request, context, "/error"+exception.getHttpStatus().getStatusCode());
		} catch (Exception e) {
		    return getDefaultErrorPage(request, context, exception);
		}
	}

	protected String getDefaultErrorPage(HttpRequest request, Context context, HttpException exception) {
	    try {
	        return getTemplatePage(request, context, "/error");
	    } catch (Exception e) {
	        return getDefaultErrorHtml(exception);
	    }
	}
	
	protected String getDefaultErrorHtml(HttpException exception) {
		String errorMessage = exception.getHttpStatus().getStatusCode()
				+ " " + exception.getHttpStatus().getReasonPhrase();
		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html>");
		html.append("<html><head><meta charset=\"UTF-8\" />");
		html.append("<title>" + errorMessage + "</title>");
		html.append("</head><body>");
		html.append("<h1>" + errorMessage + "</h1>");
		html.append("</body></html>");
		return html.toString();
	}
}
