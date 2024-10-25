package com.servion.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.BeneficiaryDtlsInquiryDAO;
import com.servion.dao.ListBeneficiaryDAO;
import com.servion.dao.TelecomCustomerInfoDAO;
import com.servion.dao.TelecomPostpaidBalanceDetailsDAO;
import com.servion.dao.TelecomSubscriberInfoDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.MenuIDMap;
import com.servion.model.billPayment.TelecomCustomerInfo_HostRes;
import com.servion.model.billPayment.TelecomPostpaidBalanceDetails_HostRes;
import com.servion.model.billPayment.Utility_BeneficiaryDetailList_HostRes;
import com.servion.model.billPayment.Utility_BeneficiaryDetails;
import com.servion.model.billPayment.Utility_BeneficiaryShortDetails;
import com.servion.model.billPayment.Utility_BenfPayeeDetails_HostRes;
import com.servion.model.reporting.HostReportDetails;

public class UtilityBillPaymentMobLandIntrImpl implements IUtilityBillPaymentMobLandIntr{

	private static Logger logger = LoggerObject.getLogger();
	private ListBeneficiaryDAO listBeneficiaryDAO;
	private BeneficiaryDtlsInquiryDAO beneficiaryDtlsInquiryDAO;
	
	//TODO -CR -postpaid bill payment
	private TelecomPostpaidBalanceDetailsDAO telecomPostpaidBalanceDetailsDAO;
	private TelecomCustomerInfoDAO telecomCustomerInfoDAO;
	
	 
	public TelecomCustomerInfoDAO getTelecomCustomerInfoDAO() {
		return telecomCustomerInfoDAO;
	}

	public void setTelecomCustomerInfoDAO(
			TelecomCustomerInfoDAO telecomCustomerInfoDAO) {
		this.telecomCustomerInfoDAO = telecomCustomerInfoDAO;
	}

