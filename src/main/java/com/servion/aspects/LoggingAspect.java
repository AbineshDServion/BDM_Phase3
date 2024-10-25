package com.servion.aspects;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;

import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.exception.ServiceException;
import com.servion.model.CallInfo;
import com.servion.model.Field;

public class LoggingAspect {
	private static Logger logger = LoggerObject.getLogger();

	//@Before("execution(* com.servion.services.*.*(..))")
	public void checkLoggerObject(ProceedingJoinPoint joinPoint) throws ServiceException {

		if(logger!=null){
			if(logger.isInfoEnabled()){WriteLog.writeUtil(WriteLog.INFO, "ENTER: LoggingAspect.checkLoggerObject()");}
			if(logger.isDebugEnabled()){WriteLog.writeUtil(WriteLog.DEBUG, "Logger Objec is available" + logger);}
		}else{
			Object[] obj = joinPoint.getArgs();
			if (obj[0] instanceof CallInfo) {
				CallInfo callInfo = (CallInfo) obj[0];
				if (callInfo != null) {
					Logger callInfo_loggerObj = (Logger) callInfo.getField(Field.LOGGER);
					LoggerObject.setLogger(callInfo_loggerObj);
					if(callInfo_loggerObj.isInfoEnabled()){WriteLog.writeUtil(WriteLog.INFO, "ENTER: LoggingAspect.checkLoggerObject()");}
					if(callInfo_loggerObj.isDebugEnabled()){WriteLog.writeUtil(WriteLog.DEBUG, "Logger Objec is Not available");}
					if(callInfo_loggerObj.isDebugEnabled()){WriteLog.writeUtil(WriteLog.DEBUG, "assigned the CallInfo Logger Object successfully to the logger object" + callInfo_loggerObj);}

				} else {
					//if(logger.isDebugEnabled()){WriteLog.writeUtil(WriteLog.DEBUG, "************CallInfo Object is null******************");}
					 throw new ServiceException("CallInof object is null");
				}
			}
			else
			{
				//if(logger.isDebugEnabled()){WriteLog.writeUtil(WriteLog.DEBUG,"For the class :"+joinPoint.getSourceLocation()+" Target  :  "+joinPoint.getTarget()+"Pass the Callinfo object as the first parameter");}
				throw new ServiceException("CallInof object is not set as first object for the location "+joinPoint.getSourceLocation()+"  Tager is  :"+joinPoint.getTarget());
			}

		}

	}
}
