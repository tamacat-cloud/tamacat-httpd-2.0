/*
 * Copyright (c) 2017 tamacat.org
 * All rights reserved.
 */
package cloud.tamacat.httpd.error;

import cloud.tamacat.httpd.core.BasicHttpStatus;

public class BadRequestException extends HttpException {

	private static final long serialVersionUID = 1L;

	public BadRequestException() {
		super(BasicHttpStatus.SC_BAD_REQUEST);
	}
	
	public BadRequestException(String message) {
		super(BasicHttpStatus.SC_BAD_REQUEST, message);
	}
	
	public BadRequestException(Throwable cause) {
		super(BasicHttpStatus.SC_BAD_REQUEST, cause);
	}

	public BadRequestException(String message, Throwable cause) {
		super(BasicHttpStatus.SC_BAD_REQUEST, message, cause);
	}
}