	public TelecomPostpaidBalanceDetailsDAO getTelecomPostpaidBalanceDetailsDAO() {
		return telecomPostpaidBalanceDetailsDAO;
	}
	public void setTelecomPostpaidBalanceDetailsDAO(
			TelecomPostpaidBalanceDetailsDAO telecomPostpaidBalanceDetailsDAO) {
		this.telecomPostpaidBalanceDetailsDAO = telecomPostpaidBalanceDetailsDAO;
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
	
	/*
	 * Ooredoo change on 18-Apr-2017
	 */
	private TelecomSubscriberInfoDAO telecomSubscriberInfoDAO;
	
	public TelecomSubscriberInfoDAO getTelecomSubscriberInfoDAO() {
		return telecomSubscriberInfoDAO;
	}

	public void setTelecomSubscriberInfoDAO(
			TelecomSubscriberInfoDAO telecomSubscriberInfoDAO) {
		this.telecomSubscriberInfoDAO = telecomSubscriberInfoDAO;
	}

	@Override
	public String getMobBroadExstOrAddBenfPhrases(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentMobLandIntrImpl.getMobBroadExstOrAddBenfPhrases()");}
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
					
					throw new ServiceException("There is no registered benef and add benefi option");
				}
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("MOBILE_LANDLINE_BENEFICIARY");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Utility_Bill_Payment_Mobile_Landline_Internet");
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
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UtilityBillPaymentMobLandIntrImpl.getMobBroadExstOrAddBenfPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UtilityBillPaymentMobLandIntrImpl.getMobBroadExstOrAddBenfPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String getMobBroadbandAccountNumberMenuPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentMobLandIntrImpl.getMobBroadbandAccountNumberMenuPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();


			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ICEFeatureData object is null / empty");
			}
			//Need to get the FeatureConfig Data
			String utlityCode = (String)callInfo.getField(Field.LASTSELECTEDVALUE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected service provider is  "+utlityCode);}


			HashMap<String, ArrayList<Utility_BeneficiaryDetails>> utilityCodeMap = null;
			ArrayList<Utility_BeneficiaryDetails> beneficiaryDetailsList = new ArrayList<Utility_BeneficiaryDetails>();
			ArrayList<Utility_BeneficiaryDetails> tempArrayList = new ArrayList<Utility_BeneficiaryDetails>();

			//Calling the beneficiary Details host serivce to get all beneficiary details of the available utility type beneficiary ids
			String beneficiaryDetlHostCode = getMobileBroadPayeeDetails(callInfo);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Response code for the BeneficiaryDetailsInq service" + beneficiaryDetlHostCode);}

			if(Constants.ONE.equalsIgnoreCase(beneficiaryDetlHostCode)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Failed while calling Beneficiary details host service");}
				throw new ServiceException("Utility_BenfPayeeDetails_HostRes object is null / Empty");
			}

			if(!util.isNullOrEmpty(callInfo.getUtility_BenfPayeeDetails_HostRes())){
				if(!util.isNullOrEmpty(callInfo.getUtility_BenfPayeeDetails_HostRes().getUtility_utilityCodeMap())){
					utilityCodeMap = callInfo.getUtility_BenfPayeeDetails_HostRes().getUtility_utilityCodeMap();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Service Utility Code retrieved from host is "  + utilityCodeMap);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no Of utility code type available is"  + utilityCodeMap.size());}
				}else{
					throw new ServiceException("Utility code Map object is null / Empty");
				}
			}else{
				throw new ServiceException("Utility bill host access object is null / Empty");
			}

			
			String landlineCode = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_UTILITY_CODE_FOR_LANDLINE);
			String mobileCode = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_UTILITY_CODE_FOR_MOBILE);
			String internetCode = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_UTILITY_CODE_FOR_INTERNET);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The utility code configured in UI for landline is "+landlineCode);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The utility code configured in UI for mobile is "+mobileCode);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The utility code configured in UI for internet is "+internetCode);}

			
			String[] landlineArray = landlineCode.split(Constants.COMMA);
			String[] mobileArray = mobileCode.split(Constants.COMMA);
			String[] InternetArray = internetCode.split(Constants.COMMA);
			
			for(int i=0; i<landlineArray.length ; i++){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The landline utility code retrieving is "+landlineArray[i]);}
				tempArrayList = utilityCodeMap.get(landlineArray[i]);
				if(!util.isNullOrEmpty(tempArrayList)){
					beneficiaryDetailsList.addAll(tempArrayList);
				}
			}
			for(int i=0; i<mobileArray.length ; i++){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Mobile utility code retrieving is "+mobileArray[i]);}
				tempArrayList = utilityCodeMap.get(mobileArray[i]);
				if(!util.isNullOrEmpty(tempArrayList)){
					beneficiaryDetailsList.addAll(tempArrayList);
				}
			}
			for(int i=0; i<InternetArray.length ; i++){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The internet utility code retrieving is "+InternetArray[i]);}
				tempArrayList = utilityCodeMap.get(InternetArray[i]);
				if(!util.isNullOrEmpty(tempArrayList)){
					beneficiaryDetailsList.addAll(tempArrayList);
				}
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Received Beneficiary Payee Details list is"+beneficiaryDetailsList);}
			
			
			if(util.isNullOrEmpty(beneficiaryDetailsList)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The beneficiaryDetailsList is null / empty");}
				throw new ServiceException("no record found for the selected uitily code type");
			}

			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "More Count value : "+int_moreCount);}

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;
			Utility_BeneficiaryDetails temp_benefDetail = null;
			int validPayeeCount = Constants.GL_ZERO;

			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			int temp_MoreCount = int_moreCount - 1;
	

			for(int count=Constants.GL_ZERO;count<beneficiaryDetailsList.size();count++){
				temp_benefDetail = beneficiaryDetailsList.get(count);
				String pervSelectedUtilityCode = Constants.EMPTY;
				if(!util.isNullOrEmpty(temp_benefDetail)){
					/**
					 *  Issue #7035
					 * **/
					boolean isOoredooService = false;
					String serviceProviderCode = temp_benefDetail.getServiceProviderCode();
					ICEFeatureData iceFeatureData = callInfo.getICEFeatureData();
					
					if(util.isNullOrEmpty(iceFeatureData)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "ICEFeature Data object is null or empty");}	
					}
					
					/*if(!util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_OMANTEL_SERVICE_PROVIDER_CODES))){
						String omantelServProviderCodes = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_OMANTEL_SERVICE_PROVIDER_CODES);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Configured Omantel Service Provider Codes are " + omantelServProviderCodes);}
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "And the user selected Service provider code is " + serviceProviderCode);}
						
						isOoredooService = !(util.isCodePresentInTheConfigurationList(serviceProviderCode, omantelServProviderCodes));
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Has the user selected service provider is a ooredoo ?" + isOoredooService);}
					}*/
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CUI_TOP_UP_NAWRAS_PROVIDERS : "+(String) iceFeatureData.getConfig().getParamValue(Constants.CUI_TOP_UP_NAWRAS_PROVIDERS));}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CUI_OMANTEL_SERVICE_PROVIDER_CODES : "+(String) iceFeatureData.getConfig().getParamValue(Constants.CUI_OMANTEL_SERVICE_PROVIDER_CODES));}
					if(!util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_TOP_UP_NAWRAS_PROVIDERS))){
						String ooredooServProviderCodes = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_TOP_UP_NAWRAS_PROVIDERS);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Configured Ooredoo Service Provider Codes are " + ooredooServProviderCodes);}
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "And the user selected Service provider code is " + serviceProviderCode);}
						
						isOoredooService = util.isCodePresentInTheConfigurationList(serviceProviderCode, ooredooServProviderCodes);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Has the user selected service provider is a ooredoo ?" + isOoredooService);}
					}
					
					
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The "+count+" beneficiary Contract number is " +temp_benefDetail.getBenefContractNo());}

					if(util.isCodePresentInTheConfigurationList(utlityCode, landlineCode)&& 
							!(util.isNullOrEmpty(temp_benefDetail.getBenefGSMNo()) && util.isNullOrEmpty(temp_benefDetail.getBenefTelephoneNo()))
							){
						/**
						 *  Issue #7035
						 * **/
						if(isOoredooService){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Landline code: isOoredooService" + isOoredooService);}
							if(util.isNullOrEmpty(temp_benefDetail.getBenefTelephoneNo())){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Landline code: Assigning getBenefGSMNo" + temp_benefDetail.getBenefGSMNo());}
								temp_Str = temp_benefDetail.getBenefGSMNo();
							}else{
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Landline code: Assigning getBenefTelephoneNo" + temp_benefDetail.getBenefTelephoneNo());}
								temp_Str = temp_benefDetail.getBenefTelephoneNo();
							}
						}else{
							temp_Str = temp_benefDetail.getBenefTelephoneNo();
						}
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "For Landline utility type, the mobile number is "+temp_Str);}
						validPayeeCount++;
						dynamicValueArray.add((temp_Str));
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added Beneficiary number in the grammar list "+temp_Str);}

						if(util.isNullOrEmpty(grammar)){
							pervSelectedUtilityCode = temp_benefDetail.getUtilityCode();
//			TODO - CR				temp_Str = temp_Str + Constants.STR_CONSTANT_POINT+ pervSelectedUtilityCode +Constants.STR_CONSTANT_POINT + (""+temp_benefDetail.getBenefContractNo());
							temp_Str = temp_Str + pervSelectedUtilityCode;
							grammar = temp_Str;
						}else{
							pervSelectedUtilityCode = temp_benefDetail.getUtilityCode();
//			TODO - CR				temp_Str = temp_Str +Constants.STR_CONSTANT_POINT+ pervSelectedUtilityCode +Constants.STR_CONSTANT_POINT + (""+temp_benefDetail.getBenefContractNo());
							temp_Str = temp_Str + pervSelectedUtilityCode;
							grammar = grammar + Constants.COMMA + temp_Str;
						}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}

						
					}else if(util.isCodePresentInTheConfigurationList(utlityCode, mobileCode)
							&& !(util.isNullOrEmpty(temp_benefDetail.getBenefGSMNo()) && util.isNullOrEmpty(temp_benefDetail.getBenefTelephoneNo()))
							){
						/**
						 *  Issue #7035
						 * **/
						if(isOoredooService){
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Mobile Code: isOoredooService" + isOoredooService);}
							if(util.isNullOrEmpty(temp_benefDetail.getBenefGSMNo())){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Mobile Code: Assigning getBenefTelephoneNo" + temp_benefDetail.getBenefTelephoneNo());}
								temp_Str = temp_benefDetail.getBenefTelephoneNo();
							}else{
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Mobile Code: Assigning getBenefGSMNo" + temp_benefDetail.getBenefGSMNo());}
								temp_Str = temp_benefDetail.getBenefGSMNo();
							}
						}else{
							temp_Str = temp_benefDetail.getBenefGSMNo();
						}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "For mobile utility type, the mobile number is "+temp_Str);}
						validPayeeCount++;
						dynamicValueArray.add((temp_Str));
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added Beneficiary number in the grammar list "+temp_Str);}

						if(util.isNullOrEmpty(grammar)){
							pervSelectedUtilityCode = temp_benefDetail.getUtilityCode();
//		TODO - CR					temp_Str = temp_Str +Constants.STR_CONSTANT_POINT+ pervSelectedUtilityCode+Constants.STR_CONSTANT_POINT + (""+temp_benefDetail.getBenefContractNo());
							temp_Str = temp_Str + pervSelectedUtilityCode;
							grammar = temp_Str;
						}else{
							pervSelectedUtilityCode = temp_benefDetail.getUtilityCode();
//		TODO - CR					temp_Str = temp_Str +Constants.STR_CONSTANT_POINT+ pervSelectedUtilityCode+Constants.STR_CONSTANT_POINT + (""+temp_benefDetail.getBenefContractNo());
							temp_Str = temp_Str + pervSelectedUtilityCode;
							grammar = grammar + Constants.COMMA + temp_Str;
						}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}

						
					}else if(util.isCodePresentInTheConfigurationList(utlityCode, internetCode) && !(temp_benefDetail.getBenefContractNo()==null || "null".equalsIgnoreCase(temp_benefDetail.getBenefContractNo()) || Constants.EMPTY.equalsIgnoreCase(temp_benefDetail.getBenefContractNo()))){
						temp_Str = temp_benefDetail.getBenefContractNo();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "For Intenet utility type, the mobile number is "+temp_Str);}
						validPayeeCount++;
						dynamicValueArray.add((temp_Str));
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added Beneficiary number in the grammar list "+temp_Str);}

						if(util.isNullOrEmpty(grammar)){
							pervSelectedUtilityCode = temp_benefDetail.getUtilityCode();
//		TODO - CR					temp_Str = temp_Str +Constants.STR_CONSTANT_POINT+ pervSelectedUtilityCode+Constants.STR_CONSTANT_POINT + (""+temp_benefDetail.getBenefContractNo());
							temp_Str = temp_Str + pervSelectedUtilityCode;
							grammar = temp_Str;
						}else{
							pervSelectedUtilityCode = temp_benefDetail.getUtilityCode();
//		TODO - CR					temp_Str = temp_Str +Constants.STR_CONSTANT_POINT+ pervSelectedUtilityCode+Constants.STR_CONSTANT_POINT + (""+temp_benefDetail.getBenefContractNo());
							temp_Str = temp_Str + pervSelectedUtilityCode;
							grammar = grammar + Constants.COMMA + temp_Str;
						}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}

						
					}
					//vijay
					//dynamicValueArray.add((selectedServiceProvider+Constants.WAV_EXTENSION).trim());
