/*
 * Copyright 2020 tamacat.org
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
open module cloud.tamacat.httpd.jetty {
	
	exports cloud.tamacat.httpd.jetty;
	
	requires transitive org.eclipse.jetty.server;
	requires transitive org.eclipse.jetty.servlet;
	requires transitive org.eclipse.jetty.apache.jsp;

	requires transitive cloud.tamacat.httpd;
	requires transitive org.eclipse.jetty.webapp;
	requires transitive org.mortbay.apache.jasper;
}