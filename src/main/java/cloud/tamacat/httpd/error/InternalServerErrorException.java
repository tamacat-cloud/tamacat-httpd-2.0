/*
 * Copyright 2020 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.error;

import cloud.tamacat.httpd.core.BasicHttpStatus;

/**
 * <p>Throws 500 Internal Server Error.
 */
public class InternalServerErrorException extends HttpException {

	private static final long serialVersionUID = 1L;

	public InternalServerErrorException() {
		super(BasicHttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	public InternalServerErrorException(Throwable cause) {
		super(BasicHttpStatus.SC_INTERNAL_SERVER_ERROR, cause);
	}

	public InternalServerErrorException(String message) {
		super(BasicHttpStatus.SC_INTERNAL_SERVER_ERROR, message);
	}

	public InternalServerErrorException(String message, Throwable cause) {
		super(BasicHttpStatus.SC_INTERNAL_SERVER_ERROR, message, cause);
	}
}