//					dynamicValueArray.add((temp_Str));

					if(validPayeeCount == temp_MoreCount){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the more option count");}
						moreOption = true;
						callInfo.setField(Field.MOREOPTIONCOUNT, int_moreCount);
					}


//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added Beneficiary number in the grammar list "+temp_Str);}
//
//					if(util.isNullOrEmpty(grammar)){
//						grammar = temp_Str;
//					}else{
//						grammar = grammar + Constants.COMMA + temp_Str;
//					}
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}

				}
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Valid payee account number total count is "+validPayeeCount);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			
			/**
			 * Added by vinoth on 14-07-2014 while changing the utility code to online
			 */
			
			if(util.isNullOrEmpty(dynamicValueArray)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "dynamicValueArray key value is null or empty so throwing exceptoin");}
				throw new ServiceException("dynamicValueArray is null or empty");
			}
			//END
			
			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("MOBILE_LANDLINE_INTERNET_NUMBER_SELECTION");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Utility_Bill_Payment_Mobile_Landline_Internet");
			//Vijay
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID+Constants.UNDERSCORE+utlityCode;

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


			
			if((Constants.ALPHA_H).equalsIgnoreCase(languageKey) || (Constants.hindi).equalsIgnoreCase(languageKey)){
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
			}else{
				//Overriding the total prompts, received from the property file
				if(validPayeeCount >int_moreCount){
					totalPrompt = Constants.GL_FOUR * int_moreCount;
					totalPrompt = totalPrompt + Constants.GL_TWO;

					//				/**
					//				 * Added to fix the issue
					//				 */
					//				int temp1 = validPayeeCount / int_moreCount;
					//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient value is "+temp1);}
					//
					//				int temp2 =  validPayeeCount % int_moreCount;
					//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Remainder value is "+temp2);}
					//				if(temp2 > 0){
					//					temp1++;
					//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient increased by one "+temp1);}
					//				}
					//				totalPrompt = totalPrompt + (temp1*Constants.GL_TWO);
					//				//END Vinoth
				}
				else{
					totalPrompt = Constants.GL_FOUR * validPayeeCount;
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

			temp_MoreCount = int_moreCount + 1;
			String strLimiter=DynaPhraseConstants.PHRASE_PRESS_+temp_MoreCount+".wav";
			//Need to handle if we want to append pipeseperator sign
			if(!util.isNullOrEmpty(finalResult)){

				if(finalResult.contains(strLimiter)){
					finalResult=finalResult.replaceAll(strLimiter+DynaPhraseConstants.STAR,strLimiter+Constants.PIPESEPERATOR);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The Final Result string is after apending pipe seperator is "+finalResult);}
				}
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UtilityBillPaymentMobLandIntrImpl.getMobBroadbandAccountNumberMenuPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UtilityBillPaymentMobLandIntrImpl.getMobBroadbandAccountNumberMenuPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String getMobBroadbandServiceProvidersPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentMobLandIntrImpl.getMobBroadbandServiceProvidersPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			ArrayList<String> utilityCodeList =  new ArrayList<String>();
			//Need to get the FeatureConfig Data

			if(!util.isNullOrEmpty(callInfo.getUtility_BeneficiaryDetailList_HostRes())){
				if(!util.isNullOrEmpty(callInfo.getUtility_BeneficiaryDetailList_HostRes().getUtilityCodeList())){
					utilityCodeList = callInfo.getUtility_BeneficiaryDetailList_HostRes().getUtilityCodeList();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Utility Code list retrieved from host is "  + utilityCodeList);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no Of utility codes available are"  + utilityCodeList.size());}
				}
			}else{
				throw new ServiceException("Utility code list object is null / Empty");
			}


			ArrayList<String>tempArrayList =  new ArrayList<String>();
			String strUtilityCode = Constants.EMPTY_STRING;
			ICEFeatureData iceFeatureData = callInfo.getICEFeatureData();
			
			if(util.isNullOrEmpty(iceFeatureData)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "ICEFeature Data object is null or empty");}	
			}
			
			if( callInfo.getUtility_BeneficiaryDetailList_HostRes() != null &&  callInfo.getUtility_BeneficiaryDetailList_HostRes().getUtilityCodeList()!=null){

				String hostUtilityCodeList = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_APPLICABLE_UTILITY_CODE);

				for(int i=0; i<utilityCodeList.size();i++){
					strUtilityCode = utilityCodeList.get(i);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The utility code is " + strUtilityCode);}
					if(util.isCodePresentInTheConfigurationList(strUtilityCode,  hostUtilityCodeList)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The utility type  " + strUtilityCode +" is in the UI list ");}
						tempArrayList.add(strUtilityCode);
					}
				}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The final Utility codes are " + tempArrayList);}
			}


			/**
			 * Note temp_Str is nothing but the product name.  The wave file also should recorded in the same product name
			 * 
			 * eg landline --> landline.wav or (XYZ -- > XYZ.wav)
			 * 
			 */

			
			/**
			 * Following are the changes which was handled in order to have two or more utility code
			 */
			
			String applicableMobileUtilityCode = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_UTILITY_CODE_FOR_MOBILE);
			String applicableLandlineUtilityCode = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_UTILITY_CODE_FOR_LANDLINE);
			String applicableInternetUtilityCode = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_UTILITY_CODE_FOR_INTERNET);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Applicable Mobile Utility code" + applicableMobileUtilityCode);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Applicable Internet Utility code" + applicableLandlineUtilityCode);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Applicable Internet Utility code" + applicableInternetUtilityCode);}
			
			boolean isMobilePhraseAdded = false;
			boolean isLandlinePhraseAdded = false;
			boolean isInternetPhraseAdded = false;
			
			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;

			int temp_MoreCount = int_moreCount - 1;
			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			int availableUtilityCodeCount = Constants.GL_ZERO;
			for(int count=Constants.GL_ZERO;count<tempArrayList.size();count++){
				temp_Str = tempArrayList.get(count);
				
				
				if(util.isCodePresentInTheConfigurationList(temp_Str, applicableMobileUtilityCode) && !isMobilePhraseAdded){
					isMobilePhraseAdded = true;
					dynamicValueArray.add((temp_Str+Constants.WAV_EXTENSION).trim());
					
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added Product type "+temp_Str);}

					if(util.isNullOrEmpty(grammar)){
						grammar = temp_Str;
					}else{
						grammar = grammar + Constants.COMMA + temp_Str;
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}

					availableUtilityCodeCount++;
				}else if(util.isCodePresentInTheConfigurationList(temp_Str, applicableLandlineUtilityCode) && !isLandlinePhraseAdded){
					isLandlinePhraseAdded = true;
					dynamicValueArray.add((temp_Str+Constants.WAV_EXTENSION).trim());
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added Product type "+temp_Str);}

					if(util.isNullOrEmpty(grammar)){
						grammar = temp_Str;
					}else{
						grammar = grammar + Constants.COMMA + temp_Str;
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}

					availableUtilityCodeCount++;
				}else if(util.isCodePresentInTheConfigurationList(temp_Str, applicableInternetUtilityCode)&& !isInternetPhraseAdded){
					isInternetPhraseAdded =true;
					dynamicValueArray.add((temp_Str+Constants.WAV_EXTENSION).trim());
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added Product type "+temp_Str);}

					if(util.isNullOrEmpty(grammar)){
						grammar = temp_Str;
					}else{
						grammar = grammar + Constants.COMMA + temp_Str;
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}
					availableUtilityCodeCount++;
				}

				if(count == temp_MoreCount){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the more option count");}
					moreOption = true;
					callInfo.setField(Field.MOREOPTIONCOUNT, int_moreCount);
				}
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("MOBILE_LANDLINE_INTERNET_SELECTION");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Utility_Bill_Payment_Mobile_Landline_Internet");
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
			if(availableUtilityCodeCount >int_moreCount){
				totalPrompt = Constants.GL_THREE * availableUtilityCodeCount;
				//totalPrompt = totalPrompt + Constants.GL_TWO;
				
				/**
				 * Added to fix the issue
				 */
				int temp1 = availableUtilityCodeCount / int_moreCount;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient value is "+temp1);}

//				int temp2 =  availableUtilityCodeCount % int_moreCount;
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Remainder value is "+temp2);}
//				if(temp2 > 0){
//					temp1++;
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient increased by one "+temp1);}
//				}
				totalPrompt = totalPrompt + (temp1*Constants.GL_TWO);
				//END Vinoth
				
			}
			else{
				totalPrompt = Constants.GL_THREE * availableUtilityCodeCount;
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


			//Need to handle if we want to append pipe seperator sign

			if(!util.isNullOrEmpty(finalResult)){
				temp_MoreCount = int_moreCount + 1;
				if(finalResult.contains(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION).trim())){
					finalResult = finalResult.replaceAll(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION),(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION)+Constants.PIPE));
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The Final Result string is after apending pipe seperator is "+finalResult);}
				}
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UtilityBillPaymentMobLandIntrImpl.getMobBroadbandServiceProvidersPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UtilityBillPaymentMobLandIntrImpl.getMobBroadbandServiceProvidersPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String getMobileBroadPayeeList(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentMobLandIntrImpl.getMobileBroadPayeeList()");}
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

			/*
			 * Ooredoo Change
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
			
			/****Duplicate RRN Fix 25012016 *****/
			strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerID
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			/************************************/
			
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
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UtilityBillPaymentMobLandIntrImpl.getMobileBroadPayeeList()");}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UtilityBillPaymentMobLandIntrImpl.getMobileBroadPayeeList() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;		


	}

	public String getMobileBroadPayeeDetails(CallInfo callInfo)throws ServiceException{
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentMobLandIntrImpl.getMobileBroadPayeeDetails()");}
		String code = Constants.EMPTY_STRING;
		try{
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to call Beneficiary Detail Enquiry Service");}

			String customerID = (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer id is "+ customerID);}

			if(util.isNullOrEmpty(customerID)){
				throw new ServiceException("customerID value is null");
			}
			
			/**
			 * Following are the fix we need to pass utility code instead of service provider code.
			 */

			String selectedUtilityCode = (String)callInfo.getField(Field.UTILITYCODE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Utility code is " + selectedUtilityCode);}

			if(util.isNullOrEmpty(selectedUtilityCode)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Utility code value is null or empty");}
				throw new ServiceException("Service provide code is null or empty");
			}


			if(util.isNullOrEmpty(callInfo.getUtility_BeneficiaryDetailList_HostRes()) || util.isNullOrEmpty(callInfo.getUtility_BeneficiaryDetailList_HostRes().getBenefShortDescDetailMap())){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Beneficiary Short Detail map is null or empty");}
				throw new ServiceException("Beneficiary Short Detail map is null or empty");
			}
			
			
			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}
			
			
			String applicableMobileUtilityCode = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_UTILITY_CODE_FOR_MOBILE);
			String applicableLandlineUtilityCode = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_UTILITY_CODE_FOR_LANDLINE);
			String applicableInternetUtilityCode = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_UTILITY_CODE_FOR_INTERNET);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Applicable Mobile Utility code" + applicableMobileUtilityCode);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Applicable Internet Utility code" + applicableLandlineUtilityCode);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Applicable Internet Utility code" + applicableInternetUtilityCode);}
			
			
			String selectedUtilityCodelist = Constants.EMPTY_STRING;
			
			if(util.isCodePresentInTheConfigurationList(selectedUtilityCode, applicableMobileUtilityCode)){
				selectedUtilityCodelist = applicableMobileUtilityCode;
			}
			else if(util.isCodePresentInTheConfigurationList(selectedUtilityCode, applicableLandlineUtilityCode)){
				selectedUtilityCodelist = applicableLandlineUtilityCode;
			}
			else if(util.isCodePresentInTheConfigurationList(selectedUtilityCode, applicableInternetUtilityCode)){
				selectedUtilityCodelist = applicableInternetUtilityCode;
			}
			

			HashMap<String, Utility_BeneficiaryShortDetails> beneficiaryIDMap = callInfo.getUtility_BeneficiaryDetailList_HostRes().getBenefShortDescDetailMap();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Retrieved Beneficiary Short Desc Detail map are " + beneficiaryIDMap);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Retrieved Beneficiary Short Desc Detail map's size is  " + beneficiaryIDMap.size());}

			ArrayList<String> beneficiaryIdList = new ArrayList<>();
			Utility_BeneficiaryShortDetails utility_BeneficiaryShortDetails = null;
			String utilityCode = Constants.EMPTY_STRING;
			Iterator iterator = beneficiaryIDMap.entrySet().iterator();
			Map.Entry mapEntry = null;
			while (iterator.hasNext()) {
				mapEntry = (Map.Entry) iterator.next();

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Beneficiary ID is " + mapEntry.getKey());}

				utility_BeneficiaryShortDetails = (Utility_BeneficiaryShortDetails)mapEntry.getValue();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Utility_BeneficiaryShortDetails object is " + utility_BeneficiaryShortDetails);}

				if(!util.isNullOrEmpty(utility_BeneficiaryShortDetails)){
					utilityCode = utility_BeneficiaryShortDetails.getUtilityCode();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Beneficiary Utility code  is " + utilityCode);}
					if(util.isCodePresentInTheConfigurationList(utilityCode, selectedUtilityCodelist)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected utility code matches with this beneficiary uitlity code and the benef id is" + utility_BeneficiaryShortDetails.getBeneficiaryId());}
						beneficiaryIdList.add(utility_BeneficiaryShortDetails.getBeneficiaryId());
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding Beneficiary id " +utility_BeneficiaryShortDetails.getBeneficiaryId() );}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The number of beneficiary id records are" + beneficiaryIdList.size());}
					}

				}

			}


