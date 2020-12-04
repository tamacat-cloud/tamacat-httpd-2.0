/*
 * Copyright 2009 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.error;

import cloud.tamacat.httpd.core.BasicHttpStatus;

/**
 * <p>Throws 403 Forbidden.
 */
public class ForbiddenException extends HttpException {

	private static final long serialVersionUID = 1L;

	public ForbiddenException(){
		super(BasicHttpStatus.SC_FORBIDDEN);
	}
	
	public ForbiddenException(String message) {
		super(BasicHttpStatus.SC_FORBIDDEN, message);
	}
	
	public ForbiddenException(Throwable cause) {
		super(BasicHttpStatus.SC_FORBIDDEN, cause);
	}
	
	public ForbiddenException(String message, Throwable cause) {
		super(BasicHttpStatus.SC_FORBIDDEN, message, cause);
	}
}
