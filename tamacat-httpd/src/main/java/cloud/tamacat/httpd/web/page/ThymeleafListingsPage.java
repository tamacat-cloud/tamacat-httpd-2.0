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
package cloud.tamacat.httpd.web.page;

import java.io.File;
import java.io.FileFilter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.hc.core5.http.HttpRequest;
import org.thymeleaf.context.Context;

import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;
import cloud.tamacat.util.DateUtils;
import cloud.tamacat.util.StringUtils;

/**
 * <p>It is the directory listings page that used Velocity template.
 */
public class ThymeleafListingsPage extends ThymeleafPage {

	static final Log LOG = LogFactory.getLog(ThymeleafListingsPage.class);

	protected static final String DEFAULT_CONTENT_TYPE = "text/html; charset=UTF-8";

	protected static final String DEFAULT_ERROR_500_HTML
		= "<html><body><p>500 Internal Server Error.<br /></p></body></html>";
	protected String listingsPage = "listings";

	protected String encoding;
	protected boolean useSearch = true;
	protected String dateFormat = "yyyy-MM-dd HH:mm";

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public void setLocale(String str) {
		if (StringUtils.isNotEmpty(str)) {
			this.locale = StringUtils.getLocale(str);
		}
	}

	protected Locale locale = Locale.getDefault();

	/**
	 * @since 1.1
	 * @param encoding
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setListingsPage(String listingsPage) {
		this.listingsPage = listingsPage;
	}

	public ThymeleafListingsPage(Properties props) {
	    super(props, null);
		if ("true".equalsIgnoreCase(props.getProperty("list.resource.search","false"))) {
			useSearch = true;
		}
	}

	public String getListingsPage(HttpRequest request, File file) {
		Context context = new Context();
		return getListingsPage(request, context, file);
	}

	public String getListingsPage(HttpRequest request, Context context, File file) {
		try {
			context.setVariable("path", URLDecoder.decode(request.getPath(), "UTF-8"));
		} catch (Exception e) {
		    context.setVariable("path", request.getPath());
		}
		if (request.getPath().lastIndexOf('/') >= 0) {
		    context.setVariable("parent", "../");
		}
		final String q = useSearch? getParameter(request, "q"): "";
		context.setVariable("q", q);
		File[] files = file.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (StringUtils.isNotEmpty(q)) {
					return pathname.getName().indexOf(q)>=0;
				} else {
					return ! pathname.isHidden()
						&& ! pathname.getName().startsWith(".");
				}
			}
		});
		List<Map<String, String>> list = new ArrayList<>();
		if (files != null) {
			Arrays.sort(files, new FileSort());
			for (File f : files) {
				Map<String, String> map = new HashMap<>();
				String name = StringUtils.isNotEmpty(encoding)? StringUtils.encode(f.getName(),"UTF-8") : f.getName();
				if (f.isDirectory()) {
					map.put("getName", name + "/");
					map.put("length", "-");
				} else {
					map.put("getName", name);
					map.put("length", String.format("%1$,3d KB", (long)Math.ceil(f.length()/1024d)).trim());
				}
				map.put("isDirectory", String.valueOf(f.isDirectory()));
				map.put("lastModified", DateUtils.getTime(new Date(f.lastModified()), dateFormat, locale));
				list.add(map);
			}
		}
		
		context.setVariable("list", list);
		
		try {
		    return getTemplatePage(request, context, listingsPage);
		} catch (Exception e) {
			LOG.trace(e.getMessage());
			return DEFAULT_ERROR_500_HTML;
		}
	}

	static class FileSort implements Comparator<File> {
		public int compare(File src, File target) {
			if (src.isDirectory() && target.isFile()) return -1;
			if (src.isFile() && target.isDirectory()) return 1;
			int diff = src.getName().compareTo(target.getName());
			return diff;
		}
	}

	String getParameter(HttpRequest request, String name) {
		String path = request.getPath();
		if (path.indexOf('?') >= 0) {
			String[] requestParams = path.split("\\?");
			if (requestParams.length >= 2) {
				String params = requestParams[1];
				String[] param = params.split("&");
				for (String kv : param) {
					String[] p = kv.split("=");
					if (p.length >=2 && p[0].equals(name)) {
						try {
							return URLDecoder.decode(p[1], "UTF-8");
						} catch (Exception e) {
							LOG.warn(e.getMessage());
						}
					}
				}
			}
		}
		return null;
	}
}
