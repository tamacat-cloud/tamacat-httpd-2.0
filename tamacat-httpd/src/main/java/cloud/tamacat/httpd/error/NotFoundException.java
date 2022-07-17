/*
 * Copyright 2009 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.error;

import cloud.tamacat.httpd.core.BasicHttpStatus;

/**
 * <p>Throws 404 Not Found.
 */
public class NotFoundException extends HttpException {

	private static final long serialVersionUID = 1L;
	
	public NotFoundException() {
		super(BasicHttpStatus.SC_NOT_FOUND);
	}

	public NotFoundException(String message) {
		super(BasicHttpStatus.SC_NOT_FOUND, message);
	}

	public NotFoundException(Throwable cause) {
		super(BasicHttpStatus.SC_NOT_FOUND, cause);
	}

	public NotFoundException(String message, Throwable cause) {
		super(BasicHttpStatus.SC_NOT_FOUND, message, cause);
	}
}
