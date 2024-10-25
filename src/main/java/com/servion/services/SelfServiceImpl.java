package com.servion.services;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.exception.ServiceException;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.MenuIDMap;

public class SelfServiceImpl implements ISelfService{
	
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
	public String getSelfServicePhrases(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER:SelfServiceImpl.getSelfServicePhrases()");}
		
		String str_GetMessage, finalResult;
		
		String cardSuffixAnncLength = (String) callInfo.getField(Field.LastNDigits);
		String debitCardNumber = (String) callInfo.getField(Field.DEBITCARDNUMBER);
		String cinType = (String) callInfo.getField(Field.CIN_TYPE);
		String cin = (String) callInfo.getField(Field.CIN);
		
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Card Suffix Announcement length is : "+ cardSuffixAnncLength);}
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Debit card number is "+ util.maskCardOrAccountNumber(debitCardNumber));}
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CIN Type is "+ cinType);}
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CIN number is"+ util.maskCardOrAccountNumber(cin));}
		
		String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
		Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}
		
		
		String menuID = MenuIDMap.getMenuID("REPORT_LOSS_CARD_CONFIRMATION");
		//String anncID = AnncIDMap.getAnncID(code)
		String featureID = FeatureIDMap.getFeatureID("Report_Lost_Card");
		String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;
		
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}
		
		if(util.isNullOrEmpty(debitCardNumber) && !util.isNullOrEmpty(cinType) && !util.isNullOrEmpty(cin)){
			if(Constants.CIN_TYPE_DEBIT.equalsIgnoreCase(cinType)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Assinging CIN as debit card number");}
				debitCardNumber = cin;
				callInfo.setField(Field.DEBITCARDNUMBER, cin);
			}else{
				throw new ServiceException("There is no valid Debit card number");
			}
		}
		
		if(util.isNullOrEmpty(cardSuffixAnncLength)){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Assinging card suffix length as Zero");}
			cardSuffixAnncLength = Constants.ZERO;
		}

		
		String debitCardSuff = util.getSubstring(debitCardNumber, Integer.parseInt(cardSuffixAnncLength));
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The debit card suffix length is" +debitCardSuff );}
		
		
		ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
		dynamicValueArray.add(debitCardSuff);
		
		Object[] object = new Object[dynamicValueArray.size()];
		for(int count=0; count<dynamicValueArray.size();count++){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count +"element: "+dynamicValueArray.get(count) +"into Object array ");}
			object[count] = dynamicValueArray.get(count);
					
		}
		
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}
		
		str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}
		
		if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
			return (DynaPhraseConstants.SILENCE_PHRASE);
		}
		
		int totalPrompt = util.getTotalPromptCount(str_GetMessage);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}
		finalResult = util.callDynaPhraseGeneration(combinedKey, str_GetMessage, totalPrompt);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
		return finalResult;
		
	}

}
