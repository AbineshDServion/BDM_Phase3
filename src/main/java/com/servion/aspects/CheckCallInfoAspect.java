package com.servion.aspects;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;

import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.exception.ServiceException;
import com.servion.model.CallInfo;

public class CheckCallInfoAspect {
	private static Logger logger = LoggerObject.getLogger();
	
	//@Around("execution(* com.servion.services.*.*(CallInfo))")
	public Object CheckingCallInfo(ProceedingJoinPoint joinPoint) throws ServiceException {
		if(logger.isInfoEnabled()){WriteLog.writeUtil(WriteLog.INFO, "ENTER: CheckCallInfoAspect.CheckingCallInfo()");}
		
		Object[] obj = joinPoint.getArgs();
		if (obj[0] instanceof CallInfo) {
			CallInfo callInfo = (CallInfo) obj[0];
			if (callInfo != null) {
				//MDC.put("sessionID", callData.getField(Field.SESSION_ID));
				if(logger.isDebugEnabled()){WriteLog.writeUtil(WriteLog.DEBUG, "CallInfo Object is available");}
				
			} else {
				if(logger.isDebugEnabled()){WriteLog.writeUtil(WriteLog.DEBUG, "CallInfo Object is null");}
				throw new ServiceException("Callinfo Object is not available");
			}
		}
		else
		{
			if(logger.isDebugEnabled()){WriteLog.writeUtil(WriteLog.DEBUG,"First arguement should be CallData Instance");}
		}
		
		
		Object retVal = null;
		try{
		retVal = joinPoint.proceed();
		if(logger.isInfoEnabled()){WriteLog.writeUtil(WriteLog.INFO, "EXIT: CheckCallInfoAspect.CheckingCallInfo()");}
		
		} catch (Throwable t) {
			WriteLog.writeError(WriteLog.ERROR, t,  "Catch Block of CheckCallInfoAspect.CheckingCallInfo::"+t.getMessage());
			throw new ServiceException(t);
		}
		return retVal;             
	}

}
