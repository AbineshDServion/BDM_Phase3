package com.servion.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.db.dataservices.DataServices;
import com.db.resource.DBConstants;
import com.db.resource.VRUDBDataServicesInstance;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.context.Context;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICERuleParam;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.MenuIDMap;


public class EnterAmountImpl implements IEnterAmount {
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
	public boolean IsAmtExceedPerDayLimit(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try{
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: EnterAmountImpl.IsAmtExceedPerDayLimit()");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			configMap.put(DBConstants.CUSTOMERID, callInfo.getField(Field.CUSTOMERID));

			String currentDate = util.getTodayDateOrTime(Constants.DATEFORMAT_YYYYMMDD);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current date is "+ currentDate);}
			
			configMap.put(DBConstants.DATETIME, currentDate);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID" + configMap.get(DBConstants.CUSTOMERID));}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "DATETIME is " + configMap.get(DBConstants.DATETIME) );}

			List<String> amountRetrievedFrmDBList = new ArrayList(1);
			ICERuleParam iceRuleEngine = (ICERuleParam)callInfo.getICERuleParam();

			if(util.isNullOrEmpty(iceRuleEngine)){
				throw new ServiceException("iceFeatureData Object is null / empty");
			}

			
			/**
			 * Setting the feature name for rule engine
			 */
			String featureName = Constants.EMPTY + callInfo.getField(Field.FEATURENAME);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Feature name is " + featureName );}
			iceRuleEngine.setIVRParam(Constants.RULE_ENGINE_FEATURENAME, featureName);
			
			
			amountRetrievedFrmDBList =  iceRuleEngine.getParam(Constants.RULE_ENGINE_TRANSACTIONAMT);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Amt retrieved from DB List is "+ amountRetrievedFrmDBList);}

			String amountRetrievedFrmDB = !util.isNullOrEmpty(amountRetrievedFrmDBList)? (String)amountRetrievedFrmDBList.get(Constants.GL_ZERO) : Constants.EMPTY_STRING;
			double double_AmtRetrieved = util.isNullOrEmpty(amountRetrievedFrmDB)? Constants.GL_ZERO : Double.parseDouble(amountRetrievedFrmDB);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Amt retrieved in double is "+ double_AmtRetrieved);}
			//Saving in the callInfo field value
			callInfo.setField(Field.DBRETRIEVEDAMT, amountRetrievedFrmDB);

			String amtEntered = util.isNullOrEmpty(callInfo.getField(Field.AMOUNT)) ? Constants.ZERO : Constants.EMPTY + callInfo.getField(Field.AMOUNT);
			double double_AmtEntered = util.isNullOrEmpty(amtEntered)? Constants.GL_ZERO :  Double.parseDouble(amtEntered);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Amt Entered in double is "+ double_AmtEntered);}
			
			double combinedAmount = double_AmtRetrieved + double_AmtEntered;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Amount is "+ combinedAmount);}
			
			ICEFeatureData iceFeatureData = callInfo.getICEFeatureData();
			String iceConfiguredAmount = Constants.ZERO;
			if(util.isNullOrEmpty(iceFeatureData)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Feature data object is null or empty " + iceFeatureData );}
			}else{
				iceConfiguredAmount = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_PerDayTransAmtLimit);
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Feed Transaction Limit : " + iceConfiguredAmount );}

			double double_iceConfigAmt = util.isNullOrEmpty(iceConfiguredAmount)?Constants.GL_ZERO:Integer.parseInt(iceConfiguredAmount);

			if(combinedAmount > double_iceConfigAmt){
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Amount Exceeded the ICE configured amount");}
				return true;
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: EnterAmountImpl.IsAmtExceedPerDayLimit()");}
		}catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at:  EnterAmountImpl.IsAmtExceedPerDayLimit()" + e.getMessage());
			throw new ServiceException(e);
		}
		return false;
	}

