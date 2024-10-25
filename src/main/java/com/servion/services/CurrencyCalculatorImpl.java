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
import com.servion.model.AnncIDMap;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;

public class CurrencyCalculatorImpl implements ICurrencyCalculator{
	
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
	public String getCurrencyCalculationPhrases(CallInfo callInfo)
			throws ServiceException {
		
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER:CurrencyCalculatorImpl.getCurrencyCalculationPhrases()");}
		
		String str_GetMessage, finalResult;
		
		String strExchangeRateCurrRate = (String) callInfo.getField(Field.LASTUSEDEXCHAGECURRRATE);
		String strExchangeRateCurrType = (String) callInfo.getField(Field.LASTUSEDEXCHAGECURRTYPE);
		String strAmount = (String) callInfo.getField(Field.LASTSELECTEDVALUE);
		Double intConvertAmount=0.0;
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Exchange Rate Currency Rate : "+ strExchangeRateCurrRate);}
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Amount :"+ strAmount);}
		
		String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
		Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}
		
		
		
	//	String menuID = MenuIDMap.getMenuID("ENTER_CURRENCY_CALCULATOR_AMT");
		String anncID = AnncIDMap.getAnncID("Calculate_Exchange_Rate_Announcement");
		String featureID = FeatureIDMap.getFeatureID("Currency_Calculator");
		String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
		
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}
		
		if(!util.isNullOrEmpty(strExchangeRateCurrRate) && !util.isNullOrEmpty(strExchangeRateCurrType)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calculating the currency ");}
				intConvertAmount= Double.parseDouble(strAmount)*Double.parseDouble(strExchangeRateCurrRate);
			}else{
				throw new ServiceException("There is no Exchange currency ");
			}
		
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Converted amount is : "+intConvertAmount);}
		
		ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
		
		dynamicValueArray.add(strAmount);
		dynamicValueArray.add(strExchangeRateCurrType+Constants.WAV_EXTENSION);
		dynamicValueArray.add(""+intConvertAmount);
		
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
		
		String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
		String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}
		
		finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
		if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
		
		return finalResult;
		
	}
	
	/*
	 * public static void main(String[] args) { String convertAmount="12345566677";
	 * Double dAmount=Double.parseDouble(convertAmount);
	 * System.out.println(String.format("%.0f", dAmount));
	 * System.out.println(dAmount); }
	 */

}
