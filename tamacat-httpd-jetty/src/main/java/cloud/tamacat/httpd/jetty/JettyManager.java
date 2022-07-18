/*
 * Copyright 2021 tamacat.org
 * All rights reserved.
 */
package cloud.tamacat.httpd.jetty;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;

import cloud.tamacat.httpd.Middleware;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;
import cloud.tamacat.util.StringUtils;

public class JettyManager implements Middleware {

	static final Log LOG = LogFactory.getLog(JettyManager.class);
	
	static final Map<Integer, Server> MANAGER = new HashMap<>();
	
	static final JettyManager SELF = new JettyManager();
	
	public static JettyManager getInstance() {
		return SELF;
	}
		
	/**
	 * The instance corresponding to a port is returned. 
	 * @param port
	 * @return Server instance
	 */
	public synchronized Server getServer(int port) {
		return getServer(null, port);
	}
	
	/**
	 * The instance corresponding to a port is returned. 
	 * @param port
	 * @return Server instance
	 */
	public synchronized Server getServer(String host, int port) {
		Server instance = MANAGER.get(port);
		if (instance == null) {
			if (StringUtils.isNotEmpty(host)) {
				instance = new Server(InetSocketAddress.createUnresolved(host, port));
			} else {
				instance = new Server(port);
			}
			MANAGER.put(port, instance);
		}
		return instance;
	}
	
	
	/**
	 * Start the all Server instances.
	 */
	public void start() {
		for (Server server : MANAGER.values()) {
			JettyThread jetty = new JettyThread(server);
			jetty.start();
		}
	}
	
	/**
	 * Stop the all Server instances.
	 */
	public void stop() {
		for (Server instance : MANAGER.values()) {
			try {
				instance.stop();
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}
	
	private JettyManager() {}

	/**
	 * Thread of Jetty inscanse.
	 */
    static class JettyThread extends Thread {
    	final Server server;
    	JettyThread(Server server) {
    		this.server = server;
    	}
    	
    	public void run() {
    		try {
    			server.start();
    			server.join();
    		} catch (Exception e) {
				LOG.error(e.getMessage(), e);
    		}
    	}
    }
}
