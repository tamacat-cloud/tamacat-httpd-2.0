/*
 * Copyright 2019 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.jetty;

public class HttpsdWithJetty_test {

	public static void main(String[] args) {
		HttpdWithJetty.startup("service-https.json");
	}

}
