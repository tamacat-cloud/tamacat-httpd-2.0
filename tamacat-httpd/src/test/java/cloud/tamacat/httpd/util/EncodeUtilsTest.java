/*
 * Copyright 2009 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class EncodeUtilsTest {

	@Test
	public void testGetJavaEncoding() {
		assertEquals("UTF-8", EncodeUtils.getJavaEncoding("utf-8"));
		assertEquals("MS932", EncodeUtils.getJavaEncoding("Shift_JIS"));
		assertEquals("EUC_JP", EncodeUtils.getJavaEncoding("euc-jp"));
		assertEquals("ISO2022JP", EncodeUtils.getJavaEncoding("iso-2022-jp"));
	}

}
