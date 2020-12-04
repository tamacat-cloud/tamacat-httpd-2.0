/*
 * Copyright (c) 2019 tamacat.org
 */
package cloud.tamacat.httpd;

public class Httpd_test {

	public static void main(String[] args) throws Exception {
		Httpd server = new Httpd();
		server.startup(args);
	}

}
