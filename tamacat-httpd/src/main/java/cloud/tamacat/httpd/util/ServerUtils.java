/*
 * Copyright 2015 tamacat.org
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

import java.io.File;

import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

/**
 * <p>
 * Replace the "${server.home}" variable to server home directory.
 * <pre>
 * -Dserver.home=/usr/local/tamacat-httpd
 * or
 * -Duser.dir=~/tamacat-httpd
 * </pre>
 */
public class ServerUtils {
	
	static final Log LOG = LogFactory.getLog(ServerUtils.class);
	
	protected static String serverHome;

	static {
		try {
			serverHome = System.getProperty("server.home");
			if (serverHome == null) {
				serverHome = System.getProperty("user.dir");
			}
			File home = new File(serverHome);
			serverHome = home.getCanonicalPath().replace("\\", "/");
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	/**
	 * Get ${server.home}
	 * @param docsRoot
	 * @since 1.4-20180904
	 */
	public static String getServerHome() {
		return serverHome;
	}

	public static String getServerDocsRoot(String docsRoot) {
		return docsRoot.replace("${server.home}", getServerHome()).replace("\\", "/");
	}
}
