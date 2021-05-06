/*
 * Copyright 2012 tamacat.org
 * All rights reserved.
 */
package cloud.tamacat.httpd.tomcat;

/**
 * <p>It is implements of the multi-thread server.
 * The embedded Tomcat is supported. 
 */
public class TomcatHttpEngine {

	public void startup() {
		TomcatManager.start();
	}
	
	public void shutdown() {
		TomcatManager.stop();
	}
}
