package com.servion.exception;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.servion.common.util.LoggerObject;

/**
 * @author Servion Global Solutions
 *
 */
public class ServiceException extends Exception {

	
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerObject.getLogger();
	
	public ServiceException() {
	}

	public ServiceException(String arg0) {
		super(arg0);

	}

	
	public ServiceException(Throwable arg0) {
		super(arg0);
		
	}

	public ServiceException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
	
	public String getDetailedMessage() {
		String lineSep = System.getProperty("line.separator");
		String msg = " :: Message :: "+super.getMessage() + lineSep ;
		msg += " :: Cause :: "+super.getCause() + lineSep;
		msg += " :: Full StackTrace follows :: ";
		msg += ExceptionUtils.getFullStackTrace(this);
		return msg;
	}
	
}