	@Override
	public boolean IsAmtExceedPerTransactionLimit(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: EnterAmountImpl.IsAmtExceedPerTransactionLimit()");}

			ICEFeatureData iceFeatureData = (ICEFeatureData)callInfo.getField(Field.ICEFeatureData);
			String perTransactionLimit = (String) iceFeatureData.getConfig().getParamValue(Constants.CUI_PerTransactionAmtLimit);

			double double_PerTransactionLimit= util.isNullOrEmpty(perTransactionLimit)?Constants.GL_ZERO : Integer.parseInt(perTransactionLimit);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Per Transaction limit is "+ double_PerTransactionLimit);}

			String enteredAmount = util.isNullOrEmpty(callInfo.getField(Field.AMOUNT))?Constants.ZERO : Constants.EMPTY + callInfo.getField(Field.AMOUNT);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Entered Amount from callInfo field is "+ enteredAmount);}
			
			double double_enteredAmt = util.isNullOrEmpty(enteredAmount)? Constants.GL_ZERO:Double.parseDouble(enteredAmount);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Entered Amount is "+ double_enteredAmt);}

			if(double_enteredAmt > double_PerTransactionLimit){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Entered Amount > perTrnasactionlimit amount"+ double_enteredAmt);}
				return true;
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Entered Amount <= perTrnasactionlimit amount"+ double_enteredAmt);}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit: EnterAmountImpl.IsAmtExceedPerTransactionLimit()");}
		}catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at:  EnterAmountImpl.IsAmtExceedPerTransactionLimit()" + e.getMessage());
			throw new ServiceException(e);
		}

		return false;
	}

	@Override
	public String UpdateEnteredAmount(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: EnterAmountImpl.UpdateEnteredAmount()");}

			String code = Constants.ONE;
			String sessionId = (String)callInfo.getField(Field.SESSIONID);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			String currentDate = util.getTodayDateOrTime(Constants.DATEFORMAT_YYYYMMDDHHMMSS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current date is "+ currentDate);}

			String enteredAmount = Constants.EMPTY_STRING + callInfo.getField(Field.AMOUNT);
			double doubleEnteredAmt = util.isNullOrEmpty(enteredAmount)? Constants.GL_ZERO : Double.parseDouble(enteredAmount);

			/*
			 *  Start : Bug fix - 8th July 2015 - DBRETRIEVEDAMT is null if Fund Transaction OTP has been entered in the second call
			 */
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method - for OTP entered after disconnect");}
			
			List<String> amountRetrievedFrmDBList = new ArrayList(1);
			ICERuleParam iceRuleEngine = (ICERuleParam)callInfo.getICERuleParam();

			if(util.isNullOrEmpty(iceRuleEngine)){
				throw new ServiceException("iceFeatureData Object is null / empty");
			}
			
			/**
			 * Setting the feature name for rule engine
			 */
			String featureName_OTP = Constants.EMPTY + callInfo.getField(Field.FEATURENAME);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Feature name is " + featureName_OTP );}
			iceRuleEngine.setIVRParam(Constants.RULE_ENGINE_FEATURENAME, featureName_OTP);
			
			amountRetrievedFrmDBList =  iceRuleEngine.getParam(Constants.RULE_ENGINE_TRANSACTIONAMT);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Amt retrieved from DB List is "+ amountRetrievedFrmDBList);}

			String amountRetrievedFrmDB = !util.isNullOrEmpty(amountRetrievedFrmDBList)? (String)amountRetrievedFrmDBList.get(Constants.GL_ZERO) : Constants.EMPTY_STRING;
			//Saving in the callInfo field value
			callInfo.setField(Field.DBRETRIEVEDAMT, amountRetrievedFrmDB);
			
			
			
			/*
			 *  End : Bug fix - 8th July 2015 - DBRETRIEVEDAMT is null if Fund Transaction OTP has been entered in the second call
			 */
			
			String DBRetrievedAmt = Constants.EMPTY_STRING + callInfo.getField(Field.DBRETRIEVEDAMT);
			double doubleDBRetrievedAmt = util.isNullOrEmpty(DBRetrievedAmt)? Constants.GL_ZERO : Double.parseDouble(DBRetrievedAmt);

			double combinedAmt = doubleEnteredAmt + doubleDBRetrievedAmt;
			
			String featureName = util.isNullOrEmpty(callInfo.getField(Field.FEATURENAME))?Constants.EMPTY_STRING : (String)callInfo.getField(Field.FEATURENAME);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current date is "+ currentDate);}

			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Feature name is "+ featureName);}
			configMap.put(DBConstants.CUSTOMERID, callInfo.getField(Field.CUSTOMERID));
			configMap.put(DBConstants.DATETIME, currentDate);
			configMap.put(DBConstants.AMOUNT, Constants.EMPTY_STRING+combinedAmt);
			configMap.put(DBConstants.FEATURENAME, featureName);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer id is" + configMap.get(Constants.CUSTOMERID));}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Date time is " + configMap.get(Constants.DATETIME) );}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined amount is " + configMap.get(Constants.AMOUNT) );}

			String uui = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}


			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			try {
				code = dataServices.updateDailyTransaction(logger, sessionId, uui, configMap);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Result of updateDailyTransaction is  " + code );}
				code = Constants.ZERO;
			} catch (com.db.exception.ServiceException e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: EnterAmountImpl.UpdateEnteredAmount ()");}
				code = Constants.ONE;
				//e.printStackTrace();
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + code );}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit: EnterAmountImpl.UpdateEnteredAmount()");}

			return code;
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at: EnterAmountImpl.UpdateEnteredAmount ()" + e.getMessage());
			throw new ServiceException(e);
		}

	}
	
	
	/**
	 * If customer selected 2 / 3 options at transaction confirmation menu
	 * @param callInfo
	 * @return
	 * @throws ServiceException
	 */
	@Override
	public String subractEnteredAmount(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: EnterAmountImpl.neglectEnteredAmount()");}

			String code = Constants.ONE;
			String sessionId = (String)callInfo.getField(Field.SESSIONID);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
			HashMap<String, Object> configMap = new HashMap<String, Object>();

			String currentDate = util.getTodayDateOrTime(Constants.DATEFORMAT_YYYYMMDDHHMMSS);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current date is "+ currentDate);}

			String enteredAmount = Constants.EMPTY_STRING + callInfo.getField(Field.AMOUNT);
			double doubleEnteredAmt = util.isNullOrEmpty(enteredAmount)? Constants.GL_ZERO : Double.parseDouble(enteredAmount);

			String DBRetrievedAmt = Constants.EMPTY_STRING + callInfo.getField(Field.DBRETRIEVEDAMT);
			double doubleDBRetrievedAmt = util.isNullOrEmpty(DBRetrievedAmt)? Constants.GL_ZERO : Double.parseDouble(DBRetrievedAmt);
		
			double neglectAmt = Constants.GL_ZERO;
			
			if(doubleEnteredAmt > doubleDBRetrievedAmt){
				neglectAmt = doubleEnteredAmt - doubleDBRetrievedAmt;
			}else{
				neglectAmt = doubleEnteredAmt - doubleDBRetrievedAmt;
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Neglected amount is "+ neglectAmt);}
			
			String featureName = util.isNullOrEmpty(callInfo.getField(Field.FEATURENAME))?Constants.EMPTY_STRING : (String)callInfo.getField(Field.FEATURENAME);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current date is "+ currentDate);}

			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Feature name is "+ featureName);}
			configMap.put(DBConstants.CUSTOMERID, callInfo.getField(Field.CUSTOMERID));
			configMap.put(DBConstants.DATETIME, currentDate);
			configMap.put(DBConstants.AMOUNT, Constants.EMPTY_STRING+neglectAmt);
			configMap.put(DBConstants.FEATURENAME, featureName);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer id is" + configMap.get(Constants.CUSTOMERID));}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Current Date time is " + configMap.get(Constants.DATETIME) );}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Neglected amount is " + configMap.get(Constants.AMOUNT) );}

			String uui = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}


			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			try {
				code = dataServices.updateDailyTransaction(logger, sessionId, uui, configMap);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Result of updateDailyTransaction is  " + code );}
				code = Constants.ZERO;
			} catch (com.db.exception.ServiceException e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: EnterAmountImpl.Neglected Amount ()");}
				code = Constants.ONE;
				//e.printStackTrace();
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + code );}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit: EnterAmountImpl.UpdateEnteredAmount()");}

			return code;
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at: EnterAmountImpl.UpdateEnteredAmount ()" + e.getMessage());
			throw new ServiceException(e);
		}

	}

	@Override
	public String accountBalanceMenuForInsuffFunds(CallInfo callInfo)
			throws ServiceException {
//		logger = (Logger)callInfo.getField(Field.LOGGER);
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		String str_GetMessage, finalResult;
		try{
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: EnterAmountImpl.accountBalanceMenuForInsuffFunds()");}
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Fetching the Rule Object values");}

			String currentAvailableBalance = (String)callInfo.getField(Field.CURRENTACCTAVAILBALANCE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Current Account available balance is " + currentAvailableBalance );}
			
			if(!util.isNullOrEmpty(currentAvailableBalance)){
				
				//Adding the values to dynamic 
				dynamicValueArray.add(currentAvailableBalance);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Generated dynamic phrase list is"+dynamicValueArray);}
				
				
				/**
				 * Following to find out is there any other accounts are available or not
				 */
				IAccountOrCardSelection acctOrCardSel = Context.getiAccountOrCardSelection();
				int totalNoOfAcct = acctOrCardSel.getNumberOfAccountOrCreditCards(callInfo);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total Number of Available account or credit card is "+totalNoOfAcct);}
				// END  
				String grammar = Constants.EMPTY_STRING;
				boolean moreOption = false;
				
				if(totalNoOfAcct > Constants.GL_ONE){
					
					dynamicValueArray.add(DynaPhraseConstants.Cheque_Book_1011);
					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_1);
					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1011);
					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_2);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added the dyna phrase constant value "+dynamicValueArray);}
					
					grammar = Constants.GRAMMAR_ACCOUNT + Constants.COMMA + Constants.GRAMMAR_AMOUNT;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added the dyna Grammar constant value "+ grammar);}
				}
				else{
					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1011);
					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_1);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added the dyna phrase constant value "+dynamicValueArray);}
					
					grammar = Constants.GRAMMAR_AMOUNT;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Added the dyna Grammar constant value "+ grammar);}
				
				}
				
				//Getting language code 
				String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
				Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}
				
				//Forming Key combination 
				String menuID = MenuIDMap.getMenuID("AVAILBAL_FOR_INSUFF_FUNDS");
				String featureID = FeatureIDMap.getFeatureID("EnterAmount");
				String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}
				
				//Adding to object value
				Object[] object = new Object[dynamicValueArray.size()];
				for(int count=0; count<dynamicValueArray.size();count++){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count +"element: "+dynamicValueArray.get(count) +"into Object array ");}
					object[count] = dynamicValueArray.get(count);
				}
				
				//Initial prompt is formed based from
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}
				
				str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

				if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
					return (DynaPhraseConstants.SILENCE_PHRASE);
				}

				int totalPrompt = util.getTotalPromptCount(str_GetMessage);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt received from the dynaproperty file is "+totalPrompt);}

				//Overriding the total prompts, received from the property file
				if(totalNoOfAcct>Constants.GL_ONE){
					totalPrompt = Constants.GL_SEVEN ;
				}
				else{
					totalPrompt = Constants.GL_FIVE;
				}

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

				callInfo.setField(Field.DYNAMICLIST, grammar);
				callInfo.setField(Field.MOREOPTION, moreOption);
				
				String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
				String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

				finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"There is no current account available balance");}			
				throw new ServiceException("There is no current account available balance");
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: ComplaintAlertImpl.getComplaintAlertMenuPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at  ChequeBookRequestImpl.getChequeBookRequestSuccPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	
	}

}
