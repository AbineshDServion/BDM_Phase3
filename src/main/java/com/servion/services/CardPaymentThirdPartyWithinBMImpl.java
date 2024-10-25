package com.servion.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.BeneficiaryDtlsInquiryDAO;
import com.servion.dao.ListBeneficiaryDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.MenuIDMap;
import com.servion.model.creditCardPayment.CCPayment_BenefDetails;
import com.servion.model.creditCardPayment.CCPayment_BeneficiaryDetailList_HostRes;
import com.servion.model.creditCardPayment.CCPayment_BenfPayeeDetails_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class CardPaymentThirdPartyWithinBMImpl implements ICardPaymentThirdPartyWithinBM{
	
	private static Logger logger = LoggerObject.getLogger();
	
	private BeneficiaryDtlsInquiryDAO beneficiaryDtlsInquiryDAO;
	private ListBeneficiaryDAO listBeneficiaryDAO;
	
	public BeneficiaryDtlsInquiryDAO getBeneficiaryDtlsInquiryDAO() {
		return beneficiaryDtlsInquiryDAO;
	}

	public void setBeneficiaryDtlsInquiryDAO(
			BeneficiaryDtlsInquiryDAO beneficiaryDtlsInquiryDAO) {
		this.beneficiaryDtlsInquiryDAO = beneficiaryDtlsInquiryDAO;
	}

	public ListBeneficiaryDAO getListBeneficiaryDAO() {
		return listBeneficiaryDAO;
	}

	public void setListBeneficiaryDAO(ListBeneficiaryDAO listBeneficiaryDAO) {
		this.listBeneficiaryDAO = listBeneficiaryDAO;
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
	public String getCCPWithinBMBeneficiaryAccNoPhrases(CallInfo callInfo)
			throws ServiceException {
		
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CardPaymentThirdPartyWithinBMImpl.getCCPWithinBMBeneficiaryAccNoPhrases()");}
		String str_GetMessage, finalResult;
		
		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			
			
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ICEFeatureData object is null / empty");
			}
			
			//Need to get the FeatureConfig Data
			HashMap<String, CCPayment_BenefDetails> beneficiaryDtlsMap = null;
			CCPayment_BenefDetails beneficiaryDetails = new CCPayment_BenefDetails();
			
			//Calling the beneficiary Details host service to get all beneficiary details of the available utility type beneficiary ids
			String beneficiaryDetlHostCode = getCCPPayeeDetails(callInfo);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Response code for the BeneficiaryDetailsInq service" + beneficiaryDetlHostCode);}
			
			if(Constants.ONE.equalsIgnoreCase(beneficiaryDetlHostCode)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Failed while calling Beneficiary details host service");}
				throw new ServiceException("Utility_BenfPayeeDetails_HostRes object is null / Empty");
			}
			
			if(!util.isNullOrEmpty(callInfo.getCcPayment_BenfPayeeDetails_HostRes())){
				if(!util.isNullOrEmpty(callInfo.getCcPayment_BenfPayeeDetails_HostRes().getCCPayment_BenefDetailsMap())){
					beneficiaryDtlsMap = callInfo.getCcPayment_BenfPayeeDetails_HostRes().getCCPayment_BenefDetailsMap();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Service Utility Code retrieved from host is "  + beneficiaryDtlsMap);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no Of utility code type available is"  + beneficiaryDtlsMap.size());}
				}else{
					throw new ServiceException("beneficiaryDtlsMap object is null / Empty");
				}
			}else{
				throw new ServiceException("CCPayment beneficiary dtls host access object is null / Empty");
			}
			 
			
			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);
			
			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			int validPayeeCount = Constants.GL_ZERO;
			String temp_Str = Constants.EMPTY_STRING;
			
			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			int temp_MoreCount = int_moreCount - 1;
			
			String benefCCNo = Constants.EMPTY_STRING;
			Iterator iter = beneficiaryDtlsMap.keySet().iterator();
			while(iter.hasNext()) {
			    String key = (String)iter.next();
			    beneficiaryDetails = (CCPayment_BenefDetails)beneficiaryDtlsMap.get(key);
			    if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved Beneficiary details object is "  + beneficiaryDetails);}
			    
			    if(!util.isNullOrEmpty(beneficiaryDetails) && !util.isNullOrEmpty(beneficiaryDetails.getBenefCreditCardNo())){
			    	validPayeeCount++;
			    	temp_Str = beneficiaryDetails.getBenefCreditCardNo();
			    	if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Beneficiary credit card number is "  + util.getSubstring(temp_Str, Constants.GL_FOUR));}

			    	dynamicValueArray.add(temp_Str);
			    	
			    	if(validPayeeCount == temp_MoreCount){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the more option count");}
						moreOption = true;
						callInfo.setField(Field.MOREOPTIONCOUNT, int_moreCount);
					}
			    	

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added Beneficiary CC number in the grammar list "+temp_Str);}
					
					if(util.isNullOrEmpty(grammar)){
						grammar = temp_Str;
					}else{
						grammar = grammar + Constants.COMMA + temp_Str;
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}
			    }
			}
			
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Valid payee account number total count is "+validPayeeCount);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}
			
			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("CREDITCARD_PAYMENT_BENEFICIARY_ACCOUNT_SELECTION_WITHIN_BM");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Credit_Card_Payment_Third_Party_within_BM");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;
			
			/**
			 * Following are the modification done for configuring the more option of menus
			 */
			combinedKey = combinedKey + Constants.UNDERSCORE + int_moreCount;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Combined key along with more count option is "+ combinedKey);}
			//END - Vinoth
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt received from the dynaproperty file is "+totalPrompt);}
			
			
			//Overriding the total prompts, received from the property file
			if(validPayeeCount >int_moreCount){
				totalPrompt = Constants.GL_THREE * validPayeeCount;
				//totalPrompt = totalPrompt + Constants.GL_TWO;
				
				/**
				 * Added to fix the issue
				 */
				int temp1 = validPayeeCount / int_moreCount;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient value is "+temp1);}

				int temp2 =  validPayeeCount % int_moreCount;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Remainder value is "+temp2);}
				if(temp2 == 0){
					temp1--;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient decreased by one "+temp1);}
				}
				totalPrompt = totalPrompt + (temp1*Constants.GL_TWO);
				//END Vinoth
				
			}
			else{
				totalPrompt = Constants.GL_THREE * validPayeeCount;
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}
			
			//To have the property file grammar, need to call that util method here
			//No need

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);
			
			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}
			
			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
			
			//Need to handle if we want to append pipeseperator sign
			if(!util.isNullOrEmpty(finalResult)){
				temp_MoreCount = int_moreCount + 1;
				if(finalResult.contains(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION).trim())){
					finalResult = finalResult.replaceAll(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION),(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION)+Constants.PIPE));
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The Final Result string is after apending pipe seperator is "+finalResult);}
				}
			}
			
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: CardPaymentThirdPartyWithinBMImpl.getCCPWithinBMBeneficiaryAccNoPhrases()");}
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CardPaymentThirdPartyWithinBMImpl.getCCPWithinBMBeneficiaryAccNoPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
			}
		return finalResult;
	}

	@Override
	public String getCCPWithinBMBeneficiaryList(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CardPaymentThirdPartyWithinBMImpl.getCCPWithinBMBeneficiaryList()");}
		String code = Constants.EMPTY_STRING;
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_FeatureData  = (ICEFeatureData) callInfo.getICEFeatureData();
			
			if(util.isNullOrEmpty(ivr_FeatureData)){
				throw new ServiceException("ivr_FeatureData object is null");
			}
			
			
			String customerID = (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer id is "+ customerID);}
			
			if(util.isNullOrEmpty(customerID)){
				throw new ServiceException("customerID value is null");
			}

			String paymentType = (String) ivr_FeatureData.getConfig().getParamValue(Constants.CUI_CCP_PAYMENTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Payment type is "+ paymentType);}
			
			if(util.isNullOrEmpty(paymentType)){
				throw new ServiceException("Payment type is not configured in ICE Feature data");
			}
			
			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerID
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_LISTBENEFICIARY);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
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
			
			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_LISTBENEFICIARY_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_LISTBENEFICIARY_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}

			
			CCPayment_BeneficiaryDetailList_HostRes ccPayment_BeneficiaryDetailList_HostRes = listBeneficiaryDAO.getCCPaymentTWBMBenfListHostRes(callInfo, customerID, paymentType, requestType);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "utility_BeneficiaryDetailList_HostRes Object is :"+ ccPayment_BeneficiaryDetailList_HostRes);}
			callInfo.setCcPayment_BeneficiaryDetailList_HostRe(ccPayment_BeneficiaryDetailList_HostRes);
			code = ccPayment_BeneficiaryDetailList_HostRes.getErrorCode();

			/**
			 * Report Start
			 */
			String hostEndTime = ccPayment_BeneficiaryDetailList_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = ccPayment_BeneficiaryDetailList_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
			
			/****Duplicate RRN Fix 25012016 *****/
			strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerID
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			/************************************/
			
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(ccPayment_BeneficiaryDetailList_HostRes.getErrorDesc()) ?"NA" :ccPayment_BeneficiaryDetailList_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting
			 
			
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for list beneficiary");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The beneficiary id list object is " + ccPayment_BeneficiaryDetailList_HostRes.getBeneficiaryIdList());}
				if(!util.isNullOrEmpty(ccPayment_BeneficiaryDetailList_HostRes.getBeneficiaryIdList()))
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The total number of beneficiary id is :" + ccPayment_BeneficiaryDetailList_HostRes.getBeneficiaryIdList().size());}

			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for List Beneficiary host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + ccPayment_BeneficiaryDetailList_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_LISTBENEFICIARY, ccPayment_BeneficiaryDetailList_HostRes.getHostResponseCode());
			}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: CardPaymentThirdPartyWithinBMImpl.getCCPWithinBMBeneficiaryList()");}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CardPaymentThirdPartyWithinBMImpl.getCCPWithinBMBeneficiaryList() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;		
		
		
	}

	@Override
	public String getCCPWithinBMBeneficiaryPhrases(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CardPaymentThirdPartyWithinBMImpl.getCCPWithinBMBeneficiaryPhrases()");}
		String str_GetMessage, finalResult;
		
		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			
			//Need to get the FeatureConfig Data
			boolean isExistingBenefAvail = false;
			
			if(!util.isNullOrEmpty(callInfo.getCcPayment_BeneficiaryDetailList_HostRe())){
				if(!util.isNullOrEmpty(callInfo.getCcPayment_BeneficiaryDetailList_HostRe().getBeneficiaryIdList())){
					isExistingBenefAvail = callInfo.getCcPayment_BeneficiaryDetailList_HostRe().getBeneficiaryIdList().size() > Constants.GL_ZERO;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is there existing beneficiary available ? " + isExistingBenefAvail);}
				}
			}
				
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}

			
			//Defaultly considering the value as true
			String str_isToAddNewBeneficiary = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_EnableAddingNewBeneficiary))? Constants.TRUE :(String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_EnableAddingNewBeneficiary);
			 boolean isToAddNewBeneficiary = Constants.TRUE.equalsIgnoreCase(str_isToAddNewBeneficiary)?true : false;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is to Add New Beneficary ? "+isToAddNewBeneficiary);}
		
			
			
			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			
			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			
			if(isExistingBenefAvail){
				
				
				if(isToAddNewBeneficiary){
					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1002);
					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_1);
					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1003);
					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_2);
					grammar = Constants.NEW + Constants.COMMA + Constants.EXISTING;
					
				}else{
					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1003);
					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_1);
					dynamicValueArray.add(Constants.NA);
					dynamicValueArray.add(Constants.NA);
					grammar = Constants.EXISTING;
					
				}
			}else{
				if(isToAddNewBeneficiary){
				dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1002);
				dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_1);
				grammar = Constants.NEW;
				}else{
					throw new ServiceException("There is no any registered or add beneficiary option for this feature");
				}
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}
			
			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("CREDITCARD_PAYMENT_BENEFICIARY_WITHIN_BM");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Credit_Card_Payment_Third_Party_within_BM");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt received from the dynaproperty file is "+totalPrompt);}
			
			
			//Overriding the total prompts, received from the property file
			if(isExistingBenefAvail){
				totalPrompt = Constants.GL_FOUR;
			}
			else{
				totalPrompt = Constants.GL_TWO;
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}
			
			//To have the property file grammar, need to call that util method here
			//No Need

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);
			
			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}
			
			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
			
			
			//Need to handle if we want to append pipeseperator sign
			//No Need
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: CardPaymentThirdPartyWithinBMImpl.getCCPWithinBMBeneficiaryPhrases()");}
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CardPaymentThirdPartyWithinBMImpl.getCCPWithinBMBeneficiaryPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
			}
		return finalResult;
	}

	public String getCCPPayeeDetails(CallInfo callInfo)throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: CardPaymentThirdPartyWithinBMImpl.getCCPPayeeDetails()");}
		String code = Constants.EMPTY_STRING;
		try{
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to call Beneficiary Detail Enquiry Service");}

			String customerID = (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer id is "+ customerID);}
			
			if(util.isNullOrEmpty(customerID)){
				throw new ServiceException("customerID value is null");
			}
			
			ArrayList<String> beneficiaryIdList = null;
			if(!util.isNullOrEmpty(callInfo.getCcPayment_BeneficiaryDetailList_HostRe())){
				if(!util.isNullOrEmpty(callInfo.getCcPayment_BeneficiaryDetailList_HostRe().getBeneficiaryIdList())){
					beneficiaryIdList = callInfo.getCcPayment_BeneficiaryDetailList_HostRe().getBeneficiaryIdList();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The beneficiary ID list received is " + beneficiaryIdList);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The number of beneficiary id records are" + beneficiaryIdList.size());}
					
				}
			}
			if(!util.isNullOrEmpty(beneficiaryIdList)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total Beneficiary ID available is " +beneficiaryIdList.size() );}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Available Beneficiaries are " +beneficiaryIdList);}
				
			}
			
			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetailsForSecHost = new HostReportDetails();

			String featureIdForSecHost = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetailsForSecHost.setHostActiveMenu(featureIdForSecHost);
			String strHostInParamForSecHost = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerID
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetailsForSecHost.setHostInParams(strHostInParamForSecHost);
			hostReportDetailsForSecHost.setHostMethod(Constants.HOST_METHOD_BENEFICARYDTLSINQUIRY);
			//hostReportDetailsForSecHost.setHostOutParams(hostOutParams);
			hostReportDetailsForSecHost.setHostReserve1(Constants.NA);
			hostReportDetailsForSecHost.setHostReserve2(Constants.NA);
			hostReportDetailsForSecHost.setHostReserve3(Constants.NA);
			hostReportDetailsForSecHost.setHostReserve4(Constants.NA);

			hostReportDetailsForSecHost.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
			hostReportDetailsForSecHost.setHostType(Constants.HOST_TYPE_ACCOUNTSERVICES);
			//End Reporting
			
			/*
			 *  Setting NA values
			 */
			hostReportDetailsForSecHost.setHostEndTime(util.getCurrentDateTime());
			hostReportDetailsForSecHost.setHostOutParams(Constants.NA);
			hostReportDetailsForSecHost.setHostResponse(Constants.NA);
			
			callInfo.setHostReportDetails(hostReportDetailsForSecHost);
			IvrData ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
			callInfo.insertHostDetails(ivrdata);
			
			/* END */
			
			
			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			
			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}
			
			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_BENEFICIARYDELSINQUIRY_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_BENEFICIARYDELSINQUIRY_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}

			
			CCPayment_BenfPayeeDetails_HostRes ccPayment_BenfPayeeDetails_HostRes = beneficiaryDtlsInquiryDAO.getCCPaymentTPWBMBenfDelsHostRes(callInfo, beneficiaryIdList,requestType);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "ccPayment_BenfPayeeDetails_HostRes Object is :"+ ccPayment_BenfPayeeDetails_HostRes);}
			callInfo.setCcPayment_BenfPayeeDetails_HostRes(ccPayment_BenfPayeeDetails_HostRes);
			code = ccPayment_BenfPayeeDetails_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */
			String hostEndTimeForSecHost = ccPayment_BenfPayeeDetails_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTimeForSecHost);}
			hostReportDetailsForSecHost.setHostEndTime(hostEndTimeForSecHost);

			String hostResCodeForSecHost = ccPayment_BenfPayeeDetails_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCodeForSecHost);}
			hostReportDetailsForSecHost.setHostResponse(hostResCodeForSecHost);

			String responseDescForSecHost = Constants.HOST_FAILURE;
			
			
			/**
			 * Updating the isOTPCalledAfterDisconnect flag as false
			 */
			callInfo.setField(Field.ISOTPCALLEDAFTERDISCONNECT, false);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "updating Is OTP Called After Disconnected " +false );}
			//END
			
			
			
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDescForSecHost = Constants.HOST_SUCCESS;
			}
			
			/****Duplicate RRN Fix 25012016 *****/
			strHostInParamForSecHost = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerID
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetailsForSecHost.setHostInParams(strHostInParamForSecHost);
			/************************************/
			
			String hostOutputParamForSecHost = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDescForSecHost +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCodeForSecHost
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(ccPayment_BenfPayeeDetails_HostRes.getErrorDesc()) ?"NA" :ccPayment_BenfPayeeDetails_HostRes.getErrorDesc());
			hostReportDetailsForSecHost.setHostOutParams(hostOutputParamForSecHost);

			callInfo.setHostReportDetails(hostReportDetailsForSecHost);
			IvrData ivrdataForSecHost = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdataForSecHost);
			//End Reporting
			
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got success response for Beneficiary Details");}
			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for calleridentification host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + ccPayment_BenfPayeeDetails_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_BENEFICARYDTLSINQUIRY, ccPayment_BenfPayeeDetails_HostRes.getHostResponseCode());
			}
			
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: CardPaymentThirdPartyWithinBMImpl.getCCPPayeeDetails()");}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CardPaymentThirdPartyWithinBMImpl.getCCPPayeeDetails() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}
}
