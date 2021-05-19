/*
 * Copyright 2021 tamacat.org
 * All rights reserved.
 */
package cloud.tamacat.httpd.jetty;

import java.io.File;
import java.io.FileFilter;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

import cloud.tamacat.httpd.config.ServiceConfig;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;
import cloud.tamacat.util.StringUtils;

/**
 * Deployment configuration for Jetty Embedded.
 */
public class JettyDeployment {

	static final Log LOG = LogFactory.getLog(JettyDeployment.class);

	protected String serverHome = ".";
	protected String hostname = "127.0.0.1"; //Bind Address
	protected int port = 8080;
	protected String webapps = "${server.home}/webapps";
	protected String contextPath;
	protected Server server;
	protected boolean useWarDeploy = true;
	protected ClassLoader loader;

	/**
	 * Deployment Web Applications for Jetty Embedded
	 * 
	 * @param serviceUrl
	 */
	public void deploy(ServiceConfig serviceConfig) {
		setWebapps(webapps);
		LOG.debug("port=" + port + ", config=" + serviceConfig);
		server = JettyManager.getInstance(hostname, port);

		try {
			String contextRoot = serviceConfig.getPath().replaceAll("/$", "");
			if (StringUtils.isNotEmpty(contextPath)) {
				contextRoot = contextPath;
			}
			// check already add webapp.

			File baseDir = new File(getWebapps() + contextRoot);
			WebAppContext context = new WebAppContext(baseDir.getAbsolutePath(), contextRoot);
			context.setClassLoader(getClassLoader());

			HttpConfiguration httpConfig = new HttpConfiguration();
			httpConfig.addCustomizer(new ForwardedRequestCustomizer());
			ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
			connector.setHost(hostname);
			connector.setPort(port);
	        server.setConnectors(new Connector[] { connector });

			server.setHandler(context);

			LOG.info("Jetty port=" + port + ", path=" + contextRoot + ", dir=" + baseDir.getAbsolutePath());
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
		}
	}
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setServerHome(String serverHome) {
		this.serverHome = serverHome;
	}

	protected String getServerHome() {
		if (StringUtils.isEmpty(serverHome)) {
			serverHome = ServerUtils.getServerHome();
		}
		return serverHome;
	}

	public void setWebapps(String webapps) {
		if (webapps.indexOf("${server.home}") >= 0) {
			this.webapps = webapps.replace("${server.home}", getServerHome()).replace("\\", "/");
		} else {
			this.webapps = webapps;
		}
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	protected String getWebapps() {
		return webapps;
	}

	/**
	 * Auto Deployment for war files. (default: true)
	 * 
	 * @param useWarDeploy
	 */
	public void setUseWarDeploy(String useWarDeploy) {
		this.useWarDeploy = Boolean.valueOf(useWarDeploy);
	}

	/**
	 * <p.Set the ClassLoader
	 * 
	 * @param loader
	 */
	public void setClassLoader(ClassLoader loader) {
		this.loader = loader;
	}

	/**
	 * <p>
	 * Get the ClassLoader, default is getClass().getClassLoader().
	 * 
	 * @return
	 */
	public ClassLoader getClassLoader() {
		return loader != null ? loader : getClass().getClassLoader();
	}

	/**
	 * FileFilter for .war file
	 */
	static class WarFileFilter implements FileFilter {

		@Override
		public boolean accept(File file) {
			return file.isFile() && file.getName().endsWith(".war");
		}
	}
}
