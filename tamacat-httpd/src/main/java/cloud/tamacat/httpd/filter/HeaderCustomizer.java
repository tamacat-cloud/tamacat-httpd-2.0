/*
 * Copyright 2022 tamacat.org
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
package cloud.tamacat.httpd.filter;

import java.util.Map;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.message.BasicHeader;

import cloud.tamacat.httpd.util.HeaderUtils;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;
import cloud.tamacat.util.CollectionUtils;
import cloud.tamacat.util.StringUtils;

public class HeaderCustomizer {
	
	static final Log LOG = LogFactory.getLog(HeaderCustomizer.class);

	protected Map<String, String> addHeaders = CollectionUtils.newLinkedHashMap();
	protected Map<String, String> removeHeaders = CollectionUtils.newLinkedHashMap();

	/**
	 * Append Headers.
	 * @param headerValue
	 */
	public void setAddHeader(String headerValue) {
		if (headerValue != null && headerValue.indexOf(":") >= 0) {
			String[] nameValue = StringUtils.split(HeaderUtils.deleteCRLF(headerValue), ":");
			if (nameValue.length >= 2) {
				String name = nameValue[0].trim();
				String value = headerValue.replace(nameValue[0] + ":", "").trim();
				if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(value)) {
					addHeaders.put(name, value);
				}
			}
		}
	}
	
	/**
	 * Set Remove Headers.
	 * @param headerValue
	 */
	public void setRemoveHeader(String header) {
		if (header == null) {
			return;
		}
		if (header.indexOf(":") >= 0) {
			String[] nameValue = StringUtils.split(HeaderUtils.deleteCRLF(header), ":");
			if (nameValue.length >= 2) {
				String name = nameValue[0].trim();
				String value = header.replace(nameValue[0] + ":", "").trim();
				if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(value)) {
					removeHeaders.put(name, value);
				}
			}
		} else {
			removeHeaders.put(header, "");
		}
	}
	
	public void removeHeaders(HttpMessage target) {
		for (String name : removeHeaders.keySet()) {
			if (target.containsHeader(name)) {
				String value = removeHeaders.get(name);
				Header h = new BasicHeader(name, value);
				String targetValue = HeaderUtils.getHeader(target, name);
				if (StringUtils.isEmpty(value)) {
					target.removeHeader(h);
				} else if (value.equals(targetValue)) {
					target.removeHeader(h);
				}
				LOG.trace("[remove header] " + name + ": " + targetValue);
			}
		}
	}
	
	public void addHeaders(HttpMessage target) {
		//Append Headers (DO NOT Override exists headers)
		for (String name : addHeaders.keySet()) {
			if (target.containsHeader(name) == false) {
				String value = addHeaders.get(name);
				target.setHeader(name, value);
				LOG.trace("[set header] " + name + ": " + value);
			}
		}
	}

	@Override
	public String toString() {
		return "HeaderCustomizer [addHeaders=" + addHeaders + ", removeHeaders=" + removeHeaders + "]";
	}
}
