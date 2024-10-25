package com.servion.services;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICECallData;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.Field;

public class WelcomeMessageImpl implements IWelcomeMessage{
	
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
	public String getWelcomeMessagePhrases(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		String finalResult = Constants.EMPTY_STRING;
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: WelcomeMessageImpl.getWelcomeMessagePhrases()");}
		
		try{
		
			getConfigurationParam(callInfo);
			
			String strDNIS = (String) callInfo.getField(Field.DNIS);
			ArrayList<String> welcomePhraseList = (ArrayList<String>)callInfo.getField(Field.WelcomeMessagePhrases);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "DNIS is "+ strDNIS);}
			if(!util.isNullOrEmpty(welcomePhraseList)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "WelcomePhraseMap size is "+ welcomePhraseList.size());}
			}else{
				welcomePhraseList.add(DynaPhraseConstants.SILENCE_PHRASE);
			}
			
			finalResult = util.getCUIPhraseString(welcomePhraseList);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Phrase String for welcome message is"+ finalResult);}
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at WelcomeMessageImpl.getWelcomeMessagePhrases() "+ e.getMessage());}
			throw new ServiceException(e);
			}
		return finalResult;
	}

	
	public void getConfigurationParam(CallInfo callInfo)throws ServiceException{
		//Since its a setting configuration param to call info session  variable dont throw any new exception, throw if it is mandatory
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try{
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: WelcomeMessageImpl.getConfigurationParam()");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			ICECallData ivr_ICECallData = (ICECallData)callInfo.getICECallData();
			
			ArrayList<String> WelcomeMessagePhrases = new ArrayList<String>();
			String strDNIS = ivr_ICECallData.getDnis();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "DNIS fetched from ICECallData objec is :"+strDNIS);}
			
			if(util.isNullOrEmpty(callInfo.getField(Field.DNIS))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "DNIS is not setted by Application Layer"+strDNIS);}
				
				if(util.isNullOrEmpty(strDNIS)){
					//throw new ServiceException("DNIS retrieved from ICECallData object is null");
					callInfo.setField(Field.DNIS, Constants.EMPTY_STRING);
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting ICECallData Object DNIS as Callinfo DNIS");}
					callInfo.setField(Field.DNIS, strDNIS);
				}
			}
			
			WelcomeMessagePhrases = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_WelcomeMessagePhrases);

			if(!util.isNullOrEmpty(WelcomeMessagePhrases)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Bin and Account type hash map size is "+WelcomeMessagePhrases.size());}
			}

			callInfo.setField(Field.WelcomeMessagePhrases, WelcomeMessagePhrases);

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: WelcomeMessageImpl.getConfigurationParam()");}
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at WelcomeMessageImpl.getConfigurationParam() "	+ e.getMessage());}
			throw new ServiceException(e);
			}

	}
}
