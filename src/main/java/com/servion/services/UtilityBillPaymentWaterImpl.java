package com.servion.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.BeneficiaryDtlsInquiryDAO;
import com.servion.dao.GetUtilityBillInfoDAO;
import com.servion.dao.ListBeneficiaryDAO;
import com.servion.dao.UtilitySubscriberInfoDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.MenuIDMap;
import com.servion.model.billPayment.GetUtilityBillInfo_HostRes;
import com.servion.model.billPayment.UtilitySubscriberInfo_HostRes;
import com.servion.model.billPayment.Utility_BeneficiaryDetailList_HostRes;
import com.servion.model.billPayment.Utility_BeneficiaryDetails;
import com.servion.model.billPayment.Utility_BeneficiaryShortDetails;
import com.servion.model.billPayment.Utility_BenfPayeeDetails_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class UtilityBillPaymentWaterImpl implements IUtilityBillPaymentWater{
	
	private static Logger logger = LoggerObject.getLogger();
	private ListBeneficiaryDAO listBeneficiaryDAO;
	private BeneficiaryDtlsInquiryDAO beneficiaryDtlsInquiryDAO;
	
	private UtilitySubscriberInfoDAO utilitySubscriberInfoDAO;
	
	public UtilitySubscriberInfoDAO getUtilitySubscriberInfoDAO() {
		return utilitySubscriberInfoDAO;
	}

	public void setUtilitySubscriberInfoDAO(
			UtilitySubscriberInfoDAO utilitySubscriberInfoDAO) {
		this.utilitySubscriberInfoDAO = utilitySubscriberInfoDAO;
	}
	
	private GetUtilityBillInfoDAO getUtilityBillInfoDAO;
	
	public GetUtilityBillInfoDAO getGetUtilityBillInfoDAO() {
		return getUtilityBillInfoDAO;
	}

	public void setGetUtilityBillInfoDAO(
			GetUtilityBillInfoDAO getUtilityBillInfoDAO) {
		this.getUtilityBillInfoDAO = getUtilityBillInfoDAO;
	}
	
	public ListBeneficiaryDAO getListBeneficiaryDAO() {
		return listBeneficiaryDAO;
	}

	public void setListBeneficiaryDAO(ListBeneficiaryDAO listBeneficiaryDAO) {
		this.listBeneficiaryDAO = listBeneficiaryDAO;
	}

	public BeneficiaryDtlsInquiryDAO getBeneficiaryDtlsInquiryDAO() {
		return beneficiaryDtlsInquiryDAO;
	}

	public void setBeneficiaryDtlsInquiryDAO(
			BeneficiaryDtlsInquiryDAO beneficiaryDtlsInquiryDAO) {
		this.beneficiaryDtlsInquiryDAO = beneficiaryDtlsInquiryDAO;
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
	public String getWaterBillAccountNumberMenuPhrases(CallInfo callInfo)
			throws ServiceException {
		
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentWaterImpl.getWaterBillAccountNumberMenuPhrases()");}
		String str_GetMessage, finalResult;
		
		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();
			
			
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ICEFeatureData object is null / empty");
			}
			//Need to get the FeatureConfig Data
			String selectedServiceProvider = (String)callInfo.getField(Field.SELECTEDSERVICEPROVIDER);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected service provider is  "+selectedServiceProvider);}
			
			
			HashMap<String, ArrayList<Utility_BeneficiaryDetails>> serviceProviderCodeMap = null;
			ArrayList<Utility_BeneficiaryDetails> beneficiaryDetailsList = new ArrayList<Utility_BeneficiaryDetails>();
			
			//Calling the beneficiary Details host service to get all beneficiary details of the available utility type beneficiary ids
			String beneficiaryDetlHostCode = getWaterBillPayeeDetail(callInfo);
			
			if(Constants.ONE.equalsIgnoreCase(beneficiaryDetlHostCode)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Failed while calling Beneficiary details host service");}
				throw new ServiceException("Utility_BenfPayeeDetails_HostRes object is null / Empty");
			}
			
			if(!util.isNullOrEmpty(callInfo.getUtility_BenfPayeeDetails_HostRes())){
				if(!util.isNullOrEmpty(callInfo.getUtility_BenfPayeeDetails_HostRes().getUtility_serviceProviderMap())){
					serviceProviderCodeMap = callInfo.getUtility_BenfPayeeDetails_HostRes().getUtility_serviceProviderMap();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Service Utility Code retrieved from host is "  + serviceProviderCodeMap);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no Of utility code type available is"  + serviceProviderCodeMap.size());}
				}else{
					throw new ServiceException("Utility code Map object is null / Empty");
				}
			}else{
				throw new ServiceException("Utility bill host access object is null / Empty");
			}
			 
			
			beneficiaryDetailsList = serviceProviderCodeMap.get(selectedServiceProvider);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Received Beneficiary Payee Details list is"+beneficiaryDetailsList);}
			
			
			if(util.isNullOrEmpty(beneficiaryDetailsList)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The beneficiaryDetailsList is null / empty");}
				throw new ServiceException("no record found for the selected uitily code type");
			}
			
			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);
			
			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;
			Utility_BeneficiaryDetails temp_benefDetail = null;
			int validPayeeCount = Constants.GL_ZERO;
			
			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			int temp_MoreCount = int_moreCount - 1;
			
			for(int count=Constants.GL_ZERO;count<beneficiaryDetailsList.size();count++){
				temp_benefDetail = beneficiaryDetailsList.get(count);
				
				if(!util.isNullOrEmpty(temp_benefDetail)){
					validPayeeCount++;
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The "+count+" beneficiary account number is " +temp_benefDetail.getBenefContractNo());}

					temp_Str = temp_benefDetail.getBenefContractNo();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Water bill account number is "+temp_Str);}
					
					//vijay
					dynamicValueArray.add((selectedServiceProvider+Constants.WAV_EXTENSION).trim());
					dynamicValueArray.add((temp_Str));
					
					if(count == temp_MoreCount){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the more option count");}
						moreOption = true;
						callInfo.setField(Field.MOREOPTIONCOUNT, int_moreCount);
					}
					
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added Beneficiary number in the grammar list "+temp_Str);}
					
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

			String menuID = MenuIDMap.getMenuID("WATER_NUMBER_SELECTION");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Utility_Bill_Payment_Water");
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
			
			
			if(Constants.ALPHA_H.equalsIgnoreCase(languageKey) || Constants.hindi.equalsIgnoreCase(languageKey)){
				
				//Overriding the total prompts, received from the property file
				if(validPayeeCount >int_moreCount){
					totalPrompt = Constants.GL_SIX * int_moreCount;
					totalPrompt = totalPrompt + Constants.GL_TWO;
//					/**
//					 * Added to fix the issue
//					 */
//					int temp1 = validPayeeCount / int_moreCount;
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient value is "+temp1);}
	//
//					int temp2 =  validPayeeCount % int_moreCount;
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Remainder value is "+temp2);}
//					if(temp2 > 0){
//						temp1++;
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient increased by one "+temp1);}
//					}
//					totalPrompt = totalPrompt + (temp1*Constants.GL_TWO);
//					//END Vinoth
					
				}
				else{
					totalPrompt = Constants.GL_SIX * validPayeeCount;
				}
				
			}else{
				
				//Overriding the total prompts, received from the property file
				if(validPayeeCount >int_moreCount){
					totalPrompt = Constants.GL_FIVE * int_moreCount;
					totalPrompt = totalPrompt + Constants.GL_TWO;
//					/**
//					 * Added to fix the issue
//					 */
//					int temp1 = validPayeeCount / int_moreCount;
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient value is "+temp1);}
	//
//					int temp2 =  validPayeeCount % int_moreCount;
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Remainder value is "+temp2);}
//					if(temp2 > 0){
//						temp1++;
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient increased by one "+temp1);}
//					}
//					totalPrompt = totalPrompt + (temp1*Constants.GL_TWO);
//					//END Vinoth
					
				}
				else{
					totalPrompt = Constants.GL_FIVE * validPayeeCount;
				}
				
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
			
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UtilityBillPaymentWaterImpl.getWaterBillAccountNumberMenuPhrases()");}
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UtilityBillPaymentWaterImpl.getWaterBillAccountNumberMenuPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
			}
		return finalResult;
	}

	@Override
	public String getWaterBillPayeeList(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentWaterImpl.getWaterBillPayeeList()");}
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

			String paymentType = (String) ivr_FeatureData.getConfig().getParamValue(Constants.CUI_UBP_PAYMENTTYPE);
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
			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerID;
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

			/*
			 * Ooredoo change on 18-Apr-2017
			 */
			Utility_BeneficiaryDetailList_HostRes utility_BeneficiaryDetailList_HostRes = listBeneficiaryDAO.getMobBroadBenfListHostRes(callInfo, customerID, paymentType, requestType, beneficiaryDtlsInquiryDAO);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "utility_BeneficiaryDetailList_HostRes Object is :"+ utility_BeneficiaryDetailList_HostRes);}
			callInfo.setUtility_BeneficiaryDetailList_HostRes(utility_BeneficiaryDetailList_HostRes);
			code = utility_BeneficiaryDetailList_HostRes.getErrorCode();

			/**
			 * Report Start
			 */
			String hostEndTime = utility_BeneficiaryDetailList_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = utility_BeneficiaryDetailList_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(utility_BeneficiaryDetailList_HostRes.getErrorDesc()) ?"NA" :utility_BeneficiaryDetailList_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting
			 
			
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for list beneficiary");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The beneficiary id list object is " + utility_BeneficiaryDetailList_HostRes.getBeneficiaryIdList());}
				if(!util.isNullOrEmpty(utility_BeneficiaryDetailList_HostRes.getBeneficiaryIdList()))
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The total number of beneficiary id is :" + utility_BeneficiaryDetailList_HostRes.getBeneficiaryIdList().size());}

			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for List Beneficiary host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + utility_BeneficiaryDetailList_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_LISTBENEFICIARY, utility_BeneficiaryDetailList_HostRes.getHostResponseCode());
			}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UtilityBillPaymentWaterImpl.getWaterBillPayeeList()");}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UtilityBillPaymentWaterImpl.getWaterBillPayeeList() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;		
		
		
	}

	@Override
	public String getWaterBillServiceProvidersPhrases(CallInfo callInfo)
			throws ServiceException {
		
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentWaterImpl.getWaterBillServiceProvidersPhrases()");}
		String str_GetMessage, finalResult;
		
		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			ArrayList<String> serviceProviderCodeList = new ArrayList<String>();
			//Need to get the FeatureConfig Data
			
			ICEFeatureData iceFeatureData = callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("ICEFeature Date object is null / Empty");
			}
			
			String hostUtilityCodeList = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_APPLICABLE_UTILITY_CODE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Utility Code list retrieved from UI is "  + hostUtilityCodeList);}
			
			if(util.isNullOrEmpty(hostUtilityCodeList)){
				throw new ServiceException("Utility Code List is not configured in the UI");
			}
			
			if(util.isNullOrEmpty(callInfo.getUtility_BeneficiaryDetailList_HostRes()) || util.isNullOrEmpty(callInfo.getUtility_BeneficiaryDetailList_HostRes().getUtilityCodeList())){
				throw new ServiceException("Utilit code from list beneficiary host is null or empty");
			}
			
			
			
			if(util.isNullOrEmpty(callInfo.getUtility_BeneficiaryDetailList_HostRes())
					|| util.isNullOrEmpty(callInfo.getUtility_BeneficiaryDetailList_HostRes().getUtilityCodeMap())
					|| util.isNullOrEmpty(callInfo.getUtility_BeneficiaryDetailList_HostRes().getServiceCodeMap())){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Beneficiary Host Detail List is null or empty"  + callInfo.getUtility_BeneficiaryDetailList_HostRes());}
				return Constants.EMPTY_STRING;
			}
			
			
			HashMap<String, ArrayList<String>> utilityServiceCodeMap = callInfo.getUtility_BeneficiaryDetailList_HostRes().getUtilityCodeMap();
			ArrayList<String> serviceProviderCodeListFromHost = null;
			ArrayList<String> utilityCodeListFrmHost = callInfo.getUtility_BeneficiaryDetailList_HostRes().getUtilityCodeList();
			
			String[] strHostArrayList = hostUtilityCodeList.split(Constants.COMMA);
			String strServiceProviderList = Constants.EMPTY_STRING;
			String strUtilityCode = Constants.EMPTY_STRING;
			String strServiceCode = Constants.EMPTY_STRING;
			boolean isServiceCodeAvailable = false;
			
			for(int i=0; i<strHostArrayList.length;i++){
				strUtilityCode = strHostArrayList[i];
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The utility code is "  + strUtilityCode);}
				
				if(utilityCodeListFrmHost.contains(strUtilityCode)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The list beneficiary utlity code is applicable"  + strUtilityCode);}
					strServiceProviderList = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_SERVICEPROVIDER_CODE+strUtilityCode);
					
					if(util.isNullOrEmpty(strServiceProviderList)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured service provider code at UI is null / empty"  + strServiceProviderList);}
						return Constants.EMPTY_STRING;
					}
					
					serviceProviderCodeListFromHost = utilityServiceCodeMap.get(strUtilityCode);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved Service Provide code list for the Utility code "+strUtilityCode+" is "  + serviceProviderCodeListFromHost);}
					
					
					if(util.isNullOrEmpty(serviceProviderCodeListFromHost)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured service provider List from host is null / empty"  + serviceProviderCodeListFromHost);}
						return Constants.EMPTY_STRING;
					}
					
					for(int j=0; j < serviceProviderCodeListFromHost.size(); j++){
						strServiceCode = serviceProviderCodeListFromHost.get(j);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Service code is "  + strServiceCode);}
						
						
						isServiceCodeAvailable = util.isCodePresentInTheConfigurationList(strServiceCode, strServiceProviderList);
						if(isServiceCodeAvailable){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the service provider code into the arraylist object");}
							serviceProviderCodeList.add(strServiceCode);
						}
					}
					
//					
//					serviceProviderArr = strServiceProviderList.split(Constants.COMMA);
//					
//					for(int j=0; j<serviceProviderArr.length;j++){
//						strServiceCode = serviceProviderArr[j];
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The service provider code for the above utility code" + strUtilityCode+" is"  + strServiceCode);}
//						serviceProviderCodeList.add(strServiceCode);
//					}
				}
			}
			
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The final service provider list going to read to customer is "  + serviceProviderCodeList);}
			
