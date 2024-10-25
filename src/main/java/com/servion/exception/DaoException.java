package com.servion.exception;

import com.servion.common.util.util;

/**
 * @author balakumar.m
 *
 */
public class DaoException extends Exception {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	/**
	 * 
	 */
	public DaoException() {

	}

	/**
	 * @param message
	 */
	public DaoException(String message) {
		super(message);

	}

	/**
	 * @param cause
	 */
	public DaoException(Throwable cause) {
		super(cause);
		util.getFullStackTrace(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DaoException(String message, Throwable cause) {
		super(message, cause);
		util.getFullStackTrace(cause);
	}

}
