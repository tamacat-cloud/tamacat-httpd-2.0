/*
 * Copyright (c) 2019 tamacat.org
 */
package cloud.tamacat.httpd.listener;

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
