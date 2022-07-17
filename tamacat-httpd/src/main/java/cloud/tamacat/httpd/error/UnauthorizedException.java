/*
 * Copyright 2009 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
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
