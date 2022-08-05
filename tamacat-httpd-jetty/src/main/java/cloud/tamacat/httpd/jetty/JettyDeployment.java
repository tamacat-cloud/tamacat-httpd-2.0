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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.jsp.JettyJspServlet;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import cloud.tamacat.httpd.config.ServiceConfig;
import cloud.tamacat.httpd.util.ServerUtils;
import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;
import cloud.tamacat.util.StringUtils;

/**
 * Deployment configuration for Jetty Embedded.
 */
public class JettyDeployment {

	static final Log LOG = LogFactory.getLog(JettyDeployment.class);

	protected String serverHome = ".";
	protected String hostname = "127.0.0.1"; // Bind Address
	protected int port = 8080;
	protected String webapps = "${server.home}/webapps";
	protected String work = "${server.home}/work";
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
		server = JettyManager.getInstance().getServer(hostname, port);

		try {
			String contextRoot = serviceConfig.getPath().replaceAll("/$", "");
			if (StringUtils.isNotEmpty(contextPath)) {
				contextRoot = contextPath;
			}
			// check already add webapp.

			File baseDir = new File(getWebapps() + contextRoot);
			ServletContextHandler context = new WebAppContext(baseDir.getAbsolutePath(), contextRoot);
			context.setClassLoader(getClassLoader());

			enableEmbeddedJspSupport(context);

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

	/**
	 * https://github.com/jetty-project/embedded-jetty-jsp
	 * 
	 * Setup JSP Support for ServletContextHandlers.
	 * <p>
	 * NOTE: This is not required or appropriate if using a WebAppContext.
	 * </p>
	 *
	 * @param servletContextHandler the ServletContextHandler to configure
	 * @throws IOException if unable to configure
	 */
	void enableEmbeddedJspSupport(ServletContextHandler servletContextHandler) throws IOException {
		// Establish Scratch directory for the servlet context (used by JSP compilation)
		servletContextHandler.setAttribute("javax.servlet.context.tempdir", getWork());

		// Set Classloader of Context to be sane (needed for JSTL)
		// JSP requires a non-System classloader, this simply wraps the embedded System classloader
		// in a way that makes it suitable for JSP to use
		ClassLoader jspClassLoader = new URLClassLoader(new URL[0], this.getClass().getClassLoader());
		servletContextHandler.setClassLoader(jspClassLoader);

		// Manually call JettyJasperInitializer on context startup
		servletContextHandler.addBean(new EmbeddedJspStarter(servletContextHandler));

		// Create / Register JSP Servlet (must be named "jsp" per spec)
		ServletHolder holderJsp = new ServletHolder("jsp", JettyJspServlet.class);
		holderJsp.setInitOrder(0);
		servletContextHandler.addServlet(holderJsp, "*.jsp");

		servletContextHandler.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
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

	public void setWork(String work) {
		this.work = work;
	}

	protected String getWork() {
		if (work.indexOf("${server.home}") >= 0) {
			this.work = work.replace("${server.home}", getServerHome()).replace("\\", "/");// .replaceAll("/work$", "");
		}
		return work;
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
