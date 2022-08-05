/*
 * Copyright 2014 tamacat.org
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

import java.util.Properties;

import cloud.tamacat.util.PropertyUtils;
import cloud.tamacat.util.StringUtils;

/**
 * Properties file in CLASSPATH
 * - cloud/tamacat/httpd/mime-types.properties
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
