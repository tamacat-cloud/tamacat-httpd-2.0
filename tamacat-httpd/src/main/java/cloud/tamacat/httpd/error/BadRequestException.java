/*
 * Copyright 2017 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
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
