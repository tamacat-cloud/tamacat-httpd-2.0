/*
 * Copyright 2015 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ServerUtilsTest {

	@Test
	public void testGetServerDocsRoot() {
		String serverHome = System.getProperty("server.home");
		String userDir = System.getProperty("user.dir");
		String home = serverHome != null ? serverHome : userDir;
		assertEquals((home + "/htdocs/root").replace("\\", "/"), ServerUtils.getServerDocsRoot("${server.home}/htdocs/root"));
	}
}
