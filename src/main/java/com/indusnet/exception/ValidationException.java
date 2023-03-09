package com.indusnet.exception;

//  This class for Validation Exception
public class ValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ValidationException() {
	}

	public ValidationException(String msg) {
		super(msg);
	}
}
