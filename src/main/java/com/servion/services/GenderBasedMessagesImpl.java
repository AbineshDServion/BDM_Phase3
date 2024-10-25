package com.servion.services;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;

public class GenderBasedMessagesImpl implements IGenderBasedMessages{
	
	private static Logger logger = LoggerObject.getLogger();
	
	private MessageSource messageSource;

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	
	
	//If Gender not received or phrase not configured then send finalResult = "";
	
	//The following keys should be configured in the ICE global config
	
	//<Host Return Value - Gender>_Phrase = <wav file>
	
	//Ex : Male_Phrase = Male.wav"
	
	@Override
	public String getGenderBasedMessagePhrases(CallInfo callInfo)
			throws ServiceException {
		
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GenderBasedMessagesImpl.getGenderBasedMessagePhrases()");}
		String finalResult;
		try{
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			String genderValue = Constants.EMPTY_STRING;
			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getCustomerShortDetails())){
					genderValue = callInfo.getCallerIdentification_HostRes().getCustomerShortDetails().getGender();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Gender from the host is" + genderValue);}
				}
			}

			finalResult = (String) ivr_ICEFeatureData.getConfig().getParamValue(genderValue+Constants._PHRASE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Gender phrase received from the ICE  is" + finalResult);}
			
			if(util.isNullOrEmpty(finalResult)){
				finalResult = Constants.EMPTY_STRING;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the gender phrase as empty");}
			}
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ReportLossCardImpl.getReportLostCardMenuPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
			}
		return finalResult;
		
		
	}

}
