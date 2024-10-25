package com.servion.services;

import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.db.dataservices.DataServices;
import com.db.resource.DBConstants;
import com.db.resource.VRUDBDataServicesInstance;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.Field;

public class BirthDayGreetingImpl implements IBirthDayGreeting {
	
	private static Logger logger = LoggerObject.getLogger();
	
	private MessageSource messageSource;

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	

	@Override
	public String UpdateBirthdayMessagePlayedStatus(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: BirthDayGreetingImpl. UpdateBirthdayMessagePlayedStatus()");}

			String code = Constants.ONE;
			String sessionId = (String)callInfo.getField(Field.SESSIONID);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			String currentDate = util.getTodayDateOrTime(Constants.DATEFORMAT_YYYYMMDDHHMMSS);
			configMap.put(DBConstants.CUSTOMERID, callInfo.getField(Field.CUSTOMERID));
			configMap.put(DBConstants.DATETIME, currentDate);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer id is " + configMap.get(Constants.CUSTOMERID));}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Date is " + configMap.get(Constants.DATETIME) );}

			String uui = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}
			
			
			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			try {
				code = dataServices.updateDOBAnnounced(logger, sessionId, uui, configMap);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Result of updateBirthDay Annc phrase is " + code );}

			} catch (com.db.exception.ServiceException e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR:BirthDayGreetingImpl. UpdateBirthdayMessagePlayedStatus ()");}
				code = Constants.ONE;
				//e.printStackTrace();
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + code );}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit:BirthDayGreetingImpl. UpdateBirthdayMessagePlayedStatus ()");}

			return code;
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at: BirthDayGreetingImpl. UpdateBirthdayMessagePlayedStatus ()" + e.getMessage());
			throw new ServiceException(e);
		}

	}

	@Override
	public String getBirthdayAnn(CallInfo callInfo) throws ServiceException {
		
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: BirthDayGreetingImpl.getBirthdayAnn()");}
		
		try{
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			
			XMLGregorianCalendar todayCalender = util.getXMLGregorianCalendarNow();
			
			XMLGregorianCalendar DOB = null;
			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerShortDetails())){
					DOB = callInfo.getCallerIdentification_HostRes().getCustomerShortDetails().getDOB();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "DOB received from the host is "+ DOB);}
				}
			}
			DOB.setYear(todayCalender.getYear());
			
			GregorianCalendar calendardob = DOB.toGregorianCalendar();
			GregorianCalendar calendartoday = todayCalender.toGregorianCalendar();
			
//			if(Constants.GL_ZERO == calendartoday.compareTo(calendardob)){
				
			if(calendardob.getTime().getDate() == calendartoday.getTime().getDate() && calendardob.getTime().getMonth() == calendartoday.getTime().getMonth()){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Playing the birth day announcement");}
				return DynaPhraseConstants.BirthDayWishes ;
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Playing the Advance birth day announcement");}
				return  DynaPhraseConstants.AdvanceBirthDayWishes;
			}
			
			
//			long miliSecondForDate1 = calendardob.getTimeInMillis();
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The DOB Millisec"+ miliSecondForDate1);}
//			
//			long miliSecondForDate2 = calendartoday.getTimeInMillis();
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The today;s Millisec"+ miliSecondForDate2);}
//			
//			// Calculate the difference in millisecond between two dates
//			long diffInMilis = miliSecondForDate1 - miliSecondForDate2;
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The difference between dates in milli seconds "+ diffInMilis);}
//			
//			long diffInDays = diffInMilis / (24 * 60 * 60 * 1000);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The difference between date is "+ diffInDays);}
//			
//			ICEFeatureData  ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
//
//			if(util.isNullOrEmpty(ICEFeatureData)){
//				throw new ServiceException("ICEFeatureData object is null");
//			}
//			
//			long dateRange = (Long) callInfo.getField(Field.DateRangeForBirthDayAnnc);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Date Range configured for birthday announcement is "+ dateRange);}
//			
//			if(diffInDays == Constants.GL_ZERO){
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Playing the birth day announcement");}
//				return DynaPhraseConstants.SILENCE_PHRASE ;
//			}
//			if(diffInDays<=dateRange){
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Playing the Advance birth day announcement");}
//				return  DynaPhraseConstants.SILENCE_PHRASE;
//			}else{
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Returning empty string");}
//				return Constants.EMPTY_STRING;
//			}
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at BirthDayGreetingImpl.getBirthdayAnn() "	+ e.getMessage());}
			throw new ServiceException(e);
			}
	}

}
