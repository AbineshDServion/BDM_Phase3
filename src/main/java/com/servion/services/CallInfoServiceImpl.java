package com.servion.services;

import java.util.Map;

import org.apache.log4j.Logger;

import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.exception.ServiceException;
import com.servion.model.CallInfo;
import com.servion.model.CallInfoImpl;
import com.servion.model.Field;


public class CallInfoServiceImpl implements ICallInfoService{

	
	/**
	 * This method creates a logger object.
	 * 
	 * @param Map
	 *            <Field, String> Fields, Logger logger>
	 * @return CallData
	 */
	
	public void getLoggerInitialized(Logger loggerObject, String sessionId)throws ServiceException{
		/**
		 * Initializing Logger object for the whole call
		 */
		try{
		if(loggerObject == null) {
			loggerObject = Logger.getLogger(WriteLog.class.getClass());
		}
		
		if(sessionId == null) {
            sessionId = "";
		}
		
		LoggerObject.setLogger(loggerObject);
		LoggerObject.setSessionId(sessionId);
		
		//Initiating WriteLog here itself enough, since its a static method
		WriteLog.loggerInit(LoggerObject.getLogger(), LoggerObject.getSessionId());
		
//		if(loggerObject.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: getLoggerInitialized()");}
//		
//		if(loggerObject.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Successfully retrieved the logger object");}
//		
//		if(loggerObject.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: getLoggerInitialized()");}
//		
		
		}catch(Exception e){
			throw new ServiceException("****Exception while setting the logger object in the method getLoggerInitialized()**** "+e);
		}
	}
	
	
	
	
	/**
	 * This method creates a new CallInfo instance, and sets in it all values contained by parameters of "fields" variable
	 * 
	 * @param Map
	 *            <Field, String> Fields, Logger logger>
	 * @return CallData
	 */

	public CallInfo getCallInfoDetails(Map<Field, String> fields)throws ServiceException{

		CallInfo callInfo = new CallInfoImpl();

		try{
			
			Logger logger = (Logger) callInfo.getField(Field.LOGGER);
			String sessionId = (String) callInfo.getField(Field.SESSIONID);
			WriteLog.loggerInit(logger, sessionId);
			

			String ani = fields.get(Field.ANI);
			String dnis = fields.get(Field.DNIS);
			String session_ID = fields.get(Field.SESSIONID);
			
			/**
			 * Setting session id for load testing issue on 24-Jun-2014
			 */
			callInfo.setField(Field.SESSIONID, session_ID);
			callInfo.setField(Field.ANI, ani);

			//SETTED FOR REPORTING
			callInfo.setField(Field.CLI, ani);
			callInfo.setField(Field.DNIS, dnis);
			
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID, "ENTER: getCallInfoDetails()");}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID, "Setted the logger object");};
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID, "Session id got from OD is " + session_ID);};
			//End Vinoth on 24-Jun-2014


			//SETTED FOR REPORTING

			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID, "Setting ANI=" + ani);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID, "Setting DNIS=" + dnis);}
			
			
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID, "EXIT: getCallInfoDetails()");}

		}catch(Exception e){
			throw new ServiceException("****Exception while setting the logger object**** "+e);
		}

		return callInfo;

	}
}
