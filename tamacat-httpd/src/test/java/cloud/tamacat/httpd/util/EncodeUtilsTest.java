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
