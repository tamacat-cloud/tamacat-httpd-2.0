/*
 * Copyright 2019 tamacat.org
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
package cloud.tamacat.httpd.reverse.listener;

import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.pool.ConnPoolListener;
import org.apache.hc.core5.pool.ConnPoolStats;
import org.apache.hc.core5.pool.PoolStats;

import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;

public class TraceConnPoolListener implements ConnPoolListener<HttpHost> {
	
	static final Log LOG = LogFactory.getLog(TraceConnPoolListener.class);
	
	@Override
	public void onLease(HttpHost route, ConnPoolStats<HttpHost> connPoolStats) {
		if (LOG.isTraceEnabled()) {
			StringBuilder buf = new StringBuilder();
			buf.append("[proxy->origin] connection leased ").append(route);
			LOG.trace(buf.toString());
		}
	}

	@Override
	public void onRelease(HttpHost route, ConnPoolStats<HttpHost> connPoolStats) {
		if (LOG.isTraceEnabled()) {
			StringBuilder buf = new StringBuilder();
			buf.append("[httpd->origin] connection released ").append(route);
			PoolStats totals = connPoolStats.getTotalStats();
			buf.append("; total kept alive: ").append(totals.getAvailable()).append("; ");
			buf.append("total allocated: ").append(totals.getLeased() + totals.getAvailable());
			buf.append(" of ").append(totals.getMax());
			LOG.trace(buf.toString());
		}
	}
}