//
//			HashMap<String, ArrayList<Utility_BeneficiaryShortDetails>>	serviceProviderCodeMap = callInfo.getUtility_BeneficiaryDetailList_HostRes().getServiceCodeMap(); 
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Host Retrieved service Provider codes are " + serviceProviderCodeMap);}
//
//			if(util.isNullOrEmpty(serviceProviderCodeMap)){
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Service provide code map is null or empty");}
//				throw new ServiceException("Service provide code map is null or empty");
//			}
//
//			utility_BeneficiaryShortDetailsList = serviceProviderCodeMap.get(selectedServiceProvider);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved utility Beneficiary Short Details is " + utility_BeneficiaryShortDetailsList);}
//
//
//			if(util.isNullOrEmpty(utility_BeneficiaryShortDetailsList)){
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "utility_BeneficiaryShortDetailsList retrieved is null or empty");}
//				throw new ServiceException("utility_BeneficiaryShortDetailsList retrieved is null or empty");
//			}
//
//
//
//			Utility_BeneficiaryShortDetails utility_BeneficiaryShorDetails = null;
//			for(int i=0; i<utility_BeneficiaryShortDetailsList.size(); i++){
//				utility_BeneficiaryShorDetails = utility_BeneficiaryShortDetailsList.get(i);
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Retrieved Utility Short Details object is " +utility_BeneficiaryShorDetails );}
//
//				if(!util.isNullOrEmpty(utility_BeneficiaryShorDetails)){
//					beneficiaryIdList.add(utility_BeneficiaryShorDetails.getBeneficiaryId());
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding Beneficiary id " +utility_BeneficiaryShorDetails.getBeneficiaryId() );}
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The number of beneficiary id records are" + beneficiaryIdList.size());}
//				}
//			}


