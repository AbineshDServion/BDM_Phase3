package com.servion.services;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.DepositDtlsInqDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.model.AnncIDMap;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.fixedDepositBalance.FixedDepositBalance_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class FDBalanceImpl implements IFDBalance{
	
	private static Logger logger = LoggerObject.getLogger();
	private DepositDtlsInqDAO depositDtlsInqDAO;
	
	
	public DepositDtlsInqDAO getDepositDtlsInqDAO() {
		return depositDtlsInqDAO;
	}

	public void setDepositDtlsInqDAO(DepositDtlsInqDAO depositDtlsInqDAO) {
		this.depositDtlsInqDAO = depositDtlsInqDAO;
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
	public String getFDAccountBalance(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: FDBalanceImpl.getFDAccountBalance()");}
		String code = Constants.EMPTY_STRING;
		try{

			String contractID = (String) callInfo.getField(Field.SRCNO);
			
			if(!util.isNullOrEmpty(contractID)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected FD account number ending with is :"+ util.getSubstring(contractID, Constants.GL_FOUR));}
			}
			
			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();
			
			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
			String customerIDObbj = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))?Constants.NA : (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerIDObbj);}

			
			String strHostInParam =Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +  Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.SRCNO))
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_DEPOSITDTLSINQ);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);
			
			String startTime = util.getCurrentDateTime();
			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
		
			
			hostReportDetails.setHostStartTime(startTime); //It should be in the format of 31/07/2013 18:11:11
			hostReportDetails.setHostType(Constants.HOST_TYPE_ACCOUNTSERVICES);
			//End Reporting
			
			/*
			 *  Setting NA values
			 */
			hostReportDetails.setHostEndTime(Constants.NA);
			hostReportDetails.setHostOutParams(Constants.NA);
			hostReportDetails.setHostResponse(Constants.NA);
			
			callInfo.setHostReportDetails(hostReportDetails);
			IvrData ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
			callInfo.insertHostDetails(ivrdata);
			
			/* END */
			
			
			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			
			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}
			
			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_DEPOSITDTLSINQ_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_DEPOSITDTLSINQ_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}


			
			FixedDepositBalance_HostRes fixedDepositBalance_HostRes = depositDtlsInqDAO.getFDBalanceHostRes(callInfo, contractID, requestType);
			

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "fixedDepositBalance_HostRes Object is :"+ fixedDepositBalance_HostRes);}
			callInfo.setFixedDepositBalance_HostRes(fixedDepositBalance_HostRes);

			code = fixedDepositBalance_HostRes.getErrorCode();

			
			String hostEndTime = fixedDepositBalance_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);
			
			
			
			
			String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Access duration is " + durationTime);}


			/*strHostInParam = 	Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber(contractID)
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_TYPE + Constants.EQUALTO + Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_REQUEST_TYPE + Constants.EQUALTO +Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_DURATION +Constants.EQUALTO + durationTime ;*/
			
			/****Duplicate RRN Fix 25012016 *****/
			strHostInParam =Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +  Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.SRCNO))
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
					hostReportDetails.setHostInParams(strHostInParam);
			/************************************/
					
			String hostResCode = fixedDepositBalance_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);
			
			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(fixedDepositBalance_HostRes.getErrorDesc()) ?"NA" :fixedDepositBalance_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);
			
			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
			
			callInfo.updateHostDetails(ivrdata);
			//End Reporting
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Success for FDBalanceImpl.getFDAccountBalance");}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for Deposit Dtls Inquriry host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + fixedDepositBalance_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_DEPOSITDTLSINQ, fixedDepositBalance_HostRes.getHostResponseCode());
				/**
				 * Following will be called only if there occured account selection before this host access
				 */
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
				util.setEligibleAccountCounts(callInfo, hostResCode);
			
			}
			
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at FDBalanceImpl.getFDAccountBalance "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

	@Override
	public String getFDAccountBalancePhrases(CallInfo callInfo)throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		String str_GetMessage = Constants.EMPTY_STRING;
		String finalResult = Constants.EMPTY_STRING;
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: FDBalanceImpl.getFDAccountBalancePhrases()");}
		
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			ArrayList<String>FDBalanceFieldsAndOrder = new ArrayList<String>();
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_FDBalanceFieldsAndOrder))){
				FDBalanceFieldsAndOrder = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_FDBalanceFieldsAndOrder);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting FD Account Balance fields and Order in the a local variable");}
			}

			if(!util.isNullOrEmpty(FDBalanceFieldsAndOrder)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "FD Account Balance Fields and Order size is" + FDBalanceFieldsAndOrder.size());}
			}
			
			callInfo.setField(Field.FDBalanceFieldsAndOrder, FDBalanceFieldsAndOrder);

			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			
			FixedDepositBalance_HostRes fixedDepositBalance_HostRes = callInfo.getFixedDepositBalance_HostRes();
			ArrayList<String> balanceListForAnnc = (ArrayList<String>)callInfo.getField(Field.FDBalanceFieldsAndOrder);
			
			
			if(util.isNullOrEmpty(fixedDepositBalance_HostRes)){
				throw new ServiceException("FD Account balance host response object bean is null");
			}
			
			if(util.isNullOrEmpty(balanceListForAnnc)){
				throw new ServiceException("User doesn't configure the balance types in UI");
			}
			
			String maturityDate = fixedDepositBalance_HostRes.getMaturityDate();
			String InterestRate = fixedDepositBalance_HostRes.getInterestRate();
			String Tenor = fixedDepositBalance_HostRes.getTenor();
			String MaturityAmount = fixedDepositBalance_HostRes.getMaturityAmount();
			
			
			if(util.isNullOrEmpty(maturityDate))
				maturityDate = Constants.EMPTY_STRING;
			
			if(util.isNullOrEmpty(InterestRate))
				InterestRate = Constants.EMPTY_STRING;
			
			if(util.isNullOrEmpty(Tenor))
				Tenor = Constants.EMPTY_STRING;
			
			if(util.isNullOrEmpty(MaturityAmount))
				MaturityAmount = Constants.EMPTY_STRING;
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Maturity Date for the account is"+ maturityDate);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "InterestRate for the account is"+ InterestRate);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Tenor for the account is"+ Tenor);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Maturity Amount for the account is"+ MaturityAmount);}
			
			
			
			/**
			 * Following are the parameter declared to make the prompt announcement / balance announcement as dynamic
			 */
			String languageKey = Constants.EMPTY_STRING;
			Locale locale = null;
			String anncID = Constants.EMPTY_STRING;
			String featureID = Constants.EMPTY_STRING;
			String combinedKey = Constants.EMPTY_STRING;
			Object[] object = null;
			int totalPrompt = Constants.GL_ZERO;
			String dynamicPhraseKey = Constants.EMPTY_STRING;
			String dynamicMessageValue = Constants.EMPTY_STRING;
			String grammar = Constants.EMPTY_STRING;
			
			languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			//String menuID = MenuIDMap.getMenuID("REPORT_LOSS_CARD_CONFIRMATION");
			anncID = AnncIDMap.getAnncID("FD_Balance_Message");
			featureID = FeatureIDMap.getFeatureID("Account_Balance_FD");
			combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
			//END 
			
			
			String temp_Str;
			for(int count=0; count<balanceListForAnnc.size(); count++){
				temp_Str = balanceListForAnnc.get(count);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Balance type for announcement is"+ temp_Str);}
				
				switch(temp_Str)
				{
				case Constants.BALANCE_TYPE_MATURITY_DATE:
					dynamicValueArray = new ArrayList<Object>();
					if(!util.isNullOrEmpty(maturityDate)){
						dynamicValueArray.add(DynaPhraseConstants.FD_Balance_1000);
						String strDate = Constants.EMPTY_STRING;
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Before Converting the date into yyyyMMdd" + strDate );}
						strDate = util.convertDateStringFormat(maturityDate, Constants.DATEFORMAT_YYYY_MM_DD, "yyyyMMdd");
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "After Converting the date into yyyyMMdd" + strDate );}
						dynamicValueArray.add(strDate);
					}else{
						dynamicValueArray.add(Constants.NA);
						dynamicValueArray.add(Constants.NA);
					}
					
					combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
					combinedKey = combinedKey+Constants.UNDERSCORE+Constants.BALANCE_TYPE_MATURITY_DATE;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

					object = new Object[dynamicValueArray.size()];
					for(int count_i=0; count_i<dynamicValueArray.size();count_i++){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count_i +"element: "+dynamicValueArray.get(count_i) +"into Object array ");}
						object[count_i] = dynamicValueArray.get(count_i);
					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}

					str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

					if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
						return (DynaPhraseConstants.SILENCE_PHRASE);
					}

					totalPrompt = util.getTotalPromptCount(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

					dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}

					dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}


					if(util.isNullOrEmpty(finalResult)){
						finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
					}else{
						finalResult =finalResult +Constants.ASTERISK+ util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
						finalResult = finalResult.trim();
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
					
					break;
				case Constants.BALANCE_TYPE_INTEREST_RATE:
					dynamicValueArray = new ArrayList<Object>();
					if(!util.isNullOrEmpty(InterestRate)){
						dynamicValueArray.add(DynaPhraseConstants.FD_Balance_1001);
						dynamicValueArray.add(InterestRate);
						dynamicValueArray.add(DynaPhraseConstants.Percentage);
					}else{
						dynamicValueArray.add(Constants.NA);
						dynamicValueArray.add(Constants.NA);
						dynamicValueArray.add(Constants.NA);
					}
					
					combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
					combinedKey = combinedKey+Constants.UNDERSCORE+Constants.BALANCE_TYPE_INTEREST_RATE;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

					object = new Object[dynamicValueArray.size()];
					for(int count_i=0; count_i<dynamicValueArray.size();count_i++){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count_i +"element: "+dynamicValueArray.get(count_i) +"into Object array ");}
						object[count_i] = dynamicValueArray.get(count_i);
					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}

					str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

					if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
						return (DynaPhraseConstants.SILENCE_PHRASE);
					}

					totalPrompt = util.getTotalPromptCount(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

					dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}

					dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}


					if(util.isNullOrEmpty(finalResult)){
						finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
					}else{
						finalResult =finalResult +Constants.ASTERISK+ util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
						finalResult = finalResult.trim();
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
					
					break;
				case Constants.BALANCE_TYPE_TENOR:
					dynamicValueArray = new ArrayList<Object>();
					if(!util.isNullOrEmpty(Tenor)){
						dynamicValueArray.add(DynaPhraseConstants.FD_Balance_1002);
						dynamicValueArray.add(Tenor);
					}else{
						dynamicValueArray.add(Constants.NA);
						dynamicValueArray.add(Constants.NA);
					}
					
					combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
					combinedKey = combinedKey+Constants.UNDERSCORE+Constants.BALANCE_TYPE_TENOR;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

					object = new Object[dynamicValueArray.size()];
					for(int count_i=0; count_i<dynamicValueArray.size();count_i++){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count_i +"element: "+dynamicValueArray.get(count_i) +"into Object array ");}
						object[count_i] = dynamicValueArray.get(count_i);

					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}

					str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

					if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
						return (DynaPhraseConstants.SILENCE_PHRASE);
					}

					totalPrompt = util.getTotalPromptCount(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

					dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}

					dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}


					if(util.isNullOrEmpty(finalResult)){
						finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
					}else{
						finalResult =finalResult +Constants.ASTERISK+ util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
						finalResult = finalResult.trim();
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
					
					break;
				case Constants.BALANCE_TYPE_MATURITY_AMOUNT:
					dynamicValueArray = new ArrayList<Object>();
					if(!util.isNullOrEmpty(MaturityAmount)){
						dynamicValueArray.add(DynaPhraseConstants.FD_Balance_1003);
						dynamicValueArray.add(MaturityAmount);
					}else{
						dynamicValueArray.add(Constants.NA);
						dynamicValueArray.add(Constants.NA);
					}
					
					combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;
					combinedKey = combinedKey+Constants.UNDERSCORE+Constants.BALANCE_TYPE_MATURITY_AMOUNT;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

					object = new Object[dynamicValueArray.size()];
					for(int count_i=0; count_i<dynamicValueArray.size();count_i++){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count_i +"element: "+dynamicValueArray.get(count_i) +"into Object array ");}
						object[count_i] = dynamicValueArray.get(count_i);

					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}

					str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

					if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
						return (DynaPhraseConstants.SILENCE_PHRASE);
					}

					totalPrompt = util.getTotalPromptCount(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

					dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}

					dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}


					if(util.isNullOrEmpty(finalResult)){
						finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
					}else{
						finalResult =finalResult +Constants.ASTERISK+ util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
						finalResult = finalResult.trim();
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
					
					break;
				}
			}

			/*
			 * Handling Grammar and MoreOptions for OD Use
			 */
			grammar = util.getGrammar(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Grammar value is"+grammar);}
			callInfo.setField(Field.DYNAMICLIST, grammar);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Setting moreoption as false");}
			callInfo.setField(Field.MOREOPTION, false);
			//End

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: FDBalanceImpl.getFDAccountBalancePhrases()");}
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at FDBalanceImpl.getFDAccountBalancePhrases() "	+ e.getMessage());
			throw new ServiceException(e);
			}
		}
		return finalResult;
	}
}
