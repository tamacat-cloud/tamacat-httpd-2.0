/*
 * Copyright 2009 tamacat.org
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package cloud.tamacat.httpd.error;

import cloud.tamacat.httpd.core.HttpStatus;

/**
 * <p>The {@code HttpException} exception represents a HTTP fault.
 */
public class HttpException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final HttpStatus httpStatus;
	
	public HttpException(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}
	
	public HttpException(HttpStatus httpStatus, Throwable cause) {
		super(cause);
		this.httpStatus = httpStatus;
	}
	
	public HttpException(HttpStatus httpStatus, String message) {
		super(message);
		this.httpStatus = httpStatus;
	}
	
	public HttpException(HttpStatus httpStatus, String message, Throwable cause) {
		super(message, cause);
		this.httpStatus = httpStatus;
	}
	
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
}