//						ArrayList<String> beneficiaryIdList = null;
//						if(!util.isNullOrEmpty(callInfo.getUtility_BeneficiaryDetailList_HostRes())){
//							if(!util.isNullOrEmpty(callInfo.getUtility_BeneficiaryDetailList_HostRes().getBeneficiaryIdList())){
//								beneficiaryIdList = callInfo.getUtility_BeneficiaryDetailList_HostRes().getBeneficiaryIdList();
//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The beneficiary ID list received is " + beneficiaryIdList);}
//								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The number of beneficiary id records are" + beneficiaryIdList.size());}
//								
//							}
//						}


//			if(!util.isNullOrEmpty(beneficiaryIdList)){
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total Beneficiary ID available is " +beneficiaryIdList.size() );}
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Available Beneficiaries are " +beneficiaryIdList);}
//
//			}

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
			
//			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			
			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}
			
			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_BENEFICIARYDELSINQUIRY_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_BENEFICIARYDELSINQUIRY_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}

			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined beneficiary id list is " + beneficiaryIdList);}


			Utility_BenfPayeeDetails_HostRes utility_BenfPayeeDetails_HostRes = beneficiaryDtlsInquiryDAO.getMobBroadBandBenfDelsHostRes(callInfo, beneficiaryIdList, requestType);

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

			callInfo.updateHostDetails(ivrdataForSecHost);
			//End Reporting

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got success response for Beneficiary Details");}
			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for calleridentification host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + utility_BenfPayeeDetails_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_BENEFICARYDTLSINQUIRY, utility_BenfPayeeDetails_HostRes.getHostResponseCode());
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UtilityBillPaymentMobLandIntrImpl.getMobileBroadPayeeDetails()");}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UtilityBillPaymentMobLandIntrImpl.getMobileBroadPayeeDetails() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;

	}

	@Override
	public void setServiceProviderCode(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentMobLandIntrImpl.setServiceProviderCode()");}
		String code = Constants.EMPTY_STRING;
		try{
		
			//Need to get the FeatureConfig Data
			String accountNumber = util.isNullOrEmpty(callInfo.getField(Field.LASTSELECTEDVALUE))?Constants.EMPTY : (String)callInfo.getField(Field.LASTSELECTEDVALUE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Account Number is  "+accountNumber);}

			String utilityCode = util.isNullOrEmpty(callInfo.getField(Field.UTILITYCODE))?Constants.EMPTY : (String)callInfo.getField(Field.UTILITYCODE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Utility Code is "+utilityCode);}
			
			HashMap<String, ArrayList<Utility_BeneficiaryDetails>> utilityCodeMap = null;
			ArrayList<Utility_BeneficiaryDetails> beneficiaryDetailsList = new ArrayList<Utility_BeneficiaryDetails>();
			Utility_BeneficiaryDetails utility_BeneficiaryDetails = null;
//			//Calling the beneficiary Details host serivce to get all beneficiary details of the available utility type beneficiary ids
//			String beneficiaryDetlHostCode = getMobileBroadPayeeDetails(callInfo);
//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Response code for the BeneficiaryDetailsInq service" + beneficiaryDetlHostCode);}
//
//			if(Constants.ONE.equalsIgnoreCase(beneficiaryDetlHostCode)){
//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Failed while calling Beneficiary details host service");}
//				throw new ServiceException("Utility_BenfPayeeDetails_HostRes object is null / Empty");
//			}

			if(!util.isNullOrEmpty(callInfo.getUtility_BenfPayeeDetails_HostRes())){
				if(!util.isNullOrEmpty(callInfo.getUtility_BenfPayeeDetails_HostRes().getUtility_utilityCodeMap())){
					utilityCodeMap = callInfo.getUtility_BenfPayeeDetails_HostRes().getUtility_utilityCodeMap();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Service Utility Code retrieved from host is "  + utilityCodeMap);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no Of utility code type available is"  + utilityCodeMap.size());}
				}else{
					throw new ServiceException("Utility code Map object is null / Empty");
				}
			}else{
				throw new ServiceException("Utility bill host access object is null / Empty");
			}


			beneficiaryDetailsList = utilityCodeMap.get(utilityCode);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Received Beneficiary Payee Details list is"+beneficiaryDetailsList);}


			if(util.isNullOrEmpty(beneficiaryDetailsList)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The beneficiaryDetailsList is null / empty");}
				throw new ServiceException("no record found for the selected uitily code type");
			}

			String serviceProviderCode = Constants.EMPTY_STRING;
			String contractNumber = Constants.EMPTY_STRING;
			if(!util.isNullOrEmpty(accountNumber)){
				for(int i=Constants.GL_ZERO; i<beneficiaryDetailsList.size();i++){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Iterating the "+i+" Beneficiary list account "+beneficiaryDetailsList.get(i));}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The account / Landinge / Contract Number to be searched is "+ accountNumber);}
					utility_BeneficiaryDetails = beneficiaryDetailsList.get(i);
					if(!util.isNullOrEmpty(utility_BeneficiaryDetails)){

						if(accountNumber.equalsIgnoreCase(utility_BeneficiaryDetails.getBenefContractNo())||accountNumber.equalsIgnoreCase(utility_BeneficiaryDetails.getBenefGSMNo())
								||accountNumber.equalsIgnoreCase(utility_BeneficiaryDetails.getBenefTelephoneNo())){
							serviceProviderCode = utility_BeneficiaryDetails.getServiceProviderCode();
							contractNumber = utility_BeneficiaryDetails.getBenefContractNo();
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Service Provider Code is "+utility_BeneficiaryDetails.getServiceProviderCode());}
						}
					}
				}
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the Service Provider Code is "+serviceProviderCode + "To the callInfo");}
			callInfo.setField(Field.SELECTEDSERVICEPROVIDER, serviceProviderCode);
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the Contract Number is "+contractNumber + "To the callInfo");}
			callInfo.setField(Field.CONTRACTNO, contractNumber);
						
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UtilityBillPaymentMobLandIntrImpl.setServiceProviderCode()");}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UtilityBillPaymentMobLandIntrImpl.setServiceProviderCode() "+ e.getMessage());
			throw new ServiceException(e);
		}
	}

	
	// TODO - CR following need to be uncommented
	@Override
	public String getTelecomPostpaidBalanceDetails(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, session_ID_,"ENTER: UtilityBillPaymentMobLandIntrImpl.getTelecomPostpaidBalanceDetails()");}
		String code = Constants.ONE;
		String dueAmount = "";
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

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "Contract Number" + contractNo);}

			String serviceProviderCode = util.isNullOrEmpty(callInfo.getField(Field.SELECTEDSERVICEPROVIDER))?Constants.EMPTY_STRING : (String)callInfo.getField(Field.SELECTEDSERVICEPROVIDER);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Selected service provider code is " + serviceProviderCode);}

			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}

			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_TELECOMPOSTPAIDBALANCE_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_TELECOMPOSTPAIDBALANCE_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "requestType configured is " + requestType);}

			String providerType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_TELECOMPOSTPAIDBALANCE_PROVIDERTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_TELECOMPOSTPAIDBALANCE_PROVIDERTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Provider type configured is " + providerType);}
			
			
			boolean isForOmantelService = false;
			
			if(!util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_OMANTEL_SERVICE_PROVIDER_CODES))){
				String omantelServProviderCodes = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_OMANTEL_SERVICE_PROVIDER_CODES);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Configured Omantel Service Provider Codes are " + omantelServProviderCodes);}
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "And the user selected Service provider code is " + serviceProviderCode);}
				
				isForOmantelService = util.isCodePresentInTheConfigurationList(serviceProviderCode, omantelServProviderCodes);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Has the user selected service provider is a omantel ?" + isForOmantelService);}
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
		
			if(isForOmantelService){
				hostReportDetails.setHostMethod(Constants.HOST_METHOD_TELECOMCUSTOMERINFO);
			}else{
				hostReportDetails.setHostMethod(Constants.HOST_METHOD_TELECOMPOSTPAIDBALANCEDTLS);
			}
