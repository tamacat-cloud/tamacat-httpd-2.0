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

import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.ExceptionListener;
import org.apache.hc.core5.http.HttpConnection;

import cloud.tamacat.log.Log;
import cloud.tamacat.log.LogFactory;
import cloud.tamacat.util.ExceptionUtils;

public class TraceExceptionListener implements ExceptionListener {
	static final Log LOG = LogFactory.getLog(TraceExceptionListener.class);
	
	@Override
    public void onError(final Exception ex) {
        if (ex instanceof SocketException) {
            LOG.trace("[client->proxy] " + Thread.currentThread() + " " + ex.getMessage());
        } else {
        	LOG.trace("[client->proxy] " + Thread.currentThread()  + " " + ex.getMessage());
        	LOG.trace(ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public void onError(final HttpConnection connection, final Exception ex) {
        if (ex instanceof SocketTimeoutException) {
        	LOG.trace("[client->proxy] " + Thread.currentThread() + " time out");
        } else if (ex instanceof SocketException || ex instanceof ConnectionClosedException) {
        	LOG.trace("[client->proxy] " + Thread.currentThread() + " " + ex.getMessage());
        } else {
        	LOG.trace("[client->proxy] " + Thread.currentThread() + " " + ex.getMessage());
            LOG.trace(ExceptionUtils.getStackTrace(ex));
        }
    }

}
