/*
 * Copyright 2019 tamacat.org
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package cloud.tamacat.httpd.reverse.async;

import java.io.IOException;

import org.apache.hc.core5.http.impl.nio.BufferedData;
import org.apache.hc.core5.http.nio.DataStreamChannel;

/**
 * @see
 * https://hc.apache.org/httpcomponents-core-5.0.x/httpcore5/examples/AsyncReverseProxyExample.java
 */
public class ProxyBuffer extends BufferedData {

	public ProxyBuffer(final int bufferSize) {
		super(bufferSize);
	}

    int write(final DataStreamChannel channel) throws IOException {
        setOutputMode();
        if (buffer().hasRemaining()) {
            return channel.write(buffer());
        }
        return 0;
    }
}
