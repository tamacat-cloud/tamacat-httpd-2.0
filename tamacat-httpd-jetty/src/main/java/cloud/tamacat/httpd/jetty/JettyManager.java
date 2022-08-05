/*
 * Copyright 2021 tamacat.org
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
