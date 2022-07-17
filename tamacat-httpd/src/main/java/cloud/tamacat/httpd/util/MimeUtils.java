/*
 * Copyright 2014 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.util;

import java.util.Properties;

import cloud.tamacat.util.PropertyUtils;
import cloud.tamacat.util.StringUtils;

/**
 * Properties file in CLASSPATH
 * - org/tamacat/httpd/mime-types.properties
 * - mime-types.properties
 * hash data (key:file extention, value:content-type)
 */
public class MimeUtils {
	private static Properties mimeTypes;

	static {
		mimeTypes = PropertyUtils.marge(
				"cloud/tamacat/httpd/util/mime-types.properties",
				"mime-types.properties");
	}

	/**
	 * Get a content-type from mime-types.properties.
	 * content-type was unknown then returns null.
	 * @param path
	 * @return
	 */
	public static String getContentType(String path) {
		if (StringUtils.isEmpty(path)) return null;
		if (path.indexOf('?')>=0) {
			String[] tmp = StringUtils.split(path, "?");
			if (tmp.length >= 1) {
				path = tmp[0];
			}
		}
		String ext = path.substring(path.lastIndexOf('.') + 1, path.length());
		String contentType = mimeTypes.getProperty(ext.toLowerCase());
		return contentType;
	}
}
