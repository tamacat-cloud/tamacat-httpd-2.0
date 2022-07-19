/*
 * Copyright (c) 2019 tamacat.org
 * All rights reserved.
 */
package cloud.tamacat.httpd.web;

import java.io.StringWriter;
import java.util.Locale;
import java.util.Properties;

import org.apache.hc.core5.http.HttpRequest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import cloud.tamacat.httpd.error.NotFoundException;
import cloud.tamacat.httpd.error.ServiceUnavailableException;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;
import cloud.tamacat.util.StringUtils;

/**
 * <p>It is the HTTP page that used Velocity template.
 */
public class ThymeleafPage {
	static final Log LOG = LogFactory.getLog(ThymeleafPage.class);
    
    static final String THYMELEAF_PREFIX = "spring.thymeleaf.prefix";
    static final String THYMELEAF_SUFFIX = "spring.thymeleaf.suffix";
    static final String THYMELEAF_ENCODING = "spring.thymeleaf.suffix";
    static final String THYMELEAF_CACHE = "spring.thymeleaf.cache";
    
    static final String DEFAULT_PREFIX = "classpath:/templates-thymeleaf/";
    static final String DEFAULT_SUFFIX = ".html";
    static final String DEFAULT_ENCODING = "UTF-8";
    static final boolean DEFAULT_CACHE = false;
    
	TemplateEngine templateEngine;
	Properties props;

	public ThymeleafPage() {
        init(new Properties(), null);
    }
	
	public ThymeleafPage(Properties props, String docsRoot) {
		init(props, docsRoot);
	}
	
	protected void init(Properties props, String docsRoot) {
	    this.props = props;
		try {
		    templateEngine = new TemplateEngine();
		    
		    if (docsRoot == null) {
		        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
		        resolver.setPrefix(getPrefix());
	            resolver.setSuffix(getSuffix());
	            resolver.setCacheable(getCacheable());
	            resolver.setCharacterEncoding(getCharacterEncoding());
	            templateEngine.setTemplateResolver(resolver);
		    } else {
		        FileTemplateResolver resolver = new FileTemplateResolver();
		        
		        resolver.setPrefix(docsRoot);
		        resolver.setSuffix(getSuffix());
		        resolver.setCacheable(getCacheable());
		        resolver.setCharacterEncoding(getCharacterEncoding());
		        templateEngine.setTemplateResolver(resolver);
		    }
		} catch (Exception e) {
			LOG.warn(e.getMessage());
		}
	}

	public Properties getProperties() {
		return props;
	}
	
	public String getPage(HttpRequest request, String page) {
	    Context context = new Context();
		return getPage(request, context, page);
	}

	public String getPage(HttpRequest request, Context context, String page) {
		return getTemplatePage(request, context, page);
	}

	public String getTemplatePage(HttpRequest request, Context context, String page) {
	    if (request != null) {
	        context.setVariable("url", request.getPath());
	        context.setVariable("method", request.getMethod().toUpperCase(Locale.ENGLISH));
	    }
		try {
			StringWriter writer = new StringWriter();
			templateEngine.process(page, context, writer);
			return writer.toString();
		} catch (TemplateInputException e) {
			//LOG.trace(e.getMessage());
			throw new NotFoundException("File:" + page);
		} catch (Exception e) {
			throw new ServiceUnavailableException(e);
		}
	}
	
	public String getPrefix() {
	    String value = props.getProperty(THYMELEAF_PREFIX, DEFAULT_PREFIX);
	    String[] values = StringUtils.split(value, ":");
	    if (values.length == 2) {
	        //String protocol = values[0];
	        String prefix = values[1];
	        return prefix;
	    } else {
	        return value;
	    }
	}
	
	public String getSuffix() {
	    return props.getProperty(THYMELEAF_SUFFIX, DEFAULT_SUFFIX);
	}
	   
    public String getCharacterEncoding() {
        return props.getProperty(THYMELEAF_ENCODING, DEFAULT_ENCODING);
    }

    public boolean getCacheable() {
        return Boolean.valueOf(props.getProperty(THYMELEAF_CACHE, String.valueOf(DEFAULT_CACHE)));
    }
}
