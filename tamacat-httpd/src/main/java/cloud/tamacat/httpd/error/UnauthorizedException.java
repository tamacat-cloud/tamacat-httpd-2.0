/*
 * Copyright 2009 tamacat.org
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
package cloud.tamacat.httpd.error;

import cloud.tamacat.httpd.core.BasicHttpStatus;

/**
 * <p>Throws 401 Unauthorized
 */
public class UnauthorizedException extends HttpException {

	private static final long serialVersionUID = 1L;
	
	public UnauthorizedException() {
		super(BasicHttpStatus.SC_UNAUTHORIZED);
	}
	
	public UnauthorizedException(String message) {
		super(BasicHttpStatus.SC_UNAUTHORIZED, message);
	}
	
	public UnauthorizedException(Throwable cause) {
		super(BasicHttpStatus.SC_UNAUTHORIZED, cause);
	}
	
	public UnauthorizedException(String message, Throwable cause) {
		super(BasicHttpStatus.SC_UNAUTHORIZED, message, cause);
	}
}
