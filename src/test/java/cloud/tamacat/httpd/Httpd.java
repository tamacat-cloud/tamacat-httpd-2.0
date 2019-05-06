/*
 * Copyright (c) 2019 tamacat.org
 */
package cloud.tamacat.httpd;

public class Httpd {

	public static void main(String[] args) throws Exception {
		AsyncHttpd server = new AsyncHttpd();
		server.startup();
	}

}