//			
//				if(!util.isNullOrEmpty(callInfo.getUtility_BeneficiaryDetailList_HostRes())){
//					if(!util.isNullOrEmpty(callInfo.getUtility_BeneficiaryDetailList_HostRes().getUtilityCodeList())){
//						utilityCodeList = callInfo.getUtility_BeneficiaryDetailList_HostRes().getUtilityCodeList();
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Utility Code list retrieved from host is "  + utilityCodeList);}
//						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no Of utility codes available are"  + utilityCodeList.size());}
//					}
//				}else{
//					throw new ServiceException("Utility code list object is null / Empty");
//				}
//			 
			
			/**
			 * Note temp_Str is nothing but the product name.  The wave file also should recorded in the same product name
			 * 
			 * eg WaterServType --> WaterServType.wav or (XYZ -- > XYZ.wav)
			 * 
			 */
			
			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);
			
			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;
			
			int temp_MoreCount = int_moreCount - 1;
			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			for(int count=Constants.GL_ZERO;count<serviceProviderCodeList.size();count++){
				temp_Str = serviceProviderCodeList.get(count);
				dynamicValueArray.add((temp_Str+Constants.WAV_EXTENSION).trim());
				
				if(count == temp_MoreCount){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the more option count");}
					moreOption = true;
					callInfo.setField(Field.MOREOPTIONCOUNT, int_moreCount);
				}
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added Product type "+temp_Str);}
				
				if(util.isNullOrEmpty(grammar)){
					grammar = temp_Str;
				}else{
					grammar = grammar + Constants.COMMA + temp_Str;
				}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}

			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}
			
			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("WATER_SERVICEPRIVIDER_SELECTION");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Utility_Bill_Payment_Water");
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
			if(serviceProviderCodeList.size()>int_moreCount){
				totalPrompt = Constants.GL_THREE * int_moreCount;
//				totalPrompt = totalPrompt + Constants.GL_TWO;
				
				/**
				 * Added to fix the issue
				 */
				int temp1 = serviceProviderCodeList.size() / int_moreCount;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient value is "+temp1);}

//				int temp2 =  serviceProviderCodeList.size() % int_moreCount;
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Remainder value is "+temp2);}
//				if(temp2 > 0){
//					temp1++;
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient increased by one "+temp1);}
//				}
				totalPrompt = totalPrompt + (temp1*Constants.GL_TWO);
				//END Vinoth
				
			}
			else{
				totalPrompt = Constants.GL_THREE * serviceProviderCodeList.size();
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}
			
			//To have the property file grammar, need to call that util method here
			//No need here

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
			
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT:  UtilityBillPaymentWaterImpl.getWaterBillServiceProvidersPhrases()");}
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at  UtilityBillPaymentWaterImpl.getWaterBillServiceProvidersPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
			}
		return finalResult;
	}

	@Override
	public String getWaterExstOrAddBenfPhrases(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentWaterImpl.getWaterExstOrAddBenfPhrases()");}
		String str_GetMessage, finalResult;
		
		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			
			//Need to get the FeatureConfig Data
			boolean isExistingBenefAvail = false;
			
//			if(!util.isNullOrEmpty(callInfo.getUtility_BeneficiaryDetailList_HostRes())){
//				if(!util.isNullOrEmpty(callInfo.getUtility_BeneficiaryDetailList_HostRes().getBeneficiaryIdList())){
//					isExistingBenefAvail = callInfo.getUtility_BeneficiaryDetailList_HostRes().getBeneficiaryIdList().size() > Constants.GL_ZERO;
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is there existing beneficiary available ? " + isExistingBenefAvail);}
//				}
//			}
//				
			
			ArrayList<String>tempArrayList =  null;
			String strUtilityCode = Constants.EMPTY_STRING;
			if( callInfo.getUtility_BeneficiaryDetailList_HostRes() != null &&  callInfo.getUtility_BeneficiaryDetailList_HostRes().getUtilityCodeList()!=null){

				ICEFeatureData iceFeatureData = callInfo.getICEFeatureData();
				String hostUtilityCodeList = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_APPLICABLE_UTILITY_CODE);

				tempArrayList = callInfo.getUtility_BeneficiaryDetailList_HostRes().getUtilityCodeList();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The utility Code list from the beneficiary list host service is " + tempArrayList.size());}

				for(int i=0; i<tempArrayList.size();i++){
					strUtilityCode = tempArrayList.get(i);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The utility code is " + strUtilityCode);}
					if(util.isCodePresentInTheConfigurationList(strUtilityCode,  hostUtilityCodeList)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The utility type  " + strUtilityCode +" is in the UI list ");}
						isExistingBenefAvail = true;
						break;
					}

				}

			}
			
			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			
			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}

			//Defaultly considering the value as true
			String str_isToAddNewBeneficiary = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_EnableAddingNewBeneficiary))? Constants.TRUE :(String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_EnableAddingNewBeneficiary);
			 boolean isToAddNewBeneficiary = Constants.TRUE.equalsIgnoreCase(str_isToAddNewBeneficiary)?true : false;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is to Add New Beneficary ? "+isToAddNewBeneficiary);}
		
			
			if(isExistingBenefAvail){
				
				if(isToAddNewBeneficiary){
					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1003);
					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_1);
					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1002);
					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_2);
					grammar = Constants.EXISTING + Constants.COMMA + Constants.NEW;
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
					throw new ServiceException("There is no any registered or add benef option for this featuer");
				}
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}
			
			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("WATER_BENEFICIARY");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Utility_Bill_Payment_Water");
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
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UtilityBillPaymentWaterImpl.getWaterExstOrAddBenfPhrases()");}
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UtilityBillPaymentWaterImpl.getWaterExstOrAddBenfPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
			}
		return finalResult;
	}

	@Override
	public String getWaterBillPayeeDetail(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentWaterImpl.getWaterBillPayeeDetail()");}
		String code = Constants.EMPTY_STRING;
		try{
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to call Beneficiary Detail Enquiry Service");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			String customerID = (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer id is "+ customerID);}

			if(util.isNullOrEmpty(customerID)){
				throw new ServiceException("customerID value is null");
			}

			ArrayList<Utility_BeneficiaryShortDetails> utility_BeneficiaryShortDetailsList = null;

			HashMap<String, ArrayList<Utility_BeneficiaryShortDetails>>	serviceProviderCodeMap = callInfo.getUtility_BeneficiaryDetailList_HostRes().getServiceCodeMap(); 
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Retrieved service Provider codes are " + serviceProviderCodeMap);}

			if(util.isNullOrEmpty(serviceProviderCodeMap)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Service provide code map is null or empty");}
				throw new ServiceException("Service provide code map is null or empty");
			}

			String selectedServiceProvider = (String)callInfo.getField(Field.SELECTEDSERVICEPROVIDER);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Service Provider code is " + selectedServiceProvider);}

			if(util.isNullOrEmpty(selectedServiceProvider)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Service provide code is null or empty");}
				throw new ServiceException("Service provide code is null or empty");
			}

			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(iceFeatureData)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "iceFeatureData object is null or empty");}
				throw new ServiceException("iceFeatureData object is null or empty");
			}

			String hostUtilityCodeList = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_APPLICABLE_UTILITY_CODE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Available Utility code list from UI is " + hostUtilityCodeList );}

			if(util.isNullOrEmpty(hostUtilityCodeList)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Utility code list configured at UI is null or empty");}
				throw new ServiceException("Host Utility code list configured at UI is null or empty");
			}



			utility_BeneficiaryShortDetailsList = serviceProviderCodeMap.get(selectedServiceProvider);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved utility Beneficiary Short Details is " + utility_BeneficiaryShortDetailsList);}


			if(util.isNullOrEmpty(utility_BeneficiaryShortDetailsList)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "utility_BeneficiaryShortDetailsList retrieved is null or empty");}
				throw new ServiceException("utility_BeneficiaryShortDetailsList retrieved is null or empty");
			}

			String benefUtilityCode = Constants.EMPTY_STRING;
			ArrayList<String> beneficiaryIdList = new ArrayList<>();
			Utility_BeneficiaryShortDetails utility_BeneficiaryShorDetails = null;
			for(int i=0; i<utility_BeneficiaryShortDetailsList.size(); i++){
				utility_BeneficiaryShorDetails = utility_BeneficiaryShortDetailsList.get(i);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved Utility Short Details object is " +utility_BeneficiaryShorDetails );}

				if(!util.isNullOrEmpty(utility_BeneficiaryShorDetails)){

					benefUtilityCode = utility_BeneficiaryShorDetails.getUtilityCode();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Beneficiary utility code is " +benefUtilityCode );}

					if(util.isCodePresentInTheConfigurationList(benefUtilityCode, hostUtilityCodeList)){
						beneficiaryIdList.add(utility_BeneficiaryShorDetails.getBeneficiaryId());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding Beneficiary id " +utility_BeneficiaryShorDetails.getBeneficiaryId() );}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The number of beneficiary id records are" + beneficiaryIdList.size());}
					}
				}
			}



			//			ArrayList<String> beneficiaryIdList = null;
			//			if(!util.isNullOrEmpty(callInfo.getUtility_BeneficiaryDetailList_HostRes())){
			//				if(!util.isNullOrEmpty(callInfo.getUtility_BeneficiaryDetailList_HostRes().getBeneficiaryIdList())){
			//					beneficiaryIdList = callInfo.getUtility_BeneficiaryDetailList_HostRes().getBeneficiaryIdList();
			//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The beneficiary ID list received is " + beneficiaryIdList);}
			//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The number of beneficiary id records are" + beneficiaryIdList.size());}
			//					
			//				}
			//			}
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
			IvrData ivrdataForSecHost = (IvrData)callInfo.getField(Field.IVRDATA);
			callInfo.insertHostDetails(ivrdataForSecHost);
			
			/* END */




			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}

			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_BENEFICIARYDELSINQUIRY_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_BENEFICIARYDELSINQUIRY_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}

			//Modified by Vinoth on 11-Sep-2014 to call water beneficiary module
			//Utility_BenfPayeeDetails_HostRes utility_BenfPayeeDetails_HostRes = beneficiaryDtlsInquiryDAO.getMobBroadBandBenfDelsHostRes(callInfo, beneficiaryIdList, requestType);
			Utility_BenfPayeeDetails_HostRes utility_BenfPayeeDetails_HostRes = beneficiaryDtlsInquiryDAO.getWaterBillBenfDelsHostRes(callInfo, beneficiaryIdList, requestType);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "utility_BenfPayeeDetails_HostRes Object is :"+ utility_BenfPayeeDetails_HostRes);}
			callInfo.setUtility_BenfPayeeDetails_HostRes(utility_BenfPayeeDetails_HostRes);
			code = utility_BenfPayeeDetails_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */
			String hostEndTimeForSecHost = utility_BenfPayeeDetails_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTimeForSecHost);}
			hostReportDetailsForSecHost.setHostEndTime(hostEndTimeForSecHost);

			String hostResCodeForSecHost = utility_BenfPayeeDetails_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCodeForSecHost);}
			hostReportDetailsForSecHost.setHostResponse(hostResCodeForSecHost);

			String responseDescForSecHost = Constants.HOST_FAILURE;
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
			+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(utility_BenfPayeeDetails_HostRes.getErrorDesc()) ?"NA" :utility_BenfPayeeDetails_HostRes.getErrorDesc());
			hostReportDetailsForSecHost.setHostOutParams(hostOutputParamForSecHost);

			callInfo.setHostReportDetails(hostReportDetailsForSecHost);
			ivrdataForSecHost = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.insertHostDetails(ivrdataForSecHost);
			//End Reporting

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got success response for Beneficiary Details");}
			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for calleridentification host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + utility_BenfPayeeDetails_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_BENEFICARYDTLSINQUIRY, utility_BenfPayeeDetails_HostRes.getHostResponseCode());
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UtilityBillPaymentWaterImpl.getWaterBillPayeeDetail()");}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UtilityBillPaymentWaterImpl.getWaterBillPayeeDetail() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;

	}
	
	@Override
	public String getWaterBalanceDetails(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, session_ID_,"ENTER: UtilityBillPaymentWaterImpl.getWaterBalanceDetails()");}
		String code = Constants.ONE;
		String requestType = Constants.EMPTY_STRING, providerType = Constants.EMPTY_STRING, dueAmount = Constants.EMPTY_STRING;
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Global Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			String contractNo = util.isNullOrEmpty(callInfo.getField(Field.CONTRACTNO))? Constants.EMPTY_STRING : (String)callInfo.getField(Field.CONTRACTNO);
			if(util.isNullOrEmpty(contractNo)){
				throw new ServiceException("Contract no is not setted while registering this beneficiary, the contract no object is null / EMpty");
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "enteredContractNumber" + contractNo);}

			String serviceProviderCode = util.isNullOrEmpty(callInfo.getField(Field.SELECTEDSERVICEPROVIDER))?Constants.EMPTY_STRING : (String)callInfo.getField(Field.SELECTEDSERVICEPROVIDER);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Selected service provider code is " + serviceProviderCode);}

			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}

			boolean isForOIFCService = false;
			
			if(!util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_OIFC_SERVICE_PROVIDER_CODES))){
				String oifcServProviderCodes = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_OIFC_SERVICE_PROVIDER_CODES);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Configured OIFC Service Provider Codes are " + oifcServProviderCodes);}
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "And the user selected Service provider code is " + serviceProviderCode);}
				
				isForOIFCService = util.isCodePresentInTheConfigurationList(serviceProviderCode, oifcServProviderCodes);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Has the user selected service provider is an OIFC ?" + isForOIFCService);}
			}
			
			
		
			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
			String strHostInParam = Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + contractNo;
			hostReportDetails.setHostInParams(strHostInParam);
		
			if(isForOIFCService){
				hostReportDetails.setHostMethod(Constants.HOST_METHOD_UTILITYSUBSCRIBERINFO);
			}else{
				hostReportDetails.setHostMethod(Constants.HOST_METHOD_GETUTILITYBILLINFO);
			}
			
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

			String startTime = util.getCurrentDateTime();
			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the format of 31/07/2013 18:11:11
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
			
			
			String utilityCode = (String)callInfo.getField(Field.UTILITYCODE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The user selected utility code is " + utilityCode);}
			
			if(isForOIFCService){
				callInfo.setField(Field.SELECTEDSERVPRODTYPE, Constants.OIFC);
				requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_UTILITYOFICBALANCE_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_UTILITYOFICBALANCE_REQUESTTYPE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "requestType configured is " + requestType);}

				providerType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_UTILITYOFICBALANCE_PROVIDERTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_UTILITYOFICBALANCE_PROVIDERTYPE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Provider type configured is " + providerType);}
				
				
				UtilitySubscriberInfo_HostRes utilityBalanceDetails_HostRes = utilitySubscriberInfoDAO.getUtilitySubscriberInfo_HostRes(callInfo, requestType, providerType, serviceProviderCode, utilityCode, contractNo, false, null);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "utilityBalanceDetails_HostRes Object is :"+ utilityBalanceDetails_HostRes);}
				callInfo.setUtilitySubscriberInfo_HostRes(utilityBalanceDetails_HostRes);

				code = utilityBalanceDetails_HostRes.getErrorCode();

				/*
				 * For Reporting Start
				 */
				
				String hostEndTime = utilityBalanceDetails_HostRes.getHostEndTime();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Actual Host End time is is " + hostEndTime);}
				hostReportDetails.setHostEndTime(hostEndTime);

				String hostResCode = utilityBalanceDetails_HostRes.getHostResponseCode();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Actual Host response code is " + hostResCode);}
				hostReportDetails.setHostResponse(hostResCode);

				String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "Host Access duration is " + durationTime);}
			
				String responseDesc = Constants.HOST_FAILURE;
				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					responseDesc = Constants.HOST_SUCCESS;
				}
				
				String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
						+ Constants.EQUALTO + hostResCode;

				hostReportDetails.setHostOutParams(hostOutputParam);
				
				callInfo.setHostReportDetails(hostReportDetails);
				ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

				callInfo.updateHostDetails(ivrdata);
				//End Reporting

				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for the service Utilitysubscriber");}
					
					if( callInfo.getUtilitySubscriberInfo_HostRes() != null &&  callInfo.getUtilitySubscriberInfo_HostRes().getDueBalance()!=null){
						dueAmount = Double.toString(callInfo.getUtilitySubscriberInfo_HostRes().getDueBalance().getAmt());
						callInfo.setField(Field.UTILITY_DUE_AMOUNT, dueAmount);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for the service Utilitysubscriber");}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for the service Utilitysubscriber" + dueAmount);}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Received the total bill amount needs to be paid" + dueAmount);}
					}else{
						throw new ServiceException("UtilitySubscriberInfo_HostRes bean object or Total Amt value is null or empty");
					}
					
				}else{

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "Got failure response for Utilitysubscriber host service");}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "The original response code of host access is " + utilityBalanceDetails_HostRes.getHostResponseCode());}

					util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_UTILITYSUBSCRIBERINFO, utilityBalanceDetails_HostRes.getHostResponseCode());

					/**
					 * Following will be called only if there occured account selection before this host access
					 */
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "Setting the ErrorCodeAnotherAccount as Y");}
					util.setEligibleAccountCounts(callInfo, hostResCode);
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.INFO, session_ID_, "EXIT :  UtilityBillPaymentWaterImpl.getWaterBalanceDetails()");}
				}
				
			}else{
				callInfo.setField(Field.SELECTEDSERVPRODTYPE, Constants.ONIEC);
				requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_UTILITYONEICBALANCE_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_UTILITYONEICBALANCE_REQUESTTYPE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "requestType configured is " + requestType);}

				providerType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_UTILITYONEICBALANCE_PROVIDERTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_UTILITYONEICBALANCE_PROVIDERTYPE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Provider type configured is " + providerType);}
				
				
				GetUtilityBillInfo_HostRes getUtilityBillInfoBalanceDetails_HostRes = getUtilityBillInfoDAO.getGetUtilityBillInfo_HostRes(callInfo, requestType, providerType, serviceProviderCode, utilityCode, contractNo);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "getUtilityBillInfoBalanceDetails_HostRes Object is :"+ getUtilityBillInfoBalanceDetails_HostRes);}
				callInfo.setGetUtilityBillInfo_HostRes(getUtilityBillInfoBalanceDetails_HostRes);

				code = getUtilityBillInfoBalanceDetails_HostRes.getErrorCode();

				/*
				 * For Reporting Start
				 */
				
				String hostEndTime = getUtilityBillInfoBalanceDetails_HostRes.getHostEndTime();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Actual Host End time is is " + hostEndTime);}
				hostReportDetails.setHostEndTime(hostEndTime);

				String hostResCode = getUtilityBillInfoBalanceDetails_HostRes.getHostResponseCode();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Actual Host response code is " + hostResCode);}
				hostReportDetails.setHostResponse(hostResCode);

				String durationTime = util.hostServiceTimeDuration(startTime, hostEndTime, Constants.DATEFORMAT_dd_MM_yyyyHH_mm_ss);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "Host Access duration is " + durationTime);}
			
				String responseDesc = Constants.HOST_FAILURE;
				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					responseDesc = Constants.HOST_SUCCESS;
				}
				
				String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
						+ Constants.EQUALTO + hostResCode;

				hostReportDetails.setHostOutParams(hostOutputParam);
				
				callInfo.setHostReportDetails(hostReportDetails);
				ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

				callInfo.updateHostDetails(ivrdata);
				//End Reporting

				if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for the service GetUtilityBillInfo");}
					
					if( callInfo.getGetUtilityBillInfo_HostRes() != null && callInfo.getGetUtilityBillInfo_HostRes().getRecCount() > 0 
							&&  callInfo.getGetUtilityBillInfo_HostRes().getDueBalance()!=null &&  callInfo.getGetUtilityBillInfo_HostRes().getDueBalance() > 0){
						dueAmount = Double.toString(callInfo.getGetUtilityBillInfo_HostRes().getDueBalance());
						callInfo.setField(Field.UTILITY_DUE_AMOUNT, dueAmount);
						callInfo.setField(Field.INQ_REF_NO, callInfo.getGetUtilityBillInfo_HostRes().getInqRefNo());
						callInfo.setField(Field.BILL_NO, callInfo.getGetUtilityBillInfo_HostRes().getBillNo());
						callInfo.setField(Field.BILL_STATUS, callInfo.getGetUtilityBillInfo_HostRes().getBillStatus());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Received the total bill amount needs to be paid" + dueAmount);}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Received the INQ_REF_NO" + callInfo.getField(Field.INQ_REF_NO));}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Received the Bill No" + callInfo.getField(Field.BILL_NO));}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Received the Bill Status" + callInfo.getField(Field.BILL_STATUS));}
						if(callInfo.getGetUtilityBillInfo_HostRes().isPartialPayFlag())
							callInfo.setField(Field.PARTIALPAYFLAG, Constants.TRUE);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Parital Pay Flag:" + callInfo.getField(Field.PARTIALPAYFLAG));}
					}else{
						throw new ServiceException("GetUtilityBillInfo_HostRes bean object or recCount or Total Amt value is null or empty or zero");
					}
					
				}else{

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "Got failure response for GetUtilityBillInfo host service");}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "The original response code of host access is " + getUtilityBillInfoBalanceDetails_HostRes.getHostResponseCode());}

					util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_GETUTILITYBILLINFO, getUtilityBillInfoBalanceDetails_HostRes.getHostResponseCode());

					/**
					 * Following will be called only if there occured account selection before this host access
					 */
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "Setting the ErrorCodeAnotherAccount as Y");}
					util.setEligibleAccountCounts(callInfo, hostResCode);
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.INFO, session_ID_, "EXIT :  UtilityBillPaymentWaterImpl.getWaterBalanceDetails()");}
				}
			}

			
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at   UtilityBillPaymentWaterImpl.getWaterBalanceDetails() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;

	}


	
	@Override
  	public void isSelectedServProviderAOnline(CallInfo callInfo)
  			throws ServiceException {
  		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
  		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_,  "ENTER: UtilityBillPaymentWaterImpl.isSelectedServProviderAOnline()");}
  		try{
  
  			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "Fetching the Feature Object values");}
  			ICEFeatureData ivr_FeatureData  = (ICEFeatureData) callInfo.getICEFeatureData();
  
  			if(util.isNullOrEmpty(ivr_FeatureData)){
  				throw new ServiceException("ivr_FeatureData object is null");
  			}
  			
  			String serviceProviderCode = util.isNullOrEmpty((String)callInfo.getField(Field.SELECTEDSERVICEPROVIDER)) ? null : (String)callInfo.getField(Field.SELECTEDSERVICEPROVIDER);
  			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Selected Service PRovider code is " + serviceProviderCode);}
  			
  			String contractNo = util.isNullOrEmpty((String)callInfo.getField(Field.DESTNO)) ? null : (String)callInfo.getField(Field.DESTNO);
  			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "Selected Destination number is " + contractNo);}
  			
  			callInfo.setField(Field.CONTRACTNO, contractNo);
  			
  			HashMap<String, ArrayList<Utility_BeneficiaryDetails>> serviceProviderCodeMap = null;
  			
  			if(!util.isNullOrEmpty(callInfo.getUtility_BenfPayeeDetails_HostRes())){
				if(!util.isNullOrEmpty(callInfo.getUtility_BenfPayeeDetails_HostRes().getUtility_serviceProviderMap())){
					serviceProviderCodeMap = callInfo.getUtility_BenfPayeeDetails_HostRes().getUtility_serviceProviderMap();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Service Utility Code map retrieved from host is "  + serviceProviderCodeMap);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no Of utility code map type available is"  + serviceProviderCodeMap.size());}
				}else{
					throw new ServiceException("Utility code Map object is null / Empty");
				}
			}else{
				throw new ServiceException("Utility bill host access object is null / Empty");
			}
			
  			String utilityCode = "";
			ArrayList<Utility_BeneficiaryDetails> beneficiaryDetailsList = serviceProviderCodeMap.get(serviceProviderCode);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Received Beneficiary Payee Details list is"+beneficiaryDetailsList);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Received Beneficiary Payee Details list size is"+beneficiaryDetailsList.size());}
  			
			if(!util.isNullOrEmpty(beneficiaryDetailsList)){
				for(int i=0;i<beneficiaryDetailsList.size();i++){
					if(beneficiaryDetailsList.get(i).getBenefContractNo().equalsIgnoreCase(contractNo)){
						utilityCode = beneficiaryDetailsList.get(i).getUtilityCode();
					}
				}
			}else{
				throw new ServiceException("Utility Beneficiary details list is null / Empty");
			}
			
  			//String utilityCode = util.isNullOrEmpty((String)callInfo.getField(Field.UTILITYCODE)) ? null : (String)callInfo.getField(Field.UTILITYCODE);
			
  			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "Retrieved utility code is " + utilityCode);}
  
  			if(util.isNullOrEmpty(utilityCode)){
  				throw new ServiceException("Utility code is not setted or the value is null");
  			}else{
  				callInfo.setField(Field.UTILITYCODE, utilityCode);
  			}
  			
  			if(util.isNullOrEmpty(serviceProviderCode)){
  				throw new ServiceException("Service Provider code is not setted or the value is null");
  			}
  			
  			String combinedKey = utilityCode + Constants.UNDERSCORE + serviceProviderCode + Constants.UNDERSCORE + Constants.CUI_IS_A_ONLINE_SERVICEPROVIDER;
  			combinedKey = combinedKey.trim();
  			
  			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Combined key is " + combinedKey);}
  			
  			String isSelectedUtilityCodeOnline = util.isNullOrEmpty(ivr_FeatureData.getConfig().getParamValue(combinedKey)) ? Constants.FALSE : (String)ivr_FeatureData.getConfig().getParamValue(combinedKey);
  			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Selected Utility Code is a Online or Offline " + isSelectedUtilityCodeOnline);}
  			
  			isSelectedUtilityCodeOnline = util.isTrue(isSelectedUtilityCodeOnline) ? Constants.TRUE : Constants.FALSE;
  			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Updating the boolean value as TRUE / FALSE and the result is " + isSelectedUtilityCodeOnline);}
  			
  			callInfo.setField(Field.ISSERVICEPROVIDERONLINE, isSelectedUtilityCodeOnline);
  
  			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, session_ID_,"EXIT: UtilityBillPaymentWaterImpl.isSelectedServProviderAOnline()");}
  		}catch(Exception e){
  			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_, "ERROR: UtilityBillPaymentWaterImpl.isSelectedServProviderAOnline()");}
  
  			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UtilityBillPaymentWaterImpl.isSelectedServProviderAOnline() "+ e.getMessage());}
  			throw new ServiceException(e);
  		}
  	}
	
	@Override
  	public String totalBillPaymentAmtMenuPhrases(CallInfo callInfo)
  			throws ServiceException {
  		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
  		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentWaterImpl.totalBillPaymentAmtMenuPhrases()");}
  		String str_GetMessage, finalResult;
  		boolean isForOIFC = false;
  
  		try{
  			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
  
  			  //Need to get the FeatureConfig Data
  			String totalBillAmt = Constants.EMPTY_STRING;
  
  			
  			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
  
  			if(util.isNullOrEmpty(iceFeatureData)){
  				throw new ServiceException("iceFeatureData object is null or empty");
  			}
  			
  			String serviceProviderType = (String)callInfo.getField(Field.SELECTEDSERVPRODTYPE);
  			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"ServiceProviderType:" + serviceProviderType);}
  			if(serviceProviderType.equalsIgnoreCase(Constants.OIFC)){
				isForOIFC = true;
			}
  			
  			totalBillAmt = (String) callInfo.getField(Field.UTILITY_DUE_AMOUNT);
  			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Received the total bill amount needs to be paid" + totalBillAmt);}
  			
  			/*if( callInfo.getUtilitySubscriberInfo_HostRes() != null &&  callInfo.getUtilitySubscriberInfo_HostRes().getDueBalance()!=null){
  					
  				totalBillAmt = String.valueOf(callInfo.getUtilitySubscriberInfo_HostRes().getDueBalance().getAmt().doubleValue());
  				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Received the total bill amount needs to be paid" + totalBillAmt);}
  			}else{
  				throw new ServiceException("UtilitySubscriberInfo_HostRes bean object or Total Amt value is null or empty");
  			}*/
  			
  			
  
  			String grammar = Constants.EMPTY_STRING;
  			boolean moreOption = false;
  
  			  //Need to handle the Dynamic phrase list and Mannual Grammar portions
  			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
  			double amount = Double.parseDouble(totalBillAmt);
  			int index;
  			if(isForOIFC){
  				if(amount <= 0){
  					index = 1;
  					grammar = Constants.OTHER + Constants.COMMA + Constants.CANCEL;
  				}else{
  					index = 2;
  					grammar = totalBillAmt + Constants.COMMA + Constants.OTHER + Constants.COMMA + Constants.CANCEL;
  				}
  			}else{
  				String partialPayFlag = util.isNullOrEmpty((String) callInfo.getField(Field.PARTIALPAYFLAG))? Constants.FALSE : (String) callInfo.getField(Field.PARTIALPAYFLAG);
  				boolean isPartialPayAllowed = partialPayFlag.equalsIgnoreCase(Constants.TRUE)? true : false; 
  				if(amount <= 0 && isPartialPayAllowed){
  					index = 3;
  					grammar = Constants.OTHER + Constants.COMMA + Constants.CANCEL;
  				}else if(amount <= 0 && (!isPartialPayAllowed)) {
  					index = 4;
  					grammar = Constants.CANCEL;
  				}else if(amount > 0 && isPartialPayAllowed){
  					index = 5;
  					grammar = totalBillAmt + Constants.COMMA + Constants.OTHER + Constants.COMMA + Constants.CANCEL;
  				}//else if(amount > 0 && (!isPartialPayAllowed)) {
  				else{
  					index = 6;
  					grammar = totalBillAmt + Constants.COMMA + Constants.CANCEL;
  				}
  			}
  			
  			totalBillAmt = totalBillAmt.contains(Constants.MINUS)?totalBillAmt.substring(Constants.GL_ONE, totalBillAmt.length()) : totalBillAmt;
  			
  			dynamicValueArray.add(totalBillAmt);
  			//grammar = totalBillAmt + Constants.COMMA + Constants.OTHER;
  
  			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
  			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}
  
  			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
  			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));
  
  			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
  			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}
  
  			String menuID = MenuIDMap.getMenuID("Water_TOTALBILLMENU");
  			//String anncID = AnncIDMap.getAnncID("");
  			String featureID = FeatureIDMap.getFeatureID("UtilityBillPaymentWater");
  			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID+Constants.UNDERSCORE+index;
  
  			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Combined Key is "+ combinedKey);}
  
  			Object[] object = new Object[dynamicValueArray.size()];
  			for(int count=0; count<dynamicValueArray.size();count++){
  				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Adding "+ count +"element: "+dynamicValueArray.get(count) +"into Object array ");}
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
  			  //No Need
			
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
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UtilityBillPaymentWaterImpl.totalBillPaymentAmtMenuPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UtilityBillPaymentWaterImpl.totalBillPaymentAmtMenuPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

}
