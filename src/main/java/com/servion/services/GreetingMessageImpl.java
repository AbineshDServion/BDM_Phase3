package com.servion.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.dto.ICEDataTable;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;

public class GreetingMessageImpl implements IGreetingMessage{
	
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
	public String getGreetingMessagePhrases(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		String finalResult = Constants.EMPTY_STRING;
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GreetingMessageImpl.getGreetingMessagePhrases()");}
		
		try{
			
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ICEFeatureData object is null or empty");
			}
			
			//ICEDataTable iceDataTable = (ICEDataTable)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_GreetingMessagePhrases);
			
			ArrayList<String> iceDataList = (ArrayList<String>)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_GreetingMessagePhrasesList);
			
			if(util.isNullOrEmpty(iceDataList)){
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "iceDataList is null or empty");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "There is no Greeting message configured in the ICE");}
				
				return Constants.EMPTY_STRING;
			}
			
			int totalRecords = iceDataList.size();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The total number of Greeting records configured " +iceDataList.size());}
			//if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The total number of column presents in the Greeting records configured is " +iceDataTable.columns());}
			String fromDate = Constants.EMPTY_STRING;
			String toDate = Constants.EMPTY_STRING;
			String greetingMessage = Constants.EMPTY_STRING;
			
			for(int count=Constants.GL_ZERO; count < totalRecords; count++){
				String phrases = iceDataList.get(count);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Pointing to the "+count+"Row of the ICEDataTable Object");}
				//iceDataTable.moveToRow(count);
				
				if(!util.isNullOrEmpty(phrases)) {
					String[] phraseArray = phrases.split("\\*");
					if(phraseArray.length==Constants.GL_THREE) {
						greetingMessage = phraseArray[0];
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured greeting message is " + greetingMessage);}
						fromDate = phraseArray[1];
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured from date is " + fromDate);}
						toDate = phraseArray[2];
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured to date is " + toDate);}
						SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATEFORMAT_ddMMyyyy);
						Date todayDate = formatter.parse(formatter.format(new Date()));
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Today's Date is " + todayDate);}
						
						Date date_1 = formatter.parse(fromDate);
						Date date_2 = formatter.parse(toDate);
						
						if((todayDate.after(date_1) && todayDate.before(date_2)) || todayDate.equals(date_1) ||  todayDate.equals(date_2)){
							
							if(util.isNullOrEmpty(finalResult)){
								finalResult = greetingMessage;
							}else{
								finalResult = finalResult+Constants.ASTERISK+greetingMessage;
							}
						}
						
					}
				}
				
				
				
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Phrase String is"+ finalResult);}
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at GreetingMessageImpl.getGreetingMessagePhrases() "+ e.getMessage());
			throw new ServiceException(e);
			}
		}
		return finalResult;
	}
	
	public void getConfigurationParam(CallInfo callInfo)throws ServiceException{
		//Since its a setting configuration param to call info session  variable dont throw any new exception, throw if it is mandatory
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try{
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: GreetingMessageImpl.getConfigurationParam()");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			
			HashMap<String, ArrayList<String>> GreetingMessagePhrases = new HashMap<String, ArrayList<String>>();
			
			GreetingMessagePhrases = (HashMap<String, ArrayList<String>>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_GreetingMessagePhrases);

			if(!util.isNullOrEmpty(GreetingMessagePhrases)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Bin and Account type hash map size is "+GreetingMessagePhrases.size());}
			}

			callInfo.setField(Field.GreetingMessagePhrases, GreetingMessagePhrases);

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: GreetingMessageImpl.getConfigurationParam()");}
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at GreetingMessageImpl.getConfigurationParam() "	+ e.getMessage());
			throw new ServiceException(e);
			}
		}

	}

}