//			
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
			
			
			if(isForOmantelService){
				
				requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_TELECOMCUSTOMERINFO_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_TELECOMCUSTOMERINFO_REQUESTTYPE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "requestType configured is " + requestType);}

				providerType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_TELECOMCUSTOMERINFO_PROVIDERTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_TELECOMCUSTOMERINFO_PROVIDERTYPE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Provider type configured is " + providerType);}
				
				
				String subscriberNumber = (String)callInfo.getField(Field.DESTNO);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected Subscriber Number or Destination number is " + subscriberNumber);}
				
				String subscriberType = Constants.EMPTY;
				
				String utilityCode = (String)callInfo.getField(Field.UTILITYCODE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The user selected utility code is " + utilityCode);}
				
				String applicableMobileUtilityCode = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_UTILITY_CODE_FOR_MOBILE);
				String applicableLandlineUtilityCode = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_UTILITY_CODE_FOR_LANDLINE);
				String applicableInternetUtilityCode = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_UTILITY_CODE_FOR_INTERNET);
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Applicable Mobile Utility code" + applicableMobileUtilityCode);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Applicable Internet Utility code" + applicableLandlineUtilityCode);}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Applicable Internet Utility code" + applicableInternetUtilityCode);}
				
				if(util.isCodePresentInTheConfigurationList(utilityCode, applicableMobileUtilityCode)){
					subscriberType = Constants.TelecomCustomerInfo_SubscriberType_GSMPOSTPAID;
				}else if(util.isCodePresentInTheConfigurationList(utilityCode, applicableInternetUtilityCode)){
					subscriberType = Constants.TelecomCustomerInfo_SubscriberType_INTERNET;
				}else if(util.isCodePresentInTheConfigurationList(utilityCode, applicableLandlineUtilityCode)){
					subscriberType = Constants.TelecomCustomerInfo_SubscriberType_LANDLINE;
				}
				
				
				TelecomCustomerInfo_HostRes telecomCustomerInfo_HostRes = telecomCustomerInfoDAO.getTelecomCustomerInfo_HostRes(callInfo, requestType, providerType, serviceProviderCode, subscriberNumber, contractNo, subscriberType);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "telecomCustomerInfo_HostRes Object is :"+ telecomCustomerInfo_HostRes);}
				callInfo.setTelecomCustomerInfo_HostRes(telecomCustomerInfo_HostRes);

				code = telecomCustomerInfo_HostRes.getErrorCode();

				/*
				 * For Reporting Start
				 */
				
				String hostEndTime = telecomCustomerInfo_HostRes.getHostEndTime();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Actual Host End time is is " + hostEndTime);}
				hostReportDetails.setHostEndTime(hostEndTime);

				String hostResCode = telecomCustomerInfo_HostRes.getHostResponseCode();
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
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for the service telecomSubscriber");}
					
					if( callInfo.getTelecomCustomerInfo_HostRes() != null &&  callInfo.getTelecomCustomerInfo_HostRes().getTotalDues()!=null){

						dueAmount = callInfo.getTelecomCustomerInfo_HostRes().getTotalDues();
						callInfo.setField(Field.TELECOM_DUE_AMOUNT, dueAmount);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Received the Omantel total bill amount needs to be paid" + dueAmount);}
					}else{
						throw new ServiceException("TelecomCustomerInfo_HostRes bean object or Total Amt value is null or empty");
					}
					
					
				}else{

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "Got failure response for Telecomsubscriber host service");}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "The original response code of host access is " + telecomCustomerInfo_HostRes.getHostResponseCode());}

					util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_TELECOMCUSTOMERINFO, telecomCustomerInfo_HostRes.getHostResponseCode());

					/**
					 * Following will be called only if there occurred account selection before this host access
					 */
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "Setting the ErrorCodeAnotherAccount as Y");}
					util.setEligibleAccountCounts(callInfo, hostResCode);
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.INFO, session_ID_, "EXIT :  UtilityBillPaymentMobLandIntrImpl.getTelecomPostpaidBalanceDetails()");}
				}
			}
			else{
				
				TelecomPostpaidBalanceDetails_HostRes telecomPostpaidBalanceDetails_HostRes = telecomPostpaidBalanceDetailsDAO.getTelecomPostpaidBalanceDetails_HostRes(callInfo, requestType, providerType, serviceProviderCode, contractNo);
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "telecomPostpaidBalanceDetails_HostRes Object is :"+ telecomPostpaidBalanceDetails_HostRes);}
				callInfo.setTelecomPostpaidBalanceDetails_HostRes(telecomPostpaidBalanceDetails_HostRes);

				code = telecomPostpaidBalanceDetails_HostRes.getErrorCode();

				/*
				 * For Reporting Start
				 */
				
				String hostEndTime = telecomPostpaidBalanceDetails_HostRes.getHostEndTime();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Actual Host End time is is " + hostEndTime);}
				hostReportDetails.setHostEndTime(hostEndTime);

				String hostResCode = telecomPostpaidBalanceDetails_HostRes.getHostResponseCode();
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
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for the service telecomSubscriber");}
					
					if( callInfo.getTelecomPostpaidBalanceDetails_HostRes() != null &&  callInfo.getTelecomPostpaidBalanceDetails_HostRes().getTotalAmt()!=null){
						
						dueAmount = callInfo.getTelecomPostpaidBalanceDetails_HostRes().getTotalAmt();
						callInfo.setField(Field.TELECOM_DUE_AMOUNT, dueAmount);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Received the total bill amount needs to be paid" + dueAmount);}
					}else{
						throw new ServiceException("TelecomPostpaidBalanceDetails_HostRes bean object or Total Amt value is null or empty");
					}
					
				}else{

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "Got failure response for Telecomsubscriber host service");}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "The original response code of host access is " + telecomPostpaidBalanceDetails_HostRes.getHostResponseCode());}

					util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_TELECOMPOSTPAIDBALANCEDTLS, telecomPostpaidBalanceDetails_HostRes.getHostResponseCode());

					/**
					 * Following will be called only if there occured account selection before this host access
					 */
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "Setting the ErrorCodeAnotherAccount as Y");}
					util.setEligibleAccountCounts(callInfo, hostResCode);
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.INFO, session_ID_, "EXIT :  UtilityBillPaymentMobLandIntrImpl.getTelecomPostpaidBalanceDetails()");}
				}
				
				
			}
			
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at   UtilityBillPaymentMobLandIntrImpl.getTelecomPostpaidBalanceDetails() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;

	}

	@Override
	public void isSelectedServProviderAOnline(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_,  "ENTER: UtilityBillPaymentMobLandIntrImpl.isSelectedServProviderAOnline()");}
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "Fetching the Feature Object values");}
			ICEFeatureData ivr_FeatureData  = (ICEFeatureData) callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(ivr_FeatureData)){
				throw new ServiceException("ivr_FeatureData object is null");
			}

			String utilityCode = util.isNullOrEmpty((String)callInfo.getField(Field.UTILITYCODE)) ? null : (String)callInfo.getField(Field.UTILITYCODE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,  "Selected utility code is " + utilityCode);}

			String serviceProviderCode = util.isNullOrEmpty((String)callInfo.getField(Field.SELECTEDSERVICEPROVIDER)) ? null : (String)callInfo.getField(Field.SELECTEDSERVICEPROVIDER);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Selected Service PRovider code is " + serviceProviderCode);}
			
			if(util.isNullOrEmpty(utilityCode)){
				throw new ServiceException("Utility code is not setted or the value is null");
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

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, session_ID_,"EXIT: UtilityBillPaymentMobLandIntrImpl.isSelectedServProviderAOnline()");}
		}catch(Exception e){
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_, "ERROR: UtilityBillPaymentMobLandIntrImpl.isSelectedServProviderAOnline()");}

			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CallerIdentificationImpl.isCallerIdentified() "+ e.getMessage());}
			throw new ServiceException(e);
		}
	}

	@Override
	public String totalBillPaymentAmtMenuPhrases(CallInfo callInfo)
			throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: UtilityBillPaymentMobLandIntrImpl.totalBillPaymentAmtMenuPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();

			//Need to get the FeatureConfig Data
			String totalBillAmt = Constants.EMPTY_STRING;

			
			boolean isForOmantelService = false;
			
			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}
			
			String serviceProviderCode = (String)callInfo.getField(Field.SELECTEDSERVICEPROVIDER);
			if(!util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_OMANTEL_SERVICE_PROVIDER_CODES))){
				String omantelServProviderCodes = (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_OMANTEL_SERVICE_PROVIDER_CODES);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Configured Omantel Service Provider Codes are " + omantelServProviderCodes);}
				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "And the user selected Service provider code is " + serviceProviderCode);}
				
				isForOmantelService = util.isCodePresentInTheConfigurationList(serviceProviderCode, omantelServProviderCodes);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_, "Has the user selected service provider is a omantel ?" + isForOmantelService);}
			}
			
			if(isForOmantelService){
				
				if( callInfo.getTelecomCustomerInfo_HostRes() != null &&  callInfo.getTelecomCustomerInfo_HostRes().getTotalDues()!=null){

					totalBillAmt = callInfo.getTelecomCustomerInfo_HostRes().getTotalDues();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Received the Omantel total bill amount needs to be paid" + totalBillAmt);}
				}else{
					throw new ServiceException("TelecomPostpaidBalanceDetails_HostRes bean object or Total Amt value is null or empty");
				}

			}else{
				if( callInfo.getTelecomPostpaidBalanceDetails_HostRes() != null &&  callInfo.getTelecomPostpaidBalanceDetails_HostRes().getTotalAmt()!=null){
					
					totalBillAmt = callInfo.getTelecomPostpaidBalanceDetails_HostRes().getTotalAmt();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Received the total bill amount needs to be paid" + totalBillAmt);}
				}else{
					throw new ServiceException("TelecomPostpaidBalanceDetails_HostRes bean object or Total Amt value is null or empty");
				}
			}
			

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;

			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}

			
			totalBillAmt = totalBillAmt.contains(Constants.MINUS)?totalBillAmt.substring(Constants.GL_ONE, totalBillAmt.length()) : totalBillAmt;
			
			dynamicValueArray.add(totalBillAmt);
			grammar = totalBillAmt + Constants.COMMA + Constants.OTHER;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("MOBILE_LANDLINE_TOTALBILLMENU");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Utility_Bill_Payment_Mobile_Landline_Internet");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;

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
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: UtilityBillPaymentMobLandIntrImpl.totalBillPaymentAmtMenuPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UtilityBillPaymentMobLandIntrImpl.getMobBroadExstOrAddBenfPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}
	/*
	 * public static void main(String s[]){ String sr = null; Object obj = sr;
	 * System.out.println("s:"+util.isNullOrEmpty(obj)); }
	 */
}

