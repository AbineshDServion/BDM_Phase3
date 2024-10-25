package com.servion.services;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.CustomerProfileAggregateDAO;
import com.servion.dao.GetDebitCardDetailsDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.MenuIDMap;

public class EnterDebitOrCreditCardNoImpl implements IEnterDebitOrCreditCardNo {
	
	private static Logger logger = LoggerObject.getLogger();
	private GetDebitCardDetailsDAO getDebitCardDetailsDAO;
	private CustomerProfileAggregateDAO customerProfileAggregateDAO;
	
	
	public GetDebitCardDetailsDAO getGetDebitCardDetailsDAO() {
		return getDebitCardDetailsDAO;
	}

	public void setGetDebitCardDetailsDAO(
			GetDebitCardDetailsDAO getDebitCardDetailsDAO) {
		this.getDebitCardDetailsDAO = getDebitCardDetailsDAO;
	}

	public CustomerProfileAggregateDAO getCustomerProfileAggregateDAO() {
		return customerProfileAggregateDAO;
	}

	public void setCustomerProfileAggregateDAO(
			CustomerProfileAggregateDAO customerProfileAggregateDAO) {
		this.customerProfileAggregateDAO = customerProfileAggregateDAO;
	}
	private MessageSource messageSource;

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
	

	@Override
	public String getCardType(CallInfo arg0) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEnterDebitOrCreditCardNoPhrases(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		String str_GetMessage = Constants.EMPTY_STRING;
		String finalResult = Constants.EMPTY_STRING;
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: EnterDebitOrCreditCardNoImpl.getEnterDebitOrCreditCardNoPhrases()");}
		
		try{
			
			getConfigurationParam(callInfo);
			
			String DebitOrCreditCardLength = (String) callInfo.getField(Field.DebitOrCreditCardLength);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Debit card length is"+ DebitOrCreditCardLength);}
			
			if(util.isNullOrEmpty(DebitOrCreditCardLength)){
				DebitOrCreditCardLength = Constants.SIXTEEN;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Debit card length is setting to 16");}
			}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}


			String menuID = MenuIDMap.getMenuID("ENTER_DEBIT_OR_CREDIT_NO");
			//String anncID = AnncIDMap.getAnncID(code)
			String featureID = FeatureIDMap.getFeatureID("Enter_Debit_Or_Credit_Card_No");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}
			
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			dynamicValueArray.add(DebitOrCreditCardLength);

			Object[] object = new Object[dynamicValueArray.size()];
			for(int count=0; count<dynamicValueArray.size();count++){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count +"element as: "+dynamicValueArray.get(count) +"into Object array ");}
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

			/*
			 * Handling Grammar and MoreOptions for OD Use
			 */
			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			
			String grammar = util.getGrammar(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Grammar value is"+grammar);}
			callInfo.setField(Field.DYNAMICLIST, grammar);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Setting moreoption as false");}
			callInfo.setField(Field.MOREOPTION, false);
			//End

			finalResult = util.callDynaPhraseGeneration(combinedKey, str_GetMessage, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ReportLossCardImpl.getReportLostCardMenuPhrases() "	+ e.getMessage());
			throw new ServiceException(e);
			}
		}
		return finalResult;
	}

	public void getConfigurationParam(CallInfo callInfo)throws ServiceException{
		//Since its a setting configuration param to call info session  variable dont throw any new exception
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try{
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: EnterDebitOrCreditCardNoImpl.getConfigurationParam()");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			int DebitOrCreditCardLength = Constants.GL_ZERO;

			String tempStr = (String) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_DebitOrCreditCardLength);
			if(!util.isNullOrEmpty(tempStr)){
				DebitOrCreditCardLength = Integer.parseInt(tempStr);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Debit or Credit card lenght is "+ DebitOrCreditCardLength);}
			}

			callInfo.setField(Field.DebitOrCreditCardLength, DebitOrCreditCardLength);

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT:EnterDebitOrCreditCardNoImpl.getConfigurationParam()");}
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at EnterDebitOrCreditCardNoImpl.getConfigurationParam() "	+ e.getMessage());
			throw new ServiceException(e);
			}
		}

	}
	
	@Override
	public String getEnteredCardCallerIdentification(CallInfo arg0)
			throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValidDebitCardNumber(CallInfo arg0)
			throws ServiceException {
		// TODO Auto-generated method stub
		return false;
	}

}
