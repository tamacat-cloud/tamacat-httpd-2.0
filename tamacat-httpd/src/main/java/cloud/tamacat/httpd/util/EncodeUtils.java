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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Properties;

import cloud.tamacat.util.PropertyUtils;

import java.util.Map.Entry;

public abstract class EncodeUtils {

	private static HashMap<String,String> charsetMap = new HashMap<>();
	private static final String ENCODE_MAPPING_FILE = "encode-mapping.properties";
	
	static {
		try {
			Properties props = PropertyUtils.getProperties(
				"cloud/tamacat/httpd/util/" + ENCODE_MAPPING_FILE);
			addCharsetMap(props);
		} catch (Exception e) { //skip.
		}
		try {
			Properties props = PropertyUtils.getProperties(ENCODE_MAPPING_FILE);
			addCharsetMap(props);
		} catch (Exception e) { //skip.
		}
	}
	
	private static void addCharsetMap(Properties props) {
		for (Entry<Object, Object> entry : props.entrySet()) {
			String key = (String)entry.getKey();
			String value = (String)entry.getValue();
			if (key != null && value != null) {
				charsetMap.put(key.toLowerCase().trim(), value.trim());
			}
		}
	}
	
	public static String getJavaEncoding(String charset) {
		return getJavaEncoding(charset, null);
	}
	
	public static String getJavaEncoding(String charset, String defaultCharset) {
		if (charset == null) return defaultCharset;
		String encoding = charsetMap.get(charset.toLowerCase());
		return encoding != null ? encoding : defaultCharset;
	}
	
	public static String urlencode(String x) {
		if (x != null) {
			try {
				return URLEncoder.encode(x.toString(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			return "";
		}
	}
	
	public static String urldecode(String x) {
		if (x != null) {
			try {
				return URLDecoder.decode(x.toString(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			return "";
		}
	}
}
